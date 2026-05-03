package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Team
import com.kourt.app.data.model.TeamMember
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

    suspend fun createTeamWithMembers(team: Team, members: List<TeamMember>): String {
        val batch = firestore.batch()
        val teamRef = collection.document()
        batch.set(teamRef, team)
        val memberCollection = firestore.collection("Team_member")
        members.forEach { member ->
            batch.set(memberCollection.document(), member.copy(teamId = teamRef.id))
        }
        batch.commit().await()
        return teamRef.id
    }

    suspend fun updateTeam(team: Team) {
        collection.document(team.id).set(team).await()
    }

    suspend fun deleteTeam(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun getTeamByJoinCode(joinCode: String): Team? =
        collection.whereEqualTo("joinCode", joinCode.uppercase()).get().await()
            .toObjects(Team::class.java)
            .firstOrNull()
}
