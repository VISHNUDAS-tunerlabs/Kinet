package com.example.kinet.ui.habito

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitCategory
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun HabitoScreen(viewModel: HabitoViewModel, modifier: Modifier = Modifier) {
    val subScreen by viewModel.subScreen.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val editingHabit by viewModel.editingHabit.collectAsState()

    when (subScreen) {
        HabitoSubScreen.LIST -> HabitListScreen(
            habits = habits,
            todayLogs = todayLogs,
            onAddHabit = { viewModel.navigateTo(HabitoSubScreen.ADD_EDIT) },
            onEditHabit = { viewModel.navigateTo(HabitoSubScreen.ADD_EDIT, it) },
            onDeleteHabit = { viewModel.deleteHabit(it) },
            onMarkToday = { viewModel.navigateTo(HabitoSubScreen.DAILY_LOG) },
            modifier = modifier
        )
        HabitoSubScreen.ADD_EDIT -> AddEditHabitScreen(
            editingHabit = editingHabit,
            onSave = { title, category, isStepBased, stepTarget, reminderEnabled, reminderTime ->
                viewModel.saveHabit(title, category, isStepBased, stepTarget, reminderEnabled, reminderTime)
            },
            onCancel = { viewModel.navigateTo(HabitoSubScreen.LIST) },
            modifier = modifier
        )
        HabitoSubScreen.DAILY_LOG -> DailyLogScreen(
            habits = habits,
            todayLogs = todayLogs,
            todaySteps = todaySteps,
            onLog = { habitId, status -> viewModel.logHabit(habitId, status) },
            onBack = { viewModel.navigateTo(HabitoSubScreen.LIST) },
            modifier = modifier
        )
    }
}

// ─── Habit List Screen ────────────────────────────────────────────────────────

@Composable
private fun HabitListScreen(
    habits: List<Habit>,
    todayLogs: List<HabitLog>,
    onAddHabit: () -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Int) -> Unit,
    onMarkToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (habits.isNotEmpty()) {
            val completedCount = todayLogs.count { it.status == HabitStatus.COMPLETED }
            Button(
                onClick = onMarkToday,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Mark Today's Habits  ($completedCount / ${habits.size} done)")
            }
        }

        if (habits.isEmpty()) {
            EmptyHabitsState(onAddHabit = onAddHabit, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                items(habits, key = { it.habitId }) { habit ->
                    val log = todayLogs.find { it.habitId == habit.habitId }
                    HabitCard(
                        habit = habit,
                        todayStatus = log?.status,
                        onEdit = { onEditHabit(habit) },
                        onDelete = { onDeleteHabit(habit.habitId) }
                    )
                }
            }
        }

        Button(
            onClick = onAddHabit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Habit")
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    todayStatus: HabitStatus?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = habit.category.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (habit.reminderEnabled && habit.reminderTime != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Alarm, null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = formatTime(habit.reminderTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = if (habit.streakCount > 0) "${habit.streakCount} day streak" else "— day streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (habit.streakCount > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StatusBadge(status = todayStatus)

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, "Edit", modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete, "Delete",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete habit?") },
            text = { Text("\"${habit.title}\" will be removed.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: HabitStatus?) {
    val (label, color) = when (status) {
        HabitStatus.COMPLETED -> "Done" to MaterialTheme.colorScheme.tertiary
        HabitStatus.SKIPPED   -> "Skipped" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> return
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun EmptyHabitsState(onAddHabit: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add your first habit to start building a healthy routine.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddHabit) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add First Habit")
        }
    }
}

// ─── Add / Edit Habit Screen ──────────────────────────────────────────────────

private data class HabitSuggestion(
    val title: String,
    val category: HabitCategory,
    val isStepBased: Boolean = false,
    val stepTarget: Int? = null
)

private val builtInSuggestions = listOf(
    HabitSuggestion("Drink a glass of water after waking up", HabitCategory.HEALTH),
    HabitSuggestion("Go to bed before 11 PM", HabitCategory.SLEEP),
    HabitSuggestion("Walk 8,000 steps", HabitCategory.FITNESS, isStepBased = true, stepTarget = 8000),
    HabitSuggestion("Read for 15 minutes", HabitCategory.MINDFULNESS),
    HabitSuggestion("Morning stretching", HabitCategory.FITNESS)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditHabitScreen(
    editingHabit: Habit?,
    onSave: (String, HabitCategory, Boolean, Int?, Boolean, String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = editingHabit != null

    var title by rememberSaveable { mutableStateOf(editingHabit?.title ?: "") }
    var category by rememberSaveable { mutableStateOf(editingHabit?.category ?: HabitCategory.HEALTH) }
    var isStepBased by rememberSaveable { mutableStateOf(editingHabit?.isStepBased ?: false) }
    var stepTargetText by rememberSaveable {
        mutableStateOf(editingHabit?.stepTarget?.toString() ?: "8000")
    }
    var reminderEnabled by rememberSaveable { mutableStateOf(editingHabit?.reminderEnabled ?: false) }
    var reminderHour by rememberSaveable {
        mutableIntStateOf(editingHabit?.reminderTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8)
    }
    var reminderMinute by rememberSaveable {
        mutableIntStateOf(editingHabit?.reminderTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0)
    }
    var showTimePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = if (isEditing) "Edit Habit" else "Add Habit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Suggestions — only when adding and title is still empty
        if (!isEditing && title.isBlank()) {
            item {
                Text(
                    "Quick suggestions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    builtInSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                title = suggestion.title
                                category = suggestion.category
                                isStepBased = suggestion.isStepBased
                                if (suggestion.stepTarget != null) {
                                    stepTargetText = suggestion.stepTarget.toString()
                                }
                            },
                            label = { Text(suggestion.title) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Habit name") },
                placeholder = { Text("e.g. Drink more water") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category chips
        item {
            Text(
                "Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label()) },
                        leadingIcon = {
                            Icon(cat.icon(), null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        // Habit type toggle
        item {
            Text(
                "Habit type",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isStepBased,
                    onClick = { isStepBased = false },
                    label = { Text("Manual") }
                )
                FilterChip(
                    selected = isStepBased,
                    onClick = { isStepBased = true },
                    label = { Text("Step-based") },
                    leadingIcon = {
                        Icon(Icons.Filled.DirectionsRun, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        // Step target — only for step-based
        if (isStepBased) {
            item {
                OutlinedTextField(
                    value = stepTargetText,
                    onValueChange = { stepTargetText = it.filter(Char::isDigit).take(6) },
                    label = { Text("Step target") },
                    suffix = { Text("steps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "This habit auto-completes when your daily step count reaches the target.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Reminder card
        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Alarm, null)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Daily reminder", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Get notified at a set time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }

                    AnimatedVisibility(visible = reminderEnabled) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Alarm, null)
                                Spacer(Modifier.width(8.dp))
                                Text(formatTime("%02d:%02d".format(reminderHour, reminderMinute)))
                            }
                        }
                    }
                }
            }
        }

        // Save / Cancel
        item {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val stepTarget = if (isStepBased) stepTargetText.toIntOrNull() else null
                        val reminderTime = if (reminderEnabled) {
                            "%02d:%02d".format(reminderHour, reminderMinute)
                        } else null
                        onSave(title, category, isStepBased, stepTarget, reminderEnabled, reminderTime)
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Save Changes" else "Add Habit")
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m -> reminderHour = h; reminderMinute = m; showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) }
    )
}

// ─── Daily Log Screen ─────────────────────────────────────────────────────────

@Composable
private fun DailyLogScreen(
    habits: List<Habit>,
    todayLogs: List<HabitLog>,
    todaySteps: Int,
    onLog: (Int, HabitStatus) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = todayLogs.count { it.status == HabitStatus.COMPLETED }
    val total = habits.size
    val allDone = total > 0 && completedCount == total

    val pending = habits.filter { habit ->
        val log = todayLogs.find { it.habitId == habit.habitId }
        log == null || log.status == HabitStatus.PENDING
    }
    val done = habits.filter { habit ->
        todayLogs.find { it.habitId == habit.habitId }?.let { it.status != HabitStatus.PENDING } == true
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Progress header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Habits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completedCount / $total completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) completedCount.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = allDone,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.weight(1f)
        ) {
            AllDoneState()
        }

        AnimatedVisibility(
            visible = !allDone,
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                if (pending.isNotEmpty()) {
                    item {
                        Text(
                            "Pending",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    items(pending, key = { it.habitId }) { habit ->
                        HabitLogCard(
                            habit = habit,
                            todaySteps = todaySteps,
                            log = todayLogs.find { it.habitId == habit.habitId },
                            onComplete = { onLog(habit.habitId, HabitStatus.COMPLETED) },
                            onSkip = { onLog(habit.habitId, HabitStatus.SKIPPED) }
                        )
                    }
                }

                if (done.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Completed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    items(done, key = { it.habitId }) { habit ->
                        HabitLogCard(
                            habit = habit,
                            todaySteps = todaySteps,
                            log = todayLogs.find { it.habitId == habit.habitId },
                            onComplete = {},
                            onSkip = {}
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Back to Habits")
        }
    }
}

@Composable
private fun HabitLogCard(
    habit: Habit,
    todaySteps: Int,
    log: HabitLog?,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val isDone = log != null && log.status != HabitStatus.PENDING
    val isAutoCompleted = isDone && habit.isStepBased && log?.status == HabitStatus.COMPLETED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = habit.category.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (isDone) {
                    Icon(
                        imageVector = if (log?.status == HabitStatus.COMPLETED) Icons.Filled.CheckCircle
                        else Icons.Filled.SkipNext,
                        contentDescription = null,
                        tint = if (log?.status == HabitStatus.COMPLETED) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Step progress for step-based habits
            if (habit.isStepBased && habit.stepTarget != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (todaySteps.toFloat() / habit.stepTarget).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%,d / %,d steps".format(todaySteps, habit.stepTarget),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isAutoCompleted) {
                        Text(
                            text = "Auto-completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Action buttons — only for pending non-auto habits
            if (!isDone) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Completed")
                    }
                    OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.SkipNext, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Skip")
                    }
                }
            }
        }
    }
}

@Composable
private fun AllDoneState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "All done for today!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Great work. Come back tomorrow to keep your streak going.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun HabitCategory.icon(): ImageVector = when (this) {
    HabitCategory.HEALTH      -> Icons.Filled.Favorite
    HabitCategory.SLEEP       -> Icons.Filled.Bedtime
    HabitCategory.FITNESS     -> Icons.Filled.DirectionsRun
    HabitCategory.MINDFULNESS -> Icons.Filled.SelfImprovement
    HabitCategory.CUSTOM      -> Icons.Filled.CheckCircle
}

private fun HabitCategory.label(): String = when (this) {
    HabitCategory.HEALTH      -> "Health"
    HabitCategory.SLEEP       -> "Sleep"
    HabitCategory.FITNESS     -> "Fitness"
    HabitCategory.MINDFULNESS -> "Mindfulness"
    HabitCategory.CUSTOM      -> "Custom"
}

private fun formatTime(hhmm: String): String {
    val parts = hhmm.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return hhmm
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: return hhmm
    val ampm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0  -> 12
        hour > 12  -> hour - 12
        else       -> hour
    }
    return "$displayHour:%02d $ampm".format(minute)
}
