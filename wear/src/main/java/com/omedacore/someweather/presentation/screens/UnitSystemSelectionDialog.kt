package com.omedacore.someweather.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.*
import com.omedacore.someweather.shared.data.model.UnitSystem

@Composable
fun UnitSystemSelectionDialog(
    onUnitSelected: (UnitSystem) -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Select Unit System",
                style = MaterialTheme.typography.title2,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Button(
                onClick = { onUnitSelected(UnitSystem.METRIC) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Metric (°C, km/h)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { onUnitSelected(UnitSystem.IMPERIAL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Imperial (°F, mph)")
            }
        }
    }
}
