package com.example.tastymap.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.tastymap.model.User

class AuthViewModel : ViewModel() {
    private var auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    fun loginUser(context: Context, email: String, password: String, onLoginSuccess: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    showToast(context,"Uspesan login!")
                    onLoginSuccess()
                } else {
                    showToast(context, "Neuspesan login: ${task.exception?.message}")
                }
            }
    }

    fun registerUser(context: Context, email: String, password: String, onRegisterSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "name" to "Ime Korisnika",
                            "phone" to "Broj telefona",
                            "profilePictureUrl" to "URL fotografije"
                        )
                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                showToast(context, "Uspesna registracija!")
                                onRegisterSuccess()
                            }
                            .addOnFailureListener { e ->
                                showToast(context, "Neuspesna registracija: ${e.message}")
                            }
                    }
                } else {
                    showToast(context, "Neuspesna registracija: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}