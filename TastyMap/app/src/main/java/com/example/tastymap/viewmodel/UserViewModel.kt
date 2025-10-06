package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastymap.helper.Helper
import com.example.tastymap.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    companion object {
        const val POINTS_CREATE_FOOD = 20
        const val POINTS_ADD_RATING = 5
        const val POINTS_ADD_COMMENT = 10
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUserInitial()
    }

    private fun loadCurrentUserInitial() {
        val currentUserId = auth.currentUser?.uid ?: null
        if (currentUserId != null) {
            fetchUserData(currentUserId) { user ->
                _currentUser.value = user
            }
        }
    }

    private suspend fun loadCurrentUser(): User? {
        val currentUserId = auth.currentUser?.uid ?: return null

        return try {
            val documentSnapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val user = documentSnapshot.toObject(User::class.java)
            _currentUser.value = user
            user
        } catch (e: Exception) {
            null
        }
    }

    fun fetchUserData(userId: String, onComplete: (User?) -> Unit) {

        viewModelScope.launch {
            try {
                val documentSnapshot = firestore.collection("users").document(userId).get().await()

                val user = documentSnapshot.toObject(User::class.java)
                onComplete(user)
            } catch (e: Exception) {
                onComplete(null)
            }
        }
    }

    fun addPointsToUser(
        userId: String,
        points: Int,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                firestore.collection("users")
                    .document(userId)
                    .update("points", FieldValue.increment(points.toLong()))
                    .await()

                if (userId == auth.currentUser?.uid) {
                    loadCurrentUser()
                }

                onSuccess?.invoke()
            } catch (e: Exception) {
                println("Greška pri dodavanju poena: ${e.message}")
                onFailure?.invoke(e)
            }
        }
    }

    fun givePointsForCreatingFood(
        userId: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val oldPoints = currentUser.value?.points ?: 0

            addPointsToUser(
                userId = userId,
                points = POINTS_CREATE_FOOD,
                onSuccess = {
                    viewModelScope.launch {
                        val updatedUser = loadCurrentUser()
                        val newPoints = updatedUser?.points ?: 0

                        Helper.showSnackbar("Osvojili ste $POINTS_CREATE_FOOD poena!")
                        checkLevelUp(oldPoints, newPoints)

                        onSuccess?.invoke()
                    }
                }
            )
        }
    }

    fun givePointsForRating(
        userId: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val oldPoints = currentUser.value?.points ?: 0
        addPointsToUser(
            userId = userId,
            points = POINTS_ADD_RATING,
            onSuccess = {
                viewModelScope.launch {
                    val updatedUser = loadCurrentUser()
                    val newPoints = updatedUser?.points ?: 0

                    Helper.showSnackbar("Osvojili ste $POINTS_ADD_RATING poena!")
                    checkLevelUp(oldPoints, newPoints)

                    onSuccess?.invoke()
                }
            }
        )
    }

    fun givePointsForComment(
        userId: String,
        onSuccess: (() -> Unit)? = null
    ) {
        val oldPoints = currentUser.value?.points ?: 0
        addPointsToUser(
            userId = userId,
            points = POINTS_ADD_COMMENT,
            onSuccess = {
                viewModelScope.launch {
                    val updatedUser = loadCurrentUser()
                    val newPoints = updatedUser?.points ?: 0

                    Helper.showSnackbar("Osvojili ste $POINTS_ADD_COMMENT poena!")
                    checkLevelUp(oldPoints, newPoints)

                    onSuccess?.invoke()
                }
            }
        )
    }

    fun checkLevelUp(oldPoints: Int, newPoints: Int) {
        val oldLevel = oldPoints / 100
        val newLevel = newPoints / 100
        println("old $oldPoints new $newPoints")
        if(oldLevel < newLevel)
            Helper.showSnackbar("Povećali ste svoj nivo!")
    }
}