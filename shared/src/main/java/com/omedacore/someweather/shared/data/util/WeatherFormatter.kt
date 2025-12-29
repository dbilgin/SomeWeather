package com.omedacore.someweather.shared.data.util

import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherCondition
import com.omedacore.someweather.shared.data.model.WeatherResponse
import kotlin.math.roundToInt
import java.util.Locale

object WeatherFormatter {
    /**
     * Formats temperature with unit based on the unit system.
     * @param weather The weather response containing temperature data
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return Formatted temperature string like "22°C" or "72°F"
     */
    fun formatTemperature(weather: WeatherResponse, unitSystem: UnitSystem): String {
        val temp = weather.main.temp.roundToInt()
        return if (unitSystem == UnitSystem.METRIC) {
            "${temp}°C"
        } else {
            "${temp}°F"
        }
    }

    /**
     * Formats temperature value with unit based on the unit system.
     * @param temp The temperature value
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return Formatted temperature string like "22°C" or "72°F"
     */
    fun formatTemperature(temp: Double, unitSystem: UnitSystem): String {
        val tempInt = temp.roundToInt()
        return if (unitSystem == UnitSystem.METRIC) {
            "${tempInt}°C"
        } else {
            "${tempInt}°F"
        }
    }

    /**
     * Formats wind speed with unit and locale-aware formatting.
     * Open-Meteo API returns correct units based on windspeed_unit parameter (m/s for metric, mph for imperial).
     * @param weather The weather response containing wind data
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @param locale The locale for number formatting
     * @return Formatted wind speed string like "10.50 m/s" or "6.52 mph"
     */
    fun formatWindSpeed(weather: WeatherResponse, unitSystem: UnitSystem, locale: Locale): String {
        return if (unitSystem == UnitSystem.METRIC) {
            // Open-Meteo returns m/s for metric (when windspeed_unit=ms)
            String.format(locale, "%.2f m/s", weather.wind.speed)
        } else {
            // Open-Meteo returns mph for imperial
            String.format(locale, "%.2f mph", weather.wind.speed)
        }
    }

    /**
     * Formats wind speed with unit and locale-aware formatting.
     * Open-Meteo API returns correct units based on windspeed_unit parameter (m/s for metric, mph for imperial).
     * @param speed The wind speed (already in correct units from API)
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @param locale The locale for number formatting
     * @return Formatted wind speed string like "10.50 m/s" or "6.52 mph"
     */
    fun formatWindSpeed(speed: Double, unitSystem: UnitSystem, locale: Locale): String {
        return if (unitSystem == UnitSystem.METRIC) {
            // Open-Meteo returns m/s for metric (when windspeed_unit=ms)
            String.format(locale, "%.2f m/s", speed)
        } else {
            // Open-Meteo returns mph for imperial
            String.format(locale, "%.2f mph", speed)
        }
    }

    /**
     * Formats "feels like" temperature value with unit based on the unit system.
     * @param feelsLike The feels like temperature value
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return Formatted temperature string like "22°C" or "72°F"
     */
    fun formatFeelsLikeTemperature(feelsLike: Double?, unitSystem: UnitSystem): String? {
        if (feelsLike == null) {
            return null
        }
        val tempInt = feelsLike.roundToInt()
        return if (unitSystem == UnitSystem.METRIC) {
            "${tempInt}°C"
        } else {
            "${tempInt}°F"
        }
    }

    /**
     * Formats atmospheric pressure.
     * @param pressure The pressure value in hPa
     * @return Formatted pressure string like "1015 hPa"
     */
    fun formatPressure(pressure: Int?): String? {
        return pressure?.let { "$it hPa" }
    }

    /**
     * Formats visibility distance.
     * Open-Meteo API always returns visibility in meters regardless of unit parameters.
     * @param visibility The visibility in meters
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @param locale The locale for number formatting
     * @return Formatted visibility string like "10.0 km" or "6.2 mi"
     */
    fun formatVisibility(visibility: Int?, unitSystem: UnitSystem, locale: Locale): String? {
        return visibility?.let { vis ->
            if (unitSystem == UnitSystem.METRIC) {
                // Convert meters to km
                val km = vis / 1000.0
                String.format(locale, "%.1f km", km)
            } else {
                // Convert meters to miles
                val miles = vis / 1609.34
                String.format(locale, "%.1f mi", miles)
            }
        }
    }

    /**
     * Formats wind gust speed with unit and locale-aware formatting.
     * Open-Meteo API returns correct units based on windspeed_unit parameter (m/s for metric, mph for imperial).
     * @param gust The wind gust speed (already in correct units from API)
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @param locale The locale for number formatting
     * @return Formatted wind gust string like "15.50 m/s" or "9.63 mph", or null if not available
     */
    fun formatWindGust(gust: Double?, unitSystem: UnitSystem, locale: Locale): String? {
        return gust?.let { g ->
            if (unitSystem == UnitSystem.METRIC) {
                // Open-Meteo returns m/s for metric (when windspeed_unit=ms)
                String.format(locale, "%.2f m/s", g)
            } else {
                // Open-Meteo returns mph for imperial
                String.format(locale, "%.2f mph", g)
            }
        }
    }

    /**
     * Formats cloudiness percentage.
     * @param cloudiness The cloudiness percentage (0-100)
     * @return Formatted cloudiness string like "75%"
     */
    fun formatCloudiness(cloudiness: Int?): String? {
        return cloudiness?.let { "$it%" }
    }

    fun formatPop(pop: Double?): String? {
        if (pop == 0.0) {
            return null
        }
        return pop?.let { "$it%" }
    }

    /**
     * Formats precipitation amount.
     * @param precipitation The precipitation data
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @param locale The locale for number formatting
     * @return Formatted precipitation string like "2.5 mm" or "0.1 in", or null if not available
     */
    fun formatPrecipitation(
        precipitation: com.omedacore.someweather.shared.data.model.Precipitation?,
        unitSystem: UnitSystem,
        locale: Locale
    ): String? {
        return precipitation?.let { prec ->
            val amount = prec.threeHour ?: prec.oneHour
            amount?.let { amt ->
                if (unitSystem == UnitSystem.METRIC) {
                    // mm for metric
                    String.format(locale, "%.1f mm", amt)
                } else {
                    // Convert mm to inches
                    val inches = amt / 25.4
                    String.format(locale, "%.2f in", inches)
                }
            }
        }
    }

    /**
     * Formats wind direction from degrees to cardinal direction.
     * @param deg The wind direction in degrees (0-360)
     * @return Cardinal direction string like "N", "NE", "E", etc.
     */
    fun formatWindDirection(deg: Int?): String? {
        return deg?.let { direction ->
            val directions = arrayOf(
                "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
            )
            val index = ((direction + 11.25) / 22.5).toInt() % 16
            directions[index]
        }
    }

    /**
     * Extracts weather condition from the weather response with fallback.
     * @param weather The weather response
     * @return WeatherCondition object, or empty condition if none found
     */
    fun getCondition(weather: WeatherResponse): WeatherCondition {
        return weather.weather.firstOrNull() ?: WeatherCondition()
    }
}

