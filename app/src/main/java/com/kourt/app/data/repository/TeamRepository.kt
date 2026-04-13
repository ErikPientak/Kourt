package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Team
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Team")

    suspend fun getTeam(id: String): Team? =
        collection.document(id).get().await().toObject(Team::class.java)

    suspend fun getTeamsByClub(clubId: String): List<Team> =
        collection.whereEqualTo("clubId", clubId).get().await()
            .toObjects(Team::class.java)

    suspend fun createTeam(team: Team): String {
        val ref = collection.add(team).await()
        return ref.id
    }

    suspend fun updateTeam(team: Team) {
        collection.document(team.id).set(team).await()
    }

    suspend fun deleteTeam(id: String) {
        collection.document(id).delete().await()
    }
}
