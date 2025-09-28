package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tastymap.model.Food
import com.example.tastymap.services.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MapState(
    val lastKnownLocation: LatLng = LatLng(43.3209, 21.8958),
    val foodObjects: List<Food> = emptyList(),
    val isTracking: Boolean = false,
    val zoomLevel: Float = 15f
)

class MapViewModel(
    private val locationService: LocationService
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    // Ovde cu da pozovem da dobijem samu lokaciju TODO

    fun startLocationUpdates() {
        locationService.requestLocationUpdates { location ->
            viewModelScope.launch {
                _state.update { currentState ->

                    currentState.copy(
                        lastKnownLocation = LatLng(location.latitude, location.longitude)
                    )

                }
            }

            //Sad kad imamo novu lokaciju zvacemo da proverimo blizinu hrane u okolini TODO

        }
    }

    override fun onCleared() {
        locationService.removeLocationUpdates()
        super.onCleared()
    }

    // TODO uzmi iz baze hranu

}