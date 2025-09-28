package com.example.tastymap.ui.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastymap.viewmodel.MapViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.example.tastymap.viewmodel.MapViewModelFactory
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MapScreen() {

    val context = LocalContext.current.applicationContext as Application
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(context)
    )

    var hasLocationPermission by remember {
        mutableStateOf(checkLocationPermission(context))
    }

    var isMapLoaded by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasLocationPermission = isGranted
        if (isGranted) {
            mapViewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            mapViewModel.startLocationUpdates()
        }
    }

    val state by mapViewModel.state.collectAsState()
    var hasAnimatedToUserLocation by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.lastKnownLocation, state.zoomLevel)
    }

    LaunchedEffect(state.lastKnownLocation, isMapLoaded) {
        if (isMapLoaded
            && !hasAnimatedToUserLocation
            && state.lastKnownLocation.latitude != 0.0
            && state.lastKnownLocation.longitude != 0.0) {
            try {
                var currentZoom = cameraPositionState.position.zoom
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(
                        state.lastKnownLocation,
                        if (currentZoom > 10f) currentZoom else state.zoomLevel
                    ),
                    durationMs = 1000
                )
                hasAnimatedToUserLocation = true;
            } catch (e: Exception) {
                println("Greska pri animaciji kamere: ${e.message}")
            }
        }
    }

    val centerMap: () -> Unit = {
        if(hasLocationPermission && state.lastKnownLocation.latitude != 0.0 && state.lastKnownLocation.longitude != 0.0) {
            val update = CameraUpdateFactory.newLatLngZoom(
                state.lastKnownLocation,
                state.zoomLevel
            )

            CoroutineScope(Dispatchers.Main).launch {
                cameraPositionState.animate(update = update, durationMs = 500)
            }
        }
    }

    if (hasLocationPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = true,
                    compassEnabled = true,
                ),
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapType = MapType.HYBRID
                ),
                onMapLoaded = {
                    isMapLoaded = true
                }
            ) {
                state.foodObjects.forEach { food ->
                    Marker(
                        state = rememberMarkerState(position = food.getLatLng()),
                        title = food.name,
                        snippet = "Više detalja",
                        onClick = { marker ->
                            //TODO vise detalja i recenzije
                            true
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = centerMap,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Centriraj na trenutnu lokaciju korisnika"
                )
            }

        }
    } else {
        Text(
            "Dozvola za lokaciju je iskljucena!",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
