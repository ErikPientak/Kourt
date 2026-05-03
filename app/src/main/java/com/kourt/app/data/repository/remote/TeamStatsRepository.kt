package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.TeamStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("teamStats")

    suspend fun getTeamStats(teamId: String): TeamStats? =
        collection.document(teamId).get().await().toObject(TeamStats::class.java)

    suspend fun createTeamStats(stats: TeamStats): String  {
        val ref = collection.add(stats).await()
        return ref.id
    }
    suspend fun updateTeamStats(stats: TeamStats) {
        collection.document(stats.teamId).set(stats).await()
    }
}
