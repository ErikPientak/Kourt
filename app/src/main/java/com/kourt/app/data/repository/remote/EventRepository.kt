package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val WRITES_PER_BATCH = 450

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

    /** True when attendance or match stats were recorded for the event; such events must not be deleted. */
    suspend fun hasResults(eventId: String): Boolean {
        val attendance = firestore.collection("attendance")
            .whereEqualTo("eventId", eventId).limit(1).get().await()
        if (!attendance.isEmpty) return true
        return firestore.collection("matchStats").document(eventId).get().await().exists()
    }

    /** Marks all [eventIds] cancelled in one batch (chunked below the 500-write limit). */
    suspend fun cancelEvents(eventIds: List<String>) {
        eventIds.chunked(WRITES_PER_BATCH).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.update(collection.document(it), "status", "cancelled") }
            batch.commit().await()
        }
    }

    /**
     * Deletes the events together with their RSVPs and nominations. The security rules look the
     * team up through the event document, so the related docs go first and the events last.
     * Callers must have checked [hasResults]: stats totals are not rolled back here.
     */
    suspend fun deleteEventsWithRelated(eventIds: List<String>) {
        val refs = mutableListOf<com.google.firebase.firestore.DocumentReference>()
        eventIds.forEach { id ->
            firestore.collection("RSVP").whereEqualTo("eventId", id).get().await()
                .documents.forEach { refs.add(it.reference) }
            refs.add(firestore.collection("nominations").document(id))
        }
        eventIds.forEach { refs.add(collection.document(it)) }
        refs.chunked(WRITES_PER_BATCH).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

}
