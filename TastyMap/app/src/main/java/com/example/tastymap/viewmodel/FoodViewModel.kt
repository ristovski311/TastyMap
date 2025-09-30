package com.example.tastymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastymap.data.FoodRepository
import com.example.tastymap.data.RatingRepository
import com.example.tastymap.helper.Helper
import com.example.tastymap.model.Food
import com.example.tastymap.model.Comment
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FoodDetailState(
    val food: Food? = null,
    val userRating: Float? = null,
    val averageRating: Float = 0f,
    val totalRatings: Int = 0,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false
)

class FoodViewModel(
    private val foodRepository: FoodRepository = FoodRepository(),
    private val ratingRepository: RatingRepository = RatingRepository(),
    private val userViewModel: UserViewModel
) : ViewModel() {
    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state.asStateFlow()

    fun loadFoodDetails(foodId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val food = foodRepository.getFoodById(foodId)
            _state.value = _state.value.copy(food = food)

            val userRating = ratingRepository.getUserRatingForFood(foodId)
            _state.value = _state.value.copy(userRating = userRating)

            ratingRepository.getAverageRatingForFood(foodId)
                .collect { (average, count) ->
                    _state.value = _state.value.copy(
                        averageRating = average,
                        totalRatings = count,
                        isLoading = false
                    )
                }
        }

        viewModelScope.launch {
            ratingRepository.getCommentsForFood(foodId)
                .collect { comments ->
                    _state.value = _state.value.copy(comments = comments)
                }
        }
    }

    fun submitRating(foodId: String, rating: Float) {
        viewModelScope.launch {

            val hasRated = ratingRepository.hasUserRatedFood(foodId)
            ratingRepository.addOrUpdateRating(foodId, rating)

            if (!hasRated) {
                userViewModel.givePointsForRating(ratingRepository.getCurrentUserId())
            }

            val updatedRating = ratingRepository.getUserRatingForFood(foodId)
            _state.value = _state.value.copy(userRating = updatedRating)
        }
    }


    fun addComment(foodId: String, text: String, userName: String) {
        viewModelScope.launch {

            val hasCommented = ratingRepository.hasUserCommentedOnFood(foodId)

            ratingRepository.addComment(foodId, text, userName)

            if (!hasCommented) {
                userViewModel.givePointsForComment(ratingRepository.getCurrentUserId())
            }
        }
    }
}