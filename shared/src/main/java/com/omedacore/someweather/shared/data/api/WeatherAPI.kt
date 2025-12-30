package com.omedacore.someweather.shared.data.api

import com.omedacore.someweather.shared.data.model.GeocodingResponse
import com.omedacore.someweather.shared.data.model.WeatherResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Request body for the weather endpoint
 */
data class GetWeatherRequest(
    val city: String,
    val units: String = "metric"
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
