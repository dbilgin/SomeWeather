package com.omedacore.someweather.data.repository

import com.omedacore.someweather.data.api.MobileRetrofitClient
import com.omedacore.someweather.data.model.ForecastResponse
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.repository.WeatherRepository

class MobileWeatherRepository(
    private val preferencesManager: PreferencesManager,
    private val apiKey: String
) {
    private val baseRepository = WeatherRepository(preferencesManager, apiKey)
    private val forecastAPI = MobileRetrofitClient.forecastAPI
    private val FORECAST_CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes

    // Delegate all base repository methods
    suspend fun getCoordinates(city: String) = baseRepository.getCoordinates(city)
    suspend fun getCurrentWeather(city: String) = baseRepository.getCurrentWeather(city)
    suspend fun getSavedCity() = baseRepository.getSavedCity()
    suspend fun clearWeatherCache() = baseRepository.clearWeatherCache()
    suspend fun saveCity(city: String) = baseRepository.saveCity(city)
    suspend fun saveCoordinates(coordinates: Coordinates) = baseRepository.saveCoordinates(coordinates)
    suspend fun getUnitSystem() = baseRepository.getUnitSystem()
    suspend fun saveUnitSystem(unitSystem: UnitSystem) = baseRepository.saveUnitSystem(unitSystem)

    suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse> {
        // Check cache first
        val cachedForecastJson = preferencesManager.getCachedForecast()
        val cacheTimestamp = preferencesManager.getForecastCacheTimestamp()
        val cacheAge = System.currentTimeMillis() - cacheTimestamp

        if (cachedForecastJson != null && cacheAge < FORECAST_CACHE_DURATION_MS) {
            // Verify cached data is for the requested coordinates
            val cachedCoords = preferencesManager.getCachedCoordinates()
            if (cachedCoords != null && 
                kotlin.math.abs(cachedCoords.first - lat) < 0.01 && 
                kotlin.math.abs(cachedCoords.second - lon) < 0.01) {
                try {
                    val cachedForecast = com.google.gson.Gson().fromJson(cachedForecastJson, ForecastResponse::class.java)
                    return Result.success(cachedForecast)
                } catch (e: Exception) {
                    // Cache deserialization failed, continue to fetch
                }
            }
        }

        // Get unit system for API call
        val unitSystem = getUnitSystem() ?: UnitSystem.METRIC
        val units = when (unitSystem) {
            UnitSystem.METRIC -> "metric"
            UnitSystem.IMPERIAL -> "imperial"
        }

        // Fetch from API
        return try {
            val response = forecastAPI.getForecast(
                lat = lat,
                lon = lon,
                units = units,
                apiKey = apiKey
            )
            // Save to cache
            preferencesManager.saveCachedForecast(com.google.gson.Gson().toJson(response))
            Result.success(response)
        } catch (e: Exception) {
            // If API fails but we have cached data (even if stale), return it
            val cachedCoords = preferencesManager.getCachedCoordinates()
            val cachedForecastJson = preferencesManager.getCachedForecast()
            if (cachedForecastJson != null && cachedCoords != null &&
                kotlin.math.abs(cachedCoords.first - lat) < 0.01 &&
                kotlin.math.abs(cachedCoords.second - lon) < 0.01) {
                try {
                    val cachedForecast = com.google.gson.Gson().fromJson(cachedForecastJson, ForecastResponse::class.java)
                    return Result.success(cachedForecast)
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            } else {
                return Result.failure(e)
            }
        }
    }

    suspend fun getForecast(city: String): Result<ForecastResponse> {
        // First, get coordinates for the city
        val coordinatesResult = getCoordinates(city)
        val (lat, lon) = coordinatesResult.getOrElse {
            return Result.failure(it)
        }
        return getForecast(lat, lon)
    }

    suspend fun clearForecastCache() {
        preferencesManager.clearCachedForecast()
    }
}
