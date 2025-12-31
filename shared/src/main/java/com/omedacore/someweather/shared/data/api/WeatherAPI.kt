package com.omedacore.someweather.shared.data.api

import com.google.gson.annotations.SerializedName
import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.WeatherResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Request body for the weather endpoint
 */
data class GetWeatherRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("temperature_unit")
    val temperatureUnit: String,
    @SerializedName("windspeed_unit")
    val windspeedUnit: String,
    @SerializedName("precipitation_unit")
    val precipitationUnit: String,
    val current: String,
    val hourly: String,
    val daily: String,
    val timezone: String
)

/**
 * Request body for the city search endpoint
 */
data class SearchCityRequest(
    val query: String,
    val count: Int = 5
)

/**
 * API interface for weather endpoint
 */
interface WeatherAPI {
    @POST("weather")
    suspend fun getWeather(@Body request: GetWeatherRequest): WeatherResponse
}

/**
 * API interface for city search endpoint
 */
interface CitySearchAPI {
    @POST("search")
    suspend fun searchCity(@Body request: SearchCityRequest): GeocodingResponse
}
