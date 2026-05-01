package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.TeamMember
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamMemberRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Team_member")

    suspend fun getMembersByTeam(teamId: String): List<TeamMember> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(TeamMember::class.java)

    suspend fun getMembersByUser(userId: String): List<TeamMember> =
        collection.whereEqualTo("userId", userId).get().await()
            .toObjects(TeamMember::class.java)

    suspend fun createMember(member: TeamMember): String {
        val ref = collection.add(member).await()
        return ref.id
    }

    suspend fun removeMember(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun deleteMembersByTeam(teamId: String) {
        val docs = collection.whereEqualTo("teamId", teamId).get().await()
        docs.forEach { it.reference.delete().await() }
    }

    suspend fun isMember(userId: String, teamId: String): Boolean =
        collection
            .whereEqualTo("userId", userId)
            .whereEqualTo("teamId", teamId)
            .get().await()
            .isEmpty.not()
}
