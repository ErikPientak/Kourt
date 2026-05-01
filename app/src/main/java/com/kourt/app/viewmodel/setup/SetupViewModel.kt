package com.kourt.app.viewmodel.setup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.repository.AppPreferencesRepository
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.PlayerStatsRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.ui.screens.setup.SetupScreenActions
import com.kourt.app.ui.screens.setup.SetupScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SetupViewModel"

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel(), SetupScreenActions {

    var uiState by mutableStateOf(SetupScreenUiState())
        private set

    override fun onJoinCodeChange(code: String) {
        uiState = uiState.copy(joinCode = code.uppercase(), error = null)
    }

    override fun onCreateClub() {
        Log.d(TAG, "Create Club tapped")
    }

    override fun onJoinTeam() {
        val code = uiState.joinCode.trim()
        if (code.isBlank()) {
            uiState = uiState.copy(error = R.string.error_join_code_required)
            return
        }

        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val team = teamRepository.getTeamByJoinCode(code)
                if (team == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_invalid_join_code)
                    return@launch
                }

                val alreadyMember = teamMemberRepository.isMember(uid, team.id)
                if (alreadyMember) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_already_member)
                    return@launch
                }

                val memberId = teamMemberRepository.createMember(
                    TeamMember(
                        userId = uid,
                        teamId = team.id,
                        role = "player",
                    )
                )

                playerStatsRepository.createPlayerStats(
                    PlayerStats(
                        teamMemberId = memberId,
                        teamId = team.id,
                    )
                )


                appPreferencesRepository.activeClubId = team.clubId
                appPreferencesRepository.activeTeamId = team.id

                Log.d(TAG, "Joined team ${team.id} in club ${team.clubId}")
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to join team", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_invalid_join_code)
            }
        }
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }
}
