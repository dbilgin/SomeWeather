package com.omedacore.someweather.shared.data.api

import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingAPI {
    @GET("search")
    suspend fun getCoordinates(
        @Query("name") cityName: String,
        @Query("count") count: Int = 1
    ): GeocodingResponse
}

interface WeatherAPI {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("temperature_unit") temperatureUnit: String,
        @Query("windspeed_unit") windspeedUnit: String,
        @Query("precipitation_unit") precipitationUnit: String,
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

