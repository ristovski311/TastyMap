package com.example.tastymap.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastymap.model.User
import com.example.tastymap.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.tastymap.viewmodel.UserViewModel


@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.fetchCurrentUserData { fetcheduser ->
            user = fetcheduser
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showEditDialog = true
                }
            ) {
                Icon(Icons.Filled.Create, contentDescription = "Izmeni profil")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->

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
        //Slika
        Spacer(
            modifier = Modifier.height(48.dp)
        )
        Card(
            shape = CircleShape,
            modifier = Modifier
                .size(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Image(
                painter = rememberVectorPainter(Icons.Filled.Person),
                contentDescription = "Profilna slika",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            )
        }
        Spacer(
            modifier = Modifier.height(24.dp)
        )
        //Ime
        Text(
            text = user?.name ?: "Korisnik",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(
            modifier = Modifier.height(8.dp)
        )
        //Email i telefon
        Text(
            text = "${user?.email ?: "email"} | ${user?.phone ?: "phone"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(
            modifier = Modifier.height(16.dp)
        )
        //Poeni
        ProgressBar(
            user ?: User()
        )
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
                    text = "Nivo ${user?.currentLevel() ?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Nivo ${(user?.currentLevel() ?: 0) + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(
                modifier = Modifier.height(4.dp)
            )
            LinearProgressIndicator(
                progress = { user?.percentageUntilNextLevel() ?: 0.0.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${user?.points ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${100 * ((user?.currentLevel() ?: 0) + 1)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(
                modifier = Modifier.height(4.dp)
            )
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
                    text = user?.currentLevel().toString() ?: "0",
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
    var context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Izmenite podatke") },
        text = {
            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ime") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
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
                        authViewModel.updateUserData(context, name, phone) { success ->
                            if (success) {
                                onSuccess()
                            } else {
                                authViewModel.showToast(context, "Greška pri ažuriranju podataka!")
                            }
                        }
                    } else {
                        authViewModel.showToast(context, "Polja ne smeju biti prazna!")
                    }
                }
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
                title = {Text("Korisnik")},
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
    ) {
        padding ->
        when{
            isLoading -> {
                CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
            }
            userProfile == null -> {
                Text("Korisnik nije pronadjen!", Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
            }
            else -> {
                UserContent(padding = padding, user = userProfile!!)
            }
        }
    }
}