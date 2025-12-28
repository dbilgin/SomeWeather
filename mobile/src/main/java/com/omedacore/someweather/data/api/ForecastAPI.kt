package com.omedacore.someweather.data.api

import com.omedacore.someweather.data.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastAPI {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): ForecastResponse
}
