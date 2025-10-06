package com.example.tastymap.ui.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastymap.helper.Helper
import com.example.tastymap.helper.Helper.showToast
import com.example.tastymap.model.User
import com.example.tastymap.services.CloudinaryManager
import com.example.tastymap.viewmodel.AuthViewModel
import com.example.tastymap.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import android.content.Context
import android.os.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.core.content.edit

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Helper.showSnackbar("Notifikacije su omogućene")
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit {
                    putBoolean("notification_permission_asked", true)
                }
        } else {
            Helper.showSnackbar("Notifikacije su onemogućene")
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUserData { fetcheduser ->
            user = fetcheduser
            isLoading = false

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val permissionAsked = prefs.getBoolean("notification_permission_asked", false)

            if (!permissionAsked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showNotificationPermissionDialog = true
            }
        }
    }

    if (showNotificationPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationPermissionDialog = false
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit {
                        putBoolean("notification_permission_asked", true)
                    }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Omogući notifikacije") },
            text = {
                Text(
                    "Želite li da primate obaveštenja o novoj hrani i korisnicima u blizini?",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ) {
                    Text("Omogući")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionDialog = false
                        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .edit {
                                putBoolean("notification_permission_asked", true)
                            }
                    }
                ) {
                    Text("Ne sada")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = { showEditDialog = true }
                ) {
                    Icon(Icons.Filled.Create, contentDescription = "Izmeni profil")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (showEditDialog && user != null) {
                EditProfileDialog(
                    currentUser = user!!,
                    authViewModel = authViewModel,
                    onDismiss = { showEditDialog = false },
                    onSuccess = {
                        showEditDialog = false
                        authViewModel.fetchCurrentUserData { fetchedUser ->
                            user = fetchedUser
                        }
                    }
                )
            }
            UserContent(padding, user)
        }
    }
}

@Composable
fun UserContent(
    padding: PaddingValues,
    user: User?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Card(
            shape = CircleShape,
            modifier = Modifier.size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            if (!user?.profilePicture.isNullOrEmpty()) {
                AsyncImage(
                    model = user?.profilePicture,
                    contentDescription = "Profilna slika",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Filled.Person)
                )
            } else {
                Image(
                    painter = rememberVectorPainter(Icons.Filled.Person),
                    contentDescription = "Profilna slika",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = user?.name ?: "Korisnik",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${user?.email ?: "email"} | ${user?.phone ?: "phone"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProgressBar(user ?: User())
    }
}

@Composable
fun ProgressBar(
    user: User,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nivo ${user.currentLevel()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Nivo ${user.currentLevel() + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { user.percentageUntilNextLevel() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${user.points}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${100 * (user.currentLevel() + 1)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Vaš trenutni nivo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = user.currentLevel().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontSize = 64.sp
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUser: User,
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Izmenite podatke") },
        text = {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator()
                    } else {
                        val imageToShow = selectedImageUri ?: currentUser.profilePicture

                        if (imageToShow.toString().isNotEmpty()) {
                            AsyncImage(
                                model = imageToShow,
                                contentDescription = "Profilna slika",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = rememberVectorPainter(Icons.Filled.Person)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Dodaj sliku",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kliknite da promenite sliku",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ime") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        authViewModel.updateUserData(
                            name = name,
                            phone = phone,
                            profilePictureUrl = uploadedImageUrl
                        ) { success ->
                            if (success) {
                                onSuccess()
                            } else {
                                showToast(context, "Greška pri ažuriranju podataka!")
                            }
                        }
                    } else {
                        showToast(context, "Polja ne smeju biti prazna!")
                    }
                },
                enabled = !isUploading
            ) {
                Text("Sačuvaj izmene")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Otkaži")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    userId: String,
    userViewModel: UserViewModel,
    onBackClick: () -> Unit
) {
    var userProfile by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        userViewModel.fetchUserData(userId) { user ->
            userProfile = user
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Korisnik") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                CircularProgressIndicator(
                    Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }

            userProfile == null -> {
                Text(
                    "Korisnik nije pronadjen!",
                    Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }

            else -> {
                UserContent(padding = padding, user = userProfile!!)
            }
        }
    }
}