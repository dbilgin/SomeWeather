package com.omedacore.someweather.shared.data.model

import com.google.gson.annotations.SerializedName

// Open-Meteo Geocoding API Response
data class GeocodingResponse(
    @SerializedName("results")
    val results: List<GeocodingResult>?,
    @SerializedName("generationtime_ms")
    val generationtimeMs: Double?
) {
    // Helper to get first result for compatibility
    val firstResult: GeocodingResult?
        get() = results?.firstOrNull()
}

data class GeocodingResult(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("elevation")
    val elevation: Double?,
    @SerializedName("country")
    val country: String,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("admin1")
    val admin1: String?,
    @SerializedName("timezone")
    val timezone: String?,
    @SerializedName("population")
    val population: Int?
) {
    // Compatibility properties for existing code
    val lat: Double get() = latitude
    val lon: Double get() = longitude
    
    // Display helper: shows "City, State, Country" or "City, Country"
    val displayLocation: String
        get() = if (!admin1.isNullOrBlank()) "$name, $admin1, $country" else "$name, $country"
}

// Open-Meteo Forecast API Response (includes current, hourly, and daily data)
data class WeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("current")
    val current: CurrentWeatherData,
    @SerializedName("hourly")
    val hourly: HourlyWeatherData?,
    @SerializedName("daily")
    val daily: DailyWeatherData,
    @SerializedName("timezone")
    val timezone: String?,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String?,
    @SerializedName("elevation")
    val elevation: Double?
) {
    // Compatibility properties for existing code
    val coord: Coordinates
        get() = Coordinates(latitude, longitude)
    
    val weather: List<WeatherCondition>
        get() = listOf(WeatherCondition(
            id = current.weathercode,
            main = WmoWeatherCodes.getWeatherMain(current.weathercode),
            description = WmoWeatherCodes.getWeatherDescription(current.weathercode),
            icon = ""
        ))
    
    val main: MainWeather
        get() = MainWeather(
            temp = current.temperature2m,
            feelsLike = current.temperature2m, // Open-Meteo doesn't provide feels_like in current
            tempMin = daily.temperature2mMin?.firstOrNull() ?: current.temperature2m,
            tempMax = daily.temperature2mMax?.firstOrNull() ?: current.temperature2m,
            pressure = current.pressureMsl?.toInt(),
            seaLevel = current.pressureMsl?.toInt(),
            grndLevel = null,
            humidity = current.relativeHumidity2m?.toInt() ?: 0
        )
    
    val wind: Wind
        get() = Wind(
            speed = current.windSpeed10m ?: 0.0, // Already in m/s for metric, mph for imperial
            deg = current.windDirection10m,
            gust = current.windGusts10m // Already in m/s for metric, mph for imperial
        )
    
    val sys: SystemInfo
        get() = SystemInfo(
            sunrise = WmoWeatherCodes.parseSunriseSunset(daily.sunrise?.firstOrNull()),
            sunset = WmoWeatherCodes.parseSunriseSunset(daily.sunset?.firstOrNull()),
            country = "" // Not available in Open-Meteo current response
        )
    
    var name: String = "" // Set from geocoding result
    
    val visibility: Int?
        get() = current.visibility?.toInt()
    
    val clouds: Clouds? = null // Open-Meteo doesn't provide cloud cover in current
    
    val rain: Precipitation? = null // Precipitation is in hourly/daily
    
    val snow: Precipitation? = null
    
    val dt: Long?
        get() = WmoWeatherCodes.parseDateTime(current.time)

    // Forecast list from hourly data (for UI compatibility)
    val forecastList: List<ForecastItem>
        get() {
            val hourlyData = hourly ?: return emptyList()
            val times = hourlyData.time ?: return emptyList()
            val temps = hourlyData.temperature2m ?: return emptyList()
            val humidity = hourlyData.relativeHumidity2m ?: emptyList()
            val weathercodes = hourlyData.weathercode ?: emptyList()
            val windSpeed = hourlyData.windSpeed10m ?: emptyList()
            val windDirection = hourlyData.windDirection10m ?: emptyList()
            val windGusts = hourlyData.windGusts10m ?: emptyList()
            val precipitation = hourlyData.precipitation ?: emptyList()
            val precipitationProbability = hourlyData.precipitationProbability ?: emptyList()
            
            return times.mapIndexed { index, time ->
                ForecastItem(
                    dt = WmoWeatherCodes.parseDateTime(time) ?: 0,
                    dtTxt = time,
                    main = MainWeather(
                        temp = temps.getOrNull(index) ?: 0.0,
                        feelsLike = temps.getOrNull(index),
                        tempMin = temps.getOrNull(index) ?: 0.0,
                        tempMax = temps.getOrNull(index) ?: 0.0,
                        pressure = null,
                        seaLevel = null,
                        grndLevel = null,
                        humidity = humidity.getOrNull(index)?.toInt() ?: 0
                    ),
                    weather = listOf(WeatherCondition(
                        id = weathercodes.getOrNull(index) ?: 0,
                        main = WmoWeatherCodes.getWeatherMain(weathercodes.getOrNull(index) ?: 0),
                        description = WmoWeatherCodes.getWeatherDescription(weathercodes.getOrNull(index) ?: 0),
                        icon = ""
                    )),
                    wind = Wind(
                        speed = windSpeed.getOrNull(index) ?: 0.0,
                        deg = windDirection.getOrNull(index),
                        gust = windGusts.getOrNull(index)
                    ),
                    visibility = null,
                    pop = precipitationProbability.getOrNull(index)?.toDouble(),
                    clouds = null,
                    rain = if (precipitation.getOrNull(index) != null && precipitation.getOrNull(index)!! > 0) {
                        Precipitation(oneHour = precipitation.getOrNull(index), threeHour = null)
                    } else null,
                    snow = null
                )
            }
        }

    val forecastCity: ForecastCity
        get() = ForecastCity(
            id = 0,
            name = name,
            coord = Coordinates(latitude, longitude),
            country = "",
            population = null,
            timezone = 0,
            sunrise = sys.sunrise,
            sunset = sys.sunset
        )
}

data class CurrentWeatherData(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temperature2m: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Double?,
    @SerializedName("weathercode")
    val weathercode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double?,
    @SerializedName("wind_direction_10m")
    val windDirection10m: Int?,
    @SerializedName("wind_gusts_10m")
    val windGusts10m: Double?,
    @SerializedName("pressure_msl")
    val pressureMsl: Double?,
    @SerializedName("visibility")
    val visibility: Double?,
    @SerializedName("is_day")
    val isDay: Int?
)

data class DailyWeatherData(
    @SerializedName("time")
    val time: List<String>?,
    @SerializedName("weathercode")
    val weathercode: List<Int>?,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double>?,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double>?,
    @SerializedName("sunrise")
    val sunrise: List<String>?,
    @SerializedName("sunset")
    val sunset: List<String>?
)

data class HourlyWeatherData(
    @SerializedName("time")
    val time: List<String>?,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>?,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: List<Double>?,
    @SerializedName("weathercode")
    val weathercode: List<Int>?,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: List<Double>?,
    @SerializedName("wind_direction_10m")
    val windDirection10m: List<Int>?,
    @SerializedName("wind_gusts_10m")
    val windGusts10m: List<Double>?,
    @SerializedName("precipitation")
    val precipitation: List<Double>?,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>?
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

// Shared utility object for WMO weather code interpretation
object WmoWeatherCodes {
    fun getWeatherMain(weathercode: Int): String {
        return when (weathercode) {
            0 -> "Clear"
            in 1..3 -> "Clouds"
            45, 48 -> "Fog"
            in 51..57 -> "Drizzle"
            in 61..67 -> "Rain"
            in 71..77 -> "Snow"
            in 80..82 -> "Rain"
            in 85..86 -> "Snow"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }
    
    fun getWeatherDescription(weathercode: Int): String {
        return when (weathercode) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45 -> "Fog"
            48 -> "Depositing rime fog"
            51 -> "Drizzle: Light intensity"
            53 -> "Drizzle: Moderate intensity"
            55 -> "Drizzle: Dense intensity"
            56 -> "Freezing Drizzle: Light intensity"
            57 -> "Freezing Drizzle: Dense intensity"
            61 -> "Rain: Slight intensity"
            63 -> "Rain: Moderate intensity"
            65 -> "Rain: Heavy intensity"
            66 -> "Freezing Rain: Light intensity"
            67 -> "Freezing Rain: Heavy intensity"
            71 -> "Snow fall: Slight intensity"
            73 -> "Snow fall: Moderate intensity"
            75 -> "Snow fall: Heavy intensity"
            77 -> "Snow grains"
            80 -> "Rain showers: Slight"
            81 -> "Rain showers: Moderate"
            82 -> "Rain showers: Violent"
            85 -> "Snow showers: Slight"
            86 -> "Snow showers: Heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96 -> "Thunderstorm with slight hail"
            99 -> "Thunderstorm with heavy hail"
            else -> "Unknown"
        }
    }
    
    fun parseSunriseSunset(timeString: String?): Long {
        if (timeString == null) return 0
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
            format.parse(timeString)?.time?.div(1000) ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun parseDateTime(timeString: String?): Long? {
        if (timeString == null) return null
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
            format.parse(timeString)?.time?.div(1000)
        } catch (e: Exception) {
            null
        }
    }
}


data class ForecastItem(
    val dt: Long,
    val dtTxt: String,
    val main: MainWeather,
    val weather: List<WeatherCondition>,
    val wind: Wind,
    val visibility: Int?,
    val pop: Double?,
    val clouds: Clouds?,
    val rain: Precipitation?,
    val snow: Precipitation?
)

data class ForecastCity(
    val id: Int,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int?,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

