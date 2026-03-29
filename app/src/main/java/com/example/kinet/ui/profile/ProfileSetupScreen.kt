package com.example.kinet.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProfileSetupScreen(
    onSave: (heightCm: Float, weightKg: Float, strideLengthCm: Float) -> Unit
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var strideText by remember { mutableStateOf("") }
    var strideManuallyEdited by remember { mutableStateOf(false) }

    val heightFloat = heightText.toFloatOrNull()

    // Auto-fill stride from height unless user already edited it
    val autoStride = if (heightFloat != null && heightFloat > 0) {
        "%.0f".format(heightFloat * 0.415f)
    } else null

    val displayedStride = if (!strideManuallyEdited && autoStride != null) autoStride else strideText
    val resolvedStride = displayedStride.toFloatOrNull()

    val isValid = heightFloat != null && heightFloat > 0 &&
            weightText.toFloatOrNull()?.let { it > 0 } == true &&
            resolvedStride != null && resolvedStride > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Set Up Your Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Used to calculate distance and calories accurately.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("Height (cm)") },
            placeholder = { Text("e.g. 170") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Weight (kg)") },
            placeholder = { Text("e.g. 70") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = displayedStride,
            onValueChange = {
                strideManuallyEdited = true
                strideText = it
            },
            label = { Text("Stride Length (cm)") },
            placeholder = { Text("e.g. 75") },
            supportingText = {
                if (!strideManuallyEdited && autoStride != null) {
                    Text("Auto-calculated from height — tap to override")
                } else {
                    Text("Tip: stride ≈ height × 0.415")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                onSave(
                    heightFloat!!,
                    weightText.toFloat(),
                    resolvedStride!!
                )
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}
