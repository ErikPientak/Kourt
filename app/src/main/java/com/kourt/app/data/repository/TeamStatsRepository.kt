package com.kourt.app.data.repository

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
}
