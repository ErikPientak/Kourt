package com.kourt.app.viewmodel.dashboard

import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.StringRes
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
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val ROLE_ORDER = mapOf("admin" to 0, "coach" to 1, "assistant" to 2, "player" to 3, "parent" to 4)

abstract class BaseDashboardViewModel(
    protected val authRepository: AuthRepository,
    protected val userRepository: UserRepository,
    protected val teamMemberRepository: TeamMemberRepository,
    protected val teamRepository: TeamRepository,
    protected val eventRepository: EventRepository,
    protected val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {

    protected abstract val memberRoles: Set<String>
    protected abstract val logTag: String

    // ── Template methods ──────────────────────────────────────────────────────

    protected fun loadDashboard() {
        viewModelScope.launch {
            onLoadingStart()
            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                onLoadError(R.string.error_not_signed_in)
                return@launch
            }
            runCatching {
                coroutineScope {
                    val nameDeferred = async { userRepository.getUser(uid) }
                    val membershipsDeferred = async { teamMemberRepository.getMembersByUser(uid) }

                    val name = nameDeferred.await()?.displayName.orEmpty()
                    val memberships = membershipsDeferred.await()
                        .filter { it.role.lowercase() in memberRoles }

                    val teams = memberships
                        .map { m -> async { teamRepository.getTeam(m.teamId) } }
                        .awaitAll()
                        .filterNotNull()
                        .map { team ->
                            TeamUiItem(
                                teamId = team.id,
                                teamName = team.name,
                                clubId = team.clubId,
                                accentColor = team.accentColor,
                                initials = team.initials,
                            )
                        }

                    val savedId = appPreferencesRepository.activeTeamId
                    val activeTeam = teams.firstOrNull { it.teamId == savedId } ?: teams.firstOrNull()

                    Log.d(logTag, "Loaded ${teams.size} teams, active=${activeTeam?.teamId}")
                    onDashboardLoaded(name, teams, activeTeam)

                    if (activeTeam != null) loadTeamData(activeTeam.teamId, activeTeam.teamName)
                    else onLoadingEnd()
                }
            }.onFailure { e ->
                Log.e(logTag, "Failed to load dashboard", e)
                onLoadError(R.string.error_load_failed)
            }
        }
    }

    protected fun loadTeamData(teamId: String, teamName: String) {
        viewModelScope.launch {
            onMembersLoadingStart()
            runCatching {
                coroutineScope {
                    val eventsDeferred = async { eventRepository.getEventsByTeam(teamId) }
                    val membersDeferred = async { teamMemberRepository.getMembersByTeam(teamId) }

                    val today = Calendar.getInstance()
                    val todayYear = today.get(Calendar.YEAR)
                    val todayMonth = today.get(Calendar.MONTH)
                    val todayEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

                    val sortedEvents = eventsDeferred.await().sortedBy { it.date.toDate() }
                    val futureEvents = sortedEvents.filter {
                        TimeUnit.MILLISECONDS.toDays(it.date.toDate().time) >= todayEpochDay
                    }

                    val upNext = futureEvents.firstOrNull { it.status.lowercase() == "upcoming" }?.toEventUiItem()
                    val upcoming = futureEvents.drop(1).take(5).map { it.toEventUiItem() }
                    val allEvents = sortedEvents.map { it.toEventUiItem() }
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
                            avatarUrl = u.avatarId.ifBlank { u.photoURL },
                        )
                    }

                    Log.d(logTag, "Team data: upNext=${upNext?.title}, upcoming=${upcoming.size}, roster=${members.size}")

                    onTeamDataLoaded(
                        teamId = teamId,
                        upNextEvent = upNext,
                        upcomingEvents = upcoming,
                        allEvents = allEvents,
                        members = members,
                        calendarYear = todayYear,
                        calendarMonth = todayMonth,
                        todayEpochDay = todayEpochDay,
                        eventDaysInView = eventDaysInView,
                    )
                }
            }.onFailure { e ->
                Log.e(logTag, "Failed to load team data for $teamId", e)
                onLoadError(R.string.error_load_failed)
            }
        }
    }

    // ── Abstract hooks ────────────────────────────────────────────────────────

    protected abstract fun onLoadingStart()
    protected abstract fun onLoadingEnd()
    protected abstract fun onMembersLoadingStart()
    protected abstract fun onLoadError(@StringRes error: Int)
    protected abstract fun onDashboardLoaded(name: String, teams: List<TeamUiItem>, activeTeam: TeamUiItem?)
    protected abstract fun onTeamDataLoaded(
        teamId: String,
        upNextEvent: EventUiItem?,
        upcomingEvents: List<EventUiItem>,
        allEvents: List<EventUiItem>,
        members: List<ClubMemberUiItem>,
        calendarYear: Int,
        calendarMonth: Int,
        todayEpochDay: Long,
        eventDaysInView: Set<Long>,
    )

    // ── Shared pure helpers ───────────────────────────────────────────────────

    protected fun computeEventDaysInView(events: List<EventUiItem>, year: Int, month: Int): Set<Long> {
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

    protected fun applyMemberFilter(
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

    protected fun Event.toEventUiItem(): EventUiItem {
        val eventDate = date.toDate()
        val cal = Calendar.getInstance().apply { time = eventDate }
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val isTomorrow = cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)

        val locale = Locale.getDefault()
        val shortDatePattern = DateFormat.getBestDateTimePattern(locale, "MMMd")

        return EventUiItem(
            eventId = id,
            title = title,
            type = type,
            status = status,
            teamId = teamId,
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
