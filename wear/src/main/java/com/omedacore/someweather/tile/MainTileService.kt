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
import com.omedacore.someweather.BuildConfig
import com.omedacore.someweather.R
import com.omedacore.someweather.shared.data.local.PreferencesManager
import com.omedacore.someweather.shared.data.model.Coordinates
import com.omedacore.someweather.shared.data.model.MainWeather
import com.omedacore.someweather.shared.data.model.SystemInfo
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherCondition
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.model.Wind
import com.omedacore.someweather.shared.data.repository.WeatherRepository
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import com.omedacore.someweather.presentation.MainActivity
import com.omedacore.someweather.shared.data.model.Clouds
import com.omedacore.someweather.shared.data.model.Precipitation

private const val RESOURCES_VERSION = "0"

/**
 * Main tile service for displaying weather information on Wear OS.
 * Shows current weather including city name, temperature, and condition.
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private val repository by lazy {
        val preferencesManager = PreferencesManager(applicationContext)
        WeatherRepository(preferencesManager, BuildConfig.WEATHER_API_KEY)
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources()

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        val city = repository.getSavedCity()
        val unitSystem = repository.getUnitSystem() ?: UnitSystem.METRIC
        
        val weather = if (city != null) {
            repository.getCurrentWeather(city).getOrNull()
        } else {
            null
        }

        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout(requestParams, applicationContext, weather, unitSystem, city != null))
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
            // OpenWeather attribution text
            Text.Builder(context, "OpenWeather")
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
    // Create mock weather data for preview
    val mockWeather = WeatherResponse(
        coord = Coordinates(lat = 0.0, lon = 0.0),
        weather = listOf(
            WeatherCondition(
                id = 800,
                main = "Clear",
                description = "clear sky",
                icon = "01d"
            )
        ),
        main = MainWeather(
            temp = 22.0,
            tempMin = 18.0,
            tempMax = 26.0,
            humidity = 65,
            feelsLike = 4.0,
            pressure = 1,
            seaLevel = 3,
            grndLevel = 2
        ),
        wind = Wind(speed = 5.0, deg = 180, gust = 2.0),
        sys = SystemInfo(
            sunrise = System.currentTimeMillis() / 1000 - 3600,
            sunset = System.currentTimeMillis() / 1000 + 3600,
            country = "US"
        ),
        name = "Sample City",
        visibility = 3,
        clouds = Clouds(all = 3),
        rain = Precipitation(1.0, 4.0),
        snow = Precipitation(1.0, 4.0),
        dt = 4,
        timezone = 3,
    )
    
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