package com.example.tastymap.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.tastymap.R
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    companion object {
        const val CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"

        @Volatile
        private var localLocationCallback: ((Location) -> Unit)? = null
        private var listenerCount = 0

        @Synchronized
        fun setLocalLocationCallback(callback: (Location) -> Unit) {
            localLocationCallback = callback
            listenerCount++
        }

        @Synchronized
        fun removeLocalLocationCallback() {
            localLocationCallback = null
            listenerCount--
        }

        fun hasActiveListeners(): Boolean = listenerCount > 0
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    localLocationCallback?.invoke(location)

                    serviceScope.launch {
                        updateLocationInFirebase(location.latitude, location.longitude)
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            println("Nema dozvole za lokaciju")
        }
    }

    private fun stopTracking() {
        if (!hasActiveListeners()) {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
                locationCallback = null
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private suspend fun updateLocationInFirebase(lat: Double, lon: Double) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val geoPoint = GeoPoint(lat, lon)

            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "lastKnownLocation" to geoPoint,
                        "lastLocationUpdate" to System.currentTimeMillis()
                    )
                )
                .await()

            println("Firebase lokacija ažurirana: $lat, $lon")
        } catch (e: Exception) {
            println("Greška pri ažuriranju lokacije: ${e.message}")
        }
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TastyMap praćenje")
            .setContentText("Pratimo Vašu lokaciju...")
            .setSmallIcon(R.drawable.food_placeholder)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )

            val proximityChannel = NotificationChannel(
                "proximity_channel",
                "Proximity Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(trackingChannel)
            manager.createNotificationChannel(proximityChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        serviceScope.cancel()
    }
}