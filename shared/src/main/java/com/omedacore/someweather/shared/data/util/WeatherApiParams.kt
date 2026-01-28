package com.omedacore.someweather.shared.data.util

import java.util.TimeZone

/**
 * Utility object for building weather API parameter strings
 */
object WeatherApiParams {
    /**
     * Returns the comma-separated list of current weather parameters
     */
    fun getCurrentParams(): String {
        return "temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,pressure_msl,visibility,is_day"
    }

    /**
     * Returns the comma-separated list of hourly forecast parameters
     */
    fun getHourlyParams(): String {
        return "temperature_2m,relative_humidity_2m,weathercode,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,precipitation_probability"
    }

    /**
     * Returns the comma-separated list of daily forecast parameters
     */
    fun getDailyParams(): String {
        return "weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset"
    }

    /**
     * Returns the timezone parameter using the device's IANA timezone identifier
     * @return The device's timezone ID (e.g., "Europe/Berlin", "America/New_York")
     */
    fun getTimezone(): String {
        return TimeZone.getDefault().id
    }
}

