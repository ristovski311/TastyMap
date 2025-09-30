package com.example.tastymap.model

import com.google.firebase.firestore.DocumentId

data class Comment (
    @DocumentId
    val id : String = "",
    val foodId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)