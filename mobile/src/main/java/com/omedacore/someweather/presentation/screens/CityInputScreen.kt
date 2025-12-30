package com.omedacore.someweather.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omedacore.someweather.presentation.viewmodel.MobileWeatherViewModel
import com.omedacore.someweather.presentation.viewmodel.SearchState
import com.omedacore.someweather.shared.data.model.GeocodingResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityInputScreen(
    viewModel: MobileWeatherViewModel,
    onCitySelected: (GeocodingResult) -> Unit,
    onDismiss: () -> Unit
) {
    var cityInput by remember { mutableStateOf("") }
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
                Toast.makeText(context, "Search failed. Please try again.", Toast.LENGTH_SHORT).show()
                viewModel.resetSearchState()
            }
            else -> {}
        }
    }

    // Show city selection dialog when results are found
    when (val state = searchState) {
        is SearchState.Success -> {
            CitySelectionDialog(
                cities = state.results,
                onCitySelected = { city ->
                    viewModel.resetSearchState()
                    onCitySelected(city)
                },
                onDismiss = {
                    viewModel.resetSearchState()
                }
            )
        }
        else -> {
            // Show input dialog
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Enter City Name") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("City") },
                            placeholder = { Text("e.g., London, New York") },
                            singleLine = true,
                            enabled = searchState !is SearchState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (searchState is SearchState.Loading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (cityInput.isNotBlank()) {
                                viewModel.searchCity(cityInput.trim())
                            }
                        },
                        enabled = cityInput.isNotBlank() && searchState !is SearchState.Loading
                    ) {
                        Text(if (searchState is SearchState.Loading) "Searching..." else "Search")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismiss,
                        enabled = searchState !is SearchState.Loading
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
