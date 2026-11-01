package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Rsvp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RsvpRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("RSVP")

    open suspend fun getRsvpsByEvent(eventId: String): List<Rsvp> =
        collection.whereEqualTo("eventId", eventId).get().await()
            .toObjects(Rsvp::class.java)

    /** All RSVPs for [userId]; the rules allow reading your own (or your child's) RSVPs directly. */
    suspend fun getMyRsvps(userId: String): List<Rsvp> =
        collection.whereEqualTo("userId", userId).get().await()
            .toObjects(Rsvp::class.java)

    suspend fun getMyRsvp(eventId: String, userId: String): Rsvp? =
        collection
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("userId", userId)
            .get().await()
            .toObjects(Rsvp::class.java)
            .firstOrNull()

    /**
     * Creates or replaces the RSVP of [Rsvp.userId] for [Rsvp.eventId]. The doc ID is
     * `{userId}_{eventId}`, so there is only ever one RSVP per person per event.
     */
    suspend fun upsertRsvp(rsvp: Rsvp) {
        collection.document("${rsvp.userId}_${rsvp.eventId}").set(rsvp).await()
    }

    /** Scoped per event so the query stays within what security rules let the caller read. */
    suspend fun deleteRsvpsByUserForEvents(userId: String, eventIds: List<String>) {
        eventIds.forEach { eventId ->
            val docs = collection
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", userId)
                .get().await()
            docs.forEach { it.reference.delete().await() }
        }
    }
}
