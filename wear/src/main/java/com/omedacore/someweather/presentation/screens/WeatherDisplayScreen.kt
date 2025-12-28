package com.omedacore.someweather.presentation.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import androidx.wear.remote.interactions.RemoteActivityHelper
import java.util.concurrent.Executors
import android.widget.Toast
import com.omedacore.someweather.data.util.WeatherIconHelper
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.shared.data.model.WeatherResponse
import com.omedacore.someweather.shared.data.util.WeatherFormatter
import com.omedacore.someweather.presentation.viewmodel.WeatherUiState
import com.omedacore.someweather.presentation.viewmodel.WeatherViewModel
import androidx.core.net.toUri
import java.util.Locale

@Composable
fun WeatherDisplayScreen(
    viewModel: WeatherViewModel,
    uiState: WeatherUiState,
    onCityChange: () -> Unit,
    onSettings: () -> Unit
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCity by viewModel.savedCity.collectAsState()

    // Fetch weather on initial composition and whenever the city changes
    // Use a key that ensures it triggers on initial composition: savedCity ?: ""
    // This will trigger once when composable is created (even if savedCity is null initially)
    // and again whenever savedCity changes
    LaunchedEffect(savedCity) {
        savedCity?.let { city ->
            viewModel.fetchWeather(city)
        }
    }

    when (uiState) {
        is WeatherUiState.Loading -> {
            LoadingScreen()
        }

        is WeatherUiState.Success -> {
            WeatherContent(
                weather = uiState.weather,
                unitSystem = unitSystem ?: UnitSystem.METRIC,
                onRefresh = { viewModel.refreshWeather() },
                onSettings = onSettings
            )
        }

        is WeatherUiState.Error -> {
            ErrorScreen(
                message = uiState.message,
                onRetry = { viewModel.refreshWeather() },
                onCityChange = onCityChange
            )
        }

        is WeatherUiState.Initial -> {
            LoadingScreen()
        }
    }
}

@Composable
fun WeatherContent(
    weather: WeatherResponse,
    unitSystem: UnitSystem,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    
    val temperature = WeatherFormatter.formatTemperature(weather, unitSystem)
    val windSpeed = WeatherFormatter.formatWindSpeed(weather, unitSystem, locale)
    val condition = WeatherFormatter.getCondition(weather)
    val iconResId = WeatherIconHelper.getWeatherIconResId(condition.conditionCode, weather.sys.sunrise, weather.sys.sunset)

    val listState = rememberScalingLazyListState(
        initialCenterItemIndex = 0,
        initialCenterItemScrollOffset = 0
    )

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        item {
            Text(
                text = weather.name,
                style = MaterialTheme.typography.title1,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        item {
            Text(
                text = temperature,
                style = MaterialTheme.typography.display1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Text(
                text = condition.description.ifEmpty { condition.main },
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = condition.description.ifEmpty { condition.main },
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "Humidity: ${weather.main.humidity}%",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            Text(
                text = "Wind: $windSpeed",
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OpenWeatherAttribution()
        }
    }
}

@Composable
fun OpenWeatherAttribution() {
    val context = LocalContext.current
    val remoteActivityHelper = remember {
        RemoteActivityHelper(context, Executors.newSingleThreadExecutor())
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = com.omedacore.someweather.shared.R.drawable.openweather_dark),
            contentDescription = "OpenWeather Logo",
            modifier = Modifier.size(48.dp)
        )

        Text(
            text = "Weather data provided by OpenWeather",
            style = MaterialTheme.typography.caption2.copy(
                color = MaterialTheme.colors.primary,
                textDecoration = TextDecoration.Underline
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_BROWSABLE)
                        .setData("https://openweathermap.org/".toUri())

                    remoteActivityHelper.startRemoteActivity(intent, null)
                    Toast.makeText(context, "Check your device", Toast.LENGTH_SHORT).show()
                }
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onCityChange: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Error",
                style = MaterialTheme.typography.title2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = onCityChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change City")
            }
        }
    }
}

