package com.example.tastymap.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tastymap.R
import com.example.tastymap.model.Food
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SuggestionChip

@Composable
fun FoodDetailsDialog(
    foodObject: Food,
    onDismiss: () -> Unit,
    onDetailsClick: (Food) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(foodObject.name, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!foodObject.image.isNullOrBlank()) {
                    AsyncImage(
                        model = foodObject.image,
                        contentDescription = foodObject.name,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.food_placeholder)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.food_placeholder),
                        contentDescription = foodObject.name,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = foodObject.description.takeIf { it.isNotBlank() } ?: "Nema opisa.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    foodObject.types.forEach { type ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(type) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDetailsClick(foodObject) }) {
                Text("Prikaži detalje")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Zatvori")
            }
        }
    )
}