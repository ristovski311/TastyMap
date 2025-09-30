package com.example.tastymap.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tastymap.R
import com.example.tastymap.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var name by remember {mutableStateOf("")}
    var phone by remember {mutableStateOf("")}

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(
                id = if (isSystemInDarkTheme()) R.drawable.illustration_login else R.drawable.illustration_login_light
            ),
            contentDescription = "Logo aplikacije",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = {name = it},
            label = {Text("Ime i prezime")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = {phone = it},
            label = {Text("Telefon")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier.height(8.dp),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Lozinka") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        Button(
            onClick = {
                authViewModel.registerUser(email, password, name, phone) {
                    onRegistrationSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Registruj se")
        }
        TextButton(
            onClick = onNavigateToLogin
        ){
            Text("Imate nalog? Prijavite se.")
        }
    }
}