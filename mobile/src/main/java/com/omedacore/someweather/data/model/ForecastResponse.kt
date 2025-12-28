package com.omedacore.someweather.data.model

import com.google.gson.annotations.SerializedName
import com.omedacore.someweather.shared.data.model.Clouds
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.MainWeather
import com.omedacore.someweather.shared.data.model.Precipitation
import com.omedacore.someweather.shared.data.model.SystemInfo
import com.omedacore.someweather.shared.data.model.WeatherCondition
import com.omedacore.someweather.shared.data.model.Wind

// Forecast API Response (5-day/3-hour forecast)
data class ForecastResponse(
    @SerializedName("cod")
    val cod: String,
    @SerializedName("message")
    val message: Int,
    @SerializedName("cnt")
    val cnt: Int,
    @SerializedName("list")
    val list: List<ForecastItem>,
    @SerializedName("city")
    val city: ForecastCity
)

data class ForecastItem(
    @SerializedName("dt")
    val dt: Long, // Unix timestamp
    @SerializedName("main")
    val main: MainWeather,
    @SerializedName("weather")
    val weather: List<WeatherCondition>,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("visibility")
    val visibility: Int?,
    @SerializedName("pop")
    val pop: Double?, // Probability of precipitation
    @SerializedName("dt_txt")
    val dtTxt: String, // Date/time string
    @SerializedName("clouds")
    val clouds: Clouds?,
    @SerializedName("rain")
    val rain: Precipitation?,
    @SerializedName("snow")
    val snow: Precipitation?
)

data class ForecastCity(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("coord")
    val coord: Coordinates,
    @SerializedName("country")
    val country: String,
    @SerializedName("population")
    val population: Int?,
    @SerializedName("timezone")
    val timezone: Int,
    @SerializedName("sunrise")
    val sunrise: Long,
    @SerializedName("sunset")
    val sunset: Long
)

