package com.example.tastymap

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tastymap.ui.login.LoginScreen
import com.example.tastymap.ui.profile.ProfileScreen
import com.example.tastymap.ui.theme.TastyMapTheme
import com.example.tastymap.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.tastymap.ui.main.MainScreen
import com.example.tastymap.ui.register.RegisterScreen
import com.example.tastymap.ui.NavGraph
import androidx.navigation.NavType
import com.example.tastymap.ui.profile.OtherUserProfileScreen
import com.example.tastymap.viewmodel.UserViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = Firebase.auth

        setContent {
            TastyMapTheme {
                Scaffold { paddingValues ->
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val context = LocalContext.current
                    val startDestination =
                        if (auth.currentUser != null) "main_screen" else "login_screen"

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable("login_screen") {
                            LoginScreen(
                                onLoginClick = { email, pass ->
                                    authViewModel.loginUser(context, email, pass) {
                                        navController.navigate("main_screen") {
                                            popUpTo("login_screen") { inclusive = true }
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
                                onNavigateToUserProfile = {
                                    userId ->
                                    navController.navigate(NavGraph.createOtherUserProfileRoute(userId))
                                },
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
                        ) {
                            backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                            var userViewModel : UserViewModel = viewModel()

                            OtherUserProfileScreen(
                                userId = userId,
                                userViewModel = userViewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}