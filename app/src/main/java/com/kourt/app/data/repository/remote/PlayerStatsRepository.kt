package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.PlayerStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("playerStats")

    suspend fun getPlayerStats(teamMemberId: String, teamId: String): PlayerStats? =
        collection.document("${teamMemberId}_${teamId}").get().await()
            .toObject(PlayerStats::class.java)

    suspend fun getPlayersStatsByTeam(teamId: String): List<PlayerStats> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(PlayerStats::class.java)

    suspend fun createPlayerStats(stats: PlayerStats): String  {
        val ref = collection.add(stats).await()
        return ref.id
    }

    suspend fun updatePlayerStats(stats: PlayerStats) {
        collection.document("${stats.teamMemberId}_${stats.teamId}").set(stats).await()
    }

    suspend fun deletePlayerStatsByTeam(teamId: String) {
        val docs = collection.whereEqualTo("teamId", teamId).get().await()
        docs.forEach { it.reference.delete().await() }
    }
}
