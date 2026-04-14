package com.kourt.app.viewmodel.team

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.model.Team
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.AuthRepository
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
    savedStateHandle: SavedStateHandle,
) : ViewModel(), AddTeamScreenActions {

    var uiState by mutableStateOf(AddTeamScreenUiState())
        private set

    private val clubId: String = savedStateHandle["clubId"] ?: ""
    private val teamId: String = savedStateHandle["teamId"] ?: ""
    private var headCoachSearchJob: Job? = null
    private var assistantSearchJob: Job? = null

    init {
        if (teamId.isNotBlank()) {
            loadTeamForEdit(teamId)
        }
    }

    private fun loadTeamForEdit(id: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching {
                val team = teamRepository.getTeam(id)
                if (team == null) {
                    uiState = uiState.copy(isLoading = false, error = "Team not found")
                    return@launch
                }
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
                    // Placeholder User preserves the display name; TeamMember records
                    // are not modified on edit (known limitation).
                    selectedHeadCoach = User(displayName = team.headCoach),
                    isHeadCoachPreloaded = true,
                )
                Log.d(TAG, "Loaded team for edit: ${team.id}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load team", e)
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Failed to load team")
            }
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
            runCatching {
                val results = userRepository.searchByDisplayName(value)
                uiState = uiState.copy(headCoachSuggestions = results)
            }.onFailure { e ->
                Log.e(TAG, "Head coach search failed", e)
            }
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
            runCatching {
                val alreadySelected = uiState.selectedAssistants.map { it.id }.toSet()
                val results = userRepository.searchByDisplayName(value)
                    .filter { it.id !in alreadySelected }
                uiState = uiState.copy(assistantSuggestions = results)
            }.onFailure { e ->
                Log.e(TAG, "Assistant search failed", e)
            }
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
        uiState = uiState.copy(category = value)
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

    override fun onLogoUploadTap() {
        Log.d(TAG, "Logo upload tapped")
    }

    override fun onSaveTeam() {
        if (uiState.teamName.isBlank()) {
            uiState = uiState.copy(error = "Team name is required")
            return
        }
        val headCoachResolved = uiState.selectedHeadCoach
        if (headCoachResolved == null && !uiState.isHeadCoachPreloaded) {
            uiState = uiState.copy(error = "Please select a head coach from the search results")
            return
        }
        if (uiState.location.isBlank() || uiState.arena.isBlank()) {
            uiState = uiState.copy(
                error = if (uiState.location.isBlank()) "Location is required" else "Arena name is required",
                isLocationExpanded = true,
            )
            return
        }

        if (uiState.isEditMode) {
            updateTeam()
        } else {
            createTeam()
        }
    }

    private fun createTeam() {
        val uid = authRepository.currentUser?.uid ?: run {
            uiState = uiState.copy(error = "You must be signed in")
            return
        }

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
                    joinCode = generateJoinCode(),
                    createdBy = uid,
                )

                val newTeamId = teamRepository.createTeam(team)

                uiState.selectedHeadCoach?.let { coach ->
                    if (coach.id.isNotBlank()) {
                        teamMemberRepository.addMember(
                            TeamMember(userId = coach.id, teamId = newTeamId, role = "coach"),
                        )
                    }
                }

                uiState.selectedAssistants.forEach { assistant ->
                    teamMemberRepository.addMember(
                        TeamMember(userId = assistant.id, teamId = newTeamId, role = "assistant"),
                    )
                }

                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team created: $newTeamId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to create team", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create team",
                )
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
                )

                teamRepository.updateTeam(updated)
                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team updated: ${uiState.teamId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to update team", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update team",
                )
            }
        }
    }

    override fun onDeleteTeam() {
        if (!uiState.isEditMode || uiState.teamId.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                teamRepository.deleteTeam(uiState.teamId)
                uiState = uiState.copy(isLoading = false, isSuccess = true)
                Log.d(TAG, "Team deleted: ${uiState.teamId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to delete team", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to delete team",
                )
            }
        }
    }

    override fun onSuccessConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }

    private fun generateJoinCode(): String =
        (1..JOIN_CODE_LENGTH).map { JOIN_CODE_CHARS.random() }.joinToString("")
}
