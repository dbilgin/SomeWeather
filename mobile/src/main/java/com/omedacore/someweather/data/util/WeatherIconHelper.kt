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
            // Sunny/Clear (800) - use day or night based on actual sunrise/sunset
            800 -> {
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
            // Clouds: 801-804
            801 -> R.drawable.partly_cloudy
            802 -> R.drawable.partly_cloudy
            803 -> R.drawable.cloudy
            804 -> R.drawable.overcast
            // Thunderstorm: 200-232
            in 200..232 -> R.drawable.rainy
            // Drizzle: 300-321
            in 300..321 -> R.drawable.rainy
            // Rain: 500-531
            in 500..531 -> R.drawable.rainy
            // Snow: 600-622
            in 600..622 -> R.drawable.snowy
            // Atmosphere (mist, fog, etc.): 701-781
            701 -> R.drawable.mist
            711 -> R.drawable.fog
            721 -> R.drawable.mist
            731 -> R.drawable.fog
            741 -> R.drawable.fog
            751 -> R.drawable.fog
            761 -> R.drawable.fog
            762 -> R.drawable.fog
            771 -> R.drawable.fog
            781 -> R.drawable.fog
            // Default to x.png for unknown conditions
            else -> R.drawable.x
        }
    }
}

