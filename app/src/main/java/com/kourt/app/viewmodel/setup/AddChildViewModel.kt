package com.kourt.app.viewmodel.setup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.setup.addchild.AddChildScreenActions
import com.kourt.app.ui.screens.setup.addchild.AddChildScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddChildViewModel"

@HiltViewModel
class AddChildViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel(), AddChildScreenActions {

    var uiState by mutableStateOf(AddChildScreenUiState())
        private set

    override fun onDisplayNameChange(name: String) {
        uiState = uiState.copy(displayName = name, profileError = null)
    }

    override fun onAvatarPickerShow() {
        uiState = uiState.copy(showAvatarPicker = true)
    }

    override fun onAvatarSelected(avatarId: String) {
        uiState = uiState.copy(avatarId = avatarId, showAvatarPicker = false)
    }

    override fun onAvatarPickerDismiss() {
        uiState = uiState.copy(showAvatarPicker = false)
    }

    override fun onSubmit() {
        val name = uiState.displayName.trim()
        if (name.isBlank()) {
            uiState = uiState.copy(profileError = R.string.error_display_name_required)
            return
        }

        val parentUid = authRepository.currentUser?.uid
        if (parentUid == null) {
            uiState = uiState.copy(profileError = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, profileError = null)
            runCatching {
                val childUid = userRepository.createManagedUser(
                    displayName = name,
                    avatarId = uiState.avatarId,
                    parentUid = parentUid,
                )
                appPreferencesRepository.activeChildId = childUid
                Log.d(TAG, "onSubmit: created child=$childUid, showing join dialog")
                uiState = uiState.copy(isLoading = false, childUid = childUid, showJoinTeamDialog = true)
            }.onFailure { e ->
                Log.e(TAG, "onSubmit: failed to create child", e)
                uiState = uiState.copy(isLoading = false, profileError = R.string.error_generic)
            }
        }
    }

    override fun onJoinCodeChange(code: String) {
        uiState = uiState.copy(joinCode = code.uppercase(), joinError = null)
    }

    override fun onJoinTeam() {
        val code = uiState.joinCode.trim()
        if (code.isBlank()) {
            uiState = uiState.copy(joinError = R.string.error_join_code_required)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, joinError = null)
            runCatching {
                val team = teamRepository.getTeamByJoinCode(code)
                if (team == null) {
                    uiState = uiState.copy(isLoading = false, joinError = R.string.error_invalid_join_code)
                    return@launch
                }

                val alreadyMember = teamMemberRepository.isMember(uiState.childUid, team.id)
                if (alreadyMember) {
                    uiState = uiState.copy(isLoading = false, joinError = R.string.error_already_member)
                    return@launch
                }

                teamMemberRepository.createMember(
                    TeamMember(userId = uiState.childUid, teamId = team.id, role = "player")
                )
                val parentUid = authRepository.currentUser?.uid
                if (parentUid != null && !teamMemberRepository.isMember(parentUid, team.id)) {
                    teamMemberRepository.createMember(
                        TeamMember(userId = parentUid, teamId = team.id, role = "parent")
                    )
                }
                appPreferencesRepository.activeClubId = team.clubId
                appPreferencesRepository.activeTeamId = team.id
                Log.d(TAG, "onJoinTeam: child=${uiState.childUid} + parent joined team=${team.id}")
                uiState = uiState.copy(isLoading = false, showJoinTeamDialog = false, isSuccess = true)
            }.onFailure { e ->
                Log.e(TAG, "onJoinTeam: failed", e)
                uiState = uiState.copy(isLoading = false, joinError = R.string.error_invalid_join_code)
            }
        }
    }

    override fun onDismissJoinTeamDialog() {
        Log.d(TAG, "onDismissJoinTeamDialog: child=${uiState.childUid} skipped team join")
        uiState = uiState.copy(showJoinTeamDialog = false, isSuccess = true)
    }

    override fun onSuccessConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }
}
