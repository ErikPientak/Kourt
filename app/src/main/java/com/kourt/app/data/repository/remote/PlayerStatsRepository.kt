package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.PlayerStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PlayerStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("playerStats")

    open suspend fun getPlayerStats(teamMemberId: String, teamId: String): PlayerStats? =
        collection.document("${teamMemberId}_${teamId}").get().await()
            .toObject(PlayerStats::class.java)

    open suspend fun getPlayersStatsByTeam(teamId: String): List<PlayerStats> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(PlayerStats::class.java)

    suspend fun createPlayerStats(stats: PlayerStats): String  {
        val id = "${stats.teamMemberId}_${stats.teamId}"
        collection.document(id).set(stats).await()
        return id
    }

    open suspend fun updatePlayerStats(stats: PlayerStats) {
        collection.document("${stats.teamMemberId}_${stats.teamId}").set(stats).await()
    }

    suspend fun deletePlayerStats(teamMemberId: String, teamId: String) {
        collection.document("${teamMemberId}_${teamId}").delete().await()
    }
}
