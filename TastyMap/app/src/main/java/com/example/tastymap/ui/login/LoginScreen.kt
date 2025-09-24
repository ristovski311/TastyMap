package com.example.tastymap.ui.login

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.tastymap.ui.theme.TastyMapTheme

@Composable
fun LoginScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { /*TODO: Napravi email onvaluechange*/ },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        );
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        OutlinedTextField(
            value = "",
            onValueChange = { /*TODO: Napravi onvaluechanged pass*/},
            label = {Text("Password")},
            modifier = Modifier.fillMaxWidth()
        );
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        Button(
            onClick = { /*TODO: login logika*/},
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Login")
        };
        Spacer(
            modifier = Modifier.height(8.dp)
        );
        TextButton(
            onClick = { /*TODO: Registracija logika*/}
        )
        {
            Text("Register")
        }
    }
}

@Composable
fun LoginScreenPreview()
{
    TastyMapTheme {
        LoginScreen()
    }
}

