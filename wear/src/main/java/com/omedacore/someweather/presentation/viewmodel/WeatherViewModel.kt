package com.omedacore.someweather.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omedacore.someweather.complication.ComplicationUpdateHelper
import com.omedacore.someweather.shared.data.model.GeocodingResult
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

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<GeocodingResult>) : SearchState()
    object NoResults : SearchState()
    data class Error(val message: String) : SearchState()
}

class WeatherViewModel(
    application: Application,
    private val repository: WeatherRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _savedCity = MutableStateFlow<String?>(null)
    val savedCity: StateFlow<String?> = _savedCity.asStateFlow()

    private val _savedCityDisplay = MutableStateFlow<String?>(null)
    val savedCityDisplay: StateFlow<String?> = _savedCityDisplay.asStateFlow()

    private val _unitSystem = MutableStateFlow<UnitSystem?>(null)
    val unitSystem: StateFlow<UnitSystem?> = _unitSystem.asStateFlow()

    private val _unitSystemLoaded = MutableStateFlow(false)
    val unitSystemLoaded: StateFlow<Boolean> = _unitSystemLoaded.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    init {
        loadSavedCity()
        loadUnitSystem()
    }

    private fun loadSavedCity() {
        viewModelScope.launch {
            repository.getSavedCity()?.let { city ->
                _savedCity.value = city
                // WeatherDisplayScreen will fetch weather when it observes savedCity
            } ?: run {
                // Explicitly set to null if no city is saved
                _savedCity.value = null
            }
            repository.getSavedCityDisplay()?.let { display ->
                _savedCityDisplay.value = display
            } ?: run {
                _savedCityDisplay.value = _savedCity.value
            }
        }
    }

    private fun loadUnitSystem() {
        viewModelScope.launch {
            repository.getUnitSystem()?.let { unit ->
                _unitSystem.value = unit
            } ?: run {
                // Explicitly keep as null if no unit system is saved
                _unitSystem.value = null
            }
            _unitSystemLoaded.value = true
        }
    }

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            repository.saveUnitSystem(unitSystem)
            // Clear cache since cached data is in the old unit system
            repository.clearWeatherCache()
            _unitSystem.value = unitSystem
            // Refetch weather with new unit system
            _savedCity.value?.let { city ->
                fetchWeather(city)
            }
            // Trigger complication update when unit system changes
            // Note: Tiles refresh automatically when the user views them
            ComplicationUpdateHelper.requestUpdate(getApplication())
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            // Fetch weather data (sunrise/sunset included in response)
            val weatherResult = repository.getWeather(city)

            weatherResult.fold(
                onSuccess = { weather ->
                    repository.saveCity(city)
                    repository.saveCoordinates(weather.coord)

                    _uiState.value = WeatherUiState.Success(weather)
                    // Trigger complication update when weather is successfully fetched
                    ComplicationUpdateHelper.requestUpdate(getApplication())
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

    fun saveCity(city: GeocodingResult) {
        viewModelScope.launch {
            _savedCity.value = city.name
            _savedCityDisplay.value = city.displayLocation
            repository.saveCityDisplay(city.displayLocation)
            // Immediately fetch weather with new city
            fetchWeather(city.name)
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading

            repository.searchCity(query).fold(
                onSuccess = { response ->
                    val results = response.results
                    if (results.isNullOrEmpty()) {
                        _searchState.value = SearchState.NoResults
                    } else {
                        _searchState.value = SearchState.Success(results)
                    }
                },
                onFailure = {
                    _searchState.value = SearchState.Error("Failed to search for cities")
                }
            )
        }
    }

    fun resetSearchState() {
        _searchState.value = SearchState.Idle
    }
}
