package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastymap.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

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

}