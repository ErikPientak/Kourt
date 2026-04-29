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
import com.kourt.app.data.model.Attendance
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.event.attendance.AttendanceFilter
import com.kourt.app.ui.screens.event.attendance.AttendanceMemberUiItem
import com.kourt.app.ui.screens.event.attendance.AttendanceScreenActions
import com.kourt.app.ui.screens.event.attendance.AttendanceScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
) : ViewModel(), AttendanceScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

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
                // Parallel: fetch event + existing attendance docs + RSVPs
                val eventDeferred      = async { eventRepository.getEvent(eventId) }
                val attendanceDeferred = async { eventRepository.getAttendance(eventId) }
                val rsvpDeferred       = async { eventRepository.getRsvps(eventId) }

                val event          = eventDeferred.await()
                val attendanceDocs = attendanceDeferred.await()
                val rsvps          = rsvpDeferred.await()

                if (event == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                    return@launch
                }

                // Format event date
                val locale = Locale.getDefault()
                val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd")
                val formatter = java.text.SimpleDateFormat(pattern, locale)
                val dateLabel = formatter.format(Date(event.date.seconds * 1000))

                // Fetch team members with player/parent roles
                val teamMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                    .filter { it.role in setOf("player", "parent") }

                // Lookup user display names and avatars
                val userIds = teamMembers.map { it.userId }.filter { it.isNotBlank() }
                val users = if (userIds.isNotEmpty()) {
                    userRepository.getUsersByIds(userIds).associateBy { it.id }
                } else emptyMap()

                // Existing saved attendance takes priority over RSVP defaults
                val existingStatusByUserId = attendanceDocs
                    .filter { it.userId.isNotBlank() }
                    .associate { it.userId to it.status }

                // RSVP-derived defaults (only used when no attendance doc exists)
                val rsvpByUserId = rsvps.associateBy { it.id }

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
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, error = null)

            runCatching {
                val membersWithStatus = uiState.members.filter { it.status != null }
                membersWithStatus.forEach { member ->
                    eventRepository.upsertAttendance(
                        eventId = eventId,
                        attendance = Attendance(
                            userId = member.userId,
                            status = member.status ?: return@forEach,
                            recordedBy = uid,
                        ),
                    )
                }
                Log.d(TAG, "Saved attendance for ${membersWithStatus.size} members on event $eventId")
                uiState = uiState.copy(isSaving = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save attendance for $eventId", e)
                uiState = uiState.copy(isSaving = false, error = R.string.error_load_failed)
            }
        }
    }

    fun onSavedConsumed() {
        uiState = uiState.copy(isSaved = false)
    }
}
