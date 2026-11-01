package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Event")

    open suspend fun getEvent(id: String): Event? =
        collection.document(id).get().await().toObject(Event::class.java)

    open suspend fun getEventsByTeam(teamId: String): List<Event> =
        collection.whereEqualTo("teamId", teamId).get().await()
            .toObjects(Event::class.java)

    /**
     * Creates all [events] in one batch, so a series is either fully created or not at all.
     * Returns the new ids in the same order. A batch holds at most 500 writes; series are
     * capped well below that (MAX_SERIES_SPAN_DAYS).
     */
    suspend fun createEvents(events: List<Event>): List<String> {
        val refs = events.map { collection.document() }
        val batch = firestore.batch()
        refs.zip(events).forEach { (ref, event) -> batch.set(ref, event) }
        batch.commit().await()
        return refs.map { it.id }
    }

    suspend fun updateEvent(event: Event) {
        collection.document(event.id).set(event).await()
    }

    // teamId filter is required by security rules
    suspend fun getEventsBySeriesId(teamId: String, seriesId: String): List<Event> =
        collection
            .whereEqualTo("teamId", teamId)
            .whereEqualTo("seriesId", seriesId)
            .get().await()
            .toObjects(Event::class.java)

    suspend fun cancelEvent(id: String) {
        collection.document(id).update("status", "cancelled").await()
    }

    suspend fun deleteEvent(id: String) {
        collection.document(id).delete().await()
    }

}
