package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.Nomination
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NominationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("nominations")

    suspend fun getNomination(eventId: String): Nomination? =
        collection.document(eventId).get().await().toObject(Nomination::class.java)

    suspend fun createNomination(nomination: Nomination) {
        collection.document(nomination.eventId).set(nomination).await()
    }

    suspend fun updateNomination(nomination: Nomination) {
        collection.document(nomination.eventId).set(nomination).await()
    }
}
