package com.omedacore.someweather.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import com.omedacore.someweather.presentation.screens.CityInputScreen
import com.omedacore.someweather.presentation.screens.SettingsScreen
import com.omedacore.someweather.presentation.screens.UnitSystemSelectionDialog
import com.omedacore.someweather.presentation.screens.WeatherDisplayScreen
import com.omedacore.someweather.presentation.theme.SomeWeatherTheme
import com.omedacore.someweather.presentation.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels {
        WeatherViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(viewModel)
        }
    }
}

object NavRoutes {
    const val UNIT_SYSTEM = "unit_system"
    const val CITY_INPUT = "city_input"
    const val WEATHER = "weather"
    const val SETTINGS = "settings"
}

@Composable
fun WearApp(viewModel: WeatherViewModel) {
    SomeWeatherTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            TimeText()

            val navController = rememberNavController()
            val unitSystem by viewModel.unitSystem.collectAsState()
            val unitSystemLoaded by viewModel.unitSystemLoaded.collectAsState()
            val savedCity by viewModel.savedCity.collectAsState()

            // Show loading until unit system is loaded
            if (!unitSystemLoaded) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Determine initial route - calculate only once when unitSystemLoaded becomes true
                // This prevents NavHost from recomposing when savedCity changes later
                val startRoute = remember(unitSystemLoaded) {
                    when {
                        unitSystem == null -> NavRoutes.UNIT_SYSTEM
                        savedCity == null -> NavRoutes.CITY_INPUT
                        else -> NavRoutes.WEATHER
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startRoute
                ) {
                    composable(NavRoutes.UNIT_SYSTEM) {
                        UnitSystemSelectionDialog(
                            onUnitSelected = { unit ->
                                viewModel.setUnitSystem(unit)
                                navController.navigate(NavRoutes.CITY_INPUT) {
                                    popUpTo(NavRoutes.UNIT_SYSTEM) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.CITY_INPUT) {
                        CityInputScreen(
                            viewModel = viewModel,
                            onCitySelected = { cityResult ->
                                viewModel.saveCity(cityResult)
                                // WeatherDisplayScreen will fetch automatically via LaunchedEffect
                                navController.navigate(NavRoutes.WEATHER) {
                                    popUpTo(NavRoutes.CITY_INPUT) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.WEATHER) {
                        val uiState by viewModel.uiState.collectAsState()
                        WeatherDisplayScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onCityChange = {
                                navController.navigate(NavRoutes.CITY_INPUT)
                            },
                            onSettings = {
                                navController.navigate(NavRoutes.SETTINGS)
                            }
                        )
                    }

                    composable(NavRoutes.SETTINGS) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}

class WeatherViewModelFactory(
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            val preferencesManager = PreferencesManager(application)
            val repository = WeatherRepository(preferencesManager)
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
