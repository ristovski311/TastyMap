package com.example.tastymap.ui.register

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tastymap.helper.Helper
import com.example.tastymap.services.CloudinaryManager
import com.example.tastymap.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import android.provider.MediaStore.Images.*

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
                Media.EXTERNAL_CONTENT_URI,
                ContentValues()
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
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { showImageSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator()
            } else if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Profilna slika",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Dodaj sliku",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ime i prezime") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRegistering
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isRegistering) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        } else {
            Button(
                onClick = {
                    isRegistering = true
                    authViewModel.registerUser(
                        email = email,
                        password = password,
                        name = name,
                        phone = phone,
                        profilePictureUrl = uploadedImageUrl
                    ) { success ->
                        isRegistering = false
                        if(success)
                            onRegistrationSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text("Registruj se")
            }
        }

        TextButton(
            onClick = onNavigateToLogin,
            enabled = !isRegistering && !isUploading
        ) {
            Text("Imate nalog? Prijavite se.")
        }
    }
}