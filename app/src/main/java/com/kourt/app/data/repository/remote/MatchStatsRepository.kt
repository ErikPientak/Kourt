package com.kourt.app.data.repository.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamStats
import com.kourt.app.viewmodel.event.applyMatch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MatchStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("matchStats")

    open suspend fun getMatchStats(eventId: String): MatchStats? =
        collection.document(eventId).get().await().toObject(MatchStats::class.java)

    suspend fun updateMatchStats(matchStats: MatchStats) {
        collection.document(matchStats.eventId).set(matchStats).await()
    }

    suspend fun createMatchStats(matchStats: MatchStats): String  {
        val ref = collection.add(matchStats).await()
        return ref.id
    }

    /**
     * Saves [matchStats] and updates the team and player season totals in one transaction.
     *
     * The previously stored match stats are read inside the transaction and used as the
     * baseline, so saving the same match again only applies what changed. Everything is
     * written or nothing is, which makes retrying a failed save (e.g. from the offline
     * sync worker) safe. Transactions need a connection, so this fails fast when offline.
     */
    suspend fun saveMatchStatsWithTotals(teamId: String, matchStats: MatchStats) {
        val now = Timestamp.now()
        val matchRef = collection.document(matchStats.eventId)
        val teamStatsRef = firestore.collection("teamStats").document(teamId)
        val playerStatsCollection = firestore.collection("playerStats")

        firestore.runTransaction { tx ->
            // All reads must happen before any write.
            val previous = tx.get(matchRef).toObject(MatchStats::class.java)
            val teamStats = tx.get(teamStatsRef).toObject(TeamStats::class.java)
                ?: TeamStats(teamId = teamId)

            // Players in either version, so someone removed on an edit is subtracted again.
            val memberIds = matchStats.playerStats.keys + previous?.playerStats?.keys.orEmpty()
            val playerStats = memberIds.associateWith { memberId ->
                val ref = playerStatsCollection.document("${memberId}_${teamId}")
                ref to (tx.get(ref).toObject(PlayerStats::class.java)
                    ?: PlayerStats(teamMemberId = memberId, teamId = teamId))
            }

            tx.set(matchRef, matchStats)
            tx.set(teamStatsRef, teamStats.applyMatch(previous, matchStats, now))
            playerStats.forEach { (memberId, refAndStats) ->
                val (ref, stats) = refAndStats
                tx.set(
                    ref,
                    stats.applyMatch(
                        previous = previous?.playerStats?.get(memberId),
                        current = matchStats.playerStats[memberId],
                        now = now,
                    ),
                )
            }
        }.await()
    }
}
