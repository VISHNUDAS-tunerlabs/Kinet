package com.example.kinet.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * First-time onboarding screen.
 * Collects Height, Weight, and Daily Step Goal.
 * Stride is auto-calculated from height (height × 0.415) and stored transparently.
 *
 * Layout is future-ready — illustration and animation slots are marked clearly.
 */
@Composable
fun ProfileSetupScreen(
    onSave: (name: String, heightCm: Float, weightKg: Float, dailyStepGoal: Int) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var stepGoalText by remember { mutableStateOf("10000") }

    val heightFloat = heightText.toFloatOrNull()
    val weightFloat = weightText.toFloatOrNull()
    val stepGoalInt = stepGoalText.toIntOrNull()

    val isValid = heightFloat != null && heightFloat > 0 &&
            weightFloat != null && weightFloat > 0 &&
            stepGoalInt != null && stepGoalInt > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Illustration slot ──────────────────────────────────────────────
        // Replace this Card with a hero illustration / Lottie animation later
        IllustrationPlaceholder()

        Spacer(Modifier.height(32.dp))

        // ── Welcome header ─────────────────────────────────────────────────
        Text(
            text = "Let's personalize\nyour journey",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.headlineLarge.lineHeight
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Tell us a bit about yourself to get accurate step tracking and calorie estimates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        // ── Input fields ───────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Your name") },
                    placeholder = { Text("e.g. Alex") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height") },
                    placeholder = { Text("e.g. 170") },
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
                    placeholder = { Text("e.g. 70") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stepGoalText,
                    onValueChange = { stepGoalText = it },
                    label = { Text("Daily Step Goal") },
                    placeholder = { Text("e.g. 10000") },
                    suffix = { Text("steps") },
                    supportingText = { Text("Recommended: 8,000 – 12,000 steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── CTA ────────────────────────────────────────────────────────────
        Button(
            onClick = { onSave(nameText.trim(), heightFloat!!, weightFloat!!, stepGoalInt!!) },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Start Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "You can update these anytime from your profile.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))
    }
}

/**
 * Placeholder for the hero illustration / animation.
 * Slot is intentionally sized and shaped for a future Lottie animation
 * or illustrated graphic (fitness avatar, running graphic, etc.).
 */
@Composable
private fun IllustrationPlaceholder() {
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 160.dp)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsRun,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            // ← Replace with Lottie / Coil image asset in future
            Text(
                text = "Illustration",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
