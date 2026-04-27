package com.kourt.app.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoURL: String = "",
    val avatarId: String = "",
    val childIds: List<String> = emptyList(),
    val isManagedProfile: Boolean = false
)
