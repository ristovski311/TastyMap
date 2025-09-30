package com.example.tastymap.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tastymap.R
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.example.tastymap.model.Food
import androidx.compose.foundation.lazy.items

@Composable
fun FoodListBottomSheet(
    foodList: List<Food>,
    onFoodClick: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hrana!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (foodList.isEmpty()) {
            Image(
                painter = painterResource(
                    id = if (isSystemInDarkTheme())
                        R.drawable.illustration_empty_food_list
                    else
                        R.drawable.illustration_empty_food_list_light
                ),
                contentDescription = "Food list slika",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Text("Trenutno nema hrane u radijusu!")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(
                            id = if (isSystemInDarkTheme())
                                R.drawable.illustration_food_list
                            else
                                R.drawable.illustration_food_list_light
                        ),
                        contentDescription = "Food list",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
                items(foodList, key = { it.id }) { food ->
                    FoodListItem(food = food, onClick = { onFoodClick(food.id) })
                }
            }
        }
    }
}

@Composable
fun FoodListItem(
    food: Food,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Image(
                painter = painterResource(id = R.drawable.food_placeholder),
                contentDescription = food.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(bottom = 8.dp)
            )
            Spacer(
                modifier = Modifier.width(16.dp)

            )
            Column(
                modifier = Modifier.weight(16f)
            ) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    food.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Detalji",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}