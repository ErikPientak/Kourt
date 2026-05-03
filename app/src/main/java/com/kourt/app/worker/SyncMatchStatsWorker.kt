package com.kourt.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.repository.local.LocalMatchStatsRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
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
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = localMatchStatsRepository.getAllPending()
        if (pending.isEmpty()) return Result.success()

        var allSucceeded = true

        for (entity in pending) {
            runCatching {
                val playerStatsMap = localMatchStatsRepository.deserializePlayerStats(entity.playerStatsJson)
                val now = Timestamp.now()

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
