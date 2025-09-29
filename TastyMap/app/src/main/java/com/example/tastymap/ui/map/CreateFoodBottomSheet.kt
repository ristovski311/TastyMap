package com.example.tastymap.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun CreateFoodBottomSheet(
    onCreateFood: (name: String, description: String, types: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currentTypeInput by remember { mutableStateOf("") }
    var selectedTypes by remember { mutableStateOf(emptyList<String>()) }

    val normalizeType: (String) -> String = { input ->
        if (input.isBlank()) "" else input.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Kreiraj objekat hrane",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naziv hrane (obavezno)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis (opciono)") },
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTypeInput,
                onValueChange = { currentTypeInput = it },
                label = { Text("Dodaj tip hrane (npr. 'Slatka', 'Kineska')") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true
            )

            TextButton(
                onClick = {
                    val normalized = normalizeType(currentTypeInput)
                    if (normalized.isNotBlank() && !selectedTypes.contains(normalized)) {
                        selectedTypes = selectedTypes + normalized
                        currentTypeInput = ""
                    }
                },
                enabled = currentTypeInput.isNotBlank()
            ) {
                Text("Dodaj tip")
            }
        }

        if (selectedTypes.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(selectedTypes) { type ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = { Text(type) },
                        trailingIcon = {
                            IconButton(onClick = {
                                selectedTypes = selectedTypes - type
                            }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Ukloni tip")
                            }
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Otkaži")
            }
            Button(
                onClick = {
                    onCreateFood(name, description, selectedTypes)
                },
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Sačuvaj", modifier = Modifier.size(20.dp).padding(end = 4.dp))
                Text("Dodaj")
            }
        }
    }
}