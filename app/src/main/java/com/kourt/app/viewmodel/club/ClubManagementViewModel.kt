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
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.ClubManagementScreenActions
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.MemberFilter
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

                // Kick off member loading now that we have teams
                loadMembers(teams)
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

                // 2. Build a map of userId → (TeamMember, teamName), de-duplicated
                //    by userId so each person appears once. First occurrence wins.
                val userIdToEntry = linkedMapOf<String, Pair<com.kourt.app.data.model.TeamMember, String>>()
                for ((team, members) in teamMembersByTeam) {
                    for (member in members) {
                        if (member.userId.isNotEmpty() && !userIdToEntry.containsKey(member.userId)) {
                            userIdToEntry[member.userId] = member to team.name
                        }
                    }
                }

                // 3. Resolve user display names in parallel
                val userMap = userIdToEntry.keys
                    .map { uid -> async { uid to userRepository.getUser(uid) } }
                    .awaitAll()
                    .toMap()

                // 4. Build UI projections
                val items = userIdToEntry.entries.mapNotNull { (userId, pair) ->
                    val (teamMember, teamName) = pair
                    val user = userMap[userId] ?: return@mapNotNull null

                    val subtitle = buildSubtitle(
                        role = teamMember.role,
                        teamName = teamName,
                        athleteName = user.displayName,
                    )

                    ClubMemberUiItem(
                        memberId = teamMember.id,
                        userId = userId,
                        displayName = user.displayName.ifBlank { user.email },
                        role = teamMember.role.lowercase(),
                        subtitle = subtitle,
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
    private fun buildSubtitle(role: String, teamName: String, athleteName: String): String {
        return if (role.lowercase() == "parent") {
            "Athlete: $athleteName"
        } else {
            "Team: $teamName"
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
        Log.d(TAG, "onMemberMenuClick: memberId=$memberId — opening action sheet")
        uiState = uiState.copy(selectedMemberId = memberId)
    }

    override fun onAddMember() {
        // Stub — add-member flow not yet implemented
        Log.d(TAG, "onAddMember")
    }

    // ── Member Action bottom sheet ────────────────────────────────────────────

    override fun onMemberActionDismiss() {
        Log.d(TAG, "onMemberActionDismiss")
        uiState = uiState.copy(selectedMemberId = null)
    }

    override fun onEditMember(memberId: String) {
        Log.d(TAG, "onEditMember: memberId=$memberId")
        uiState = uiState.copy(selectedMemberId = null)
    }

    override fun onChangeRole(memberId: String) {
        Log.d(TAG, "onChangeRole: memberId=$memberId")
        uiState = uiState.copy(selectedMemberId = null)
    }

    override fun onRemoveFromClub(memberId: String) {
        Log.d(TAG, "onRemoveFromClub: memberId=$memberId")
        uiState = uiState.copy(selectedMemberId = null)
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
