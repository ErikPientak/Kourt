package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.Nomination
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.NominationRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.event.nomination.NominationPlayerItem
import com.kourt.app.ui.screens.event.nomination.NominationScreenActions
import com.kourt.app.ui.screens.event.nomination.NominationScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NominationViewModel"

@HiltViewModel
class NominationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamStatsRepository: TeamStatsRepository,
    private val nominationRepository: NominationRepository,
    private val authRepository: AuthRepository,
) : ViewModel(), NominationScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    var uiState by mutableStateOf(NominationScreenUiState())
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching {
                val event = eventRepository.getEvent(eventId)
                    ?: error("Event $eventId not found")
                val teamId = event.teamId

                val teamMembers = teamMemberRepository.getMembersByTeam(teamId)
                    .filter { it.role == "player" }

                val users = userRepository.getUsersByIds(teamMembers.map { it.userId })
                    .associateBy { it.id }

                val statsByMember = playerStatsRepository.getPlayersStatsByTeam(teamId)
                    .associateBy { it.teamMemberId }

                val totalTrainings = teamStatsRepository.getTeamStats(teamId)?.totalTrainings ?: 0

                val nominatedIds = nominationRepository.getNomination(eventId)
                    ?.players?.toSet() ?: emptySet()

                val players = teamMembers.map { member ->
                    val user = users[member.userId]
                    val stats = statsByMember[member.id]
                    val attended = (stats?.totalTrainingsOnTime ?: 0) + (stats?.totalTrainingsLate ?: 0)
                    val rate = if (totalTrainings > 0) attended.toFloat() / totalTrainings else 0f
                    NominationPlayerItem(
                        userId = member.userId,
                        displayName = user?.displayName ?: "",
                        avatarUrl = user?.avatarId?.ifBlank { user.photoURL } ?: "",
                        attendanceRate = rate,
                        isSelected = member.userId in nominatedIds,
                    )
                }.sortedByDescending { it.attendanceRate }

                uiState = uiState.copy(isLoading = false, players = players)
                Log.d(TAG, "Loaded ${players.size} players for event $eventId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load nomination data", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onTogglePlayer(userId: String) {
        val player = uiState.players.firstOrNull { it.userId == userId } ?: return
        if (!player.isSelected && uiState.selectedCount >= uiState.rosterLimit) return
        uiState = uiState.copy(
            players = uiState.players.map {
                if (it.userId == userId) it.copy(isSelected = !it.isSelected) else it
            }
        )
    }

    override fun onSave() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching {
                val selectedIds = uiState.players.filter { it.isSelected }.map { it.userId }
                nominationRepository.updateNomination(
                    Nomination(
                        eventId = eventId,
                        players = selectedIds,
                        status = "confirmed",
                        createdBy = authRepository.currentUser?.uid ?: "",
                    )
                )
                Log.d(TAG, "Nomination saved: ${selectedIds.size} players for event $eventId")
                uiState = uiState.copy(isLoading = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save nomination", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_generic)
            }
        }
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSaved = false)
    }
}
