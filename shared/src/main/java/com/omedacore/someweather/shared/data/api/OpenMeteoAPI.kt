package com.omedacore.someweather.shared.data.api

import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo Weather API interface
 * Uses GET requests with query parameters
 */
interface OpenMeteoWeatherAPI {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("temperature_unit") temperatureUnit: String,
        @Query("windspeed_unit") windspeedUnit: String,
        @Query("precipitation_unit") precipitationUnit: String,
        @Query("timezone") timezone: String
    ): WeatherResponse
}

/**
 * Open-Meteo Geocoding API interface
 * Uses GET requests with query parameters
 */
interface OpenMeteoGeocodingAPI {
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 5
    ): GeocodingResponse
}

