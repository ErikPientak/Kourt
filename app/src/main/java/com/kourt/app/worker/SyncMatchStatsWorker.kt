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
import com.kourt.app.viewmodel.event.isConnectionError
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException

private const val TAG = "SyncMatchStatsWorker"

/** After this many runs a save that still fails only on connection errors is given up. */
private const val MAX_ATTEMPTS = 5

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

        var shouldRetry = false

        for (entity in pending) {
            try {
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

                // A save made while this one was syncing replaced the row; keep that one.
                localMatchStatsRepository.deleteIfUnchanged(entity)
                Log.d(TAG, "Synced pending match stats for event ${entity.eventId}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e.isConnectionError() && runAttemptCount < MAX_ATTEMPTS) {
                    Log.w(TAG, "Offline while syncing match stats for event ${entity.eventId}, will retry", e)
                    shouldRetry = true
                } else {
                    // Rules denial, corrupt row, or still failing after MAX_ATTEMPTS: retrying
                    // can't help and would block the other pending rows, so drop this one.
                    Log.e(TAG, "Dropping pending match stats for event ${entity.eventId}", e)
                    localMatchStatsRepository.deleteIfUnchanged(entity)
                }
            }
        }

        return if (shouldRetry) Result.retry() else Result.success()
    }
}
