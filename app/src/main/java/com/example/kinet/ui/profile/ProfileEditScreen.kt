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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onSave: (name: String, heightCm: Float, weightKg: Float, strideLengthCm: Float, dailyStepGoal: Int) -> Unit,
    onCancel: () -> Unit
) {
    var nameText by remember { mutableStateOf(current.name) }
    var heightText by remember { mutableStateOf("%.0f".format(current.heightCm)) }
    var weightText by remember { mutableStateOf("%.0f".format(current.weightKg)) }
    var strideText by remember { mutableStateOf("%.0f".format(current.strideLengthCm)) }
    var stepGoalText by remember { mutableStateOf("${current.dailyStepGoal}") }

    val heightFloat = heightText.toFloatOrNull()
    val weightFloat = weightText.toFloatOrNull()
    val strideFloat = strideText.toFloatOrNull()
    val stepGoalInt = stepGoalText.toIntOrNull()

    val isValid = heightFloat != null && heightFloat > 0 &&
            weightFloat != null && weightFloat > 0 &&
            strideFloat != null && strideFloat > 0 &&
            stepGoalInt != null && stepGoalInt > 0

    val hasChanges = nameText != current.name ||
            heightFloat != current.heightCm ||
            weightFloat != current.weightKg ||
            strideFloat != current.strideLengthCm ||
            stepGoalInt != current.dailyStepGoal

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

            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Alex") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height") },
                suffix = { Text("cm") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight") },
                suffix = { Text("kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = strideText,
                onValueChange = { strideText = it },
                label = { Text("Stride Length") },
                suffix = { Text("cm") },
                supportingText = { Text("Tip: stride ≈ height × 0.415") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = stepGoalText,
                onValueChange = { stepGoalText = it },
                label = { Text("Daily Step Goal") },
                suffix = { Text("steps") },
                supportingText = { Text("Recommended: 8,000 – 12,000 steps") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(nameText.trim(), heightFloat!!, weightFloat!!, strideFloat!!, stepGoalInt!!)
                    },
                    enabled = isValid && hasChanges,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
