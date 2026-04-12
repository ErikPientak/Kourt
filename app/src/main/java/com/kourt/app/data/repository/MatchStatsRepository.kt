package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.MatchStats
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("matchStats")

    suspend fun getMatchStats(eventId: String): MatchStats? =
        collection.document(eventId).get().await().toObject(MatchStats::class.java)

    suspend fun createMatchStats(matchStats: MatchStats) {
        collection.document(matchStats.eventId).set(matchStats).await()
    }

    suspend fun updateMatchStats(matchStats: MatchStats) {
        collection.document(matchStats.eventId).set(matchStats).await()
    }
}
