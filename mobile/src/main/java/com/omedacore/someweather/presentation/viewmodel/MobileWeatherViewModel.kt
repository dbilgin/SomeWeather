package com.omedacore.someweather.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.repository.WeatherRepository
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

class MobileWeatherViewModel(
    application: Application,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

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
            _unitSystem.value = unitSystem
            // Refetch weather with new unit system
            _savedCity.value?.let { city ->
                fetchWeather(city)
            }
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val weatherResult = repository.getWeather(city)

            weatherResult.fold(
                onSuccess = { weather ->
                    repository.saveCity(city)
                    repository.saveCoordinates(weather.coord)
                    _uiState.value = WeatherUiState.Success(weather)
                },
                onFailure = {
                    _uiState.value = WeatherUiState.Error(
                        "An error occurred while fetching weather data"
                    )
                }
            )
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            repository.clearWeatherCache()
            _savedCity.value?.let { city ->
                fetchWeather(city)
            }
        }
    }

    fun saveCity(city: String) {
        viewModelScope.launch {
            _savedCity.value = city
        }
    }
}
