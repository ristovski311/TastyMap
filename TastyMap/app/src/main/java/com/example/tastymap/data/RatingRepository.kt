package com.example.tastymap.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.tastymap.model.Rating
import com.example.tastymap.model.Comment
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class RatingRepository (
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth: FirebaseAuth = Firebase.auth
    private val ratingsCollection = firestore.collection("ratings")
    private val commentsCollection = firestore.collection("comments")

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    suspend fun addOrUpdateRating(foodId: String, rating: Float) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        try {
            val existingRating = ratingsCollection
                .whereEqualTo("foodId", foodId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (existingRating.documents.isNotEmpty()) {
                val docId = existingRating.documents[0].id
                ratingsCollection.document(docId)
                    .update("rating", rating, "timestamp", System.currentTimeMillis())
                    .await()
            } else {
                val newRating = Rating(
                    foodId = foodId,
                    userId = userId,
                    rating = rating
                )
                ratingsCollection.add(newRating).await()
            }
        } catch (e: Exception) {
            println("Greska pri dodavanju ili azuriranju rating-a: ${e.message}")
        }
    }

    suspend fun getUserRatingForFood(foodId: String): Float? {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return null

        return try {
            val result = ratingsCollection
                .whereEqualTo("foodId", foodId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.firstOrNull()?.getDouble("rating")?.toFloat()
        } catch (e: Exception) {
            println("Greska kod pribavljanja ocene korisnika za datu hranu: ${e.message}")
            null
        }
    }

    fun getAverageRatingForFood(foodId: String): Flow<Pair<Float, Int>> = callbackFlow {
        val subscription = ratingsCollection
            .whereEqualTo("foodId", foodId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ratings = snapshot.documents.mapNotNull {
                        it.getDouble("rating")?.toFloat()
                    }

                    val average = if (ratings.isNotEmpty()) {
                        ratings.average().toFloat()
                    } else {
                        0f
                    }

                    trySend(Pair(average, ratings.size))
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun addComment(foodId: String, text: String, userName: String) {
        val userId = getCurrentUserId()
        if (userId.isEmpty() || text.isBlank()) return

        try {
            val comment = Comment(
                foodId = foodId,
                userId = userId,
                userName = userName,
                text = text
            )
            commentsCollection.add(comment).await()
        } catch (e: Exception) {
            println("Greska pri dodavanju komentara: ${e.message}")
        }
    }

    fun getCommentsForFood(foodId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = commentsCollection
            .whereEqualTo("foodId", foodId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)?.copy(id = doc.id)
                    }
                    trySend(comments)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun hasUserCommentedOnFood(foodId: String): Boolean {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return true

        return try {
            val result = commentsCollection
                .whereEqualTo("foodId", foodId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.isNotEmpty()
        } catch (e: Exception) {
            println("Greska pri proveri komentara: ${e.message}")
            false
        }
    }

    suspend fun hasUserRatedFood(foodId : String) : Boolean
    {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return true

        try {
            val existingRating = ratingsCollection
                .whereEqualTo("foodId", foodId)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            return existingRating.documents.isNotEmpty()
        } catch (e: Exception) {
            println("Greska pri proveri postojanja ratinga ${e.message}")
            return true
        }
    }
}