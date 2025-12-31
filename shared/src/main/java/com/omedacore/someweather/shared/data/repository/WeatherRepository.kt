package com.omedacore.someweather.shared.data.repository

import com.omedacore.someweather.shared.data.api.GetWeatherRequest
import com.omedacore.someweather.shared.data.api.RetrofitClient
import com.omedacore.someweather.shared.data.api.SearchCityRequest
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.util.WeatherApiParams
import com.omedacore.someweather.shared.data.util.UnitConverter

class WeatherRepository(
    private val preferencesManager: PreferencesManager,
    private val useOpenMeteo: Boolean = false
) {
    private val weatherAPI by lazy { RetrofitClient.weatherAPI }
    private val citySearchAPI by lazy { RetrofitClient.citySearchAPI }
    private val openMeteoWeatherAPI by lazy { RetrofitClient.openMeteoWeatherAPI }
    private val openMeteoGeocodingAPI by lazy { RetrofitClient.openMeteoGeocodingAPI }
    private val LOCAL_CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes local cache

    /**
     * Search for cities by name.
     * Results are cached permanently on the backend (if using backend mode).
     */
    suspend fun searchCity(query: String): Result<GeocodingResponse> {
        return try {
            val response = if (useOpenMeteo) {
                openMeteoGeocodingAPI.searchCity(name = query, count = 5)
            } else {
                citySearchAPI.searchCity(
                    SearchCityRequest(query = query, count = 5)
                )
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get weather data using coordinates.
     * Backend caches for 30 minutes, local cache for 5 minutes.
     */
    suspend fun getWeatherWithCoordinates(lat: Double, lon: Double, unitSystem: UnitSystem): Result<WeatherResponse> {
        // Check local cache first (5 min)
        val cachedWeather = preferencesManager.getCachedWeather()
        val cacheTimestamp = preferencesManager.getCacheTimestamp()
        val cacheAge = System.currentTimeMillis() - cacheTimestamp

        if (cachedWeather != null && cacheAge < LOCAL_CACHE_DURATION_MS) {
            // Verify cached data is for the requested coordinates
            val cachedCoords = preferencesManager.getCachedCoordinates()
            if (cachedCoords != null) {
                val (cachedLat, cachedLon) = cachedCoords
                // Check if coordinates match (within small tolerance)
                if (kotlin.math.abs(cachedLat - lat) < 0.001 && kotlin.math.abs(cachedLon - lon) < 0.001) {
                    // Local cache is fresh and for the correct coordinates
                    return Result.success(cachedWeather)
                }
            }
        }

        // Convert unit system to Open-Meteo unit parameters
        val temperatureUnit = UnitConverter.convertTemperatureUnit(unitSystem)
        val windspeedUnit = UnitConverter.convertWindspeedUnit(unitSystem)
        val precipitationUnit = UnitConverter.convertPrecipitationUnit(unitSystem)

        // Get weather API parameter strings
        val currentParams = WeatherApiParams.getCurrentParams()
        val hourlyParams = WeatherApiParams.getHourlyParams()
        val dailyParams = WeatherApiParams.getDailyParams()
        val timezone = WeatherApiParams.getTimezone()

        // Fetch from API (backend caches for 30 min, Open-Meteo has no backend cache)
        return try {
            val response = if (useOpenMeteo) {
                openMeteoWeatherAPI.getWeather(
                    latitude = lat,
                    longitude = lon,
                    current = currentParams,
                    hourly = hourlyParams,
                    daily = dailyParams,
                    temperatureUnit = temperatureUnit,
                    windspeedUnit = windspeedUnit,
                    precipitationUnit = precipitationUnit,
                    timezone = timezone
                )
            } else {
                weatherAPI.getWeather(
                    GetWeatherRequest(
                        latitude = lat,
                        longitude = lon,
                        temperatureUnit = temperatureUnit,
                        windspeedUnit = windspeedUnit,
                        precipitationUnit = precipitationUnit,
                        current = currentParams,
                        hourly = hourlyParams,
                        daily = dailyParams,
                        timezone = timezone
                    )
                )
            }

            // Save coordinates for cache verification
            preferencesManager.saveCachedCoordinates(response.latitude, response.longitude)

            // Save to local cache
            preferencesManager.saveCachedWeather(response)
            Result.success(response)
        } catch (e: Exception) {
            // If API fails but we have cached data (even if stale), return it
            val cachedCoords = preferencesManager.getCachedCoordinates()
            if (cachedWeather != null && cachedCoords != null) {
                val (cachedLat, cachedLon) = cachedCoords
                if (kotlin.math.abs(cachedLat - lat) < 0.001 && kotlin.math.abs(cachedLon - lon) < 0.001) {
                    Result.success(cachedWeather)
                } else {
                    Result.failure(e)
                }
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getSavedCity(): String? {
        return preferencesManager.getSavedCity()
    }

    suspend fun getSavedCityDisplay(): String? {
        return preferencesManager.getSavedCityDisplay()
    }

    suspend fun clearWeatherCache() {
        preferencesManager.clearCachedWeather()
    }

    suspend fun saveCity(city: String) {
        preferencesManager.saveCityName(city)
    }

    suspend fun saveCityDisplay(display: String) {
        preferencesManager.saveCityDisplay(display)
    }

    suspend fun saveCoordinates(coordinates: Coordinates) {
        preferencesManager.saveCachedCoordinates(coordinates.lat, coordinates.lon)
    }

    suspend fun saveSelectedCityCoordinates(lat: Double, lon: Double) {
        preferencesManager.saveSelectedCityCoordinates(lat, lon)
    }

    suspend fun getSelectedCityCoordinates(): Pair<Double, Double>? {
        return preferencesManager.getSelectedCityCoordinates()
    }

    suspend fun getUnitSystem(): UnitSystem? {
        return preferencesManager.getUnitSystem()
    }

    suspend fun saveUnitSystem(unitSystem: UnitSystem) {
        preferencesManager.saveUnitSystem(unitSystem)
    }
}
