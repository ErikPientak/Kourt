package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.TeamMember
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class TeamMemberRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val rsvpRepository: RsvpRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val eventRepository: EventRepository
) {
    companion object {
        /** Deterministic doc ID so security rules can look up a user's membership in a team. */
        fun memberDocId(userId: String, teamId: String) = "${userId}_${teamId}"
    }

    private val collection = firestore.collection("Team_member")

    open suspend fun getMembersByTeam(teamId: String): List<TeamMember> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(TeamMember::class.java)

    open suspend fun getMembersByUser(userId: String): List<TeamMember> =
        collection.whereEqualTo("userId", userId).get().await()
            .toObjects(TeamMember::class.java)

    suspend fun getMemberByUserId(userId: String): TeamMember? =
        collection.whereEqualTo("userId", userId).get().await()
            .toObjects(TeamMember::class.java).firstOrNull()

    suspend fun createMember(member: TeamMember): String {
        val id = memberDocId(member.userId, member.teamId)
        collection.document(id).set(member).await()
        return id
    }

    suspend fun removeMember(id: String) {
        val member = collection.document(id).get().await()
            .toObject(TeamMember::class.java)
        if (member != null && member.userId.isNotBlank()) {
            val eventIds = eventRepository.getEventsByTeam(member.teamId).map { it.id }
            rsvpRepository.deleteRsvpsByUserForEvents(member.userId, eventIds)
            playerStatsRepository.deletePlayerStats(id, member.teamId)
        }
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

    suspend fun changeRole(memberId: String, newRole: String) {
        collection.document(memberId).update("role", newRole).await()
    }

    fun changeJerseyNumber(memberId: String, newJerseyNumber: Int) {
        collection.document(memberId).update("jerseyNumber", newJerseyNumber)
    }
}
