package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Club
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("clubs")

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
}
