package com.example.tastymap.viewmodel

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastymap.R
import com.example.tastymap.data.FoodRepository
import com.example.tastymap.helper.Helper
import com.example.tastymap.model.Food
import com.example.tastymap.services.LocationService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FoodCreator(
    val id: String,
    val name: String
)

data class NearbyUser(
    val id: String,
    val name: String,
    val location: LatLng,
    val profilePicture: String = ""
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
    val nearbyUsers: List<NearbyUser> = emptyList(),
    val isTracking: Boolean = false,
    val zoomLevel: Float = 18f,
    val filterRadiusKm: Float = 5.0f,
    val filterSettings: FilterSettings = FilterSettings(),
    val uniqueFoodTypes: List<String> = emptyList(),
    val uniqueCreators: List<FoodCreator> = emptyList()
)

data class LocationData(
    val location: LatLng,
    val radiusKm: Float
)

data class FilterData(
    val settings: FilterSettings,
    val allFoods: List<Food>
)

data class UserData(
    val userNames: Map<String, String>,
    val nearbyUsers: List<NearbyUser>
)

class MapViewModel(
    private val context: Context,
    private val locationService: LocationService,
    private val foodRepository: FoodRepository,
    private val userViewModel: UserViewModel
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    private val _lastKnownLocation = MutableStateFlow(_state.value.lastKnownLocation)
    private val _filterRadiusKm = MutableStateFlow(_state.value.filterRadiusKm)
    private val _filterSettings = MutableStateFlow(_state.value.filterSettings)

    private val notifiedFoodIds = mutableSetOf<String>()
    private val notifiedUserIds = mutableSetOf<String>()
    private var previousLocation: LatLng? = null

    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    private val _userNames = MutableStateFlow<Map<String, String>>(emptyMap())

    private var usersLocationListener: ListenerRegistration? = null

    val currentUserId: String
        get() = foodRepository.getCurrentUserId()

    private fun loadUserNames(creatorIds: Set<String>) {
        creatorIds.forEach { creatorId ->
            if (!_userNames.value.containsKey(creatorId)) {
                userViewModel.fetchUserData(creatorId) { user ->
                    if (user != null) {
                        _userNames.value = _userNames.value + (creatorId to user.name)
                    }
                }
            }
        }
    }

    private val allFoodObjects: StateFlow<List<Food>> = foodRepository.getAllFoodObjects().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val state: StateFlow<MapState> = combine(
        combine(_lastKnownLocation, _filterRadiusKm) { loc, radius ->
            LocationData(loc, radius)
        },
        combine(_filterSettings, allFoodObjects) { settings, foods ->
            FilterData(settings, foods)
        },
        combine(_userNames, _nearbyUsers) { names, users ->
            UserData(names, users)
        }
    ) { locationData, filterData, userData ->

        val allUniqueTypes = filterData.allFoods
            .flatMap { it.types }
            .distinct()
            .sorted()

        val allCreatorIds = filterData.allFoods.map { it.creatorId }.toSet()
        loadUserNames(allCreatorIds)

        val allUniqueCreators = filterData.allFoods
            .distinctBy { it.creatorId }
            .map { food ->
                FoodCreator(
                    id = food.creatorId,
                    name = userData.userNames[food.creatorId] ?: "Učitavanje..."
                )
            }
            .sortedBy { it.name }

        val filteredFoods = filterData.allFoods.filter { food ->
            val distance = Helper.calculateDistanceInKm(
                lat1 = locationData.location.latitude,
                lon1 = locationData.location.longitude,
                lat2 = food.latitude,
                lon2 = food.longitude
            )
            val isWithinRadius = distance <= locationData.radiusKm

            if (!isWithinRadius) return@filter false

            val isTypeMatch = if (filterData.settings.selectedTypes.isEmpty()) {
                true
            } else {
                food.types.any { it in filterData.settings.selectedTypes }
            }

            if (!isTypeMatch) return@filter false

            val isCreatorMatch = if (filterData.settings.selectedCreatorIds.isEmpty()) {
                true
            } else {
                food.creatorId in filterData.settings.selectedCreatorIds
            }

            if (!isCreatorMatch) return@filter false

            val isDateMatch = when (filterData.settings.filterByDateOption) {
                DateFilterOption.ALL_TIME -> true
                else -> {
                    filterData.settings.filterByDateOption.days?.let { days ->
                        val timeLimit = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
                        food.creationDate >= timeLimit
                    } != false
                }
            }

            isDateMatch
        }

        if (previousLocation != locationData.location &&
            locationData.location.latitude != 0.0 &&
            locationData.location.longitude != 0.0
        ) {
            checkProximity(locationData.location, locationData.radiusKm, filteredFoods)
            previousLocation = locationData.location
        }

        MapState(
            lastKnownLocation = locationData.location,
            foodObjects = filteredFoods,
            nearbyUsers = userData.nearbyUsers,
            filterRadiusKm = locationData.radiusKm,
            filterSettings = filterData.settings,
            uniqueFoodTypes = allUniqueTypes,
            uniqueCreators = allUniqueCreators
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = _state.value
    )

    fun saveFood(food: Food) {
        viewModelScope.launch {
            foodRepository.addFoodObject(food)
            userViewModel.givePointsForCreatingFood(
                userId = currentUserId,
                onSuccess = {
                    println("Dodati poeni korisniku za kreiranje hrane!")
                }
            )
        }
    }

    private fun startListeningToUserLocations() {
        val currentUserId = foodRepository.getCurrentUserId()
        if (currentUserId.isBlank()) return

        usersLocationListener?.remove()

        usersLocationListener = FirebaseFirestore.getInstance()
            .collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Greška pri praćenju korisnika: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch {
                        updateNearbyUsersFromSnapshot(snapshot.documents, currentUserId)
                    }
                }
            }
    }

    private fun updateNearbyUsersFromSnapshot(
        documents: List<DocumentSnapshot>,
        currentUserId: String
    ) {
        val currentLocation = _lastKnownLocation.value
        val radiusKm = _filterRadiusKm.value

        val currentlyInRangeIds = mutableSetOf<String>()
        val nearbyUsersList = mutableListOf<NearbyUser>()
        val newUserNames = mutableListOf<String>()

        documents.forEach { doc ->
            val userId = doc.id
            if (userId == currentUserId) return@forEach

            val geoPoint = doc.getGeoPoint("lastKnownLocation") ?: return@forEach
            val userName = doc.getString("name") ?: "Korisnik"
            val profilePicture = doc.getString("profilePicture") ?: ""

            val userLocation = LatLng(geoPoint.latitude, geoPoint.longitude)

            val distance = Helper.calculateDistanceInKm(
                currentLocation.latitude,
                currentLocation.longitude,
                geoPoint.latitude,
                geoPoint.longitude
            )

            if (distance <= radiusKm) {
                currentlyInRangeIds.add(userId)

                nearbyUsersList.add(
                    NearbyUser(
                        id = userId,
                        name = userName,
                        location = userLocation,
                        profilePicture = profilePicture
                    )
                )

                if (userId !in notifiedUserIds) {
                    newUserNames.add(userName)
                }
            }
        }

        if (newUserNames.isNotEmpty()) {
            val title = "Novi Korisnici u blizini!"
            val message = if (newUserNames.size == 1) {
                "${newUserNames.first()} je novootkriven u Vašem radijusu."
            } else {
                "Pronašli smo ${newUserNames.size} NOVIH korisnika: ${newUserNames.joinToString(", ")}."
            }
            sendNotification(title, message)
        }

        _nearbyUsers.value = nearbyUsersList

        notifiedUserIds.clear()
        notifiedUserIds.addAll(currentlyInRangeIds)
    }

    private fun checkProximity(location: LatLng, radiusKm: Float, filteredFoods: List<Food>) {
        viewModelScope.launch {
            checkNearbyFood(location, radiusKm, filteredFoods)
        }
    }

    private fun checkNearbyFood(location: LatLng, radiusKm: Float, filteredFoods: List<Food>) {
        val currentlyInRangeIds = mutableSetOf<String>()
        val newFoodNames = mutableListOf<String>()

        filteredFoods.forEach { food ->
            val distance = Helper.calculateDistanceInKm(
                location.latitude,
                location.longitude,
                food.latitude,
                food.longitude
            )

            if (distance <= radiusKm) {
                currentlyInRangeIds.add(food.id)
                if (food.id !in notifiedFoodIds) {
                    newFoodNames.add(food.name)
                }
            }
        }

        val hasNewFood = newFoodNames.any { it !in notifiedFoodIds }

        if (hasNewFood) {
            val title = "NOVA Hrana u blizini!"
            val message = if (newFoodNames.size == 1) {
                "${newFoodNames.first()} je novootkrivena u Vašem radijusu."
            } else {
                "Pronašli smo ${newFoodNames.size} NOVIH vrsta hrane: ${newFoodNames.joinToString(", ")}."
            }

            sendNotification(title, message)
        }

        notifiedFoodIds.clear()
        notifiedFoodIds.addAll(currentlyInRangeIds)
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "proximity_channel")
            .setSmallIcon(R.drawable.food_placeholder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun updateFilterRadiusKm(newRadius: Float) {
        _filterRadiusKm.value = newRadius
        previousLocation = null
    }

    fun updateFilterSettings(newSettings: FilterSettings) {
        _filterSettings.value = newSettings
        notifiedFoodIds.clear()
        previousLocation = null
    }

    fun startLocationUpdates() {
        locationService.requestLocationUpdates { location ->
            viewModelScope.launch {
                _lastKnownLocation.value = LatLng(location.latitude, location.longitude)
            }
        }

        startListeningToUserLocations()
    }

    override fun onCleared() {
        locationService.removeLocationUpdates()
        usersLocationListener?.remove()
        super.onCleared()
    }
}