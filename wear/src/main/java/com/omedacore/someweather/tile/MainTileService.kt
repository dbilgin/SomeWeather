package com.omedacore.someweather.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.omedacore.someweather.R
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.CurrentWeatherData
import com.omedacore.someweather.shared.data.model.DailyWeatherData
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import com.omedacore.someweather.presentation.MainActivity
import com.omedacore.someweather.shared.data.model.HourlyWeatherData

private const val RESOURCES_VERSION = "0"

/**
 * Main tile service for displaying weather information on Wear OS.
 * Shows current weather including city name, temperature, and condition.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private val repository by lazy {
        val preferencesManager = PreferencesManager(applicationContext)
        WeatherRepository(preferencesManager)
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources()

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val coords = repository.getSelectedCityCoordinates()
        val unitSystem = repository.getUnitSystem() ?: UnitSystem.METRIC
        
        val weather = if (coords != null) {
            val (lat, lon) = coords
            repository.getWeatherWithCoordinates(lat, lon, unitSystem).getOrNull()
        } else {
            null
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout(requestParams, applicationContext, weather, unitSystem, coords != null))
                            .build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(singleTileTimeline)
            .build()
    }
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    weather: WeatherResponse?,
    unitSystem: UnitSystem,
    hasCity: Boolean
): LayoutElementBuilders.LayoutElement {
    if (weather == null) {
        val errorMessage = if (!hasCity) {
            "No city set"
        } else {
            "Unable to load weather"
        }
        
        return PrimaryLayout.Builder(requestParams.deviceConfiguration)
            .setResponsiveContentInsetEnabled(true)
            .setContent(
                Text.Builder(context, errorMessage)
                    .setColor(argb(Colors.DEFAULT.onSurface))
                    .setTypography(Typography.TYPOGRAPHY_BODY1)
                    .build()
            )
            .build()
    }

    val temperature = WeatherFormatter.formatTemperature(weather, unitSystem)
    val condition = WeatherFormatter.getCondition(weather)
    val conditionText = condition.description.ifEmpty { condition.main }

    // Create column layout for essential weather information only
    // Tiles are not scrollable, so we keep it minimal
    val content = LayoutElementBuilders.Column.Builder()
        .addContent(
            // App name
            Text.Builder(context, try {
                context.resources.getString(R.string.app_name)
            } catch (_: Exception) {
                "Some Weather"
            })
                .setColor(argb(Colors.DEFAULT.onSurface))
                .setTypography(Typography.TYPOGRAPHY_CAPTION3)
                .build()
        )
        .addContent(
            // City name
            Text.Builder(context, weather.name)
                .setColor(argb(Colors.DEFAULT.onSurface))
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .build()
        )
        .addContent(
            // Spacer
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.DpProp.Builder(4f).build())
                .build()
        )
        .addContent(
            // Temperature (large)
            Text.Builder(context, temperature)
                .setColor(argb(Colors.DEFAULT.onSurface))
                .setTypography(Typography.TYPOGRAPHY_DISPLAY1)
                .build()
        )
        .addContent(
            // Spacer
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.DpProp.Builder(4f).build())
                .build()
        )
        .addContent(
            // Weather condition
            Text.Builder(context, conditionText)
                .setColor(argb(Colors.DEFAULT.onSurface))
                .setTypography(Typography.TYPOGRAPHY_BODY2)
                .build()
        )
        .addContent(
            // Spacer
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(DimensionBuilders.DpProp.Builder(4f).build())
                .build()
        )
        .addContent(
            // Open-Meteo attribution text
            Text.Builder(context, "Open-Meteo")
                .setColor(argb(Colors.DEFAULT.primary))
                .setTypography(Typography.TYPOGRAPHY_CAPTION3)
                .build()
        )
        .setModifiers(
            ModifiersBuilders.Modifiers.Builder()
                .setPadding(
                    ModifiersBuilders.Padding.Builder()
                        .setStart(DimensionBuilders.DpProp.Builder(4f).build())
                        .setEnd(DimensionBuilders.DpProp.Builder(4f).build())
                        .setTop(DimensionBuilders.DpProp.Builder(4f).build())
                        .setBottom(DimensionBuilders.DpProp.Builder(4f).build())
                        .build()
                )
                .setClickable(
                    ModifiersBuilders.Clickable.Builder()
                        .setOnClick(
                            ActionBuilders.LaunchAction.Builder()
                                .setAndroidActivity(
                                    ActionBuilders.AndroidActivity.Builder()
                                        .setClassName(MainActivity::class.java.name)
                                        .setPackageName(context.packageName)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
        .build()

    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setContent(content)
        .build()
}

private fun resources(): ResourceBuilders.Resources {
    // Resources version is required for tile caching/updates
    // Actions are defined inline in layout modifiers, so no resources to register
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(
    { _ -> resources() }
) { requestParams ->
    // Create mock weather data for preview (Open-Meteo structure)
    val sunriseTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(System.currentTimeMillis() - 3600000))
    val sunsetTime = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(System.currentTimeMillis() + 3600000))
    
    val mockWeather = WeatherResponse(
        latitude = 0.0,
        longitude = 0.0,
        current = CurrentWeatherData(
            time = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date()),
            temperature2m = 22.0,
            relativeHumidity2m = 65.0,
            weathercode = 0, // Clear sky
            windSpeed10m = 5.0,
            windDirection10m = 180,
            windGusts10m = 7.0,
            pressureMsl = 1013.0,
            visibility = 10000.0,
            isDay = 1
        ),
        hourly = HourlyWeatherData(
            time = null,
            temperature2m = null,
            relativeHumidity2m = null,
            weathercode = null,
            windSpeed10m = null,
            windDirection10m = null,
            windGusts10m = null,
            precipitation = null,
            precipitationProbability = null
        ),
        daily = DailyWeatherData(
            time = listOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())),
            weathercode = listOf(0),
            temperature2mMax = listOf(26.0),
            temperature2mMin = listOf(18.0),
            sunrise = listOf(sunriseTime),
            sunset = listOf(sunsetTime)
        ),
        timezone = "UTC",
        timezoneAbbreviation = "UTC",
        elevation = 0.0
    ).apply {
        name = "Sample City"
    }
    
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context, mockWeather, UnitSystem.METRIC, true))
                        .build()
                )
                .build()
        )
        .build()

    TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .build()
}