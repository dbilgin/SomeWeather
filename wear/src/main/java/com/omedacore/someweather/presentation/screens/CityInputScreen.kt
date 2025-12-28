package com.omedacore.someweather.presentation.screens

import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*

@Composable
fun CityInputScreen(
    onCityEntered: (String) -> Unit
) {
    CityInputDialog(
        onDismiss = {},
        onConfirm = onCityEntered
    )
}

@Composable
fun CityInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
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
                            imeOptions = EditorInfo.IME_ACTION_DONE
                            setOnEditorActionListener { _, actionId, _ ->
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    val text = text.toString()
                                    if (text.isNotBlank()) {
                                        cityInput = text
                                        keyboardController?.hide()
                                        onConfirm(cityInput)
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
                                onConfirm(cityInput)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cityInput.isNotBlank()
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
