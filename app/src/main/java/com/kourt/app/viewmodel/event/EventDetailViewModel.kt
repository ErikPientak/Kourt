package com.kourt.app.viewmodel.event

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.Rsvp
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.NominationRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.event.detail.EventDetailScreenActions
import com.kourt.app.ui.screens.event.detail.EventDetailScreenUiState
import com.kourt.app.ui.screens.event.detail.EventDetailUiItem
import com.kourt.app.ui.screens.event.detail.NominatedPlayerUiItem
import com.kourt.app.ui.screens.event.detail.RsvpMemberUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private const val TAG = "EventDetailViewModel"

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val rsvpRepository: RsvpRepository,
    private val userRepository: UserRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val nominationRepository: NominationRepository,
) : ViewModel(), EventDetailScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""
    private var loadedMyRsvp: Rsvp? = null

    var uiState by mutableStateOf(EventDetailScreenUiState())
        private set

    init {
        loadEventDetail()
    }

    fun loadEventDetail() {
        val uid = authRepository.currentUser?.uid ?: run {
            uiState = uiState.copy(isLoading = false, error = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                // Parallel fetch
                val eventDeferred = async { eventRepository.getEvent(eventId) }
                val rsvpsDeferred = async { rsvpRepository.getRsvpsByEvent(eventId) }
                val membershipsDeferred = async { teamMemberRepository.getMembersByUser(uid) }
                val nominationDeferred = async { nominationRepository.getNomination(eventId) }

                val event = eventDeferred.await()
                val rsvps = rsvpsDeferred.await()
                val memberships = membershipsDeferred.await()
                val nomination = nominationDeferred.await()

                if (event == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                    return@runCatching
                }

                val membership = memberships.firstOrNull { it.teamId == event.teamId }
                val role = membership?.role ?: ""
                val isEditable = role in setOf("coach", "assistant", "admin")
                val canRsvp = role in setOf("player", "parent")

                // Formatted date: e.g. "Wednesday, Apr 30 • 18:00"
                val epochMillis = event.date.toDate().time
                val locale = Locale.getDefault()
                val datePattern = DateFormat.getBestDateTimePattern(locale, "EEEEMMMdy")
                val sdf = java.text.SimpleDateFormat(datePattern, locale)
                val dateString = sdf.format(event.date.toDate())
                val formattedDate = if (event.startTime.isNotBlank()) "$dateString • ${event.startTime}" else dateString

                val eventUiItem = EventDetailUiItem(
                    eventId = event.id,
                    teamId = event.teamId,
                    title = event.title,
                    type = event.type,
                    status = event.status,
                    formattedDate = formattedDate,
                    location = event.location,
                    notes = event.notes,
                )

                // RSVP counts
                val rsvpYes = rsvps.count { it.status == "yes" }
                val rsvpLate = rsvps.count { it.status == "late" }
                val rsvpNo = rsvps.count { it.status == "no" }

                val myRsvp = rsvps.firstOrNull { it.submittedBy == uid }
                loadedMyRsvp = myRsvp
                val myRsvpStatus = myRsvp?.status

                // Fetch user details for going/not-going members
                val goingRsvps = rsvps.filter { it.status == "yes" || it.status == "late" }
                val notGoingRsvps = rsvps.filter { it.status == "no" }

                val allRelevantUids = (goingRsvps + notGoingRsvps)
                    .map { it.submittedBy }
                    .filter { it.isNotBlank() }
                    .distinct()

                val usersDeferred = async {
                    if (allRelevantUids.isNotEmpty()) userRepository.getUsersByIds(allRelevantUids)
                    else emptyList()
                }
                val teamMembersDeferred = async { teamMemberRepository.getMembersByTeam(event.teamId) }

                val userMap = usersDeferred.await().associateBy { it.id }
                val memberRoleMap = teamMembersDeferred.await().associate { it.userId to it.role }

                val goingMembers = goingRsvps.mapNotNull { rsvp ->
                    val user = userMap[rsvp.submittedBy] ?: return@mapNotNull null
                    val memberRole = memberRoleMap[rsvp.submittedBy]
                        ?.replaceFirstChar { it.uppercase() } ?: ""
                    RsvpMemberUiItem(
                        userId = user.id,
                        displayName = user.displayName.ifBlank { user.email },
                        subtitle = memberRole,
                        avatarUrl = user.avatarId.ifBlank { user.photoURL },
                        rsvpStatus = rsvp.status,
                    )
                }

                val notGoingMembers = notGoingRsvps.mapNotNull { rsvp ->
                    val user = userMap[rsvp.submittedBy] ?: return@mapNotNull null
                    val reasonDisplay = rsvp.reason
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }
                        .ifBlank { "No reason given" }
                    RsvpMemberUiItem(
                        userId = user.id,
                        displayName = user.displayName.ifBlank { user.email },
                        subtitle = reasonDisplay,
                        avatarUrl = user.avatarId.ifBlank { user.photoURL },
                        rsvpStatus = rsvp.status,
                    )
                }

                val nominatedPlayers = if (
                    event.type.lowercase() == "match" && !nomination?.players.isNullOrEmpty()
                ) {
                    val nominatedIds = nomination!!.players
                    val teamMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                        .associateBy { it.userId }
                    userRepository.getUsersByIds(nominatedIds).mapNotNull { user ->
                        val member = teamMembers[user.id] ?: return@mapNotNull null
                        NominatedPlayerUiItem(
                            userId = user.id,
                            displayName = user.displayName.ifBlank { user.email },
                            avatarUrl = user.avatarId.ifBlank { user.photoURL },
                            jerseyNumber = member.jerseyNumber,
                        )
                    }.sortedWith(compareBy({ it.jerseyNumber == 0 }, { it.jerseyNumber }))
                } else emptyList()

                uiState = uiState.copy(
                    isLoading = false,
                    event = eventUiItem,
                    myRsvpStatus = myRsvpStatus,
                    rsvpYes = rsvpYes,
                    rsvpLate = rsvpLate,
                    rsvpNo = rsvpNo,
                    goingMembers = goingMembers,
                    notGoingMembers = notGoingMembers,
                    isEditable = isEditable,
                    canRsvp = canRsvp,
                    nominatedPlayers = nominatedPlayers,
                )
            }.onFailure { e ->
                Log.e(TAG, "Failed to load event detail for $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onUpdateRsvp() {
        uiState = uiState.copy(isRsvpSheetOpen = true)
    }

    override fun onRsvpSelected(status: String) {
        uiState = uiState.copy(isRsvpSheetOpen = false)
        when (status) {
            "no" -> {
                // Open reason sheet for "not going"
                uiState = uiState.copy(isRsvpReasonSheetOpen = true, selectedReason = null, reasonNote = "")
            }
            "yes", "late" -> {
                // Submit directly — no reason sheet for "yes" or "late"
                submitRsvp(status = status, reason = "", note = "")
            }
        }
    }

    override fun onRsvpReasonSelected(reason: String) {
        uiState = uiState.copy(selectedReason = reason)
    }

    override fun onRsvpNoteChanged(note: String) {
        uiState = uiState.copy(reasonNote = note)
    }

    override fun onSubmitRsvp() {
        val reason = uiState.selectedReason ?: return
        val note = uiState.reasonNote
        uiState = uiState.copy(isRsvpReasonSheetOpen = false)
        submitRsvp(status = "no", reason = reason, note = note)
    }

    override fun onDismissRsvpSheet() {
        uiState = uiState.copy(isRsvpSheetOpen = false)
    }

    override fun onDismissReasonSheet() {
        uiState = uiState.copy(isRsvpReasonSheetOpen = false, selectedReason = null, reasonNote = "")
    }

    override fun onToggleShowAllGoing() {
        uiState = uiState.copy(showAllGoing = !uiState.showAllGoing)
    }

    override fun onToggleShowAllNotGoing() {
        uiState = uiState.copy(showAllNotGoing = !uiState.showAllNotGoing)
    }

    private fun submitRsvp(status: String, reason: String, note: String) {
        val uid = authRepository.currentUser?.uid ?: run {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isRsvpSubmitting = true, error = null)
            runCatching {
                val existing = loadedMyRsvp
                if (existing != null) {
                    rsvpRepository.updateRsvp(
                        existing.copy(status = status, reason = reason, reasonNote = note)
                    )
                } else {
                    rsvpRepository.saveRsvp(
                        Rsvp(eventId = eventId, status = status, reason = reason, reasonNote = note, submittedBy = uid)
                    )
                }
                Log.d(TAG, "RSVP submitted: status=$status for event=$eventId")
                uiState = uiState.copy(isRsvpSubmitting = false)
                loadEventDetail()
            }.onFailure { e ->
                Log.e(TAG, "Failed to submit RSVP for $eventId", e)
                uiState = uiState.copy(isRsvpSubmitting = false, error = R.string.error_load_failed)
            }
        }
    }
}
