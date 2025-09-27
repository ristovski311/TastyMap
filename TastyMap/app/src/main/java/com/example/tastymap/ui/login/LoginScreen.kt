package com.example.tastymap.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tastymap.R


@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(
                id = if (isSystemInDarkTheme()) R.drawable.illustration_login else R.drawable.illustration_login_light
            ),
            contentDescription = "Logo aplikacije",
            modifier = Modifier
                .size(400.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        );
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            modifier = Modifier.fillMaxWidth()
        );
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prijavi se")
        };
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        TextButton(
            onClick = { onRegisterClick() }
        ) {
            Text("Registruj se")
        }
        Spacer(
            modifier = Modifier.height(8.dp)
        )
    }
}