package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Attendance
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("attendance")

    suspend fun getAttendanceByEvent(eventId: String): List<Attendance> =
        collection.whereEqualTo("eventId", eventId).get().await()
            .toObjects(Attendance::class.java)

    suspend fun saveAttendance(attendance: Attendance): String  {
        val ref = collection.add(attendance).await()
        return ref.id
    }

    suspend fun updateAttendance(attendance: Attendance) {
        collection.document("${attendance.userId}_${attendance.eventId}").set(attendance).await()
    }

}
