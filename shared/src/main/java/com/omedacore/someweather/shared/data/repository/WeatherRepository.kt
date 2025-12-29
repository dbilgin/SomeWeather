package com.omedacore.someweather.shared.data.repository

import com.omedacore.someweather.shared.data.api.RetrofitClient
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse

class WeatherRepository(
    private val preferencesManager: PreferencesManager
) {
    private val weatherAPI = RetrofitClient.weatherAPI
    private val geocodingAPI = RetrofitClient.geocodingAPI
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    suspend fun getCoordinates(city: String): Result<Triple<Double, Double, String>> {
        // Check for cached coordinates first (if city name matches saved city)
        val savedCity = getSavedCity()
        val cachedCoords = preferencesManager.getCachedCoordinates()

        if (savedCity != null && savedCity.equals(city, ignoreCase = true) && cachedCoords != null) {
            // Return cached coordinates with saved city name
            return Result.success(Triple(cachedCoords.first, cachedCoords.second, savedCity))
        }

        // Fetch coordinates from Open-Meteo Geocoding API
        return try {
            val response = geocodingAPI.getCoordinates(
                cityName = city,
                count = 1
            )

            val firstResult = response.firstResult
            if (firstResult == null || response.results.isNullOrEmpty()) {
                Result.failure(Exception("Location not found"))
            } else {
                val cityName = firstResult.name
                // Cache coordinates
                preferencesManager.saveCachedCoordinates(firstResult.latitude, firstResult.longitude)
                Result.success(Triple(firstResult.latitude, firstResult.longitude, cityName))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeather(city: String): Result<WeatherResponse> {
        // First, get coordinates and proper city name from geocoding
        val coordinatesResult = getCoordinates(city)
        val (lat, lon, cityName) = coordinatesResult.getOrElse {
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
                // Ensure city name is set (in case it wasn't set before)
                if (cachedWeather.name.isEmpty()) {
                    cachedWeather.name = savedCity
                }
                return Result.success(cachedWeather)
            }
            // Cache is for different city, treat as invalid
        }

        // Get unit system for API call
        val unitSystem = getUnitSystem() ?: UnitSystem.METRIC
        val temperatureUnit = when (unitSystem) {
            UnitSystem.METRIC -> "celsius"
            UnitSystem.IMPERIAL -> "fahrenheit"
        }
        val windspeedUnit = when (unitSystem) {
            UnitSystem.METRIC -> "ms" // Use m/s to match existing formatter expectations
            UnitSystem.IMPERIAL -> "mph"
        }
        val precipitationUnit = when (unitSystem) {
            UnitSystem.METRIC -> "mm"
            UnitSystem.IMPERIAL -> "inch"
        }

        // Cache is stale, missing, or for different city - fetch from API
        return try {
            val response = weatherAPI.getWeather(
                lat = lat,
                lon = lon,
                current = "temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,pressure_msl,visibility,is_day",
                hourly = "temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,precipitation_probability",
                daily = "weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset",
                temperatureUnit = temperatureUnit,
                windspeedUnit = windspeedUnit,
                precipitationUnit = precipitationUnit,
                timezone = "auto"
            )
            
            // Set the city name from geocoding result (proper name from Open-Meteo)
            response.name = cityName
            
            // Save the proper city name
            saveCity(cityName)
            
            // Save to cache
            preferencesManager.saveCachedWeather(response)
            Result.success(response)
        } catch (e: Exception) {
            // If API fails but we have cached data (even if stale), return it
            // But only if it's for the same city
            val savedCity = getSavedCity()
            if (cachedWeather != null && savedCity != null && savedCity.equals(city, ignoreCase = true)) {
                // Ensure city name is set
                if (cachedWeather.name.isEmpty()) {
                    cachedWeather.name = savedCity
                }
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
