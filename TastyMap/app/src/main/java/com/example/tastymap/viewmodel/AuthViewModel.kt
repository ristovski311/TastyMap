package com.example.tastymap.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.tastymap.helper.Helper
import com.example.tastymap.helper.Helper.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.tastymap.model.User

class AuthViewModel : ViewModel() {
    var auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    val currentUser = auth.currentUser

    fun loginUser(email: String, password: String, onLoginSuccess: () -> Unit) {
        if(email.isBlank() || password.isBlank()) {
            Helper.showSnackbar("Molimo unesite email i lozinku!")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Helper.showSnackbar("Uspešna prijava.")
                    onLoginSuccess()
                } else {
                    Helper.showSnackbar("Neuspesan login: ${task.exception?.message}")
                }
            }
    }

    fun registerUser(email: String, password: String, name: String, phone: String, onRegisterSuccess: () -> Unit) {
        if(email.isBlank() || password.isBlank() || name.isBlank() || phone.isBlank()) {
            Helper.showSnackbar("Molimo unesite sve podatke!")
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
                            "profilePicture" to "URL fotografije",
                            "points" to 0
                        )
                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Helper.showSnackbar("Uspešna registracija!")
                                onRegisterSuccess()
                            }
                            .addOnFailureListener { e ->
                                Helper.showSnackbar("Neuspešna registracija: ${e.message}")
                            }
                    }
                } else {
                    Helper.showSnackbar("Neuspešna registracija: ${task.exception?.message}")
                }
            }
    }

    fun updateUserData(name: String, phone: String, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if(user!=null){
            val userMap = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone
            )

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
        }
        else{
            Helper.showSnackbar("Korisnik nije prijavljen!")
            onResult(false)
        }
    }

    fun fetchCurrentUserData(onSuccess: (User) -> Unit){
        val user = auth.currentUser
        if(user != null)
        {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if(document.exists()){
                        val userData = document.toObject(User::class.java)
                        if(userData != null)
                        {
                            onSuccess(userData)
                        }
                    }

                }
        }
    }

    fun logout()
    {
        auth.signOut()
    }
}