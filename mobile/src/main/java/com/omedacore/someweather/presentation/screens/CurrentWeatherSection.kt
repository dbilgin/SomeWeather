package com.omedacore.someweather.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omedacore.someweather.shared.R as SharedR
import com.omedacore.someweather.data.util.WeatherIconHelper
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CurrentWeatherSection(
    weather: WeatherResponse,
    unitSystem: UnitSystem
) {
    var showMoreDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()

    val temperature = WeatherFormatter.formatTemperature(weather, unitSystem)
    val feelsLike = WeatherFormatter.formatFeelsLikeTemperature(weather.main.feelsLike, unitSystem)
    val windSpeed = WeatherFormatter.formatWindSpeed(weather, unitSystem, locale)
    val condition = WeatherFormatter.getCondition(weather)
    val iconResId = WeatherIconHelper.getWeatherIconResId(
        condition.conditionCode,
        weather.sys.sunrise,
        weather.sys.sunset
    )

    // Format sunrise/sunset times
    val timeFormat = SimpleDateFormat("HH:mm", locale)
    val sunriseTime = timeFormat.format(Date(weather.sys.sunrise * 1000))
    val sunsetTime = timeFormat.format(Date(weather.sys.sunset * 1000))

    // On-demand details
    val pressure = WeatherFormatter.formatPressure(weather.main.pressure)
    val visibility = WeatherFormatter.formatVisibility(weather.visibility, unitSystem, locale)
    val cloudiness = WeatherFormatter.formatCloudiness(weather.clouds?.all)
    val windGust = WeatherFormatter.formatWindGust(weather.wind.gust, unitSystem, locale)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City name
            Text(
                text = weather.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Weather icon
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = condition.description.ifEmpty { condition.main },
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            Text(
                text = temperature,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold
            )

            // Feels like temperature
            feelsLike?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Feels like $it",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Condition description
            Text(
                text = condition.description.ifEmpty { condition.main },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Info grid - Row 1: Humidity, Wind Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoCard(
                    label = "Humidity",
                    value = "${weather.main.humidity}%"
                )
                InfoCard(
                    label = "Wind",
                    value = windSpeed
                )
            }

            // More details section (on-demand)
            if (pressure != null || visibility != null || cloudiness != null || windGust != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showMoreDetails = !showMoreDetails },
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (showMoreDetails) "Hide details" else "Show more details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showMoreDetails) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = when {
                            pressure != null && visibility != null -> Arrangement.SpaceEvenly
                            else -> Arrangement.Center
                        }
                    ) {
                        pressure?.let {
                            InfoCard(
                                label = "Pressure",
                                value = it
                            )
                            if (visibility != null) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }

                        visibility?.let {
                            InfoCard(
                                label = "Visibility",
                                value = it
                            )
                        }
                    }

                    if (cloudiness != null || windGust != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = when {
                                cloudiness != null && windGust != null -> Arrangement.SpaceEvenly
                                else -> Arrangement.Center
                            }
                        ) {
                            cloudiness?.let {
                                InfoCard(
                                    label = "Cloudiness",
                                    value = it
                                )
                                if (windGust != null) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                            }

                            windGust?.let {
                                InfoCard(
                                    label = "Wind Gust",
                                    value = it
                                )
                            }
                        }
                    }
                }
            }

            // Sunrise/Sunset
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = SharedR.drawable.clear_day),
                        contentDescription = "Sunrise",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sunrise",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunriseTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = SharedR.drawable.clear_night),
                        contentDescription = "Sunset",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sunset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sunsetTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
