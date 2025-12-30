package com.omedacore.someweather.presentation.screens

import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.omedacore.someweather.presentation.viewmodel.SearchState
import com.omedacore.someweather.presentation.viewmodel.WeatherViewModel
import com.omedacore.someweather.shared.data.model.GeocodingResult

@Composable
fun CityInputScreen(
    viewModel: WeatherViewModel,
    onCitySelected: (GeocodingResult) -> Unit
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
            // Show input screen
            CityInputContent(
                isLoading = searchState is SearchState.Loading,
                onSearch = { query ->
                    viewModel.searchCity(query)
                }
            )
        }
    }
}

@Composable
private fun CityInputContent(
    isLoading: Boolean,
    onSearch: (String) -> Unit
) {
    var cityInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                AndroidView(
                    factory = { context ->
                        android.widget.EditText(context).apply {
                            hint = "Enter city name"
                            setTextColor(android.graphics.Color.WHITE)
                            setHintTextColor(android.graphics.Color.GRAY)
                            imeOptions = EditorInfo.IME_ACTION_SEARCH
                            isEnabled = !isLoading
                            setOnEditorActionListener { _, actionId, _ ->
                                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                                    val text = text.toString()
                                    if (text.isNotBlank()) {
                                        cityInput = text
                                        keyboardController?.hide()
                                        onSearch(cityInput)
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            addTextChangedListener(object : android.text.TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    cityInput = s?.toString() ?: ""
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
                    Button(
                        onClick = {
                            if (cityInput.isNotBlank()) {
                                keyboardController?.hide()
                                onSearch(cityInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = cityInput.isNotBlank()
                    ) {
                        Text("Search")
                    }
                }
            }
        }
    }
}
