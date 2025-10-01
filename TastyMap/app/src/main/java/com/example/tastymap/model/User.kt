package com.example.tastymap.model

import com.example.tastymap.helper.Helper
import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val profilePicture: String = "",
    val points: Int = 0,
    val lastKnownLocation: GeoPoint? = null,
    val lastLocationUpdate: Long = 0L,
    val image: String? = null
) {

    fun currentLevel(): Int {
        return this.points / Helper.POINTS_FOR_NEW_LEVEL; //Broj poena po levelu je 100, moze biti promenjeno
    }

    fun pointsToNextLevel(): Int {
        return this.points % Helper.POINTS_FOR_NEW_LEVEL;
    }

    fun percentageUntilNextLevel(): Float {
        return this.pointsToNextLevel() / (Helper.POINTS_FOR_NEW_LEVEL).toFloat();
    }
}