package com.omedacore.someweather.shared.data.repository

import com.omedacore.someweather.shared.data.api.GetWeatherRequest
import com.omedacore.someweather.shared.data.api.RetrofitClient
import com.omedacore.someweather.shared.data.api.SearchCityRequest
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse

class WeatherRepository(
    private val preferencesManager: PreferencesManager
) {
    private val weatherAPI = RetrofitClient.weatherAPI
    private val citySearchAPI = RetrofitClient.citySearchAPI
    private val LOCAL_CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes local cache

    /**
     * Search for cities by name.
     * Results are cached permanently on the backend.
     */
    suspend fun searchCity(query: String): Result<GeocodingResponse> {
        return try {
            val response = citySearchAPI.searchCity(
                SearchCityRequest(query = query, count = 5)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get weather data for a city.
     * Backend caches for 30 minutes, local cache for 5 minutes.
     */
    suspend fun getWeather(city: String): Result<WeatherResponse> {
        // Check local cache first (5 min)
        val cachedWeather = preferencesManager.getCachedWeather()
        val cacheTimestamp = preferencesManager.getCacheTimestamp()
        val cacheAge = System.currentTimeMillis() - cacheTimestamp

        if (cachedWeather != null && cacheAge < LOCAL_CACHE_DURATION_MS) {
            // Verify cached data is for the requested city
            val savedCity = getSavedCity()
            if (savedCity != null && savedCity.equals(city, ignoreCase = true)) {
                // Local cache is fresh and for the correct city
                return Result.success(cachedWeather)
            }
        }

        // Get unit system for API call
        val unitSystem = getUnitSystem() ?: UnitSystem.METRIC
        val units = when (unitSystem) {
            UnitSystem.METRIC -> "metric"
            UnitSystem.IMPERIAL -> "imperial"
        }

        // Fetch from Appwrite backend (which caches for 30 min)
        return try {
            val response = weatherAPI.getWeather(
                GetWeatherRequest(city = city, units = units)
            )

            // Save city name (comes from backend response)
            if (response.name.isNotEmpty()) {
                saveCity(response.name)
            } else {
                saveCity(city)
            }

            // Save coordinates for compatibility
            preferencesManager.saveCachedCoordinates(response.latitude, response.longitude)

            // Save to local cache
            preferencesManager.saveCachedWeather(response)
            Result.success(response)
        } catch (e: Exception) {
            // If API fails but we have cached data (even if stale), return it
            val savedCity = getSavedCity()
            if (cachedWeather != null && savedCity != null && savedCity.equals(city, ignoreCase = true)) {
                Result.success(cachedWeather)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getSavedCity(): String? {
        return preferencesManager.getSavedCity()
    }

    suspend fun clearWeatherCache() {
        preferencesManager.clearCachedWeather()
    }

    suspend fun saveCity(city: String) {
        preferencesManager.saveCityName(city)
    }

    suspend fun saveCoordinates(coordinates: Coordinates) {
        preferencesManager.saveCachedCoordinates(coordinates.lat, coordinates.lon)
    }

    suspend fun getUnitSystem(): UnitSystem? {
        return preferencesManager.getUnitSystem()
    }

    suspend fun saveUnitSystem(unitSystem: UnitSystem) {
        preferencesManager.saveUnitSystem(unitSystem)
    }
}
