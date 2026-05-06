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
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamStats
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import com.kourt.app.data.repository.remote.UserRepository
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
    private val rsvpRepository: RsvpRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamStatsRepository: TeamStatsRepository,
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
                // Parallel: fetch event + existing attendance docs + RSVPs
                val eventDeferred      = async { eventRepository.getEvent(eventId) }
                val attendanceDeferred = async { attendanceRepository.getAttendanceByEvent(eventId) }
                val rsvpDeferred       = async { rsvpRepository.getRsvpsByEvent(eventId) }

                val event          = eventDeferred.await()
                val attendanceDocs = attendanceDeferred.await()
                val rsvps          = rsvpDeferred.await()

                if (event == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                    return@launch
                }

                // Format event date
                val locale = Locale.getDefault()
                val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd") ?: "MMM d"
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

                teamId = event.teamId
                loadedAttendanceByUserId = attendanceDocs.associateBy { it.userId }
                memberIdByUserId = teamMembers.associate { it.userId to it.id }

                // RSVP-derived defaults (only used when no attendance doc exists)
                val rsvpByUserId = rsvps.associateBy { it.submittedBy }

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

    private suspend fun updateStats() {
        val membersWithStatus = uiState.members.filter { it.status != null }
        val isFirstSave = loadedAttendanceByUserId.isEmpty()

        for (member in membersWithStatus) {
            val memberId = memberIdByUserId[member.userId] ?: continue
            val newStatus = member.status ?: continue
            val oldStatus = loadedAttendanceByUserId[member.userId]?.status
            if (oldStatus == newStatus) continue

            val existing = playerStatsRepository.getPlayerStats(memberId, teamId)
                ?: PlayerStats(teamMemberId = memberId, teamId = teamId)
            var s = existing
            when (oldStatus) {
                "on_time"   -> s = s.copy(totalTrainingsOnTime = maxOf(0, s.totalTrainingsOnTime - 1))
                "late"      -> s = s.copy(totalTrainingsLate = maxOf(0, s.totalTrainingsLate - 1))
                "excused"   -> s = s.copy(totalTrainingsExcused = maxOf(0, s.totalTrainingsExcused - 1))
                "unexcused" -> s = s.copy(totalTrainingsUnexcused = maxOf(0, s.totalTrainingsUnexcused - 1))
            }
            when (newStatus) {
                "on_time"   -> s = s.copy(totalTrainingsOnTime = s.totalTrainingsOnTime + 1)
                "late"      -> s = s.copy(totalTrainingsLate = s.totalTrainingsLate + 1)
                "excused"   -> s = s.copy(totalTrainingsExcused = s.totalTrainingsExcused + 1)
                "unexcused" -> s = s.copy(totalTrainingsUnexcused = s.totalTrainingsUnexcused + 1)
            }
            playerStatsRepository.updatePlayerStats(s.copy(updatedAt = Timestamp.now()))
        }

        if (isFirstSave && membersWithStatus.isNotEmpty()) {
            val existing = teamStatsRepository.getTeamStats(teamId) ?: TeamStats(teamId = teamId)
            teamStatsRepository.updateTeamStats(
                existing.copy(totalTrainings = existing.totalTrainings + 1, updatedAt = Timestamp.now())
            )
        }
    }

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
                val now = Timestamp.now()
                val membersWithStatus = uiState.members.filter { it.status != null }
                val newLoaded = mutableMapOf<String, Attendance>()

                membersWithStatus.forEach { member ->
                    val status = member.status ?: return@forEach
                    val existing = loadedAttendanceByUserId[member.userId]
                    val toSave = if (existing != null) {
                        existing.copy(status = status, updatedBy = uid, updatedAt = now)
                    } else {
                        Attendance(eventId = eventId, userId = member.userId, status = status, recordedBy = uid, recordedAt = now)
                    }
                    attendanceRepository.saveAttendance(toSave)
                    newLoaded[member.userId] = toSave
                }

                updateStats()
                loadedAttendanceByUserId = newLoaded
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
