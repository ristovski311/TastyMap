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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
        {
            TastyMapTheme {
                val navController = rememberNavController()
                val authViewModel:  AuthViewModel = viewModel()
                val context = LocalContext.current
                NavHost(navController = navController, startDestination = "login_screen") {
                    composable("login_screen") {
                        LoginScreen(
                            onLoginClick = { email, pass ->
                                authViewModel.loginUser(
                                    context,
                                    email,
                                    pass
                                ) {
                                    navController.navigate("profile_screen")
                                }
                            },
                            onRegisterClick = { email, pass ->
                                authViewModel.registerUser(
                                    context,
                                    email,
                                    pass,
                                ) {
                                    navController.navigate("profile_screen")
                                }
                            },
                        )
                    }
                    composable("profile_screen") {
                        ProfileScreen()
                    }
                }
            }
        }
    }
}
