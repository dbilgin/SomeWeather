package com.omedacore.someweather.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omedacore.someweather.presentation.viewmodel.MobileWeatherViewModel
import com.omedacore.someweather.shared.data.model.UnitSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MobileWeatherViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCityDisplay by viewModel.savedCityDisplay.collectAsState()
    var showCityInputDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "City",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = savedCityDisplay ?: "Not set",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { showCityInputDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change City")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Unit System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = unitSystem == UnitSystem.METRIC,
                            onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                            label = { Text("Metric (°C, km/h)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = unitSystem == UnitSystem.IMPERIAL,
                            onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                            label = { Text("Imperial (°F, mph)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showCityInputDialog) {
        CityInputScreen(
            viewModel = viewModel,
            onCitySelected = { cityResult ->
                showCityInputDialog = false
                viewModel.saveCity(cityResult)
                viewModel.fetchWeather(cityResult.name)
            },
            onDismiss = { showCityInputDialog = false }
        )
    }
}

