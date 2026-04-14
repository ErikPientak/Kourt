package com.kourt.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("User")

    suspend fun getUser(id: String): User? =
        collection.document(id).get().await().toObject(User::class.java)

    suspend fun createUser(user: User) {
        collection.document(user.id).set(user).await()
    }

    suspend fun updateUser(user: User) {
        collection.document(user.id).set(user).await()
    }

    suspend fun deleteUser(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun searchByDisplayName(query: String): List<User> {
        if (query.isBlank()) return emptyList()
        return collection
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThan("displayName", query + "\uf8ff")
            .limit(10)
            .get()
            .await()
            .toObjects(User::class.java)
    }
}
