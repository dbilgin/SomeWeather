package com.omedacore.someweather.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.omedacore.someweather.shared.data.model.GeocodingResult
import com.omedacore.someweather.shared.data.model.UnitSystem
import com.omedacore.someweather.presentation.viewmodel.SearchState
import com.omedacore.someweather.presentation.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    viewModel: WeatherViewModel,
    onBack: () -> Unit
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val savedCityDisplay by viewModel.savedCityDisplay.collectAsState()
    var showCityInputDialog by remember { mutableStateOf(false) }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.title1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            Text(
                text = "City",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            Text(
                text = savedCityDisplay ?: "Not set",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Button(
                onClick = { showCityInputDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change City")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Unit System",
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Button(
                onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (unitSystem == UnitSystem.METRIC) {
                    ButtonDefaults.primaryButtonColors()
                } else {
                    ButtonDefaults.secondaryButtonColors()
                }
            ) {
                Text("Metric (°C, km/h)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (unitSystem == UnitSystem.IMPERIAL) {
                    ButtonDefaults.primaryButtonColors()
                } else {
                    ButtonDefaults.secondaryButtonColors()
                }
            ) {
                Text("Imperial (°F, mph)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }

    if (showCityInputDialog) {
        SettingsCityInput(
            viewModel = viewModel,
            onCitySelected = { cityResult ->
                showCityInputDialog = false
                viewModel.saveCity(cityResult)
                viewModel.fetchWeather(cityResult.name)
            },
            onDismiss = { showCityInputDialog = false }
        )
    }
}

@Composable
private fun SettingsCityInput(
    viewModel: WeatherViewModel,
    onCitySelected: (GeocodingResult) -> Unit,
    onDismiss: () -> Unit
) {
    val searchState by viewModel.searchState.collectAsState()
    val context = LocalContext.current

    // Handle search state changes
    LaunchedEffect(searchState) {
        when (searchState) {
            is SearchState.NoResults -> {
                Toast.makeText(context, "No cities found", Toast.LENGTH_SHORT).show()
                viewModel.resetSearchState()
            }
            is SearchState.Error -> {
                Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
                viewModel.resetSearchState()
            }
            else -> {}
        }
    }

    // Show city selection screen when results are found
    when (val state = searchState) {
        is SearchState.Success -> {
            CitySelectionScreen(
                cities = state.results,
                onCitySelected = { cityResult ->
                    viewModel.resetSearchState()
                    onCitySelected(cityResult)
                },
                onBack = {
                    viewModel.resetSearchState()
                }
            )
        }
        else -> {
            // Show input dialog with search
            SettingsCityInputDialog(
                isLoading = searchState is SearchState.Loading,
                onDismiss = onDismiss,
                onSearch = { query ->
                    viewModel.searchCity(query)
                }
            )
        }
    }
}

@Composable
private fun SettingsCityInputDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit
) {
    var cityInput by remember { mutableStateOf("") }

    CityInputDialogContent(
        cityInput = cityInput,
        onCityInputChange = { cityInput = it },
        isLoading = isLoading,
        onDismiss = onDismiss,
        onConfirm = { onSearch(cityInput) }
    )
}

@Composable
private fun CityInputDialogContent(
    cityInput: String,
    onCityInputChange: (String) -> Unit,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "City Name",
                    style = MaterialTheme.typography.title2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            item {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { context ->
                        android.widget.EditText(context).apply {
                            hint = "Enter city name"
                            setTextColor(android.graphics.Color.WHITE)
                            setHintTextColor(android.graphics.Color.GRAY)
                            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
                            this.isEnabled = !isLoading
                            setOnEditorActionListener { _, actionId, _ ->
                                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                                    val text = text.toString()
                                    if (text.isNotBlank()) {
                                        keyboardController?.hide()
                                        onCityInputChange(text)
                                        onConfirm()
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            addTextChangedListener(object : android.text.TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    onCityInputChange(s?.toString() ?: "")
                                }
                                override fun afterTextChanged(s: android.text.Editable?) {}
                            })
                        }.also { editText ->
                            editText.post {
                                editText.requestFocus()
                                val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (cityInput.isNotBlank()) {
                                    keyboardController?.hide()
                                    onConfirm()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = cityInput.isNotBlank()
                        ) {
                            Text("Search")
                        }
                    }
                }
            }
        }
    }
}

