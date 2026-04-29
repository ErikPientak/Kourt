package com.kourt.app.viewmodel.dashboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.repository.AppPreferencesRepository
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreenActions
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreenUiState
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CoachDashboardViewModel @Inject constructor(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    teamMemberRepository: TeamMemberRepository,
    teamRepository: TeamRepository,
    eventRepository: EventRepository,
    appPreferencesRepository: AppPreferencesRepository,
) : BaseDashboardViewModel(
    authRepository, userRepository, teamMemberRepository,
    teamRepository, eventRepository, appPreferencesRepository,
), CoachDashboardScreenActions {

    override val memberRoles = setOf("coach", "assistant")
    override val logTag = "CoachDashboardViewModel"

    var uiState by mutableStateOf(CoachDashboardScreenUiState())
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
        uiState = uiState.copy(coachName = name, teams = teams, activeTeam = activeTeam)
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
        eventDaysInView: Set<Long>,
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
        )
        if (upNextEvent != null) loadRsvpCounts(upNextEvent.eventId)
    }

    private fun loadRsvpCounts(eventId: String) {
        viewModelScope.launch {
            val rsvps = runCatching {
                val rsvps = eventRepository.getRsvps(eventId)
                val yes = rsvps.count { it.status == "yes" }
                val late = rsvps.count { it.status == "late" }
                val no = rsvps.count { it.status == "no" }
                val current = uiState.upNextEvent
                if (current?.eventId == eventId) {
                    uiState = uiState.copy(
                        upNextEvent = current.copy(rsvpYes = yes, rsvpLate = late, rsvpNo = no),
                    )
                }
                Log.d(logTag, "RSVP counts for $eventId: yes=$yes late=$late no=$no")
                rsvps
            }.onFailure { Log.w(logTag, "Failed to load RSVP counts for $eventId") }.getOrNull()
                ?: return@launch

            runCatching {
                val yesUids = rsvps.filter { it.status == "yes" }.map { it.id }.filter { it.isNotBlank() }
                val attendees = if (yesUids.isNotEmpty()) {
                    userRepository.getUsersByIds(yesUids).map { user ->
                        ClubMemberUiItem(
                            memberId = user.id,
                            userId = user.id,
                            displayName = user.displayName.ifBlank { user.email },
                            role = "",
                            subtitle = "",
                            avatarUrl = user.avatarId.ifBlank { user.photoURL },
                        )
                    }
                } else emptyList()
                if (uiState.upNextEvent?.eventId == eventId) {
                    uiState = uiState.copy(upNextAttendees = attendees)
                }
                Log.d(logTag, "Attendees for $eventId: ${attendees.size}")
            }.onFailure { e -> Log.w(logTag, "Failed to load attendee names for $eventId", e) }
        }
    }

    // ── CoachDashboardScreenActions ───────────────────────────────────────────

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

    override fun onUpdateLineup() {
        Log.d(logTag, "onUpdateLineup — not yet implemented")
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

    override fun onLogAttendance(eventId: String) {
        Log.d(logTag, "onLogAttendance: $eventId — not yet implemented")
    }

    override fun onLogStatistics(eventId: String) {
        Log.d(logTag, "onLogStatistics: $eventId — not yet implemented")
    }

    override fun onAddEvent() {
        uiState = uiState.copy(navigateToAddEvent = true)
    }

    override fun onCardClicked(eventId: String) {
        uiState = uiState.copy(navigateToEventDetailId = eventId)
    }

    fun onAddEventNavigated() {
        uiState = uiState.copy(navigateToAddEvent = false)
    }

    fun onEventDetailNavigated() {
        uiState = uiState.copy(navigateToEventDetailId = null)
    }

    fun refreshEvents() {
        val team = uiState.activeTeam ?: return
        loadTeamData(team.teamId, team.teamName)
    }
}
