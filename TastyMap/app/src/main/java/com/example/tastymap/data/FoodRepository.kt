package com.example.tastymap.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.callbackFlow
import com.example.tastymap.model.Food
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth: FirebaseAuth = Firebase.auth
    private val foodCollection = firestore.collection("food")

    fun getAllFoodObjects() : Flow<List<Food>> = callbackFlow {

        val collectionRef = firestore.collection("food")
        val subscription = collectionRef.addSnapshotListener { snapshot, e ->
            if(e != null){
                close(e)
                return@addSnapshotListener
            }

            if(snapshot!=null) {
                val foodObjects = snapshot.documents.mapNotNull { document ->
                    document.toObject<Food>()?.copy(id = document.id)
                }
                trySend(foodObjects)
            }
        }

        awaitClose {
            subscription.remove()
        }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun addFoodObject(food: Food) {
        foodCollection.add(food)
            .addOnSuccessListener {
                println("Uspešno sačuvana hrana: ${food.name}")
            }
            .addOnFailureListener { e ->
                println("FIRESTORE SAVE ERROR: ${e.message}")
            }
    }

}