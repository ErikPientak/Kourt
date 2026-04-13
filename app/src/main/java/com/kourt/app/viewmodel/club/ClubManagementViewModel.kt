package com.kourt.app.viewmodel.club

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.ui.screens.club.management.ClubManagementScreenActions
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ClubManagementViewModel"

@HiltViewModel
class ClubManagementViewModel @Inject constructor(
    private val clubRepository: ClubRepository,
    private val teamRepository: TeamRepository,
    private val authRepository: AuthRepository,
) : ViewModel(), ClubManagementScreenActions {

    var uiState by mutableStateOf(ClubManagementScreenUiState())
        private set

    init {
        loadClubData()
    }

    private fun loadClubData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val uid = authRepository.currentUser?.uid
            if (uid == null) {
                uiState = uiState.copy(isLoading = false, error = "You must be signed in")
                return@launch
            }

            runCatching {
                val club = clubRepository.getClubByAdminId(uid)
                if (club == null) {
                    uiState = uiState.copy(isLoading = false, error = "No club found")
                    return@launch
                }

                val teams = teamRepository.getTeamsByClub(club.id)
                uiState = uiState.copy(
                    isLoading = false,
                    clubName = club.name,
                    teams = teams,
                )
            }.onFailure { e ->
                Log.e(TAG, "Failed to load club data", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load club data",
                )
            }
        }
    }

    override fun onAddTeam() {
        Log.d(TAG, "onAddTeam: CreateTeam screen not yet implemented")
    }

    override fun onTeamClick(teamId: String) {
        Log.d(TAG, "onTeamClick: teamId=$teamId")
    }

    override fun onSettingsTap() {
        Log.d(TAG, "onSettingsTap: Settings screen not yet implemented")
    }
}
