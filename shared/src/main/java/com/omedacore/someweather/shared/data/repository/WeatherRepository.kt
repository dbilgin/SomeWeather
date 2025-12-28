package com.omedacore.someweather.shared.data.repository

import com.omedacore.someweather.shared.data.api.RetrofitClient
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import kotlinx.coroutines.flow.first

class WeatherRepository(
    private val preferencesManager: PreferencesManager,
    private val apiKey: String
) {
    private val weatherAPI = RetrofitClient.weatherAPI
    private val geocodingAPI = RetrofitClient.geocodingAPI
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    suspend fun getCoordinates(city: String): Result<Pair<Double, Double>> {
        // Check for cached coordinates first (if city name matches saved city)
        val savedCity = getSavedCity()
        val cachedCoords = preferencesManager.getCachedCoordinates()

        if (savedCity != null && savedCity.equals(city, ignoreCase = true) && cachedCoords != null) {
            // Return cached coordinates
            return Result.success(cachedCoords)
        }

        // Fetch coordinates from Geocoding API
        return try {
            val response = geocodingAPI.getCoordinates(
                cityName = city,
                limit = 1,
                apiKey = apiKey
            )

            if (response.isEmpty()) {
                Result.failure(Exception("Location not found"))
            } else {
                val geocodingResult = response.first()
                val coords = Pair(geocodingResult.lat, geocodingResult.lon)

                Result.success(coords)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeather(city: String): Result<WeatherResponse> {
        // First, get coordinates for the city
        val coordinatesResult = getCoordinates(city)
        val (lat, lon) = coordinatesResult.getOrElse {
            return Result.failure(it)
        }

        // Check cache first
        val cachedWeather = preferencesManager.getCachedWeather()
        val cacheTimestamp = preferencesManager.getCacheTimestamp()
        val cacheAge = System.currentTimeMillis() - cacheTimestamp

        if (cachedWeather != null && cacheAge < CACHE_DURATION_MS) {
            // Verify cached data is for the requested city
            val savedCity = getSavedCity()
            if (savedCity != null && savedCity.equals(city, ignoreCase = true)) {
                // Cache is fresh and for the correct city, return cached data
                return Result.success(cachedWeather)
            }
            // Cache is for different city, treat as invalid
        }

        // Get unit system for API call
        val unitSystem = getUnitSystem() ?: UnitSystem.METRIC
        val units = when (unitSystem) {
            UnitSystem.METRIC -> "metric"
            UnitSystem.IMPERIAL -> "imperial"
        }

        // Cache is stale, missing, or for different city - fetch from API
        return try {
            val response = weatherAPI.getCurrentWeather(
                lat = lat,
                lon = lon,
                units = units,
                apiKey = apiKey
            )
            // Save to cache
            preferencesManager.saveCachedWeather(response)
            Result.success(response)
        } catch (e: Exception) {
            // If API fails but we have cached data (even if stale), return it
            // But only if it's for the same city
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
        return preferencesManager.clearCachedWeather()
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

