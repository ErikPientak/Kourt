package com.kourt.app.viewmodel.club

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.Team
import com.kourt.app.data.repository.AppPreferencesRepository
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.ClubManagementScreenActions
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.EventStatusFilter
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClubManagementViewModel"

private val ROLE_ORDER = mapOf("admin" to 0, "coach" to 1, "assistant" to 2, "player" to 3, "parent" to 4)

@HiltViewModel
class ClubManagementViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clubRepository: ClubRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val eventRepository: EventRepository,
) : ViewModel(), ClubManagementScreenActions {

    var uiState by mutableStateOf(ClubManagementScreenUiState())
        private set

    init {
        loadClubData()
    }

    // ── Club + teams ──────────────────────────────────────────────────────────

    private fun loadClubData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                uiState = uiState.copy(isLoading = false, error = context.getString(R.string.error_not_signed_in))
                return@launch
            }

            runCatching {
                val activeClubId = appPreferencesRepository.activeClubId
                val club = if (activeClubId.isNotEmpty()) {
                    clubRepository.getClub(activeClubId)
                } else {
                    clubRepository.getClubByAdminId(uid)
                }
                if (club == null) {
                    uiState = uiState.copy(isLoading = false, error = context.getString(R.string.error_no_club_found))
                    return@launch
                }

                val teams = teamRepository.getTeamsByClub(club.id)
                uiState = uiState.copy(
                    isLoading = false,
                    clubId = club.id,
                    clubName = club.name,
                    teams = teams,
                )
                Log.d(TAG, "Club data loaded: $club")

                // Kick off member and event loading now that we have teams
                loadMembers(teams)
                loadEvents(teams)
            }.onFailure { e ->
                Log.e(TAG, "Failed to load club data", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load club data",
                )
            }
        }
    }

    // ── Members tab ───────────────────────────────────────────────────────────

    /**
     * Loads all members across every team in the club.
     *
     * Strategy:
     * 1. Fan out — fetch [TeamMember] records for each team in parallel.
     * 2. De-duplicate by userId (a user can appear in multiple teams; we keep
     *    the first occurrence to show one card per person).
     * 3. Resolve display names — fetch [User] records in parallel for each
     *    unique userId.
     * 4. Build [ClubMemberUiItem] projections with pre-formatted subtitles.
     * 5. Apply the current search query + filter and store both lists.
     */
    private fun loadMembers(teams: List<Team>) {
        viewModelScope.launch {
            uiState = uiState.copy(isMembersLoading = true, membersError = null)

            runCatching {
                // 1. Fetch TeamMember docs for all teams in parallel
                val teamMembersByTeam = teams.map { team ->
                    async { team to teamMemberRepository.getMembersByTeam(team.id) }
                }.awaitAll()

                // 2. Group all (TeamMember, teamName) entries per userId — keeps every membership.
                val userIdToEntries = linkedMapOf<String, MutableList<Pair<com.kourt.app.data.model.TeamMember, String>>>()
                for ((team, members) in teamMembersByTeam) {
                    for (member in members) {
                        if (member.userId.isNotEmpty()) {
                            userIdToEntries.getOrPut(member.userId) { mutableListOf() }
                                .add(member to team.name)
                        }
                    }
                }

                // 3. Resolve user display names in parallel
                val userMap = userIdToEntries.keys
                    .map { uid -> async { uid to userRepository.getUser(uid) } }
                    .awaitAll()
                    .toMap()

                // 4. Build UI projections
                val items = userIdToEntries.entries.mapNotNull { (userId, entries) ->
                    val (primaryMember, _) = entries.first()
                    val user = userMap[userId] ?: return@mapNotNull null
                    val teamNames = entries.map { it.second }

                    ClubMemberUiItem(
                        memberId = primaryMember.id,
                        userId = userId,
                        displayName = user.displayName.ifBlank { user.email },
                        role = primaryMember.role.lowercase(),
                        subtitle = buildSubtitle(primaryMember.role, teamNames, user.displayName),
                        avatarUrl = user.photoURL,
                    )
                }

                val filtered = applyFilter(items, uiState.memberSearchQuery, uiState.memberFilter)
                uiState = uiState.copy(
                    isMembersLoading = false,
                    members = items,
                    filteredMembers = filtered,
                )
                Log.d(TAG, "Members loaded: ${items.size}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load members", e)
                uiState = uiState.copy(
                    isMembersLoading = false,
                    membersError = e.message ?: "Failed to load members",
                )
            }
        }
    }

    /**
     * Formats the subtitle line shown on each member card.
     *
     * Parents see "Athlete: <athleteName>" (populated later once parent–child
     * linking is implemented; falls back to the team name in the interim).
     * All other roles see "Team: <teamName>".
     */
    private fun buildSubtitle(role: String, teamNames: List<String>, athleteName: String): String {
        return if (role.lowercase() == "parent") {
            "Athlete: $athleteName"
        } else {
            teamNames.joinToString(" · ")
        }
    }

    /**
     * Pure function — filters [members] by [query] (case-insensitive substring
     * match on display name) and by [filter] (role category).
     */
    private fun applyFilter(
        members: List<ClubMemberUiItem>,
        query: String,
        filter: MemberFilter,
    ): List<ClubMemberUiItem> {
        val trimmed = query.trim()
        return members.filter { item ->
            val matchesQuery = trimmed.isEmpty() ||
                    item.displayName.contains(trimmed, ignoreCase = true)
            val matchesFilter = when (filter) {
                MemberFilter.ALL -> true
                MemberFilter.COACH -> item.role == "coach"
                MemberFilter.PLAYER -> item.role == "player"
                MemberFilter.PARENT -> item.role == "parent"
                MemberFilter.ASSISTANT -> item.role == "assistant"
            }
            matchesQuery && matchesFilter
        }.sortedBy { ROLE_ORDER[it.role] ?: Int.MAX_VALUE }
    }

    override fun onMemberSearchQueryChange(query: String) {
        val filtered = applyFilter(uiState.members, query, uiState.memberFilter)
        uiState = uiState.copy(memberSearchQuery = query, filteredMembers = filtered)
    }

    override fun onMemberFilterChange(filter: MemberFilter) {
        val filtered = applyFilter(uiState.members, uiState.memberSearchQuery, filter)
        uiState = uiState.copy(memberFilter = filter, filteredMembers = filtered)
    }

    override fun onMemberMenuClick(memberId: String) {
        val member = uiState.members.find { it.memberId == memberId } ?: return
        uiState = uiState.copy(selectedMember = member)
    }

    override fun onAddMember() {
        Log.d(TAG, "onAddMember: stub")
    }

    // ── Member Action bottom sheet ────────────────────────────────────────────

    override fun onMemberActionDismiss() {
        uiState = uiState.copy(selectedMember = null)
    }

    // ── Assign to Team ────────────────────────────────────────────────────────

    override fun onAssignToTeam(memberId: String) {
        val member = uiState.members.find { it.memberId == memberId } ?: return
        uiState = uiState.copy(memberForAssignTeam = member)
    }

    override fun onAssignTeamSelected(memberId: String, teamId: String) {
        val member = uiState.memberForAssignTeam ?: return
        uiState = uiState.copy(memberForAssignTeam = null)
        viewModelScope.launch {
            runCatching {
                teamMemberRepository.addMember(
                    com.kourt.app.data.model.TeamMember(
                        userId = member.userId,
                        teamId = teamId,
                        role = member.role,
                    )
                )
                Log.d(TAG, "Assigned userId=${member.userId} to teamId=$teamId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to assign member to team", e)
            }
        }
    }

    override fun onAssignTeamDismiss() {
        uiState = uiState.copy(memberForAssignTeam = null)
    }

    // ── Make Admin ────────────────────────────────────────────────────────────

    override fun onMakeAdmin(memberId: String) {
        val member = uiState.members.find { it.memberId == memberId } ?: return
        uiState = uiState.copy(memberForMakeAdmin = member)
    }

    override fun onMakeAdminConfirm(memberId: String) {
        val member = uiState.memberForMakeAdmin ?: return
        uiState = uiState.copy(memberForMakeAdmin = null)
        viewModelScope.launch {
            runCatching {
                clubRepository.addAdmin(uiState.clubId, member.userId)
                Log.d(TAG, "Made admin: userId=${member.userId}, clubId=${uiState.clubId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to make admin", e)
            }
        }
    }

    override fun onMakeAdminDismiss() {
        uiState = uiState.copy(memberForMakeAdmin = null)
    }

    // ── Remove Member ─────────────────────────────────────────────────────────

    override fun onRemoveMember(memberId: String) {
        val member = uiState.members.find { it.memberId == memberId } ?: return
        uiState = uiState.copy(memberForRemove = member)
    }

    override fun onRemoveConfirm(memberId: String) {
        uiState = uiState.copy(memberForRemove = null)
        viewModelScope.launch {
            runCatching {
                teamMemberRepository.removeMember(memberId)
                val updated = uiState.members.filter { it.memberId != memberId }
                val filtered = applyFilter(updated, uiState.memberSearchQuery, uiState.memberFilter)
                uiState = uiState.copy(members = updated, filteredMembers = filtered)
                Log.d(TAG, "Removed member: memberId=$memberId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to remove member", e)
            }
        }
    }

    override fun onRemoveDismiss() {
        uiState = uiState.copy(memberForRemove = null)
    }

    // ── Events tab ────────────────────────────────────────────────────────────

    private fun loadEvents(teams: List<Team>) {
        viewModelScope.launch {
            uiState = uiState.copy(isEventsLoading = true)
            runCatching {
                val allEvents = teams.flatMap { team ->
                    eventRepository.getEventsByTeam(team.id).map { event ->
                        event.toClubEventUiItem(team.name, team.accentColor)
                    }
                }.sortedBy { it.epochDay }

                uiState = uiState.copy(
                    isEventsLoading = false,
                    events = allEvents,
                    filteredEvents = applyEventFilter(allEvents, uiState.eventStatusFilter),
                )
                Log.d(TAG, "Events loaded: ${allEvents.size}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load events", e)
                uiState = uiState.copy(isEventsLoading = false)
            }
        }
    }

    private fun applyEventFilter(events: List<EventUiItem>, filter: EventStatusFilter): List<EventUiItem> {
        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        return when (filter) {
            EventStatusFilter.ALL       -> events
            EventStatusFilter.UPCOMING  -> events.filter { it.epochDay >= today && it.status != "cancelled" }
            EventStatusFilter.PAST      -> events.filter { it.epochDay < today && it.status != "cancelled" }
            EventStatusFilter.CANCELLED -> events.filter { it.status == "cancelled" }
        }
    }

    private fun com.kourt.app.data.model.Event.toClubEventUiItem(teamName: String, teamColor: String): EventUiItem {
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
            teamId = teamId,
            teamName = teamName,
            teamColor = teamColor,
            dayOfWeek = SimpleDateFormat("EEE", locale).format(eventDate).uppercase(locale),
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            startTime = startTime,
            location = location,
            isToday = isToday,
            dateLabel = when {
                isToday    -> "Today"
                isTomorrow -> "Tomorrow"
                else       -> SimpleDateFormat(shortDatePattern, locale).format(eventDate)
            },
            epochDay = TimeUnit.MILLISECONDS.toDays(eventDate.time),
            subtitle = notes,
        )
    }

    override fun onEventStatusFilterChange(filter: EventStatusFilter) {
        uiState = uiState.copy(
            eventStatusFilter = filter,
            filteredEvents = applyEventFilter(uiState.events, filter),
        )
    }

    override fun onEventMenuClick(eventId: String) {
        val event = uiState.events.find { it.eventId == eventId } ?: return
        uiState = uiState.copy(selectedEvent = event)
    }

    override fun onEventActionDismiss() {
        uiState = uiState.copy(selectedEvent = null)
    }

    override fun onEventCancelClick(eventId: String) {
        val event = uiState.events.find { it.eventId == eventId } ?: return
        uiState = uiState.copy(selectedEvent = null, eventForCancel = event)
    }

    override fun onEventCancelConfirm(eventId: String) {
        uiState = uiState.copy(eventForCancel = null)
        viewModelScope.launch {
            runCatching {
                eventRepository.cancelEvent(eventId)
                val updated = uiState.events.map { if (it.eventId == eventId) it.copy(status = "cancelled") else it }
                uiState = uiState.copy(
                    events = updated,
                    filteredEvents = applyEventFilter(updated, uiState.eventStatusFilter),
                )
                Log.d(TAG, "Event cancelled: $eventId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to cancel event $eventId", e)
            }
        }
    }

    override fun onEventCancelDismiss() {
        uiState = uiState.copy(eventForCancel = null)
    }

    override fun onEventDeleteClick(eventId: String) {
        val event = uiState.events.find { it.eventId == eventId } ?: return
        uiState = uiState.copy(selectedEvent = null, eventForDelete = event)
    }

    override fun onEventDeleteConfirm(eventId: String) {
        uiState = uiState.copy(eventForDelete = null)
        viewModelScope.launch {
            runCatching {
                eventRepository.deleteEvent(eventId)
                val updated = uiState.events.filter { it.eventId != eventId }
                uiState = uiState.copy(
                    events = updated,
                    filteredEvents = applyEventFilter(updated, uiState.eventStatusFilter),
                )
                Log.d(TAG, "Event deleted: $eventId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to delete event $eventId", e)
            }
        }
    }

    override fun onEventDeleteDismiss() {
        uiState = uiState.copy(eventForDelete = null)
    }

    // ── Teams tab ─────────────────────────────────────────────────────────────

    override fun onAddTeam() {
        Log.d(TAG, uiState.clubId)
    }

    override fun onTeamClick(teamId: String) {
        Log.d(TAG, "onTeamClick: teamId=$teamId")
    }

    override fun onSettingsTap() {
        Log.d(TAG, "onSettingsTap: Settings screen not yet implemented")
    }
}
