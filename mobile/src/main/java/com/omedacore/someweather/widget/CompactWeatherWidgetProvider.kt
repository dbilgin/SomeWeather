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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompactWeatherWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        const val ACTION_REFRESH = "com.omedacore.someweather.widget.ACTION_REFRESH_COMPACT"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, forceRefresh = false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, CompactWeatherWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, forceRefresh = true)
            }
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
        appWidgetId: Int,
        forceRefresh: Boolean = false
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_compact)
        
        // Set click intent to open app
        val intent = Intent(context, com.omedacore.someweather.presentation.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Set refresh button intent
        val refreshIntent = Intent(context, CompactWeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

        scope.launch {
            try {
                val preferencesManager = PreferencesManager(context)
                val repository = WeatherRepository(preferencesManager)
                
                // Clear cache if force refresh
                if (forceRefresh) {
                    repository.clearWeatherCache()
                }
                
                val coords = repository.getSelectedCityCoordinates()
                val unitSystem = repository.getUnitSystem() ?: com.omedacore.someweather.shared.data.model.UnitSystem.METRIC
                
                // Get last updated timestamp
                val cacheTimestamp = preferencesManager.getCacheTimestamp()
                val lastUpdatedText = if (cacheTimestamp > 0) {
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "Updated: ${dateFormat.format(Date(cacheTimestamp))}"
                } else {
                    "Updated: --"
                }
                views.setTextViewText(R.id.widget_last_updated, lastUpdatedText)
                
                if (coords != null) {
                    val weatherResult = repository.getWeatherWithCoordinates()
                    weatherResult.fold(
                        onSuccess = { weather ->
                            val temperature = WeatherFormatter.formatTemperature(weather, unitSystem)
                            val condition = WeatherFormatter.getCondition(weather)
                            val iconResId = WeatherIconHelper.getWeatherIconResId(
                                condition.conditionCode,
                                weather.sys.sunrise,
                                weather.sys.sunset
                            )

                            views.setTextViewText(R.id.widget_temperature, temperature)
                            views.setImageViewResource(R.id.widget_icon, iconResId)
                            views.setTextViewText(R.id.widget_attribution_text, "Weather data by Open-Meteo.com")
                            
                            // Update last updated timestamp after successful fetch
                            val updatedCacheTimestamp = preferencesManager.getCacheTimestamp()
                            val updatedText = if (updatedCacheTimestamp > 0) {
                                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                "Updated: ${dateFormat.format(Date(updatedCacheTimestamp))}"
                            } else {
                                "Updated: --"
                            }
                            views.setTextViewText(R.id.widget_last_updated, updatedText)
                        },
                        onFailure = {
                            views.setTextViewText(R.id.widget_temperature, "--")
                            views.setTextViewText(R.id.widget_attribution_text, "Weather data by Open-Meteo.com")
                        }
                    )
                } else {
                    views.setTextViewText(R.id.widget_temperature, "--")
                    views.setTextViewText(R.id.widget_attribution_text, "Weather data by Open-Meteo.com")
                }
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_temperature, "--")
                views.setTextViewText(R.id.widget_attribution_text, "Weather data by Open-Meteo.com")
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

