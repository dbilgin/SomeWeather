package com.omedacore.someweather.data.util

import android.util.Log
import com.omedacore.someweather.shared.R

object WeatherIconHelper {
    private const val TAG = "WeatherIconHelper"

    fun isDayTime(sunrise: Long, sunset: Long): Boolean {
        return try {
            val now = System.currentTimeMillis() / 1000 // Convert to seconds (Unix timestamp)
            now in sunrise..<sunset
        } catch (e: Exception) {
            Log.e(TAG, "Error checking day time", e)
            // Fallback to hour-based check
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            hour in 6..17
        }
    }

    fun getWeatherIconResId(conditionCode: Int, sunrise: Long? = null, sunset: Long? = null): Int {
        return when (conditionCode) {
            // Clear sky (0) - use day or night based on actual sunrise/sunset
            0 -> {
                val isDay = if (sunrise != null && sunset != null) {
                    isDayTime(sunrise, sunset)
                } else {
                    // Fallback to hour-based check
                    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    hour in 6..17
                }
                if (isDay) {
                    R.drawable.clear_day
                } else {
                    R.drawable.clear_night
                }
            }
            // Mainly clear (1), Partly cloudy (2)
            1, 2 -> R.drawable.partly_cloudy
            // Overcast (3)
            3 -> R.drawable.overcast
            // Fog and depositing rime fog (45, 48)
            45, 48 -> R.drawable.fog
            // Drizzle: Light, moderate, and dense intensity (51, 53, 55)
            // Freezing Drizzle: Light and dense intensity (56, 57)
            in 51..57 -> R.drawable.rainy
            // Rain: Slight, moderate and heavy intensity (61, 63, 65)
            // Freezing Rain: Light and heavy intensity (66, 67)
            in 61..67 -> R.drawable.rainy
            // Snow fall: Slight, moderate, and heavy intensity (71, 73, 75)
            // Snow grains (77)
            in 71..77 -> R.drawable.snowy
            // Rain showers: Slight, moderate, and violent (80, 81, 82)
            in 80..82 -> R.drawable.rainy
            // Snow showers slight and heavy (85, 86)
            85, 86 -> R.drawable.snowy
            // Thunderstorm: Slight or moderate (95)
            // Thunderstorm with slight and heavy hail (96, 99)
            95, 96, 99 -> R.drawable.rainy
            // Default to x.png for unknown conditions
            else -> R.drawable.x
        }
    }
}

