package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tastymap.model.Food
import com.example.tastymap.services.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.example.tastymap.data.FoodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

data class FoodCreator(
    val id: String,
    val name: String
)

data class FilterSettings(
    val selectedTypes: Set<String> = emptySet(),
    val filterByDateOption: DateFilterOption = DateFilterOption.ALL_TIME,
    val selectedCreatorIds: Set<String> = emptySet()
)

enum class DateFilterOption(val label: String, val days: Int?) {
    ALL_TIME("Sve vreme", null),
    LAST_24_HOURS("Poslednja 24 sata", 1),
    LAST_7_DAYS("Poslednjih 7 dana", 7),
    LAST_30_DAYS("Poslednjih 30 dana", 30);
}

data class MapState(
    val lastKnownLocation: LatLng = LatLng(43.3209, 21.8958),
    val foodObjects: List<Food> = emptyList(),
    val isTracking: Boolean = false,
    val zoomLevel: Float = 15f,
    val filterRadiusKm: Float = 5.0f,
    val filterSettings: FilterSettings = FilterSettings(),
    val uniqueFoodTypes: List<String> = emptyList(),
    val uniqueCreators: List<FoodCreator> = emptyList()
)

class MapViewModel(
    private val locationService: LocationService,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    private val _lastKnownLocation = MutableStateFlow(_state.value.lastKnownLocation)
    private val _filterRadiusKm = MutableStateFlow(_state.value.filterRadiusKm)
    private val _filterSettings = MutableStateFlow(_state.value.filterSettings)

    private val allFoodObjects: StateFlow<List<Food>> = foodRepository.getAllFoodObjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val state: StateFlow<MapState> = combine(
        _lastKnownLocation,
        _filterRadiusKm,
        _filterSettings,
        allFoodObjects
    ) { location, radius, settings, allFoods ->

        val allUniqueTypes = allFoods
            .flatMap { it.types }
            .distinct()
            .sorted()

        val allUniqueCreators = allFoods
            .distinctBy { it.creatorId }
            .map { FoodCreator(
                id = it.creatorId,
                name = "Korisnik_${it.creatorId.take(4)}"
            ) }
            .sortedBy { it.name }

        val filteredFoods = allFoods.filter { food ->
            val distance = calculateDistanceInKm(
                lat1 = location.latitude,
                lon1 = location.longitude,
                lat2 = food.latitude,
                lon2 = food.longitude
            )
            val isWithinRadius = distance <= radius

            if (!isWithinRadius) return@filter false

            val isTypeMatch = if (settings.selectedTypes.isEmpty()) {
                true
            } else {
                food.types.any { it in settings.selectedTypes }
            }

            if (!isTypeMatch) return@filter false

            val isCreatorMatch = if (settings.selectedCreatorIds.isEmpty()) {
                true
            } else {
                food.creatorId in settings.selectedCreatorIds
            }

            if (!isCreatorMatch) return@filter false

            val isDateMatch = when (settings.filterByDateOption) {
                DateFilterOption.ALL_TIME -> true
                else -> {
                    settings.filterByDateOption.days?.let { days ->
                        val timeLimit = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
                        food.creationDate >= timeLimit
                    } ?: true
                }
            }

            isDateMatch
        }

        MapState(
            lastKnownLocation = location,
            foodObjects = filteredFoods,
            filterRadiusKm = radius,
            filterSettings = settings,
            uniqueFoodTypes = allUniqueTypes,
            uniqueCreators = allUniqueCreators
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _state.value
    )
    val currentUserId: String
        get() = foodRepository.getCurrentUserId()

    fun saveFood(food: Food) {
        viewModelScope.launch {
            foodRepository.addFoodObject(food)
        }
    }

    fun updateFilterRadiusKm(newRadius: Float) {
        _filterRadiusKm.value = newRadius
    }

    fun updateFilterSettings(newSettings: FilterSettings) {
        _filterSettings.value = newSettings
    }

    fun startLocationUpdates() {
        locationService.requestLocationUpdates { location ->
            viewModelScope.launch {
                _lastKnownLocation.value = LatLng(location.latitude, location.longitude)
            }
        }
    }

    override fun onCleared() {
        locationService.removeLocationUpdates()
        super.onCleared()
    }
}

fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371 // Radius Zemlje u km

    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)

    val a = sin(latDistance / 2) * sin(latDistance / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(lonDistance / 2) * sin(lonDistance / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}