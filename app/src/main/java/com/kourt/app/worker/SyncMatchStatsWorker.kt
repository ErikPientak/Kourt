package com.kourt.app.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.repository.local.LocalMatchStatsRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "SyncMatchStatsWorker"

@HiltWorker
class SyncMatchStatsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val localMatchStatsRepository: LocalMatchStatsRepository,
    private val matchStatsRepository: MatchStatsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pending = localMatchStatsRepository.getAllPending()
        if (pending.isEmpty()) return Result.success()

        var allSucceeded = true

        for (entity in pending) {
            runCatching {
                val playerStatsMap = localMatchStatsRepository.deserializePlayerStats(entity.playerStatsJson)

                val toSave = MatchStats(
                    eventId = entity.eventId,
                    myTeamScore = playerStatsMap.values.sumOf { it.points },
                    opponentScore = entity.opponentScore,
                    playerStats = playerStatsMap,
                    createdBy = entity.createdBy,
                    createdAt = Timestamp(entity.createdAtSeconds, 0),
                    updatedAt = Timestamp.now(),
                )
                // Same transaction as an online save: totals change only by the difference
                // from whatever is stored now, so syncing an edited match doesn't double-count.
                matchStatsRepository.saveMatchStatsWithTotals(entity.teamId, toSave)

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
