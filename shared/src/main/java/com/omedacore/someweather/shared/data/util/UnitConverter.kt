package com.omedacore.someweather.shared.data.util

import com.omedacore.someweather.shared.data.model.UnitSystem

/**
 * Utility object for converting UnitSystem enum to Open-Meteo API unit strings
 */
object UnitConverter {
    /**
     * Converts UnitSystem to Open-Meteo temperature unit string
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return "celsius" for METRIC, "fahrenheit" for IMPERIAL
     */
    fun convertTemperatureUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "celsius"
            UnitSystem.IMPERIAL -> "fahrenheit"
        }
    }

    /**
     * Converts UnitSystem to Open-Meteo windspeed unit string
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return "ms" for METRIC, "mph" for IMPERIAL
     */
    fun convertWindspeedUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "ms"
            UnitSystem.IMPERIAL -> "mph"
        }
    }

    /**
     * Converts UnitSystem to Open-Meteo precipitation unit string
     * @param unitSystem The unit system (METRIC or IMPERIAL)
     * @return "mm" for METRIC, "inch" for IMPERIAL
     */
    fun convertPrecipitationUnit(unitSystem: UnitSystem): String {
        return when (unitSystem) {
            UnitSystem.METRIC -> "mm"
            UnitSystem.IMPERIAL -> "inch"
        }
    }
}

