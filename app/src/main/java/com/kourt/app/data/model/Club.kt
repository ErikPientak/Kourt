package com.kourt.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Club(
    @DocumentId val id: String = "",
    val name: String = "",
    @get:PropertyName("shortName")
    @set:PropertyName("shortName")
    var shortName: String = "",
    @get:PropertyName("logoURL")
    @set:PropertyName("logoURL")
    var logoURL: String = "",
    val president: String = "",
    @get:PropertyName("technical_director")
    @set:PropertyName("technical_director")
    var technicalDirector: String = "",
    val country: String = "",
    val city: String = "",
    @get:PropertyName("join_code")
    @set:PropertyName("join_code")
    var joinCode: String = "",
    @get:PropertyName("created_by")
    @set:PropertyName("created_by")
    var createdBy: String = "",
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp = Timestamp.now(),
    @get:PropertyName("admin_ids")
    @set:PropertyName("admin_ids")
    var adminIds: List<String> = emptyList(),
)
