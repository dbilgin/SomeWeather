package com.omedacore.someweather.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omedacore.someweather.data.model.ForecastResponse
import com.omedacore.someweather.data.repository.MobileWeatherRepository
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Initial : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class ForecastUiState {
    object Initial : ForecastUiState()
    object Loading : ForecastUiState()
    data class Success(val forecast: ForecastResponse) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

class MobileWeatherViewModel(
    application: Application,
    private val repository: MobileWeatherRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _forecastState = MutableStateFlow<ForecastUiState>(ForecastUiState.Initial)
    val forecastState: StateFlow<ForecastUiState> = _forecastState.asStateFlow()

    private val _savedCity = MutableStateFlow<String?>(null)
    val savedCity: StateFlow<String?> = _savedCity.asStateFlow()

    private val _unitSystem = MutableStateFlow<UnitSystem?>(null)
    val unitSystem: StateFlow<UnitSystem?> = _unitSystem.asStateFlow()

    private val _unitSystemLoaded = MutableStateFlow(false)
    val unitSystemLoaded: StateFlow<Boolean> = _unitSystemLoaded.asStateFlow()

    init {
        loadSavedCity()
        loadUnitSystem()
    }

    private fun loadSavedCity() {
        viewModelScope.launch {
            repository.getSavedCity()?.let { city ->
                _savedCity.value = city
            } ?: run {
                _savedCity.value = null
            }
        }
    }

    private fun loadUnitSystem() {
        viewModelScope.launch {
            repository.getUnitSystem()?.let { unit ->
                _unitSystem.value = unit
            } ?: run {
                _unitSystem.value = null
            }
            _unitSystemLoaded.value = true
        }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            repository.saveUnitSystem(unitSystem)
            repository.clearWeatherCache()
            repository.clearForecastCache()
            _unitSystem.value = unitSystem
            // Refetch weather and forecast with new unit system
            _savedCity.value?.let { city ->
                fetchWeather(city)
                fetchForecast(city)
            }
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val weatherResult = repository.getCurrentWeather(city)

            weatherResult.fold(
                onSuccess = { weather ->
                    repository.saveCity(city)
                    repository.saveCoordinates(weather.coord)
                    _uiState.value = WeatherUiState.Success(weather)
                    // Also fetch forecast when weather is fetched
                    fetchForecast(city)
                },
                onFailure = {
                    _uiState.value = WeatherUiState.Error(
                        "An error occurred while fetching weather data"
                    )
                }
            )
        }
    }

    fun fetchForecast(city: String) {
        viewModelScope.launch {
            _forecastState.value = ForecastUiState.Loading

            val forecastResult = repository.getForecast(city)

            forecastResult.fold(
                onSuccess = { forecast ->
                    _forecastState.value = ForecastUiState.Success(forecast)
                },
                onFailure = {
                    _forecastState.value = ForecastUiState.Error(
                        "An error occurred while fetching forecast data"
                    )
                }
            )
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            repository.clearWeatherCache()
            repository.clearForecastCache()
            _savedCity.value?.let { city ->
                fetchWeather(city)
                fetchForecast(city)
            }
        }
    }

    fun saveCity(city: String) {
        viewModelScope.launch {
            _savedCity.value = city
        }
    }
}

