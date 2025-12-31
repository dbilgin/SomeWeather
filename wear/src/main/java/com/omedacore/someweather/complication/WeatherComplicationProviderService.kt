package com.omedacore.someweather.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.omedacore.someweather.data.util.WeatherIconHelper
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import com.omedacore.someweather.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherComplicationProviderService : SuspendingComplicationDataSourceService() {

    private val repository by lazy {
        val preferencesManager = PreferencesManager(applicationContext)
        WeatherRepository(preferencesManager)
    }

    private fun createTapIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(applicationContext, 0, intent, flags)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val icon =
            Icon.createWithResource(applicationContext, com.omedacore.someweather.shared.R.drawable.x)
        val monochromaticImage = MonochromaticImage.Builder(icon).build()
        val tapAction = createTapIntent()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("22°C").build(),
                    contentDescription = PlainComplicationText.Builder("Temperature: 22°C").build()
                )
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.RANGED_VALUE -> {
                // Preview data with safe min/max values
                val previewValue = 22f
                val previewMin = -30f
                val previewMax = 50f

                RangedValueComplicationData.Builder(
                    value = previewValue.coerceIn(previewMin, previewMax),
                    min = previewMin,
                    max = previewMax,
                    contentDescription = PlainComplicationText.Builder("Temperature: 22°C").build()
                )
                    .setText(PlainComplicationText.Builder("22°C").build())
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.LONG_TEXT -> {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                val dateText = dateFormat.format(calendar.time)

                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("22°C").build(),
                    contentDescription = PlainComplicationText.Builder("Date: $dateText, Temperature: 22°C").build()
                )
                    .setTitle(PlainComplicationText.Builder(dateText).build())
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }

    override suspend fun onComplicationRequest(
        request: ComplicationRequest
    ): ComplicationData? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching now")
        val unitSystem = repository.getUnitSystem()
        Log.d(TAG, "Unit System: " + (unitSystem?.name ?: "null"))
        val unitSystemFinal = unitSystem ?: UnitSystem.METRIC

        // Get coordinates and fetch weather data using repository
        val coords = repository.getSelectedCityCoordinates()
        if (coords == null) {
            Log.d(TAG, "No coordinates saved, returning null")
            return@withContext null
        }
        val (lat, lon) = coords
        val weatherResult = repository.getWeatherWithCoordinates(lat, lon, unitSystemFinal)
        val weatherResponse = weatherResult.getOrElse {
            Log.e(TAG, "Error fetching weather for complication", it)
            // Return fallback
            val unit = if (unitSystemFinal == UnitSystem.METRIC) "°C" else "°F"
            val calendar = Calendar.getInstance()
            val locale = applicationContext.resources.configuration.locales[0] ?: Locale.getDefault()
            val dateFormat = SimpleDateFormat("MMM d", locale)
            val dateText = dateFormat.format(calendar.time)
            
            return@withContext when (request.complicationType) {
                ComplicationType.SHORT_TEXT -> {
                    ShortTextComplicationData.Builder(
                        text = PlainComplicationText.Builder("--$unit").build(),
                        contentDescription = PlainComplicationText.Builder("Weather unavailable")
                            .build()
                    )
                        .build()
                }

                ComplicationType.LONG_TEXT -> {
                    LongTextComplicationData.Builder(
                        text = PlainComplicationText.Builder("--$unit").build(),
                        contentDescription = PlainComplicationText.Builder("Date: $dateText, Weather unavailable")
                            .build()
                    )
                        .setTitle(PlainComplicationText.Builder(dateText).build())
                        .build()
                }

                else -> null
            }
        }
        Log.d(TAG, "Got response: Temp " + weatherResponse.main.temp)

        val temperatureFormatted = WeatherFormatter.formatTemperature(weatherResponse, unitSystemFinal)
        // Extract temperature and unit for ranged value complication
        val temperature = weatherResponse.main.temp.roundToInt()
        val unit = if (unitSystemFinal == UnitSystem.METRIC) "°C" else "°F"

        Log.d(TAG, "Final temp $temperature")

        val tempMin =
            if (weatherResponse.main.tempMin.roundToInt() >= temperature) temperature - 1 else weatherResponse.main.tempMin.roundToInt()
        val tempMax =
            if (weatherResponse.main.tempMax.roundToInt() <= temperature) temperature + 1 else weatherResponse.main.tempMax.roundToInt()

        val condition = WeatherFormatter.getCondition(weatherResponse)
        val iconResId = WeatherIconHelper.getWeatherIconResId(condition.conditionCode, weatherResponse.sys.sunrise, weatherResponse.sys.sunset)
        val icon = Icon.createWithResource(applicationContext, iconResId)
        val monochromaticImage = MonochromaticImage.Builder(icon).build()
        val tapAction = createTapIntent()

        Log.d(TAG, "Updating complication ${request.complicationType}")
        when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(temperatureFormatted).build(),
                    contentDescription = PlainComplicationText.Builder("Temperature: $temperatureFormatted")
                        .build()
                )
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = temperature.toFloat(),
                    min = tempMin.toFloat(),
                    max = tempMax.toFloat(),
                    contentDescription = PlainComplicationText.Builder("Temperature: $temperatureFormatted")
                        .build()
                )
                    .setText(PlainComplicationText.Builder(temperatureFormatted).build())
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.LONG_TEXT -> {
                // Format date (e.g., "Dec 30" or "Jan 1")
                val calendar = Calendar.getInstance()
                val locale = applicationContext.resources.configuration.locales[0] ?: Locale.getDefault()
                val dateFormat = SimpleDateFormat("MMM d", locale)
                val dateText = dateFormat.format(calendar.time)

                LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(temperatureFormatted).build(),
                    contentDescription = PlainComplicationText.Builder("Date: $dateText, Temperature: $temperatureFormatted")
                        .build()
                )
                    .setTitle(PlainComplicationText.Builder(dateText).build())
                    .setMonochromaticImage(monochromaticImage)
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }

    companion object {
        private const val TAG = "WeatherComplication"
    }
}
