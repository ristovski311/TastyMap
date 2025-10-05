package com.example.tastymap.ui.ranking

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastymap.viewmodel.RankingViewModel
import com.example.tastymap.model.User
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastymap.R
import com.example.tastymap.viewmodel.AuthViewModel

@Composable
fun RankingScreen(
    onNavigateToUserProfile: (String) -> Unit,
    authViewModel: AuthViewModel
) {

    val rankingViewModel: RankingViewModel = viewModel()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUser by authViewModel.currentUserState.collectAsState()
    val currentUserId = currentUser?.uid ?: ""

    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        isLoading = true
        rankingViewModel.fetchRanking(context) { fetchedUsers ->
            users = fetchedUsers
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (users.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Text("Nema registrovanih korisnika ili slaba internet veza!")
                }
            }
        } else {
            println("RankingScreen currentUserId = '$currentUserId'")
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(
                            id = if (isSystemInDarkTheme())
                                R.drawable.illustration_ranking
                            else
                                R.drawable.illustration_ranking_light
                        ),
                        contentDescription = "Ranking illustration",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
                itemsIndexed(users) { index, user ->
                    RankingItem(index, user, currentUserId) { userId ->
                        onNavigateToUserProfile(userId)
                    }
                }
            }

        }
    }
}
