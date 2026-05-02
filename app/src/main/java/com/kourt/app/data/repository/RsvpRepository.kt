package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Rsvp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RsvpRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("RSVP")

    suspend fun getRsvpsByEvent(eventId: String): List<Rsvp> =
        collection.whereEqualTo("eventId", eventId).get().await()
            .toObjects(Rsvp::class.java)

    suspend fun getMyRsvp(eventId: String, uid: String): Rsvp? =
        collection
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("submitted_by", uid)
            .get().await()
            .toObjects(Rsvp::class.java)
            .firstOrNull()

    suspend fun saveRsvp(rsvp: Rsvp): String {
        val ref = collection.add(rsvp).await()
        return ref.id
    }

    suspend fun updateRsvp(rsvp: Rsvp) {
        collection.document(rsvp.id).set(rsvp).await()
    }

    suspend fun deleteRsvpsByUser(uid: String) {
        val docs = collection.whereEqualTo("submitted_by", uid).get().await()
        docs.forEach { it.reference.delete().await() }
    }
}
