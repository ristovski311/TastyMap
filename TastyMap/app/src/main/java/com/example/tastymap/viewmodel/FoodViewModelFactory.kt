package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tastymap.data.FoodRepository
import com.example.tastymap.data.RatingRepository

class FoodViewModelFactory(
    private val foodRepository: FoodRepository = FoodRepository(),
    private val ratingRepository: RatingRepository = RatingRepository(),
    private val userViewModel: UserViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(foodRepository, ratingRepository, userViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}