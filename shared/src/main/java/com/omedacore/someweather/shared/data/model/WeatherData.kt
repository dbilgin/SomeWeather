package com.omedacore.someweather.shared.data.model

import com.google.gson.annotations.SerializedName

// Geocoding API Response
data class GeocodingResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("country")
    val country: String,
    @SerializedName("state")
    val state: String?
)

// OpenWeatherMap Weather API Response
data class WeatherResponse(
    @SerializedName("coord")
    val coord: Coordinates,
    @SerializedName("weather")
    val weather: List<WeatherCondition>,
    @SerializedName("main")
    val main: MainWeather,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("sys")
    val sys: SystemInfo,
    @SerializedName("name")
    val name: String,
    @SerializedName("visibility")
    val visibility: Int?,
    @SerializedName("clouds")
    val clouds: Clouds?,
    @SerializedName("rain")
    val rain: Precipitation?,
    @SerializedName("snow")
    val snow: Precipitation?,
    @SerializedName("dt")
    val dt: Long?,
    @SerializedName("timezone")
    val timezone: Int?
)

data class Coordinates(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)

data class MainWeather(
    @SerializedName("temp")
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double?,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    @SerializedName("pressure")
    val pressure: Int?,
    @SerializedName("sea_level")
    val seaLevel: Int?,
    @SerializedName("grnd_level")
    val grndLevel: Int?,
    @SerializedName("humidity")
    val humidity: Int
)

data class Wind(
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("deg")
    val deg: Int?,
    @SerializedName("gust")
    val gust: Double?
)

data class SystemInfo(
    @SerializedName("sunrise")
    val sunrise: Long,
    @SerializedName("sunset")
    val sunset: Long,
    @SerializedName("country")
    val country: String
)

data class WeatherCondition(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("main")
    val main: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("icon")
    val icon: String = ""
) {
    val conditionCode: Int
        get() = id
}

data class Clouds(
    @SerializedName("all")
    val all: Int
)

data class Precipitation(
    @SerializedName("1h")
    val oneHour: Double?,
    @SerializedName("3h")
    val threeHour: Double?
)

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

