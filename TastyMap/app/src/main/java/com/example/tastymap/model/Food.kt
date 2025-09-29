package com.example.tastymap.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Food (
    @DocumentId
    val id: String = "",
    val name : String = "",
    val description : String = "",
    val latitude : Double = 0.0,
    val longitude : Double = 0.0,
    val creatorId: String = "",
    val types: List<String> = emptyList(),
    val creationDate: Long = System.currentTimeMillis(),
) {
    @Exclude
    fun getLatLng() : LatLng {
        return LatLng(latitude, longitude)
    }

}