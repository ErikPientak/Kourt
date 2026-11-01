package com.kourt.app.data.repository.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kourt.app.data.model.Attendance
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("attendance")

    open suspend fun getAttendanceByEvent(eventId: String): List<Attendance> =
        collection.whereEqualTo("eventId", eventId).get().await()
            .toObjects(Attendance::class.java)

    open suspend fun saveAttendance(attendance: Attendance): String  {
        val id = docId(attendance)
        collection.document(id).set(attendance).await()
        return id
    }

    suspend fun updateAttendance(attendance: Attendance) {
        collection.document(docId(attendance)).set(attendance).await()
    }

    /**
     * Writes one attendance save in a single batch: changed records, removed records, and the
     * matching counter changes on `playerStats` and `teamStats`. Everything is written or nothing
     * is, so a failed save can be retried without counting anyone twice.
     *
     * Counters use [FieldValue.increment], so nothing is read first and match totals written to
     * the same docs at the same time aren't overwritten.
     *
     * @param counterDeltas teamMemberId → PlayerStats field → change, e.g. `"totalTrainingsLate" to -1`.
     * @param trainingsDelta change to `teamStats.totalTrainings` (+1 for a newly recorded training).
     */
    open suspend fun saveAttendanceWithStats(
        teamId: String,
        upserts: List<Attendance>,
        deletes: List<Attendance>,
        counterDeltas: Map<String, Map<String, Long>>,
        trainingsDelta: Long,
    ) {
        val batch = firestore.batch()
        upserts.forEach { batch.set(collection.document(docId(it)), it) }
        deletes.forEach { batch.delete(collection.document(docId(it))) }

        val playerStats = firestore.collection("playerStats")
        counterDeltas.forEach { (teamMemberId, deltas) ->
            // teamMemberId and teamId are required by the rules on every playerStats write.
            val data = mapOf("teamMemberId" to teamMemberId, "teamId" to teamId) +
                deltas.mapValues { (_, delta) -> FieldValue.increment(delta) }
            batch.set(playerStats.document("${teamMemberId}_$teamId"), data, SetOptions.merge())
        }

        if (trainingsDelta != 0L) {
            batch.set(
                firestore.collection("teamStats").document(teamId),
                mapOf(
                    "teamId" to teamId,
                    "totalTrainings" to FieldValue.increment(trainingsDelta),
                    "updatedAt" to Timestamp.now(),
                ),
                SetOptions.merge(),
            )
        }
        batch.commit().await()
    }

    private fun docId(attendance: Attendance) = "${attendance.userId}_${attendance.eventId}"
}
