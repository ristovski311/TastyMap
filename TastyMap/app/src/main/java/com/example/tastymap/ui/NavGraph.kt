package com.example.tastymap.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavGraph(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Map: NavGraph("map", "Mapa", Icons.Default.Place)
    object Ranking: NavGraph("ranking", "Rang lista", Icons.Default.Menu)
    object Profile: NavGraph("profile", "Profil", Icons.Default.AccountCircle)

    object OtherUserProfile: NavGraph("userProfile/{userId}")

    companion object {
        fun getBottomNavRoutes() = listOf(Map, Ranking, Profile)
        fun createOtherUserProfileRoute(userId: String) : String {
            return OtherUserProfile.route.replace("{userId}", userId)
        }
    }
}
