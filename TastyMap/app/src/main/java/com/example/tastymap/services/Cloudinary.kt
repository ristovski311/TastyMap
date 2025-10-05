package com.example.tastymap.services

import android.content.Context
import android.net.Uri
import com.cloudinary.Cloudinary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CloudinaryConfig {
    const val CLOUD_NAME = "dj35p1pwt"
    const val API_KEY = "431932472681376"
    const val API_SECRET = "EkkHS5njhexsybpJVbti24fkTHU"
}

object CloudinaryManager {
    private lateinit var cloudinary: Cloudinary

    fun initialize() {
        val config = mapOf(
            "cloud_name" to CloudinaryConfig.CLOUD_NAME,
            "api_key" to CloudinaryConfig.API_KEY,
            "api_secret" to CloudinaryConfig.API_SECRET
        )
        cloudinary = Cloudinary(config)
    }

    fun getCloudinary(): Cloudinary = cloudinary

    suspend fun uploadImage(uri: Uri, context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val result = cloudinary.uploader().upload(inputStream, emptyMap<String, Any>())
            result["secure_url"] as? String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}