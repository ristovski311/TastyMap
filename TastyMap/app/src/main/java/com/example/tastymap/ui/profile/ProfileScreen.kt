package com.example.tastymap.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tastymap.model.User
import com.example.tastymap.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchUserData { user ->
            userData = user
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading)
            CircularProgressIndicator()
        else if (userData != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Zdravo, ${userData!!.name}!")
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        } else {
            Text("Nema podataka o korisniku.")
        }
        Button(
            onClick = {
                showLogoutDialog = true;
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Odjavi se")
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