package com.kourt.app.data.repository.remote

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kourt.app.data.model.User
import kotlinx.coroutines.tasks.await
import java.util.UUID
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

    suspend fun getUsersByIds(ids: List<String>): List<User> {
        if (ids.isEmpty()) return emptyList()
        return ids.chunked(30).flatMap { chunk ->
            collection
                .whereIn(FieldPath.documentId(), chunk)
                .get()
                .await()
                .toObjects(User::class.java)
        }
    }

    suspend fun createManagedUser(displayName: String, avatarId: String, parentUid: String): String {
        val childUid = UUID.randomUUID().toString()
        val child = User(
            id = childUid,
            displayName = displayName,
            avatarId = avatarId,
            isManagedProfile = true,
            managedBy = parentUid,
        )
        collection.document(childUid).set(child).await()
        collection.document(parentUid)
            .update("childIds", FieldValue.arrayUnion(childUid))
            .await()
        return childUid
    }

    suspend fun removeChildFromParent(parentUid: String, childUid: String) {
        collection.document(parentUid)
            .update("childIds", FieldValue.arrayRemove(childUid))
            .await()
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
