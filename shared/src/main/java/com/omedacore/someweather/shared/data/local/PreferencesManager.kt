package com.omedacore.someweather.shared.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

class PreferencesManager(private val context: Context) {
    companion object {
        private val CITY_NAME_KEY = stringPreferencesKey("city_name")
        private val UNIT_SYSTEM_KEY = stringPreferencesKey("unit_system")
        private val CACHED_WEATHER_KEY = stringPreferencesKey("cached_weather")
        private val CACHED_ASTRONOMY_KEY = stringPreferencesKey("cached_astronomy")
        private val CACHE_TIMESTAMP_KEY = longPreferencesKey("cache_timestamp")
        private val CACHED_LATITUDE_KEY = doublePreferencesKey("cached_latitude")
        private val CACHED_LONGITUDE_KEY = doublePreferencesKey("cached_longitude")
        private val CACHED_FORECAST_KEY = stringPreferencesKey("cached_forecast")
        private val FORECAST_CACHE_TIMESTAMP_KEY = longPreferencesKey("forecast_cache_timestamp")
    }

    private val gson = Gson()

    val cityName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CITY_NAME_KEY]
    }

    val unitSystem: Flow<UnitSystem?> = context.dataStore.data.map { preferences ->
        preferences[UNIT_SYSTEM_KEY]?.let { UnitSystem.valueOf(it) }
    }

    suspend fun saveCityName(cityName: String) {
        context.dataStore.edit { preferences ->
            preferences[CITY_NAME_KEY] = cityName
        }
    }

    suspend fun saveUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit { preferences ->
            preferences[UNIT_SYSTEM_KEY] = unitSystem.name
        }
    }

    suspend fun saveCachedWeather(weather: WeatherResponse) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_WEATHER_KEY] = gson.toJson(weather)
            preferences[CACHE_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun getCachedWeather(): WeatherResponse? {
        return try {
            val json = context.dataStore.data.first()[CACHED_WEATHER_KEY]
            json?.let { gson.fromJson(it, WeatherResponse::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearCachedWeather() {
        context.dataStore.edit { preferences ->
            preferences.remove(CACHED_WEATHER_KEY)
            preferences.remove(CACHE_TIMESTAMP_KEY)
        }
    }

    suspend fun getCacheTimestamp(): Long {
        return context.dataStore.data.first()[CACHE_TIMESTAMP_KEY] ?: 0L
    }

    suspend fun saveCachedCoordinates(lat: Double, lon: Double) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_LATITUDE_KEY] = lat
            preferences[CACHED_LONGITUDE_KEY] = lon
        }
    }

    suspend fun getCachedCoordinates(): Pair<Double, Double>? {
        return try {
            val prefs = context.dataStore.data.first()
            val lat = prefs[CACHED_LATITUDE_KEY]
            val lon = prefs[CACHED_LONGITUDE_KEY]
            if (lat != null && lon != null) {
                Pair(lat, lon)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSavedCity(): String? {
        return try {
            val prefs = context.dataStore.data.first()
            prefs[CITY_NAME_KEY]
        } catch (e: Exception) {
            Log.e("Failed getting city", e.message ?: "No message")
            null
        }
    }

    suspend fun getUnitSystem(): UnitSystem? {
        return try {
            val prefs = context.dataStore.data.first()
            prefs[UNIT_SYSTEM_KEY]?.let {
                try {
                    UnitSystem.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    Log.e(
                        "Failed getting unit system: IllegalArgumentException",
                        e.message ?: "No message"
                    )
                    null // Invalid enum value
                }
            }
        } catch (e: Exception) {
            Log.e("Failed getting unit system", e.message ?: "No message")
            null
        }
    }

    suspend fun saveCachedForecast(forecastJson: String) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_FORECAST_KEY] = forecastJson
            preferences[FORECAST_CACHE_TIMESTAMP_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun getCachedForecast(): String? {
        return try {
            context.dataStore.data.first()[CACHED_FORECAST_KEY]
        } catch (e: Exception) {
            null
        }
    }

    suspend fun clearCachedForecast() {
        context.dataStore.edit { preferences ->
            preferences.remove(CACHED_FORECAST_KEY)
            preferences.remove(FORECAST_CACHE_TIMESTAMP_KEY)
        }
    }

    suspend fun getForecastCacheTimestamp(): Long {
        return context.dataStore.data.first()[FORECAST_CACHE_TIMESTAMP_KEY] ?: 0L
    }
}

