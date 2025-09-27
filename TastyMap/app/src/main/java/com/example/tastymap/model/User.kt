package com.example.tastymap.model

class User {
    val uid: String = ""
    val email: String = ""
    val name: String = ""
    val phone: String = ""
    val profilePicture: String = ""
    val points: Int = 0

    fun currentLevel() : Int
    {
        return this.points / 100; //Broj poena po levelu je 100, moze biti promenjeno
    }

    fun pointsToNextLevel() : Int
    {
        return this.points % 100;
    }

    fun percentageUntilNextLevel() : Float
    {
        return (this.pointsToNextLevel() / 100).toFloat();
    }
}