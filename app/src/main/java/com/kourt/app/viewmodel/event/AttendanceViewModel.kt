package com.kourt.app.viewmodel.event

import android.text.format.DateFormat
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.Attendance
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.event.attendance.AttendanceFilter
import com.kourt.app.ui.screens.event.attendance.AttendanceMemberUiItem
import com.kourt.app.ui.screens.event.attendance.AttendanceScreenActions
import com.kourt.app.ui.screens.event.attendance.AttendanceScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "AttendanceViewModel"

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val rsvpRepository: RsvpRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel(), AttendanceScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private var teamId = ""
    private var loadedAttendanceByUserId: Map<String, Attendance> = emptyMap()
    private var memberIdByUserId: Map<String, String> = emptyMap()

    var uiState by mutableStateOf(AttendanceScreenUiState())
        private set

    init {
        loadAttendance()
    }

    private fun loadAttendance() {
        if (eventId.isBlank()) {
            uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                coroutineScope {
                    // Parallel: fetch event + existing attendance docs + RSVPs
                    val eventDeferred      = async { eventRepository.getEvent(eventId) }
                    val attendanceDeferred = async { attendanceRepository.getAttendanceByEvent(eventId) }
                    val rsvpDeferred       = async { rsvpRepository.getRsvpsByEvent(eventId) }

                    val event          = eventDeferred.await()
                    val attendanceDocs = attendanceDeferred.await()
                    val rsvps          = rsvpDeferred.await()

                    if (event == null) {
                        uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                        return@coroutineScope
                    }

                    // Format event date
                    val locale = Locale.getDefault()
                    val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd") ?: "MMM d"
                    val formatter = java.text.SimpleDateFormat(pattern, locale)
                    val dateLabel = formatter.format(Date(event.date.seconds * 1000))

                    // Only players attend; parents are on the team to RSVP for their children.
                    val teamMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                        .filter { it.role == "player" }

                    // Lookup user display names and avatars
                    val userIds = teamMembers.map { it.userId }.filter { it.isNotBlank() }
                    val users = if (userIds.isNotEmpty()) {
                        userRepository.getUsersByIds(userIds).associateBy { it.id }
                    } else emptyMap()

                    // Existing saved attendance takes priority over RSVP defaults
                    val existingStatusByUserId = attendanceDocs
                        .filter { it.userId.isNotBlank() }
                        .associate { it.userId to it.status }

                    teamId = event.teamId
                    loadedAttendanceByUserId = attendanceDocs.associateBy { it.userId }
                    memberIdByUserId = teamMembers.associate { it.userId to it.id }

                    // RSVP-derived defaults (only used when no attendance doc exists)
                    val rsvpByUserId = rsvps.associateBy { it.userId }

                    val memberItems = teamMembers.mapNotNull { member ->
                        val user = users[member.userId] ?: return@mapNotNull null
                        val status = existingStatusByUserId[member.userId]
                            ?: rsvpDefaultStatus(rsvpByUserId[member.userId]?.status, rsvpByUserId[member.userId]?.reason)
                        AttendanceMemberUiItem(
                            userId = member.userId,
                            displayName = user.displayName.ifBlank { user.email },
                            avatarUrl = user.avatarId.ifBlank { user.photoURL },
                            status = status,
                        )
                    }

                    uiState = uiState.copy(
                        isLoading = false,
                        eventTitle = event.title,
                        eventDate = dateLabel,
                        members = memberItems,
                    )

                    Log.d(TAG, "Loaded ${memberItems.size} members for event $eventId")
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load attendance for $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun rsvpDefaultStatus(rsvpStatus: String?, reason: String?): String = when (rsvpStatus) {
        "yes", "late" -> "on_time"
        "no" -> if (reason.isNullOrBlank()) "unexcused" else "excused"
        else -> "unexcused"
    }

    // ── AttendanceScreenActions ───────────────────────────────────────────────

    override fun onStatusSelected(userId: String, status: String) {
        val updatedMembers = uiState.members.map { member ->
            if (member.userId == userId) {
                val newStatus = if (member.status == status) null else status
                member.copy(status = newStatus)
            } else member
        }
        uiState = uiState.copy(members = updatedMembers)
    }

    override fun onFilterChanged(filter: AttendanceFilter) {
        uiState = uiState.copy(filter = filter)
    }

    override fun onSave() {
        if (uiState.isSaving) return
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }
        // One snapshot for the whole save, so edits made while it runs can't mix in.
        val members = uiState.members
        val previous = loadedAttendanceByUserId

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, error = null)

            runCatching {
                val plan = planAttendanceSave(eventId, members, previous, memberIdByUserId, uid, Timestamp.now())
                attendanceRepository.saveAttendanceWithStats(
                    teamId = teamId,
                    upserts = plan.upserts,
                    deletes = plan.deletes,
                    counterDeltas = plan.counterDeltas,
                    trainingsDelta = plan.trainingsDelta,
                )
                loadedAttendanceByUserId = plan.savedAfter
                Log.d(TAG, "Saved attendance for event $eventId: ${plan.upserts.size} set, ${plan.deletes.size} removed")
                uiState = uiState.copy(isSaving = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save attendance for $eventId", e)
                uiState = uiState.copy(isSaving = false, error = R.string.error_load_failed)
            }
        }
    }

    fun onErrorShown() {
        uiState = uiState.copy(error = null)
    }

    fun onSavedConsumed() {
        uiState = uiState.copy(isSaved = false)
    }
}

/** What one attendance save writes, worked out from the screen and the records loaded with it. */
internal data class AttendanceSavePlan(
    val upserts: List<Attendance>,
    val deletes: List<Attendance>,
    /** teamMemberId → PlayerStats counter field → change. */
    val counterDeltas: Map<String, Map<String, Long>>,
    val trainingsDelta: Long,
    /** The event's records once the save has gone through, keyed by userId. */
    val savedAfter: Map<String, Attendance>,
)

/**
 * Compares each member's status on screen with the record loaded for them. Only changes are
 * written: a new status sets the record and moves the player's counter from the old status to
 * the new one; a cleared status deletes the record and takes its count back off.
 * The team's training count goes up when the event gets its first record and down when the
 * last one is removed.
 */
internal fun planAttendanceSave(
    eventId: String,
    members: List<AttendanceMemberUiItem>,
    previous: Map<String, Attendance>,
    memberIdByUserId: Map<String, String>,
    uid: String,
    now: Timestamp,
): AttendanceSavePlan {
    val upserts = mutableListOf<Attendance>()
    val deletes = mutableListOf<Attendance>()
    val counterDeltas = mutableMapOf<String, MutableMap<String, Long>>()

    fun addDelta(memberId: String, status: String, delta: Long) {
        val field = attendanceCounterField(status) ?: return
        val deltas = counterDeltas.getOrPut(memberId) { mutableMapOf() }
        deltas[field] = (deltas[field] ?: 0L) + delta
    }

    for (member in members) {
        val old = previous[member.userId]
        val newStatus = member.status
        if (old?.status == newStatus) continue
        val memberId = memberIdByUserId[member.userId] ?: continue

        if (newStatus != null) {
            upserts += old?.copy(status = newStatus, updatedBy = uid, updatedAt = now)
                ?: Attendance(eventId = eventId, userId = member.userId, status = newStatus, recordedBy = uid, recordedAt = now)
            addDelta(memberId, newStatus, 1)
        } else if (old != null) {
            deletes += old
        }
        old?.let { addDelta(memberId, it.status, -1) }
    }

    val savedAfter = previous - deletes.map { it.userId }.toSet() + upserts.associateBy { it.userId }
    val trainingsDelta = (if (savedAfter.isNotEmpty()) 1L else 0L) - (if (previous.isNotEmpty()) 1L else 0L)

    return AttendanceSavePlan(
        upserts = upserts,
        deletes = deletes,
        counterDeltas = counterDeltas.filterValues { it.isNotEmpty() },
        trainingsDelta = trainingsDelta,
        savedAfter = savedAfter,
    )
}

/** The PlayerStats counter an attendance status is counted in. */
private fun attendanceCounterField(status: String): String? = when (status) {
    "on_time" -> "totalTrainingsOnTime"
    "late" -> "totalTrainingsLate"
    "excused" -> "totalTrainingsExcused"
    "unexcused" -> "totalTrainingsUnexcused"
    else -> null
}
