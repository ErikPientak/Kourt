package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Club
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("Club")

    suspend fun getClub(id: String): Club? =
        collection.document(id).get().await().toObject(Club::class.java)

    suspend fun createClub(club: Club): String {
        val ref = collection.add(club).await()
        return ref.id
    }

    suspend fun updateClub(club: Club) {
        collection.document(club.id).set(club).await()
    }

    suspend fun deleteClub(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun getClubByAdminId(uid: String): Club? =
        collection.whereArrayContains("admin_ids", uid).get().await()
            .toObjects(Club::class.java)
            .firstOrNull()

    suspend fun getClubsByAdminId(uid: String): List<Club> =
        collection.whereArrayContains("admin_ids", uid).get().await()
            .toObjects(Club::class.java)

    suspend fun addAdmin(clubId: String, userId: String) {
        collection.document(clubId).update("admin_ids", FieldValue.arrayUnion(userId)).await()
    }
}
