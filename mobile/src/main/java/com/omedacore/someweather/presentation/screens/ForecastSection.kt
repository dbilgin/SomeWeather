package com.omedacore.someweather.presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omedacore.someweather.shared.R as SharedR
import com.omedacore.someweather.shared.data.model.ForecastItem
import com.omedacore.someweather.data.util.WeatherIconHelper
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ForecastSection(
    forecastItems: List<ForecastItem>,
    sunrise: Long,
    sunset: Long,
    unitSystem: UnitSystem
) {
    if (forecastItems.isEmpty()) {
        // Empty state - show skeleton while loading
        Column {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            repeat(5) {
                ForecastDayCardSkeleton()
            }
        }
    } else {
        val groupedForecast = groupForecastByDay(forecastItems)

        Column {
            Text(
                text = "5-Day Forecast",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            groupedForecast.forEach { (date, items) ->
                ForecastDayCard(
                    date = date,
                    items = items,
                    sunrise = sunrise,
                    sunset = sunset,
                    unitSystem = unitSystem
                )
            }
        }
    }
}

@Composable
private fun ForecastDayCard(
    date: String,
    items: List<ForecastItem>,
    sunrise: Long,
    sunset: Long,
    unitSystem: UnitSystem
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()

    // Calculate min/max for the day
    val minTemp = items.minOfOrNull { it.main.tempMin } ?: 0.0
    val maxTemp = items.maxOfOrNull { it.main.tempMax } ?: 0.0
    val mainCondition = items.firstOrNull()?.weather?.firstOrNull()
    val iconResId = mainCondition?.let {
        WeatherIconHelper.getWeatherIconResId(
            it.conditionCode,
            sunrise,
            sunset
        )
    } ?: SharedR.drawable.x

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val isDarkTheme = isSystemInDarkTheme()
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = mainCondition?.description ?: "",
                        modifier = Modifier.size(48.dp),
                        colorFilter = if (isDarkTheme) ColorFilter.tint(Color.White) else null
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${
                                WeatherFormatter.formatTemperature(
                                    minTemp,
                                    unitSystem
                                )
                            } / ${WeatherFormatter.formatTemperature(maxTemp, unitSystem)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                items.forEachIndexed { index, item ->
                    ForecastItemRow(item, unitSystem, locale)
                    if (index < items.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastItemRow(
    item: ForecastItem,
    unitSystem: UnitSystem,
    locale: Locale
) {
    val timeFormat = SimpleDateFormat("HH:mm", locale)
    val time = timeFormat.format(Date(item.dt * 1000))
    val condition = item.weather.firstOrNull()
    val iconResId = condition?.let {
        WeatherIconHelper.getWeatherIconResId(it.conditionCode, null, null)
    } ?: SharedR.drawable.x

    val windSpeed = WeatherFormatter.formatWindSpeed(item.wind.speed, unitSystem, locale)
    val windGust = WeatherFormatter.formatWindGust(item.wind.gust, unitSystem, locale)
    val windDirection = WeatherFormatter.formatWindDirection(item.wind.deg)
    val precipitation =
        WeatherFormatter.formatPrecipitation(item.rain ?: item.snow, unitSystem, locale)
    val cloudiness = WeatherFormatter.formatCloudiness(item.clouds?.all)
    val pop = WeatherFormatter.formatPop(item.pop)

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Column(modifier = Modifier.weight(0.3f)) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(modifier = Modifier.weight(0.3f)) {
                // Weather icon
                val isDarkTheme = isSystemInDarkTheme()
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = condition?.description ?: "",
                    modifier = Modifier.size(36.dp),
                    colorFilter = if (isDarkTheme) ColorFilter.tint(Color.White) else null
                )

                // Temperature
                Text(
                    text = WeatherFormatter.formatTemperature(item.main.temp, unitSystem),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Additional info column
            Column(
                modifier = Modifier.weight(0.4f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Humidity: ${item.main.humidity}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Wind",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    windDirection?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = windSpeed,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                windGust?.let {
                    Text(
                        text = "Gust: $windGust",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (precipitation != null || pop != null) {
                        Text(
                            text = "Pre.: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    precipitation?.let {
                        Text(
                            text = precipitation,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    pop?.let { popValue ->
                        Text(
                            text = popValue,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                cloudiness?.let {
                    Text(
                        text = "Cloudiness: $cloudiness",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ForecastDayCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Icon skeleton
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Text skeletons
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }

            // Arrow icon skeleton
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

private fun groupForecastByDay(forecastItems: List<ForecastItem>): Map<String, List<ForecastItem>> {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val grouped = mutableMapOf<String, MutableList<ForecastItem>>()

    forecastItems.forEach { item ->
        val date = Date(item.dt * 1000)
        val dateKey = dateFormat.format(date)
        grouped.getOrPut(dateKey) { mutableListOf() }.add(item)
    }

    return grouped
}
