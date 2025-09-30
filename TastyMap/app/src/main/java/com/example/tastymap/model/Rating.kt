package com.example.tastymap.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Rating (
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val foodId: String = "",
    val rating:  Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
