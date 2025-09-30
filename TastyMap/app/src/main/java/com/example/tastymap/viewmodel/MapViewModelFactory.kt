package com.example.tastymap.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastymap.data.FoodRepository
import com.example.tastymap.model.User
import com.example.tastymap.services.LocationServiceImpl

class MapViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val locationService = LocationServiceImpl(application.applicationContext)
            val foodRepository = FoodRepository()
            val userViewModel = UserViewModel()
            return MapViewModel(locationService, foodRepository, userViewModel) as T
        }
        throw IllegalArgumentException("Nepoznata viewModel klasa!")
    }
}