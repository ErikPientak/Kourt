package com.kourt.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamStats
import com.kourt.app.data.repository.local.LocalMatchStatsRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "SyncMatchStatsWorker"

@HiltWorker
class SyncMatchStatsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val localMatchStatsRepository: LocalMatchStatsRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamStatsRepository: TeamStatsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = localMatchStatsRepository.getAllPending()
        if (pending.isEmpty()) return Result.success()

        var allSucceeded = true

        for (entity in pending) {
            runCatching {
                val playerStatsMap = localMatchStatsRepository.deserializePlayerStats(entity.playerStatsJson)
                val now = Timestamp.now()

                // Update match stats
                val toSave = MatchStats(
                    eventId = entity.eventId,
                    myTeamScore = playerStatsMap.values.sumOf { it.points },
                    opponentScore = entity.opponentScore,
                    playerStats = playerStatsMap,
                    createdBy = entity.createdBy,
                    createdAt = Timestamp(entity.createdAtSeconds, 0),
                    updatedAt = now,
                )
                matchStatsRepository.updateMatchStats(toSave)

                // Update team stats
                val existing = teamStatsRepository.getTeamStats(entity.teamId)
                    ?: TeamStats(teamId = entity.teamId)
                teamStatsRepository.updateTeamStats(
                    existing.copy(
                        matchesPlayed = existing.matchesPlayed + 1,
                        totalPointsScored = existing.totalPointsScored + toSave.myTeamScore,
                        totalPointsAgainst = existing.totalPointsAgainst + toSave.opponentScore,
                        wins = if (toSave.myTeamScore > toSave.opponentScore) existing.wins + 1 else existing.wins,
                        losses = if (toSave.myTeamScore < toSave.opponentScore) existing.losses + 1 else existing.losses,
                        draws = if (toSave.myTeamScore == toSave.opponentScore) existing.draws + 1 else existing.draws,
                        updatedAt = now,
                        )
                )

                // Update player stats
                for ((memberId, matchStat) in playerStatsMap) {
                    val existing = playerStatsRepository.getPlayerStats(memberId, entity.teamId)
                        ?: PlayerStats(teamMemberId = memberId, teamId = entity.teamId)
                    playerStatsRepository.updatePlayerStats(
                        existing.copy(
                            matchesPlayed            = existing.matchesPlayed + 1,
                            totalPoints              = existing.totalPoints + matchStat.points,
                            totalRebounds            = existing.totalRebounds + matchStat.rebounds,
                            totalAssists             = existing.totalAssists + matchStat.assists,
                            totalFouls               = existing.totalFouls + matchStat.fouls,
                            totalFreeThrowsAttempted = existing.totalFreeThrowsAttempted + matchStat.freeThrowsAttempted,
                            totalFreeThrowsMade      = existing.totalFreeThrowsMade + matchStat.freeThrowsMade,
                            updatedAt                = now,
                        )
                    )
                }

                localMatchStatsRepository.deleteDraft(entity.eventId)
                Log.d(TAG, "Synced pending match stats for event ${entity.eventId}")
            }.onFailure { e ->
                Log.e(TAG, "Failed to sync match stats for event ${entity.eventId}", e)
                allSucceeded = false
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }
}
