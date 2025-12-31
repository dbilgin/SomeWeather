package com.omedacore.someweather.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.omedacore.someweather.R
import com.omedacore.someweather.data.util.WeatherIconHelper
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import com.omedacore.someweather.shared.R as SharedR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Widget enabled
    }

    override fun onDisabled(context: Context) {
        // Widget disabled
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)
        
        // Set click intent to open app
        val intent = Intent(context, com.omedacore.someweather.presentation.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        scope.launch {
            try {
                val preferencesManager = PreferencesManager(context)
                val repository = WeatherRepository(preferencesManager, com.omedacore.someweather.BuildConfig.USE_OPENMETEO)
                val coords = repository.getSelectedCityCoordinates()
                val unitSystem = repository.getUnitSystem() ?: com.omedacore.someweather.shared.data.model.UnitSystem.METRIC
                
                if (coords != null) {
                    val (lat, lon) = coords
                    val weatherResult = repository.getWeatherWithCoordinates(lat, lon, unitSystem)
                    weatherResult.fold(
                        onSuccess = { weather ->
                            val temperature = WeatherFormatter.formatTemperature(weather, unitSystem)
                            val condition = WeatherFormatter.getCondition(weather)
                            val iconResId = WeatherIconHelper.getWeatherIconResId(
                                condition.conditionCode,
                                weather.sys.sunrise,
                                weather.sys.sunset
                            )

                            // Format sunrise/sunset times
                            val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
                            val timeFormat = SimpleDateFormat("HH:mm", locale)
                            val sunriseTime = if (weather.sys.sunrise > 0) {
                                timeFormat.format(Date(weather.sys.sunrise * 1000))
                            } else "--:--"
                            val sunsetTime = if (weather.sys.sunset > 0) {
                                timeFormat.format(Date(weather.sys.sunset * 1000))
                            } else "--:--"

                            views.setTextViewText(R.id.widget_city, weather.name)
                            views.setTextViewText(R.id.widget_temperature, temperature)
                            views.setTextViewText(R.id.widget_condition, condition.description.ifEmpty { condition.main })
                            views.setImageViewResource(R.id.widget_icon, iconResId)
                            views.setImageViewResource(R.id.widget_sunrise_icon, SharedR.drawable.clear_day)
                            views.setTextViewText(R.id.widget_sunrise_time, sunriseTime)
                            views.setImageViewResource(R.id.widget_sunset_icon, SharedR.drawable.clear_night)
                            views.setTextViewText(R.id.widget_sunset_time, sunsetTime)
                        },
                        onFailure = {
                            views.setTextViewText(R.id.widget_city, "Error")
                            views.setTextViewText(R.id.widget_temperature, "--")
                            views.setTextViewText(R.id.widget_condition, "Unable to load")
                            views.setImageViewResource(R.id.widget_sunrise_icon, SharedR.drawable.clear_day)
                            views.setTextViewText(R.id.widget_sunrise_time, "--:--")
                            views.setImageViewResource(R.id.widget_sunset_icon, SharedR.drawable.clear_night)
                            views.setTextViewText(R.id.widget_sunset_time, "--:--")
                        }
                    )
                } else {
                    views.setTextViewText(R.id.widget_city, "No city set")
                    views.setTextViewText(R.id.widget_temperature, "--")
                    views.setTextViewText(R.id.widget_condition, "Set city in app")
                    views.setImageViewResource(R.id.widget_sunrise_icon, SharedR.drawable.clear_day)
                    views.setTextViewText(R.id.widget_sunrise_time, "--:--")
                    views.setImageViewResource(R.id.widget_sunset_icon, SharedR.drawable.clear_night)
                    views.setTextViewText(R.id.widget_sunset_time, "--:--")
                }
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_city, "Error")
                views.setTextViewText(R.id.widget_temperature, "--")
                views.setTextViewText(R.id.widget_condition, "Unable to load")
                views.setImageViewResource(R.id.widget_sunrise_icon, SharedR.drawable.clear_day)
                views.setTextViewText(R.id.widget_sunrise_time, "--:--")
                views.setImageViewResource(R.id.widget_sunset_icon, SharedR.drawable.clear_night)
                views.setTextViewText(R.id.widget_sunset_time, "--:--")
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
