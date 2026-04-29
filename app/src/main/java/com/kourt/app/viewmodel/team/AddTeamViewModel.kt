package com.kourt.app.viewmodel.team

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.Team
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.team.create.AddTeamScreenActions
import com.kourt.app.ui.screens.team.create.AddTeamScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddTeamViewModel"
private const val JOIN_CODE_LENGTH = 6
private val JOIN_CODE_CHARS = ('A'..'Z') + ('0'..'9')

@HiltViewModel
class AddTeamViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val clubRepository: ClubRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), AddTeamScreenActions {

    var uiState by mutableStateOf(AddTeamScreenUiState())
        private set

    private val clubId: String = savedStateHandle["clubId"] ?: ""
    private val teamId: String = savedStateHandle["teamId"] ?: ""
    private var headCoachSearchJob: Job? = null
    private var assistantSearchJob: Job? = null
    private var clubMembers: List<User> = emptyList()

    init {
        if (teamId.isNotBlank()) {
            loadTeamForEdit(teamId)
        } else {
            loadClubMembers()
        }
    }

    private fun loadClubMembers() {
        viewModelScope.launch {
            runCatching {
                val club = clubRepository.getClub(clubId)
                val adminIds = club?.adminIds ?: emptyList()
                val teams = teamRepository.getTeamsByClub(clubId)
                val memberIds = teams
                    .flatMap { team -> teamMemberRepository.getMembersByTeam(team.id) }
                    .map { it.userId }
                    .toMutableSet()
                memberIds.addAll(adminIds)
                clubMembers = userRepository.getUsersByIds(memberIds.toList())
                club?.accentColor?.ifBlank { null }?.let { color ->
                    uiState = uiState.copy(accentColor = color)
                }
                Log.d(TAG, "Loaded ${clubMembers.size} club members (incl. admins) for search")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load club members", e)
            }
        }
    }

    private fun loadTeamForEdit(id: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching {
                val team = teamRepository.getTeam(id)
                if (team == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_team_not_found)
                    return@launch
                }
                loadClubMembersForClub(team.clubId)
                uiState = uiState.copy(
                    isLoading = false,
                    isEditMode = true,
                    teamId = team.id,
                    originalClubId = team.clubId,
                    originalJoinCode = team.joinCode,
                    originalCreatedBy = team.createdBy,
                    teamName = team.name,
                    category = team.category,
                    location = team.location,
                    arena = team.arena,
                    isLocationExpanded = team.location.isNotBlank() || team.arena.isNotBlank(),
                    headCoachQuery = team.headCoach,
                    selectedHeadCoach = User(displayName = team.headCoach),
                    isHeadCoachPreloaded = true,
                    accentColor = team.accentColor.ifBlank { "#9CA3AF" },
                    initials = team.initials.ifBlank { categoryToInitials(team.category) },
                )
                Log.d(TAG, "Loaded team for edit: ${team.id}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load team", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_team_not_found)
            }
        }
    }

    private suspend fun loadClubMembersForClub(id: String) {
        runCatching {
            val teams = teamRepository.getTeamsByClub(id)
            val memberIds = teams
                .flatMap { team -> teamMemberRepository.getMembersByTeam(team.id) }
                .map { it.userId }
                .toMutableSet()
            val adminIds = clubRepository.getClub(id)?.adminIds ?: emptyList()
            memberIds.addAll(adminIds)
            clubMembers = userRepository.getUsersByIds(memberIds.toList())
        }.onFailure { e ->
            Log.e(TAG, "Failed to load club members", e)
        }
    }

    override fun onTeamNameChange(value: String) {
        uiState = uiState.copy(teamName = value, error = null)
    }

    override fun onHeadCoachQueryChange(value: String) {
        uiState = uiState.copy(
            headCoachQuery = value,
            selectedHeadCoach = null,
            isHeadCoachPreloaded = false,
        )
        headCoachSearchJob?.cancel()
        if (value.isBlank()) {
            uiState = uiState.copy(headCoachSuggestions = emptyList())
            return
        }
        headCoachSearchJob = viewModelScope.launch {
            delay(300)
            val query = value.lowercase()
            val results = clubMembers
                .filter { it.displayName.lowercase().contains(query) }
                .take(10)
            uiState = uiState.copy(headCoachSuggestions = results)
        }
    }

    override fun onHeadCoachSelected(user: User) {
        uiState = uiState.copy(
            selectedHeadCoach = user,
            headCoachQuery = user.displayName,
            headCoachSuggestions = emptyList(),
            isHeadCoachPreloaded = false,
        )
    }

    override fun onAssistantQueryChange(value: String) {
        uiState = uiState.copy(assistantQuery = value)
        assistantSearchJob?.cancel()
        if (value.isBlank()) {
            uiState = uiState.copy(assistantSuggestions = emptyList())
            return
        }
        assistantSearchJob = viewModelScope.launch {
            delay(300)
            val query = value.lowercase()
            val alreadySelected = uiState.selectedAssistants.map { it.id }.toSet()
            val results = clubMembers
                .filter { it.displayName.lowercase().contains(query) && it.id !in alreadySelected }
                .take(10)
            uiState = uiState.copy(assistantSuggestions = results)
        }
    }

    override fun onAssistantAdd() {
        val first = uiState.assistantSuggestions.firstOrNull() ?: return
        onAssistantSelected(first)
    }

    override fun onAssistantSelected(user: User) {
        if (uiState.selectedAssistants.any { it.id == user.id }) return
        uiState = uiState.copy(
            selectedAssistants = uiState.selectedAssistants + user,
            assistantQuery = "",
            assistantSuggestions = emptyList(),
        )
    }

    override fun onAssistantRemoved(user: User) {
        uiState = uiState.copy(
            selectedAssistants = uiState.selectedAssistants.filter { it.id != user.id },
        )
    }

    override fun onCategoryChange(value: String) {
        uiState = uiState.copy(category = value, initials = categoryToInitials(value))
    }

    override fun onAccentColorChanged(color: Color) {
        val hex = "#%06X".format(color.toArgb() and 0xFFFFFF)
        uiState = uiState.copy(accentColor = hex)
    }

    override fun onInitialsChanged(value: String) {
        uiState = uiState.copy(initials = value.uppercase().take(4))
    }

    private fun categoryToInitials(category: String) = when (category) {
        "men" -> "M"
        "women" -> "W"
        "children" -> "K"
        "seniors" -> "S"
        else -> ""
    }

    override fun onToggleLocation() {
        uiState = uiState.copy(isLocationExpanded = !uiState.isLocationExpanded)
    }

    override fun onLocationChange(value: String) {
        uiState = uiState.copy(location = value, error = null)
    }

    override fun onArenaChange(value: String) {
        uiState = uiState.copy(arena = value, error = null)
    }

    override fun onSaveTeam() {
        if (uiState.teamName.isBlank()) {
            uiState = uiState.copy(error = R.string.error_team_name_required)
            return
        }

        if (uiState.isEditMode) updateTeam() else createTeam()
    }

    private fun createTeam() {
        val uid = authRepository.currentUser?.uid ?: run {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }
        val joinCode = generateJoinCode()

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                val team = Team(
                    clubId = clubId,
                    name = uiState.teamName,
                    category = uiState.category,
                    headCoach = uiState.selectedHeadCoach?.displayName ?: uiState.headCoachQuery,
                    location = uiState.location,
                    arena = uiState.arena,
                    joinCode = joinCode,
                    createdBy = uid,
                    accentColor = uiState.accentColor,
                    initials = uiState.initials,
                )

                val members = buildList {
                    uiState.selectedHeadCoach?.let { coach ->
                        if (coach.id.isNotBlank()) add(TeamMember(userId = coach.id, role = "coach"))
                    }
                    uiState.selectedAssistants.forEach { assistant ->
                        add(TeamMember(userId = assistant.id, role = "assistant"))
                    }
                }
                val newTeamId = teamRepository.createTeamWithMembers(team, members)

                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team created: $newTeamId JoinCode: $joinCode")
            }.onFailure { e ->
                Log.e(TAG, "Failed to create team", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_team_not_found)
            }
        }
    }

    private fun updateTeam() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                val updated = Team(
                    id = uiState.teamId,
                    clubId = uiState.originalClubId,
                    name = uiState.teamName,
                    category = uiState.category,
                    headCoach = uiState.selectedHeadCoach?.displayName ?: uiState.headCoachQuery,
                    location = uiState.location,
                    arena = uiState.arena,
                    joinCode = uiState.originalJoinCode,
                    createdBy = uiState.originalCreatedBy,
                    accentColor = uiState.accentColor,
                    initials = uiState.initials,
                )

                teamRepository.updateTeam(updated)
                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team updated: ${uiState.teamId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to update team", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_team_not_found)
            }
        }
    }

    override fun onDeleteTeam() {
        onDeleteTeamClick()
    }

    override fun onDeleteTeamClick() {
        if (!uiState.isEditMode || uiState.teamId.isBlank()) return
        uiState = uiState.copy(showDeleteDialog = true)
    }

    override fun onDeleteTeamDismiss() {
        uiState = uiState.copy(showDeleteDialog = false)
    }

    override fun onDeleteTeamConfirm() {
        if (!uiState.isEditMode || uiState.teamId.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, showDeleteDialog = false, error = null)

            runCatching {
                teamMemberRepository.deleteMembersByTeam(uiState.teamId)
                teamRepository.deleteTeam(uiState.teamId)
                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team deleted: ${uiState.teamId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to delete team", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_team_not_found)
            }
        }
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }

    private fun generateJoinCode(): String =
        (1..JOIN_CODE_LENGTH).map { JOIN_CODE_CHARS.random() }.joinToString("")
}
