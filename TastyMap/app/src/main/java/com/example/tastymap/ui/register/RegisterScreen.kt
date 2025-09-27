package com.example.tastymap.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tastymap.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var name by remember {mutableStateOf("")}
    var phone by remember {mutableStateOf("")}

    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registracija")
        Spacer(
            modifier = Modifier.height(16.dp)
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
                if(email.isNotBlank() && password.isNotBlank() && name.isNotBlank() && phone.isNotBlank()){
                    authViewModel.registerUser(context, email, password, name, phone) {
                        onRegistrationSuccess()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Sacuvaj")
        }
    }
}