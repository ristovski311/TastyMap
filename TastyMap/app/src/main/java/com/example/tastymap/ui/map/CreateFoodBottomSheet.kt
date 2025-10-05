package com.example.tastymap.ui.map

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.tastymap.helper.Helper
import com.example.tastymap.services.CloudinaryManager
import kotlinx.coroutines.launch

@Composable
fun CreateFoodBottomSheet(
    onCreateFood: (name: String, description: String, types: List<String>, imageUrl: String?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var currentTypeInput by remember { mutableStateOf("") }
    var selectedTypes by remember { mutableStateOf(emptyList<String>()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val normalizeType: (String) -> String = { input ->
        if (input.isBlank()) "" else input.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploading = true
            scope.launch {
                val imageUrl = CloudinaryManager.uploadImage(it, context)
                uploadedImageUrl = imageUrl
                isUploading = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let {
                isUploading = true
                scope.launch {
                    val imageUrl = CloudinaryManager.uploadImage(it, context)
                    uploadedImageUrl = imageUrl
                    isUploading = false
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                android.content.ContentValues()
            )
            selectedImageUri = uri
            uri?.let { cameraLauncher.launch(it) }
        } else {
            Helper.showSnackbar("Potrebna je dozvola za kameru")
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Izaberite izvor slike") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Galerija")
                    }
                    TextButton(
                        onClick = {
                            showImageSourceDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kamera")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Otkaži")
                }
            }
        )
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .clickable { showImageSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator()
            } else if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Slika hrane",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Dodaj sliku",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Dodaj sliku hrane (opciono)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naziv hrane (obavezno)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            enabled = !isUploading
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Opis (opciono)") },
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 8.dp),
            enabled = !isUploading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentTypeInput,
                onValueChange = { currentTypeInput = it },
                label = { Text("Dodaj tip hrane") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                enabled = !isUploading
            )

            TextButton(
                onClick = {
                    val normalized = normalizeType(currentTypeInput)
                    if (normalized.isNotBlank() && !selectedTypes.contains(normalized)) {
                        selectedTypes = selectedTypes + normalized
                        currentTypeInput = ""
                    }
                },
                enabled = currentTypeInput.isNotBlank() && !isUploading
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
                            IconButton(
                                onClick = { selectedTypes = selectedTypes - type },
                                modifier = Modifier.size(16.dp),
                                enabled = !isUploading
                            ) {
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
            OutlinedButton(
                onClick = onCancel,
                enabled = !isUploading
            ) {
                Text("Otkaži")
            }
            Button(
                onClick = {
                    onCreateFood(name, description, selectedTypes, uploadedImageUrl)
                },
                enabled = name.isNotBlank() && !isUploading
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Sačuvaj",
                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                )
                Text("Dodaj")
            }
        }
    }
}