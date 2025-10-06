package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tastymap.helper.Helper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.tastymap.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    var auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    val currentUser = auth.currentUser

    private val _currentUserState = MutableStateFlow(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserState.value = firebaseAuth.currentUser
        }
    }

    fun loginUser(email: String, password: String, onLoginSuccess: (Boolean) -> Unit) {
        if(email.isBlank() || password.isBlank()) {
            Helper.showSnackbar("Molimo unesite email i lozinku!")
            onLoginSuccess(false)
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Helper.showSnackbar("Uspešna prijava.")
                    onLoginSuccess(true)
                } else {
                    Helper.showSnackbar("Neuspesan login: ${task.exception?.message}")
                    onLoginSuccess(false)
                }
            }
    }

    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        profilePictureUrl: String? = null,
        onRegisterSuccess: (Boolean) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank() || phone.isBlank()) {
            Helper.showSnackbar("Molimo unesite sve podatke!")
            onRegisterSuccess(false)
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "name" to name,
                            "phone" to phone,
                            "profilePicture" to (profilePictureUrl ?: ""),
                            "points" to 0
                        )
                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Helper.showSnackbar("Uspešna registracija!")
                                onRegisterSuccess(false)
                            }
                            .addOnFailureListener { e ->
                                Helper.showSnackbar("Neuspešna registracija: ${e.message}")
                                onRegisterSuccess(false)
                            }
                    }
                } else {
                    Helper.showSnackbar("Neuspešna registracija: ${task.exception?.message}")
                    onRegisterSuccess(false)
                }
            }
    }

    fun updateUserData(
        name: String,
        phone: String,
        profilePictureUrl: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            val userMap = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone
            )

            profilePictureUrl?.let {
                userMap["profilePicture"] = it
            }

            db.collection("users").document(user.uid)
                .update(userMap)
                .addOnSuccessListener {
                    Helper.showSnackbar("Podaci su ažurirani!")
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    Helper.showSnackbar("Greška pri ažuriranju podataka: ${e.message}")
                    onResult(false)
                }
        } else {
            Helper.showSnackbar("Korisnik nije prijavljen!")
            onResult(false)
        }
    }

    fun fetchCurrentUserData(onSuccess: (User) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.toObject(User::class.java)
                        if (userData != null) {
                            onSuccess(userData)
                        }
                    }

                }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUserState.value = null
    }
}