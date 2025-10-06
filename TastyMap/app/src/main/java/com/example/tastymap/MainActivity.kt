package com.example.tastymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tastymap.ui.login.LoginScreen
import com.example.tastymap.ui.theme.TastyMapTheme
import com.example.tastymap.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.tastymap.ui.main.MainScreen
import com.example.tastymap.ui.register.RegisterScreen
import com.example.tastymap.ui.NavGraph
import androidx.navigation.NavType
import com.example.tastymap.helper.Helper
import com.example.tastymap.services.CloudinaryManager
import com.example.tastymap.ui.food_details.FoodDetailsScreen
import com.example.tastymap.ui.profile.OtherUserProfileScreen
import com.example.tastymap.viewmodel.FoodViewModel
import com.example.tastymap.viewmodel.FoodViewModelFactory
import com.example.tastymap.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("Nema snackBarHost-a")
}
val LocalSnackbarScope = compositionLocalOf<CoroutineScope> {
    error("Nema coroutine scope-a")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = Firebase.auth

        CloudinaryManager.initialize()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            Helper.showGlobalSnackbar = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }

            TastyMapTheme {

                CompositionLocalProvider(
                    LocalSnackbarHostState provides snackbarHostState,
                    LocalSnackbarScope provides scope
                ) {
                    Scaffold(
                        snackbarHost = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 56.dp, start = 16.dp, end = 16.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                SnackbarHost(hostState = snackbarHostState) { data ->
                                    Snackbar(
                                        snackbarData = data,
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        shape = MaterialTheme.shapes.medium,
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        val navController = rememberNavController()
                        val authViewModel: AuthViewModel = viewModel()
                        val userViewModel: UserViewModel = viewModel()
                        val startDestination =
                            if (auth.currentUser != null) "main_screen" else "login_screen"

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("login_screen") {
                                LoginScreen(
                                    onLoginClick = { email, pass, onComplete  ->
                                        authViewModel.loginUser(email, pass) { success ->
                                            onComplete()
                                            if(success){
                                                navController.navigate("main_screen") {
                                                    popUpTo("login_screen") { inclusive = true }
                                                }
                                            }
                                        }
                                    },
                                    onRegisterClick = {
                                        navController.navigate("register_screen") {
                                            popUpTo("login_screen") { inclusive = true }
                                        }
                                    },
                                )
                            }
                            composable(route = "register_screen") {
                                RegisterScreen(
                                    authViewModel,
                                    onRegistrationSuccess = {
                                        navController.navigate("main_screen") {
                                            popUpTo("register_screen") { inclusive = true }
                                        }
                                    },
                                    onNavigateToLogin = {
                                        navController.navigate("login_screen") {
                                            popUpTo("register_screen") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("main_screen") {
                                MainScreen(
                                    authViewModel = authViewModel,
                                    mainNavController = navController,
                                    onNavigateToUserProfile = { userId ->
                                        navController.navigate(
                                            NavGraph.createOtherUserProfileRoute(
                                                userId
                                            )
                                        )
                                    },
                                    userViewModel = userViewModel,
                                    onLogout = {
                                        navController.navigate("login_screen") {
                                            popUpTo("main_screen") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(
                                route = NavGraph.OtherUserProfile.route,
                                arguments = listOf(
                                    navArgument("userId") { type = NavType.StringType }
                                ),
                            ) { backStackEntry ->
                                val userId = backStackEntry.arguments?.getString("userId")
                                    ?: return@composable
                                var userViewModel: UserViewModel = viewModel()

                                OtherUserProfileScreen(
                                    userId = userId,
                                    userViewModel = userViewModel,
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable(
                                route = "food_details/{foodId}",
                                arguments = listOf(
                                    navArgument("foodId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val foodId = backStackEntry.arguments?.getString("foodId")
                                    ?: return@composable
                                val foodViewModel: FoodViewModel = viewModel(
                                    factory = FoodViewModelFactory(
                                        userViewModel = userViewModel
                                    )
                                )

                                val state = foodViewModel.state.collectAsState().value

                                LaunchedEffect(foodId) {
                                    foodViewModel.loadFoodDetails(foodId)
                                }

                                if (state.food != null) {
                                    FoodDetailsScreen(
                                        food = state.food,
                                        foodViewModel = foodViewModel,
                                        onBackClick = { navController.popBackStack() }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}