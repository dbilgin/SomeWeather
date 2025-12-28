package com.omedacore.someweather.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.presentation.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCity by viewModel.savedCity.collectAsState()
    var showCityInputDialog by remember { mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.title1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Text(
                text = "City",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            Text(
                text = savedCity ?: "Not set",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Button(
                onClick = { showCityInputDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change City")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Unit System",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Button(
                onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (unitSystem == UnitSystem.METRIC) {
                    ButtonDefaults.primaryButtonColors()
                } else {
                    ButtonDefaults.secondaryButtonColors()
                }
            ) {
                Text("Metric (°C, km/h)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (unitSystem == UnitSystem.IMPERIAL) {
                    ButtonDefaults.primaryButtonColors()
                } else {
                    ButtonDefaults.secondaryButtonColors()
                }
            ) {
                Text("Imperial (°F, mph)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }

    if (showCityInputDialog) {
        CityInputDialog(
            onDismiss = { showCityInputDialog = false },
            onConfirm = { city ->
                showCityInputDialog = false
                // Save city - WeatherDisplayScreen will fetch weather automatically
                viewModel.saveCity(city)
                onBack() // Navigate back to weather screen after city change
            }
        )
    }
}

