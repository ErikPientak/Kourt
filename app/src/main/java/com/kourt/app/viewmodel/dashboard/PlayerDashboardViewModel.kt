package com.kourt.app.viewmodel.dashboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.model.Rsvp
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem
import com.kourt.app.ui.screens.dashboard.player.PlayerDashboardScreenActions
import com.kourt.app.ui.screens.dashboard.player.PlayerDashboardScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class PlayerDashboardViewModel @Inject constructor(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    teamMemberRepository: TeamMemberRepository,
    teamRepository: TeamRepository,
    eventRepository: EventRepository,
    private val rsvpRepository: RsvpRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : BaseDashboardViewModel(
    authRepository, userRepository, teamMemberRepository,
    teamRepository, eventRepository, appPreferencesRepository,
), PlayerDashboardScreenActions {

    override val memberRoles = setOf("player", "parent")
    override val logTag = "PlayerDashboardViewModel"

    private var loadedMyRsvp: Rsvp? = null

    var uiState by mutableStateOf(PlayerDashboardScreenUiState())
        private set

    init { loadDashboard() }

    // ── Abstract hook implementations ─────────────────────────────────────────

    override fun onLoadingStart() {
        uiState = uiState.copy(isLoading = true, error = null)
    }

    override fun onLoadingEnd() {
        uiState = uiState.copy(isLoading = false)
    }

    override fun onMembersLoadingStart() {
        uiState = uiState.copy(isMembersLoading = true)
    }

    override fun onLoadError(error: Int) {
        uiState = uiState.copy(isLoading = false, isMembersLoading = false, error = error)
    }

    override fun onDashboardLoaded(name: String, teams: List<TeamUiItem>, activeTeam: TeamUiItem?) {
        uiState = uiState.copy(playerName = name, teams = teams, activeTeam = activeTeam)
    }

    override fun onTeamDataLoaded(
        teamId: String,
        upNextEvent: EventUiItem?,
        upcomingEvents: List<EventUiItem>,
        allEvents: List<EventUiItem>,
        members: List<ClubMemberUiItem>,
        calendarYear: Int,
        calendarMonth: Int,
        todayEpochDay: Long,
        eventDaysInView: Set<Long>
    ) {
        uiState = uiState.copy(
            isLoading = false,
            isMembersLoading = false,
            upNextEvent = upNextEvent,
            upcomingEvents = upcomingEvents,
            allEvents = allEvents,
            members = members,
            filteredMembers = applyMemberFilter(members, "", MemberFilter.ALL),
            memberSearchQuery = "",
            memberFilter = MemberFilter.ALL,
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            selectedEpochDay = todayEpochDay,
            eventDaysInView = eventDaysInView,
            myRsvpStatus = null,
        )
        if (upNextEvent != null) loadRsvpData(upNextEvent.eventId)
    }

    private fun loadRsvpData(eventId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val rsvps = runCatching {
                val myRsvp = rsvpRepository.getMyRsvp(eventId, uid)
                loadedMyRsvp = myRsvp
                val rsvps = rsvpRepository.getRsvpsByEvent(eventId)
                val yes = rsvps.count { it.status == "yes" }
                val late = rsvps.count { it.status == "late" }
                val no = rsvps.count { it.status == "no" }
                val current = uiState.upNextEvent
                uiState = uiState.copy(
                    myRsvpStatus = myRsvp?.status,
                    upNextEvent = current?.takeIf { it.eventId == eventId }
                        ?.copy(rsvpYes = yes, rsvpLate = late, rsvpNo = no)
                        ?: current,
                )
                Log.d(logTag, "RSVP data for $eventId: mine=${myRsvp?.status} yes=$yes late=$late no=$no")
                rsvps
            }.onFailure { Log.w(logTag, "Failed to load RSVP data for $eventId") }.getOrNull()
                ?: return@launch

            runCatching {
                val playerUserIds = uiState.members.filter { it.role == "player" }.map { it.userId }.toSet()
                val attendees = buildAttendees(rsvps.filter { it.status == "yes" && it.submittedBy in playerUserIds }.map { it.submittedBy }.filter { it.isNotBlank() })
                if (uiState.upNextEvent?.eventId == eventId) {
                    uiState = uiState.copy(upNextAttendees = attendees)
                }
            }.onFailure { e -> Log.w(logTag, "Failed to load attendee names for $eventId", e) }
        }
    }

    private fun refreshRsvpCounts(eventId: String) {
        viewModelScope.launch {
            val rsvps = runCatching {
                val rsvps = rsvpRepository.getRsvpsByEvent(eventId)
                val yes = rsvps.count { it.status == "yes" }
                val late = rsvps.count { it.status == "late" }
                val no = rsvps.count { it.status == "no" }
                val current = uiState.upNextEvent
                if (current?.eventId == eventId) {
                    uiState = uiState.copy(
                        upNextEvent = current.copy(rsvpYes = yes, rsvpLate = late, rsvpNo = no),
                    )
                }
                Log.d(logTag, "RSVP counts refreshed for $eventId: yes=$yes late=$late no=$no")
                rsvps
            }.onFailure { Log.w(logTag, "Failed to refresh RSVP counts for $eventId") }.getOrNull()
                ?: return@launch

            runCatching {
                val playerUserIds = uiState.members.filter { it.role == "player" }.map { it.userId }.toSet()
                val attendees = buildAttendees(rsvps.filter { it.status == "yes" && it.submittedBy in playerUserIds }.map { it.submittedBy }.filter { it.isNotBlank() })
                if (uiState.upNextEvent?.eventId == eventId) {
                    uiState = uiState.copy(upNextAttendees = attendees)
                }
            }.onFailure { e -> Log.w(logTag, "Failed to load attendee names for $eventId", e) }
        }
    }

    private suspend fun buildAttendees(uids: List<String>): List<ClubMemberUiItem> {
        if (uids.isEmpty()) return emptyList()
        return userRepository.getUsersByIds(uids).map { user ->
            ClubMemberUiItem(
                memberId = user.id,
                userId = user.id,
                displayName = user.displayName.ifBlank { user.email },
                role = "",
                subtitle = "",
                avatarUrl = user.avatarId.ifBlank { user.photoURL },
            )
        }
    }

    private fun submitRsvp(status: String, reason: String?, note: String?) {
        val uid = authRepository.currentUser?.uid ?: return
        val eventId = uiState.rsvpTargetEventId ?: return
        val isUpNext = eventId == uiState.upNextEvent?.eventId
        viewModelScope.launch {
            uiState = uiState.copy(isRsvpSubmitting = true)
            runCatching {
                val existing = loadedMyRsvp
                if (existing != null) {
                    rsvpRepository.updateRsvp(
                        existing.copy(status = status, reason = reason.orEmpty(), reasonNote = note.orEmpty())
                    )
                } else {
                    rsvpRepository.saveRsvp(
                        Rsvp(eventId = eventId, status = status, reason = reason.orEmpty(), reasonNote = note.orEmpty(), submittedBy = uid)
                    )
                }
                uiState = uiState.copy(
                    isRsvpSubmitting = false,
                    myRsvpStatus = if (isUpNext) status else uiState.myRsvpStatus,
                    rsvpTargetEventId = null,
                )
                if (isUpNext) refreshRsvpCounts(eventId)
                Log.d(logTag, "RSVP submitted for $eventId: status=$status reason=$reason")
            }.onFailure { e ->
                Log.e(logTag, "Failed to submit RSVP for $eventId", e)
                uiState = uiState.copy(isRsvpSubmitting = false)
            }
        }
    }

    // ── PlayerDashboardScreenActions ──────────────────────────────────────────

    override fun onTeamDropdownToggle() {
        uiState = uiState.copy(isTeamDropdownOpen = !uiState.isTeamDropdownOpen)
    }

    override fun onTeamSelected(teamId: String) {
        val team = uiState.teams.firstOrNull { it.teamId == teamId } ?: return
        appPreferencesRepository.activeTeamId = teamId
        uiState = uiState.copy(activeTeam = team, isTeamDropdownOpen = false, isLoading = true)
        Log.d(logTag, "Team selected: $teamId")
        loadTeamData(teamId, team.teamName)
    }

    override fun onUpdateAttendance() {
        uiState = uiState.copy(
            rsvpTargetEventId = uiState.upNextEvent?.eventId,
            isRsvpSelectionSheetOpen = true,
        )
    }

    override fun onUpdateAttendanceForEvent(eventId: String) {
        uiState = uiState.copy(
            rsvpTargetEventId = eventId,
            isRsvpSelectionSheetOpen = true,
        )
    }

    override fun onRsvpSelected(status: String) {
        uiState = uiState.copy(isRsvpSelectionSheetOpen = false)
        if (status == "no") {
            uiState = uiState.copy(isRsvpReasonSheetOpen = true)
        } else {
            submitRsvp(status, reason = null, note = null)
        }
    }

    override fun onDismissRsvpSheet() {
        uiState = uiState.copy(isRsvpSelectionSheetOpen = false, rsvpTargetEventId = null)
    }

    override fun onReasonSelected(reason: String) {
        uiState = uiState.copy(selectedReason = reason)
    }

    override fun onReasonNoteChanged(note: String) {
        uiState = uiState.copy(reasonNote = note)
    }

    override fun onSubmitReason() {
        submitRsvp("no", uiState.selectedReason, uiState.reasonNote)
        uiState = uiState.copy(
            isRsvpReasonSheetOpen = false,
            selectedReason = null,
            reasonNote = "",
        )
    }

    override fun onDismissReasonSheet() {
        uiState = uiState.copy(
            isRsvpReasonSheetOpen = false,
            rsvpTargetEventId = null,
            selectedReason = null,
            reasonNote = "",
        )
    }

    override fun onRosterSearchQueryChange(query: String) {
        uiState = uiState.copy(
            memberSearchQuery = query,
            filteredMembers = applyMemberFilter(uiState.members, query, uiState.memberFilter),
        )
    }

    override fun onRosterFilterChange(filter: MemberFilter) {
        uiState = uiState.copy(
            memberFilter = filter,
            filteredMembers = applyMemberFilter(uiState.members, uiState.memberSearchQuery, filter),
        )
    }

    override fun onPreviousMonth() {
        val cal = Calendar.getInstance().apply {
            set(uiState.calendarYear, uiState.calendarMonth, 1)
            add(Calendar.MONTH, -1)
        }
        val year = cal.get(Calendar.YEAR); val month = cal.get(Calendar.MONTH)
        uiState = uiState.copy(
            calendarYear = year,
            calendarMonth = month,
            eventDaysInView = computeEventDaysInView(uiState.allEvents, year, month),
        )
    }

    override fun onNextMonth() {
        val cal = Calendar.getInstance().apply {
            set(uiState.calendarYear, uiState.calendarMonth, 1)
            add(Calendar.MONTH, 1)
        }
        val year = cal.get(Calendar.YEAR); val month = cal.get(Calendar.MONTH)
        uiState = uiState.copy(
            calendarYear = year,
            calendarMonth = month,
            eventDaysInView = computeEventDaysInView(uiState.allEvents, year, month),
        )
    }

    override fun onDateSelected(epochDay: Long) {
        uiState = uiState.copy(selectedEpochDay = epochDay)
    }

    override fun onCardClicked(eventId: String) {
        uiState = uiState.copy(navigateToEventDetailId = eventId)
    }

    fun onEventDetailNavigated() {
        uiState = uiState.copy(navigateToEventDetailId = null)
    }

    fun refreshEvents() {
        val team = uiState.activeTeam ?: return
        loadTeamData(team.teamId, team.teamName)
    }
}
