package com.example.tastymap.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tastymap.viewmodel.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import com.example.tastymap.ui.NavGraph
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tastymap.ui.map.MapScreen
import com.example.tastymap.ui.profile.ProfileScreen
import com.example.tastymap.ui.ranking.RankingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    println("Primary: ${colorScheme.primary}")
    println("PrimaryContainer: ${colorScheme.primaryContainer}")

    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = navBackStackEntry?.destination?.route

    var showLogoutDialog by remember { mutableStateOf(false) }

    fun getCurrentTitle() : String {
        return when(currentRoute){
            NavGraph.Map.route -> NavGraph.Map.title
            NavGraph.Ranking.route -> NavGraph.Ranking.title
            NavGraph.Profile.route -> NavGraph.Profile.title
            else -> "TastyMap"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getCurrentTitle()) },
                actions = {
                    if(currentRoute == NavGraph.Profile.route)
                    {
                        OutlinedButton(
                            onClick = {
                                showLogoutDialog = true;
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Odjavi se")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar (
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavGraph.getBottomNavRoutes().forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = NavGraph.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavGraph.Map.route) { MapScreen() }
            composable(NavGraph.Ranking.route) { RankingScreen() }
            composable(NavGraph.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Potvrdite odjavu") },
            text = { Text("Da li ste sigurni da zelite da se odjavite?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Potvrdi")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Otkazi")
                }
            }
        )
    }
}