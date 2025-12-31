package com.omedacore.someweather.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omedacore.someweather.presentation.viewmodel.MobileWeatherViewModel
import com.omedacore.someweather.presentation.viewmodel.WeatherUiState
import com.omedacore.someweather.shared.data.model.UnitSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDisplayScreen(
    viewModel: MobileWeatherViewModel,
    uiState: WeatherUiState,
    onCityChange: () -> Unit,
    onSettings: () -> Unit
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCity by viewModel.savedCity.collectAsState()
    val uiStateValue by viewModel.uiState.collectAsState()

    LaunchedEffect(savedCity) {
        savedCity?.let { city ->
            val currentState = uiStateValue
            val shouldFetch = when (currentState) {
                is WeatherUiState.Initial -> true
                is WeatherUiState.Loading -> false // Already fetching
                is WeatherUiState.Success -> true // Always allow refresh if needed
                is WeatherUiState.Error -> true // Retry on error
            }
            // Weather is fetched via loadSavedCity() which uses coordinates
            // This LaunchedEffect is kept for compatibility but doesn't need to fetch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Some Weather") },
                actions = {
                    IconButton(onClick = { viewModel.refreshWeather() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is WeatherUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        CurrentWeatherSection(
                            weather = uiState.weather,
                            cityName = savedCity ?: "",
                            unitSystem = unitSystem ?: UnitSystem.METRIC
                        )
                    }
                    item {
                        ForecastSection(
                            forecastItems = uiState.weather.forecastList,
                            sunrise = uiState.weather.sys.sunrise,
                            sunset = uiState.weather.sys.sunset,
                            unitSystem = unitSystem ?: UnitSystem.METRIC
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        WeatherAttribution()
                    }
                }
            }

            is WeatherUiState.Error -> {
                ErrorContent(
                    message = uiState.message,
                    onRetry = { viewModel.refreshWeather() },
                    onCityChange = onCityChange,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is WeatherUiState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
