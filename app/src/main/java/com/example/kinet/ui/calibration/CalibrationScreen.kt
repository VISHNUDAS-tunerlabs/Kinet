package com.example.kinet.ui.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    viewModel: CalibrationViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Auto-exit when saved
    LaunchedEffect(state) {
        if (state is CalibrationViewModel.State.Saved) {
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calibrate Stride") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Improve step accuracy by measuring your actual stride length.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            when (val s = state) {
                is CalibrationViewModel.State.Instruction -> InstructionStep(
                    hasDetector = viewModel.hasStepDetector,
                    onStart = { viewModel.startWalk() }
                )

                is CalibrationViewModel.State.Active -> ActiveStep(
                    steps = s.steps,
                    onStop = { viewModel.stopWalk() }
                )

                is CalibrationViewModel.State.Result -> ResultStep(
                    steps = s.steps,
                    onSave = { distance -> viewModel.saveStride(distance) },
                    onRetry = { viewModel.retry() }
                )

                is CalibrationViewModel.State.Saved -> {
                    // LaunchedEffect handles navigation — show nothing
                }
            }
        }
    }
}

// region --- Step screens ---

@Composable
private fun InstructionStep(
    hasDetector: Boolean,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InstructionRow(number = "1", text = "Find a clear, straight path of at least 20 meters.")
            InstructionRow(number = "2", text = "Tap Start, then walk at your normal pace.")
            InstructionRow(number = "3", text = "Tap Stop when you reach the end.")
            InstructionRow(number = "4", text = "Enter the distance you walked.")
        }
    }

    if (!hasDetector) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Note: Your device does not have a hardware step detector. Calibration may be less precise.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Start Walk")
    }
}

@Composable
private fun InstructionRow(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActiveStep(
    steps: Int,
    onStop: () -> Unit
) {
    Text(
        text = "$steps",
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = "steps counted",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(8.dp))
    Text(
        text = "Walk your measured distance, then tap Stop.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(40.dp))

    Button(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text("Stop")
    }
}

@Composable
private fun ResultStep(
    steps: Int,
    onSave: (Float) -> Unit,
    onRetry: () -> Unit
) {
    var distanceText by remember { mutableStateOf("20") }
    val distanceMeters = distanceText.toFloatOrNull()
    val strideCm = if (distanceMeters != null && distanceMeters > 0 && steps > 0) {
        (distanceMeters * 100f) / steps
    } else null

    Text(
        text = "Walk complete",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "You took $steps steps.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(
        value = distanceText,
        onValueChange = { distanceText = it },
        label = { Text("Distance walked (meters)") },
        placeholder = { Text("e.g. 20") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        supportingText = {
            if (strideCm != null) {
                Text("Calculated stride: ${"%.1f".format(strideCm)} cm")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(24.dp))

    // Save button
    Button(
        onClick = { distanceMeters?.let { onSave(it) } },
        enabled = strideCm != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text("Save Stride")
    }

    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onRetry,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Retry")
    }
}

// endregion
