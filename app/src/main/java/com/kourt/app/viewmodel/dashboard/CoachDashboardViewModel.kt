package com.kourt.app.viewmodel.dashboard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.Event
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "CoachDashboardViewModel"

private val ROLE_ORDER = mapOf("admin" to 0, "coach" to 1, "assistant" to 2, "player" to 3, "parent" to 4)

@HiltViewModel
class CoachDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamRepository: TeamRepository,
    private val eventRepository: EventRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel(), CoachDashboardScreenActions {

    var uiState by mutableStateOf(CoachDashboardScreenUiState())
        private set

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                uiState = uiState.copy(isLoading = false, error = R.string.error_not_signed_in)
                return@launch
            }

            runCatching {
                coroutineScope {
                    val userDeferred = async { userRepository.getUser(uid) }
                    val membershipsDeferred = async { teamMemberRepository.getMembersByUser(uid) }

                    val coachName = userDeferred.await()?.displayName.orEmpty()
                    val memberships = membershipsDeferred.await()
                        .filter { it.role.lowercase() in setOf("coach", "assistant") }

                    val teams = memberships
                        .map { m -> async { teamRepository.getTeam(m.teamId) } }
                        .awaitAll()
                        .filterNotNull()
                        .map { team -> TeamUiItem(teamId = team.id, teamName = team.name, clubId = team.clubId) }

                    val savedId = appPreferencesRepository.activeTeamId
                    val activeTeam = teams.firstOrNull { it.teamId == savedId } ?: teams.firstOrNull()

                    uiState = uiState.copy(coachName = coachName, teams = teams, activeTeam = activeTeam)
                    Log.d(TAG, "Loaded ${teams.size} coached teams, active=${activeTeam?.teamId}")

                    if (activeTeam != null) {
                        loadTeamData(activeTeam.teamId, activeTeam.teamName)
                    } else {
                        uiState = uiState.copy(isLoading = false)
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load dashboard", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    private fun loadTeamData(teamId: String, teamName: String = uiState.activeTeam?.teamName.orEmpty()) {
        viewModelScope.launch {
            uiState = uiState.copy(isMembersLoading = true)
            runCatching {
                coroutineScope {
                    val eventsDeferred = async { eventRepository.getEventsByTeam(teamId) }
                    val membersDeferred = async { teamMemberRepository.getMembersByTeam(teamId) }

                    val now = Date()
                    val sortedEvents = eventsDeferred.await().sortedBy { it.date.toDate() }
                    val futureEvents = sortedEvents.filter { it.date.toDate().after(now) }

                    val upNext = futureEvents.firstOrNull()?.toEventUiItem()
                    val upcoming = futureEvents.drop(1).take(5).map { it.toEventUiItem() }
                    val allEvents = sortedEvents.map { it.toEventUiItem() }

                    val today = Calendar.getInstance()
                    val todayYear = today.get(Calendar.YEAR)
                    val todayMonth = today.get(Calendar.MONTH)
                    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(today.timeInMillis)
                    val eventDaysInView = computeEventDaysInView(allEvents, todayYear, todayMonth)

                    val teamMembers = membersDeferred.await()
                    val userIds = teamMembers.map { it.userId }.filter { it.isNotBlank() }
                    val users = userRepository.getUsersByIds(userIds)
                    val userMap = users.associateBy { it.id }

                    val members = teamMembers.mapNotNull { m ->
                        val u = userMap[m.userId] ?: return@mapNotNull null
                        val role = m.role.lowercase()
                        val subtitle = when (role) {
                            "parent" -> "Athlete: ${u.displayName.ifBlank { u.email }}"
                            else -> "Team: $teamName"
                        }
                        ClubMemberUiItem(
                            memberId = m.id,
                            userId = m.userId,
                            displayName = u.displayName.ifBlank { u.email },
                            role = role,
                            subtitle = subtitle,
                        )
                    }

                    uiState = uiState.copy(
                        isLoading = false,
                        isMembersLoading = false,
                        upNextEvent = upNext,
                        upcomingEvents = upcoming,
                        allEvents = allEvents,
                        members = members,
                        filteredMembers = applyFilter(members, "", MemberFilter.ALL),
                        memberSearchQuery = "",
                        memberFilter = MemberFilter.ALL,
                        calendarYear = todayYear,
                        calendarMonth = todayMonth,
                        selectedEpochDay = todayEpochDay,
                        eventDaysInView = eventDaysInView,
                    )
                    Log.d(TAG, "Team data loaded: upNext=${upNext?.title}, upcoming=${upcoming.size}, roster=${members.size}")
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load team data for $teamId", e)
                uiState = uiState.copy(isLoading = false, isMembersLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onTeamDropdownToggle() {
        uiState = uiState.copy(isTeamDropdownOpen = !uiState.isTeamDropdownOpen)
    }

    override fun onTeamSelected(teamId: String) {
        val team = uiState.teams.firstOrNull { it.teamId == teamId } ?: return
        appPreferencesRepository.activeTeamId = teamId
        uiState = uiState.copy(activeTeam = team, isTeamDropdownOpen = false, isLoading = true)
        Log.d(TAG, "Team selected: $teamId")
        loadTeamData(teamId, team.teamName)
    }

    override fun onUpdateLineup() {
        Log.d(TAG, "onUpdateLineup tapped — not yet implemented")
    }

    override fun onRosterSearchQueryChange(query: String) {
        uiState = uiState.copy(
            memberSearchQuery = query,
            filteredMembers = applyFilter(uiState.members, query, uiState.memberFilter),
        )
    }

    override fun onRosterFilterChange(filter: MemberFilter) {
        uiState = uiState.copy(
            memberFilter = filter,
            filteredMembers = applyFilter(uiState.members, uiState.memberSearchQuery, filter),
        )
    }

    override fun onPreviousMonth() {
        val cal = Calendar.getInstance().apply {
            set(uiState.calendarYear, uiState.calendarMonth, 1)
            add(Calendar.MONTH, -1)
        }
        val newYear = cal.get(Calendar.YEAR)
        val newMonth = cal.get(Calendar.MONTH)
        uiState = uiState.copy(
            calendarYear = newYear,
            calendarMonth = newMonth,
            eventDaysInView = computeEventDaysInView(uiState.allEvents, newYear, newMonth),
        )
    }

    override fun onNextMonth() {
        val cal = Calendar.getInstance().apply {
            set(uiState.calendarYear, uiState.calendarMonth, 1)
            add(Calendar.MONTH, 1)
        }
        val newYear = cal.get(Calendar.YEAR)
        val newMonth = cal.get(Calendar.MONTH)
        uiState = uiState.copy(
            calendarYear = newYear,
            calendarMonth = newMonth,
            eventDaysInView = computeEventDaysInView(uiState.allEvents, newYear, newMonth),
        )
    }

    override fun onDateSelected(epochDay: Long) {
        uiState = uiState.copy(selectedEpochDay = epochDay)
    }

    override fun onLogAttendance(eventId: String) {
        Log.d(TAG, "onLogAttendance: $eventId — not yet implemented")
    }

    override fun onLogStatistics(eventId: String) {
        Log.d(TAG, "onLogStatistics: $eventId — not yet implemented")
    }

    override fun onAddEvent() {
        uiState = uiState.copy(navigateToAddEvent = true)
    }

    fun onAddEventNavigated() {
        uiState = uiState.copy(navigateToAddEvent = false)
    }

    fun refreshEvents() {
        val teamId = uiState.activeTeam?.teamId ?: return
        loadTeamData(teamId)
    }

    private fun computeEventDaysInView(events: List<EventUiItem>, year: Int, month: Int): Set<Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val startDay = TimeUnit.MILLISECONDS.toDays(start.timeInMillis)
        val endDay = TimeUnit.MILLISECONDS.toDays(end.timeInMillis)
        return events.filter { it.epochDay in startDay..endDay }.map { it.epochDay }.toSet()
    }

    private fun applyFilter(
        members: List<ClubMemberUiItem>,
        query: String,
        filter: MemberFilter,
    ): List<ClubMemberUiItem> {
        val q = query.trim().lowercase()
        return members.filter { m ->
            val matchesQuery = q.isEmpty() || m.displayName.lowercase().contains(q)
            val matchesFilter = when (filter) {
                MemberFilter.ALL -> true
                MemberFilter.COACH -> m.role == "coach"
                MemberFilter.PLAYER -> m.role == "player"
                MemberFilter.PARENT -> m.role == "parent"
                MemberFilter.ASSISTANT -> m.role == "assistant"
            }
            matchesQuery && matchesFilter
        }.sortedBy { ROLE_ORDER[it.role] ?: Int.MAX_VALUE }
    }

    private fun Event.toEventUiItem(): EventUiItem {
        val eventDate = date.toDate()
        val cal = Calendar.getInstance().apply { time = eventDate }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val isTomorrow = cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)

        val locale = Locale.getDefault()
        val shortDatePattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd")

        return EventUiItem(
            eventId = id,
            title = title,
            type = type,
            status = status,
            dayOfWeek = SimpleDateFormat("EEE", locale).format(eventDate).uppercase(locale),
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            startTime = startTime,
            location = location,
            isToday = isToday,
            dateLabel = when {
                isToday -> "Today"
                isTomorrow -> "Tomorrow"
                else -> SimpleDateFormat(shortDatePattern, locale).format(eventDate)
            },
            epochDay = TimeUnit.MILLISECONDS.toDays(eventDate.time),
            subtitle = notes,
        )
    }
}
