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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import com.example.tastymap.viewmodel.MapViewModelFactory
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.tastymap.model.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.tastymap.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.unit.sp
import com.example.tastymap.helper.Helper
import androidx.compose.runtime.key
import com.example.tastymap.ui.food_details.FoodDetailsScreen
import com.example.tastymap.viewmodel.FilterSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToFoodDetails: (String) -> Unit
) {

    val context = LocalContext.current.applicationContext as Application
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(context)
    )
    var hasLocationPermission by remember {
        mutableStateOf(checkLocationPermission(context))
    }
    var isMapLoaded by remember { mutableStateOf(false) }


    // Bottom sheet prikaz bool
    var showCreationSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }
    var showFoodListSheet by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var showFoodDetailsDialog by remember { mutableStateOf(false) }


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


    //Camera pomeranje prilikom load-ovanja mape
    val state by mapViewModel.state.collectAsState()
    var hasAnimatedToUserLocation by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.lastKnownLocation, state.zoomLevel)
    }
    LaunchedEffect(state.lastKnownLocation, isMapLoaded) {
        if (isMapLoaded
            && !hasAnimatedToUserLocation
            && state.lastKnownLocation.latitude != 0.0
            && state.lastKnownLocation.longitude != 0.0
        ) {
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
        if (hasLocationPermission && state.lastKnownLocation.latitude != 0.0 && state.lastKnownLocation.longitude != 0.0) {
            val update = CameraUpdateFactory.newLatLngZoom(
                state.lastKnownLocation,
                state.zoomLevel
            )

            CoroutineScope(Dispatchers.Main).launch {
                cameraPositionState.animate(update = update, durationMs = 500)
            }
        }
    }

    val handleCreateFood: (String, String, List<String>) -> Unit = { name, description, types ->
        if (state.lastKnownLocation.latitude != 0.0 && state.lastKnownLocation.longitude != 0.0) {
            val newFood = Food(
                name = name,
                description = description,
                latitude = state.lastKnownLocation.latitude,
                longitude = state.lastKnownLocation.longitude,
                creatorId = mapViewModel.currentUserId,
                types = types
            )
            mapViewModel.saveFood(newFood)
            showCreationSheet = false
        }
    }

    val applyFilters: (FilterSettings) -> Unit = { newSettings ->
        mapViewModel.updateFilterSettings(newSettings)
        showFilterSheet = false
    }

    //UI
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
                    key("${food.latitude}_${food.longitude}_${food.name}") {
                        val markerState = rememberMarkerState(position = food.getLatLng())
                        Marker(
                            state = markerState,
                            title = food.name,
                            snippet = food.description,
                            onClick = { marker ->
                                selectedFood = food
                                showFoodDetailsDialog = true
                                true
                            },
                            icon = Helper.bitmapDescriptorFromVector(
                                context,
                                R.drawable.food_placeholder,
                                50
                            )
                        )
                    }
                }
            }


            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "R: ${"%.1f".format(state.filterRadiusKm)} km",
                                fontSize = 13.sp,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "${state.foodObjects.size} objekata",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Slider(
                            value = state.filterRadiusKm,
                            onValueChange = { newValue ->
                                mapViewModel.updateFilterRadiusKm(newValue)
                            },
                            valueRange = 1f..10f,
                            steps = 80,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (mapViewModel.currentUserId.isNotBlank()) {
                                showCreationSheet = true
                            } else {
                                println("Korisnik mora biti ulogovan da bi kreirao objekat.")
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Kreiraj objekat hrane na trenutnoj lokaciji"
                        )
                    }

                    FloatingActionButton(
                        onClick = centerMap,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Centriraj na trenutnu lokaciju korisnika"
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    showFilterSheet = true
                    showCreationSheet = false
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Filtriraj"
                )
            }

            FloatingActionButton(
                onClick = {
                    showFoodListSheet = true
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Lista hrane"
                )
            }

            if (showFoodDetailsDialog && selectedFood != null) {
                FoodDetailsDialog(
                    foodObject = selectedFood!!,
                    onDismiss = {
                        showFoodDetailsDialog = false
                        selectedFood = null
                    },
                    onDetailsClick = { food ->
                        showFoodDetailsDialog = false
                        selectedFood = null
                        onNavigateToFoodDetails(food.id)
                    }
                )
            }

            if (showCreationSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCreationSheet = false },
                    sheetState = sheetState
                ) {
                    CreateFoodBottomSheet(
                        onCreateFood = handleCreateFood,
                        onCancel = { showCreationSheet = false }
                    )
                }
            }

            if (showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showFilterSheet = false },
                    sheetState = sheetState
                ) {
                    FilterBottomSheet(
                        currentSettings = state.filterSettings,
                        uniqueFoodTypes = state.uniqueFoodTypes,
                        uniqueCreators = state.uniqueCreators,
                        onApplyFilters = applyFilters,
                        onCancel = { showFilterSheet = false }
                    )
                }
            }

            if(showFoodListSheet) {
                ModalBottomSheet(
                    onDismissRequest = {showFoodListSheet = false},
                    sheetState = sheetState
                ) {
                    FoodListBottomSheet(state.foodObjects, onFoodClick = {
                        foodId ->
                        showFoodListSheet = false
                        onNavigateToFoodDetails(foodId)
                    })
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Dozvola za lokaciju je iskljucena!",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}