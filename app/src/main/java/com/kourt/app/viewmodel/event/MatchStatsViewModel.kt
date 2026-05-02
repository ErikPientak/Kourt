package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerMatchStat
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.data.repository.MatchStatsRepository
import com.kourt.app.data.repository.NominationRepository
import com.kourt.app.data.repository.PlayerStatsRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.event.matchstats.MatchStatsScreenActions
import com.kourt.app.ui.screens.event.matchstats.MatchStatsScreenUiState
import com.kourt.app.ui.screens.event.matchstats.PlayerStatEntryUiItem
import com.kourt.app.ui.screens.event.matchstats.StatField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MatchStatsViewModel"

@HiltViewModel
class MatchStatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
    private val nominationRepository: NominationRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
    private val playerStatsRepository: PlayerStatsRepository,
) : ViewModel(), MatchStatsScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    // Cached team id from loaded event — needed for PlayerStats writes
    private var teamId = ""
    // Whether a MatchStats doc already existed — drives create vs update
    private var existingMatchStats: MatchStats? = null

    var uiState by mutableStateOf(MatchStatsScreenUiState())
        private set

    init {
        loadData()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private fun loadData() {
        if (eventId.isBlank()) {
            uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                // Parallel: event, nomination, existing match stats
                val eventDeferred       = async { eventRepository.getEvent(eventId) }
                val nominationDeferred  = async { nominationRepository.getNomination(eventId) }
                val matchStatsDeferred  = async { matchStatsRepository.getMatchStats(eventId) }

                val event        = eventDeferred.await()
                val nomination   = nominationDeferred.await()
                val matchStats   = matchStatsDeferred.await()

                if (event == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                    return@launch
                }

                teamId = event.teamId
                existingMatchStats = matchStats

                // Nominated player ids (teamMemberIds from nomination.players)
                val nominatedMemberIds: List<String> = nomination?.players ?: emptyList()

                // Fetch all team members and filter to nominated ones
                val allMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                val nominatedMembers = if (nominatedMemberIds.isNotEmpty()) {
                    allMembers.filter { it.id in nominatedMemberIds }
                } else {
                    // Fallback: all players if no nomination exists
                    allMembers.filter { it.role == "player" }
                }

                // Fetch user display names and avatars
                val userIds = nominatedMembers.map { it.userId }.filter { it.isNotBlank() }
                val usersById = if (userIds.isNotEmpty()) {
                    userRepository.getUsersByIds(userIds).associateBy { it.id }
                } else emptyMap()

                // Pre-populate from existing match stats if present
                val existingStatsByMemberId = matchStats?.playerStats ?: emptyMap()

                val playerItems = nominatedMembers.mapNotNull { member ->
                    val user = usersById[member.userId] ?: return@mapNotNull null
                    val existing = existingStatsByMemberId[member.id]
                    PlayerStatEntryUiItem(
                        userId = member.userId,
                        teamMemberId = member.id,
                        displayName = user.displayName.ifBlank { user.email },
                        avatarUrl = user.avatarId.ifBlank { user.photoURL },
                        jerseyNumber = member.jerseyNumber,
                        points = existing?.points ?: 0,
                        rebounds = existing?.rebounds ?: 0,
                        assists = existing?.assists ?: 0,
                        fouls = existing?.fouls ?: 0,
                        freeThrowsAttempted = existing?.freeThrowsAttempted ?: 0,
                        freeThrowsMade = existing?.freeThrowsMade ?: 0,
                    )
                }

                uiState = uiState.copy(
                    isLoading = false,
                    eventTitle = event.title,
                    opponentScore = matchStats?.opponentScore ?: 0,
                    players = playerItems,
                )

                Log.d(TAG, "Loaded ${playerItems.size} players for match stats on event $eventId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load match stats for $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    // ── MatchStatsScreenActions ───────────────────────────────────────────────

    override fun onOpponentScoreChanged(delta: Int) {
        val newScore = maxOf(0, uiState.opponentScore + delta)
        uiState = uiState.copy(opponentScore = newScore)
    }

    override fun onStatChanged(teamMemberId: String, stat: StatField, delta: Int) {
        val updatedPlayers = uiState.players.map { player ->
            if (player.teamMemberId != teamMemberId) return@map player
            when (stat) {
                StatField.POINTS                -> player.copy(points = maxOf(0, player.points + delta))
                StatField.REBOUNDS              -> player.copy(rebounds = maxOf(0, player.rebounds + delta))
                StatField.ASSISTS               -> player.copy(assists = maxOf(0, player.assists + delta))
                StatField.FOULS                 -> player.copy(fouls = maxOf(0, player.fouls + delta))
                StatField.FREE_THROWS_ATTEMPTED -> player.copy(freeThrowsAttempted = maxOf(0, player.freeThrowsAttempted + delta))
                StatField.FREE_THROWS_MADE      -> player.copy(freeThrowsMade = maxOf(0, player.freeThrowsMade + delta))
            }
        }
        uiState = uiState.copy(players = updatedPlayers)
    }

    override fun onSave() {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, error = null)

            runCatching {
                val now = Timestamp.now()
                val myTeamScore = uiState.myTeamScore

                // Build playerStats map keyed by teamMemberId
                val playerStatsMap: Map<String, PlayerMatchStat> = uiState.players.associate { p ->
                    p.teamMemberId to PlayerMatchStat(
                        points = p.points,
                        rebounds = p.rebounds,
                        assists = p.assists,
                        fouls = p.fouls,
                        freeThrowsAttempted = p.freeThrowsAttempted,
                        freeThrowsMade = p.freeThrowsMade,
                    )
                }

                val toSave = MatchStats(
                    id = existingMatchStats?.id ?: "",
                    eventId = eventId,
                    myTeamScore = myTeamScore,
                    opponentScore = uiState.opponentScore,
                    playerStats = playerStatsMap,
                    createdBy = existingMatchStats?.createdBy ?: uid,
                    createdAt = existingMatchStats?.createdAt ?: now,
                    updatedAt = now,
                )

                // updateMatchStats uses document(eventId).set() — works as upsert for both
                // create and update, keeping the doc keyed by eventId consistently.
                matchStatsRepository.updateMatchStats(toSave)
                Log.d(TAG, "${if (existingMatchStats != null) "Updated" else "Created"} match stats for event $eventId")

                // Update per-player cumulative stats
                uiState.players.forEach { player ->
                    val existing = playerStatsRepository.getPlayerStats(player.teamMemberId, teamId)
                        ?: PlayerStats(teamMemberId = player.teamMemberId, teamId = teamId)
                    val updated = existing.copy(
                        matchesPlayed         = existing.matchesPlayed + 1,
                        totalPoints           = existing.totalPoints + player.points,
                        totalRebounds         = existing.totalRebounds + player.rebounds,
                        totalAssists          = existing.totalAssists + player.assists,
                        totalFouls            = existing.totalFouls + player.fouls,
                        totalFreeThrowsAttempted = existing.totalFreeThrowsAttempted + player.freeThrowsAttempted,
                        totalFreeThrowsMade   = existing.totalFreeThrowsMade + player.freeThrowsMade,
                        updatedAt             = now,
                    )
                    playerStatsRepository.updatePlayerStats(updated)
                }

                Log.d(TAG, "Updated player stats for ${uiState.players.size} players")
                uiState = uiState.copy(isSaving = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save match stats for $eventId", e)
                uiState = uiState.copy(isSaving = false, error = R.string.error_generic)
            }
        }
    }

    override fun onSavedConsumed() {
        uiState = uiState.copy(isSaved = false)
    }
}
