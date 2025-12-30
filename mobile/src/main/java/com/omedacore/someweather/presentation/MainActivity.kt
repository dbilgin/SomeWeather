package com.omedacore.someweather.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omedacore.someweather.presentation.screens.CityInputScreen
import com.omedacore.someweather.presentation.screens.SettingsScreen
import com.omedacore.someweather.presentation.screens.UnitSystemSelectionScreen
import com.omedacore.someweather.presentation.screens.WeatherDisplayScreen
import com.omedacore.someweather.presentation.theme.SomeWeatherTheme
import com.omedacore.someweather.presentation.viewmodel.MobileWeatherViewModel
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.repository.WeatherRepository

class MainActivity : ComponentActivity() {
    private val viewModel: MobileWeatherViewModel by viewModels {
        MobileWeatherViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SomeWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MobileApp(viewModel)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MobileApp(viewModel: MobileWeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val unitSystemLoaded by viewModel.unitSystemLoaded.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCity by viewModel.savedCity.collectAsState()
    var showCityInput by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    if (!unitSystemLoaded) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize()
        )
    } else {
        when {
            unitSystem == null -> {
                UnitSystemSelectionScreen(
                    onUnitSelected = { unit ->
                        viewModel.setUnitSystem(unit)
                    }
                )
            }
            savedCity == null -> {
                CityInputScreen(
                    viewModel = viewModel,
                    onCitySelected = { cityResult ->
                        viewModel.saveCity(cityResult)
                        viewModel.fetchWeather(cityResult.name)
                    },
                    onDismiss = {}
                )
            }
            showSettings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { showSettings = false }
                )
            }
            else -> {
                WeatherDisplayScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onCityChange = { showCityInput = true },
                    onSettings = { showSettings = true }
                )
            }
        }

        if (showCityInput) {
            CityInputScreen(
                viewModel = viewModel,
                onCitySelected = { cityResult ->
                    viewModel.saveCity(cityResult)
                    viewModel.fetchWeather(cityResult.name)
                    showCityInput = false
                },
                onDismiss = { showCityInput = false }
            )
        }
    }
}

class MobileWeatherViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MobileWeatherViewModel::class.java)) {
            val preferencesManager = PreferencesManager(application)
            val repository = WeatherRepository(preferencesManager)
            @Suppress("UNCHECKED_CAST")
            return MobileWeatherViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

