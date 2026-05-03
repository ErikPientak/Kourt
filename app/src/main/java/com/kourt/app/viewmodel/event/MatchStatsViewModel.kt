package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.local.PendingMatchStatsEntity
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerMatchStat
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.local.LocalMatchStatsRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.NominationRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.event.matchstats.MatchStatsScreenActions
import com.kourt.app.ui.screens.event.matchstats.MatchStatsScreenUiState
import com.kourt.app.ui.screens.event.matchstats.PlayerStatEntryUiItem
import com.kourt.app.ui.screens.event.matchstats.StatField
import com.kourt.app.worker.SyncMatchStatsWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

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
    private val localMatchStatsRepository: LocalMatchStatsRepository,
    private val workManager: WorkManager,
) : ViewModel(), MatchStatsScreenActions {

    private val eventId: String = savedStateHandle["eventId"] ?: ""

    private var teamId = ""
    private var existingMatchStats: MatchStats? = null
    private var draftSaveJob: Job? = null

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
                val eventDeferred      = async { eventRepository.getEvent(eventId) }
                val nominationDeferred = async { nominationRepository.getNomination(eventId) }
                val matchStatsDeferred = async { matchStatsRepository.getMatchStats(eventId) }

                val event      = eventDeferred.await()
                val nomination = nominationDeferred.await()
                val matchStats = matchStatsDeferred.await()

                if (event == null) {
                    uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                    return@launch
                }

                teamId = event.teamId
                existingMatchStats = matchStats

                val nominatedMemberIds: List<String> = nomination?.players ?: emptyList()
                val allMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                val nominatedMembers = if (nominatedMemberIds.isNotEmpty()) {
                    allMembers.filter { it.userId in nominatedMemberIds }
                } else {
                    allMembers.filter { it.role == "player" }
                }
                Log.d(TAG, "Loaded ${nominatedMembers.size} nominatedMembers")

                val userIds = nominatedMembers.map { it.userId }.filter { it.isNotBlank() }
                val usersById = if (userIds.isNotEmpty()) {
                    userRepository.getUsersByIds(userIds).associateBy { it.id }
                } else emptyMap()

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

                // Restore local draft when no Firestore doc exists yet
                val draft = localMatchStatsRepository.getDraft(eventId)
                if (draft != null && !draft.isPendingSync && matchStats == null) {
                    val draftStats = localMatchStatsRepository.deserializePlayerStats(draft.playerStatsJson)
                    val restored = playerItems.map { item ->
                        draftStats[item.teamMemberId]?.let { s ->
                            item.copy(
                                points = s.points,
                                rebounds = s.rebounds,
                                assists = s.assists,
                                fouls = s.fouls,
                                freeThrowsAttempted = s.freeThrowsAttempted,
                                freeThrowsMade = s.freeThrowsMade,
                            )
                        } ?: item
                    }
                    uiState = uiState.copy(players = restored, opponentScore = draft.opponentScore)
                    Log.d(TAG, "Restored local draft for event $eventId")
                }

                Log.d(TAG, "Loaded ${playerItems.size} players for match stats on event $eventId")
            }.onFailure { e ->
                Log.e(TAG, "Failed to load match stats for $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    // ── MatchStatsScreenActions ───────────────────────────────────────────────

    override fun onOpponentScoreChanged(delta: Int) {
        uiState = uiState.copy(opponentScore = maxOf(0, uiState.opponentScore + delta))
        scheduleDraftSave()
    }

    override fun onStatChanged(teamMemberId: String, stat: StatField, delta: Int) {
        val updatedPlayers = uiState.players.map { player ->
            if (player.teamMemberId != teamMemberId) return@map player
            when (stat) {
                StatField.POINTS                -> player.copy(points = maxOf(0, player.points + delta))
                StatField.REBOUNDS              -> player.copy(rebounds = maxOf(0, player.rebounds + delta))
                StatField.ASSISTS               -> player.copy(assists = maxOf(0, player.assists + delta))
                StatField.FOULS                 -> player.copy(fouls = maxOf(0, min(player.fouls + delta, 5)))
                StatField.FREE_THROWS_ATTEMPTED -> player.copy(freeThrowsAttempted = maxOf(0, player.freeThrowsAttempted + delta))
                StatField.FREE_THROWS_MADE      -> player.copy(freeThrowsMade = maxOf(0, player.freeThrowsMade + delta))
            }
        }
        uiState = uiState.copy(players = updatedPlayers)
        scheduleDraftSave()
    }

    override fun onSave() {
        val uid = authRepository.currentUser?.uid
        if (uid == null) {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }

        draftSaveJob?.cancel()

        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true, error = null)

            val now = Timestamp.now()
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
                myTeamScore = uiState.myTeamScore,
                opponentScore = uiState.opponentScore,
                playerStats = playerStatsMap,
                createdBy = existingMatchStats?.createdBy ?: uid,
                createdAt = existingMatchStats?.createdAt ?: now,
                updatedAt = now,
            )

            runCatching {
                matchStatsRepository.updateMatchStats(toSave)
                Log.d(TAG, "${if (existingMatchStats != null) "Updated" else "Created"} match stats for event $eventId")

                uiState.players.forEach { player ->
                    val existing = playerStatsRepository.getPlayerStats(player.teamMemberId, teamId)
                        ?: PlayerStats(teamMemberId = player.teamMemberId, teamId = teamId)
                    playerStatsRepository.updatePlayerStats(
                        existing.copy(
                            matchesPlayed            = existing.matchesPlayed + 1,
                            totalPoints              = existing.totalPoints + player.points,
                            totalRebounds            = existing.totalRebounds + player.rebounds,
                            totalAssists             = existing.totalAssists + player.assists,
                            totalFouls               = existing.totalFouls + player.fouls,
                            totalFreeThrowsAttempted = existing.totalFreeThrowsAttempted + player.freeThrowsAttempted,
                            totalFreeThrowsMade      = existing.totalFreeThrowsMade + player.freeThrowsMade,
                            updatedAt                = now,
                        )
                    )
                }
                Log.d(TAG, "Updated player stats for ${uiState.players.size} players")

                localMatchStatsRepository.deleteDraft(eventId)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save match stats online for $eventId, queuing for sync", e)
                localMatchStatsRepository.saveDraft(
                    PendingMatchStatsEntity(
                        eventId = eventId,
                        opponentScore = uiState.opponentScore,
                        playerStatsJson = localMatchStatsRepository.serializePlayerStats(playerStatsMap),
                        createdBy = toSave.createdBy,
                        createdAtSeconds = toSave.createdAt.seconds,
                        teamId = teamId,
                        isPendingSync = true,
                    )
                )
                enqueueSyncWorker()
            }

            uiState = uiState.copy(isSaving = false, isSaved = true)
        }
    }

    override fun onSavedConsumed() {
        uiState = uiState.copy(isSaved = false)
    }

    // ── Draft helpers ─────────────────────────────────────────────────────────

    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            delay(500)
            val uid = authRepository.currentUser?.uid ?: return@launch
            localMatchStatsRepository.saveDraft(
                PendingMatchStatsEntity(
                    eventId = eventId,
                    opponentScore = uiState.opponentScore,
                    playerStatsJson = localMatchStatsRepository.serializePlayerStats(
                        uiState.players.associate { p ->
                            p.teamMemberId to PlayerMatchStat(
                                points = p.points,
                                rebounds = p.rebounds,
                                assists = p.assists,
                                fouls = p.fouls,
                                freeThrowsAttempted = p.freeThrowsAttempted,
                                freeThrowsMade = p.freeThrowsMade,
                            )
                        }
                    ),
                    createdBy = existingMatchStats?.createdBy ?: uid,
                    createdAtSeconds = existingMatchStats?.createdAt?.seconds ?: Timestamp.now().seconds,
                    teamId = teamId,
                    isPendingSync = false,
                )
            )
        }
    }

    private fun enqueueSyncWorker() {
        val request = OneTimeWorkRequestBuilder<SyncMatchStatsWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork("sync_match_stats", ExistingWorkPolicy.KEEP, request)
        Log.d(TAG, "Enqueued SyncMatchStatsWorker")
    }
}
