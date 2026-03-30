package com.example.kinet.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kinet.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    current: UserProfile,
    onSave: (heightCm: Float, weightKg: Float, strideLengthCm: Float) -> Unit,
    onCancel: () -> Unit
) {
    var heightText by remember { mutableStateOf("%.0f".format(current.heightCm)) }
    var weightText by remember { mutableStateOf("%.0f".format(current.weightKg)) }
    var strideText by remember { mutableStateOf("%.0f".format(current.strideLengthCm)) }
    var strideManuallyEdited by remember { mutableStateOf(false) }

    val heightFloat = heightText.toFloatOrNull()

    val autoStride = if (heightFloat != null && heightFloat > 0) {
        "%.0f".format(heightFloat * 0.415f)
    } else null

    val displayedStride = if (!strideManuallyEdited && autoStride != null &&
        autoStride == "%.0f".format(current.heightCm * 0.415f)
    ) strideText else if (!strideManuallyEdited && autoStride != null &&
        strideText == "%.0f".format(current.strideLengthCm)
    ) strideText else strideText

    val resolvedStride = displayedStride.toFloatOrNull()

    val isValid = heightFloat != null && heightFloat > 0 &&
            weightText.toFloatOrNull()?.let { it > 0 } == true &&
            resolvedStride != null && resolvedStride > 0

    val hasChanges = heightFloat != current.heightCm ||
            weightText.toFloatOrNull() != current.weightKg ||
            resolvedStride != current.strideLengthCm

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Update your measurements to keep distance and calorie calculations accurate.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = strideText,
                onValueChange = {
                    strideManuallyEdited = true
                    strideText = it
                },
                label = { Text("Stride Length (cm)") },
                supportingText = { Text("Tip: stride ≈ height × 0.415") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(heightFloat!!, weightText.toFloat(), resolvedStride!!)
                    },
                    enabled = isValid && hasChanges,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
