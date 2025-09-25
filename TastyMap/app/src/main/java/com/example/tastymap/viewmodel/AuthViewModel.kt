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
    var auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    fun loginUser(context: Context, email: String, password: String, onLoginSuccess: () -> Unit) {
        if(email.isBlank() || password.isBlank()) {
            showToast(context, "Molimo unesite email i lozinku!");
            return
        }
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
        if(email.isBlank() || password.isBlank()) {
            showToast(context, "Molimo unesite email i lozinku!");
            return
        }
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

    fun saveUserData(context: Context, name: String, phone: String, onDataSaved: () -> Unit) {
        val user = auth.currentUser
        if(user!=null){
            val userMap = hashMapOf<String, Any>(
                "name" to name,
                "phone" to phone
            )

            db.collection("users").document(user.uid)
                .update(userMap)
                .addOnSuccessListener {
                    showToast(context, "Podaci su sacuvani!")
                    onDataSaved()
                }
                .addOnFailureListener { e ->
                    showToast(context, "Greska pri cuvanju podataka: ${e.message}")
                }
        }
    }

    fun fetchUserData(onSuccess: (User) -> Unit){
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

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}