package com.example.tastymap.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tastymap.viewmodel.DateFilterOption
import com.example.tastymap.viewmodel.FilterSettings
import com.example.tastymap.viewmodel.FoodCreator
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider

@Composable
fun FilterBottomSheet(
    currentSettings: FilterSettings,
    uniqueFoodTypes: List<String>,
    uniqueCreators: List<FoodCreator>,
    onApplyFilters: (FilterSettings) -> Unit,
    onCancel: () -> Unit
) {
    var tempSelectedTypes by remember { mutableStateOf(currentSettings.selectedTypes) }
    var tempDateOption by remember { mutableStateOf(currentSettings.filterByDateOption) }
    var tempSelectedCreatorIds by remember { mutableStateOf(currentSettings.selectedCreatorIds) }

    val dateOptions = DateFilterOption.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Filteri za hranu", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(24.dp))

        Text("Filtriraj po tipu hrane:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (uniqueFoodTypes.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uniqueFoodTypes) { type ->
                    FilterChip(
                        selected = type in tempSelectedTypes,
                        onClick = {
                            tempSelectedTypes = if (type in tempSelectedTypes) {
                                tempSelectedTypes - type
                            } else {
                                tempSelectedTypes + type
                            }
                        },
                        label = { Text(type) }
                    )
                }
            }
        } else {
            Text("Nema dostupnih tipova hrane za filtriranje.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Filtriraj po kreatoru:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (uniqueCreators.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uniqueCreators) { creator ->
                    FilterChip(
                        selected = creator.id in tempSelectedCreatorIds,
                        onClick = {
                            tempSelectedCreatorIds = if (creator.id in tempSelectedCreatorIds) {
                                tempSelectedCreatorIds - creator.id
                            } else {
                                tempSelectedCreatorIds + creator.id
                            }
                        },
                        label = { Text(creator.name) }
                    )
                }
            }
        } else {
            Text("Nema dostupnih kreatora za filtriranje.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Filtriraj po datumu kreiranja:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            dateOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = tempDateOption == option,
                            onValueChange = {
                                if (it) tempDateOption = option
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tempDateOption == option,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Poništi")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    val newSettings = FilterSettings(
                        selectedTypes = tempSelectedTypes,
                        filterByDateOption = tempDateOption,
                        selectedCreatorIds = tempSelectedCreatorIds
                    )
                    onApplyFilters(newSettings)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Primeni Filter")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}