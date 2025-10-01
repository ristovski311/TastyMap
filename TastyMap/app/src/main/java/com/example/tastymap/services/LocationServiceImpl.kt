package com.example.tastymap.services

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build

class LocationServiceImpl(private val context: Context) : LocationService {

    override fun requestLocationUpdates(onLocationResult: (Location) -> Unit) {

        LocationTrackingService.setLocalLocationCallback(onLocationResult)

        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override fun removeLocationUpdates() {
        LocationTrackingService.removeLocalLocationCallback()
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
    }
}