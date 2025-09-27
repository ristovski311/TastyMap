package com.example.tastymap.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.tastymap.model.User
import com.google.firebase.firestore.Query

class RankingViewModel : ViewModel() {
    private val db = Firebase.firestore

    fun fetchRanking(context: Context, onSuccess: (List<User>) -> Unit) {
        db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                querySnapshot ->
                val rankingList = querySnapshot.documents.mapNotNull {
                    document ->
                    document.toObject(User::class.java)?.copy(uid = document.id)
                }
                onSuccess(rankingList)
            }
            .addOnFailureListener {
                showToast(context, "Greska pri preuzimanju rang liste!")
            }
    }

    fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}