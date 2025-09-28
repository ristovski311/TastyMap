package com.example.tastymap.services

import android.location.Location

interface LocationService {
    fun requestLocationUpdates(onLocationResult: (Location) -> Unit)
    fun removeLocationUpdates()
}