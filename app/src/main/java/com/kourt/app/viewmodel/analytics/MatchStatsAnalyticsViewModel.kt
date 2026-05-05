package com.kourt.app.viewmodel.analytics

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.analytics.matchstats.MatchStatsAnalyticsScreenUiState
import com.kourt.app.ui.screens.analytics.matchstats.PlayerMatchStatsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MatchStatsAnalyticsVM"

@HiltViewModel
class MatchStatsAnalyticsViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val teamStatsRepository: TeamStatsRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    var uiState by mutableStateOf(MatchStatsAnalyticsScreenUiState())
        private set

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                val teamId = appPreferencesRepository.activeTeamId
                if (teamId.isBlank()) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_no_team)
                    return@runCatching
                }

                Log.d(TAG, "Loading match stats analytics for team $teamId")

                val teamStatsDeferred   = async { teamStatsRepository.getTeamStats(teamId) }
                val playerStatsDeferred = async { playerStatsRepository.getPlayersStatsByTeam(teamId) }
                val membersDeferred     = async { teamMemberRepository.getMembersByTeam(teamId) }

                val teamStats   = teamStatsDeferred.await()
                val playerStats = playerStatsDeferred.await()
                val members     = membersDeferred.await()

                Log.d(TAG, "Fetched teamStats=$teamStats, ${playerStats.size} player stats, ${members.size} members")

                val userIds = members.map { it.userId }.filter { it.isNotBlank() }
                val users   = if (userIds.isNotEmpty()) userRepository.getUsersByIds(userIds) else emptyList()
                val userMap = users.associateBy { it.id }

                val playerItems = playerStats.mapNotNull { ps ->
                    val member = members.firstOrNull { it.id == ps.teamMemberId } ?: return@mapNotNull null
                    val user   = userMap[member.userId]
                    val ftPct  = if (ps.totalFreeThrowsAttempted == 0) null
                                 else ps.totalFreeThrowsMade.toFloat() / ps.totalFreeThrowsAttempted
                    PlayerMatchStatsItem(
                        teamMemberId  = ps.teamMemberId,
                        displayName   = user?.displayName?.ifBlank { user.email } ?: member.id,
                        avatarUrl     = user?.avatarId?.ifBlank { user.photoURL } ?: "",
                        totalPoints   = ps.totalPoints,
                        totalRebounds = ps.totalRebounds,
                        totalAssists  = ps.totalAssists,
                        totalFouls    = ps.totalFouls,
                        freeThrowPct  = ftPct,
                    )
                }.sortedByDescending { it.totalPoints }

                Log.d(TAG, "Built ${playerItems.size} player items")

                uiState = uiState.copy(
                    isLoading     = false,
                    wins          = teamStats?.wins ?: 0,
                    losses        = teamStats?.losses ?: 0,
                    draws         = teamStats?.draws ?: 0,
                    matchesPlayed = teamStats?.matchesPlayed ?: 0,
                    topScorer     = playerItems.maxByOrNull { it.totalPoints },
                    players       = playerItems,
                )

            }.onFailure { e ->
                Log.e(TAG, "Failed to load match stats analytics", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_generic)
            }
        }
    }
}
