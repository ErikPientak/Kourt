package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.Rsvp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Event")

    suspend fun getEvent(id: String): Event? =
        collection.document(id).get().await().toObject(Event::class.java)

    suspend fun getEventsByTeam(teamId: String): List<Event> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(Event::class.java)

    suspend fun createEvent(event: Event): String {
        val ref = collection.add(event).await()
        return ref.id
    }

    suspend fun updateEvent(event: Event) {
        collection.document(event.id).set(event).await()
    }

    suspend fun getEventsBySeriesId(seriesId: String): List<Event> =
        collection.whereEqualTo("seriesId", seriesId).get().await()
            .toObjects(Event::class.java)

    suspend fun cancelEvent(id: String) {
        collection.document(id).update("status", "cancelled").await()
    }

    suspend fun deleteEvent(id: String) {
        collection.document(id).delete().await()
    }

    // RSVP subcollection
    suspend fun getRsvps(eventId: String): List<Rsvp> =
        collection.document(eventId).collection("rsvps").get().await()
            .toObjects(Rsvp::class.java)

    suspend fun addRsvp(eventId: String, rsvp: Rsvp): String {
        val ref = collection.document(eventId).collection("rsvps").add(rsvp).await()
        return ref.id
    }

    suspend fun upsertRsvp(eventId: String, rsvp: Rsvp) {
        collection.document(eventId).collection("rsvps")
            .document(rsvp.submittedBy).set(rsvp).await()
    }

    suspend fun getMyRsvp(eventId: String, uid: String): Rsvp? =
        collection.document(eventId).collection("rsvps")
            .document(uid).get().await().toObject(Rsvp::class.java)

}
