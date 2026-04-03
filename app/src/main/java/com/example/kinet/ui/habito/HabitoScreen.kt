package com.example.kinet.ui.habito

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitCategory
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

// ─── Entry point ─────────────────────────────────────────────────────────────

@Composable
fun HabitoScreen(
    viewModel: HabitoViewModel,
    userName: String,
    profileImageUri: String?,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subScreen by viewModel.subScreen.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val todayLogs by viewModel.todayLogs.collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val editingHabit by viewModel.editingHabit.collectAsState()
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    val last30DaysLogs by viewModel.last30DaysLogs.collectAsState()

    when (subScreen) {
        HabitoSubScreen.LIST -> HabitListScreen(
            habits = habits,
            todayLogs = todayLogs,
            userName = userName,
            profileImageUri = profileImageUri,
            onOpenProfile = onOpenProfile,
            onAddHabit = { viewModel.navigateTo(HabitoSubScreen.ADD_EDIT) },
            onHabitClick = { viewModel.navigateTo(HabitoSubScreen.HABIT_DETAILS, it) },
            onToggleComplete = { habitId ->
                val log = todayLogs.find { it.habitId == habitId }
                val next = if (log?.status == HabitStatus.COMPLETED) HabitStatus.PENDING
                           else HabitStatus.COMPLETED
                viewModel.logHabit(habitId, next)
            },
            onDeleteHabit = { viewModel.deleteHabit(it) },
            onMarkToday = { viewModel.navigateTo(HabitoSubScreen.DAILY_LOG) },
            modifier = modifier
        )
        HabitoSubScreen.ADD_EDIT -> AddEditHabitScreen(
            editingHabit = editingHabit,
            onSave = { title, category, isStepBased, stepTarget, reminderEnabled, reminderTime, cardColor ->
                viewModel.saveHabit(title, category, isStepBased, stepTarget, reminderEnabled, reminderTime, cardColor)
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
        HabitoSubScreen.HABIT_DETAILS -> {
            val habit = selectedHabit
            if (habit != null) {
                HabitDetailsScreen(
                    habit = habit,
                    todayLog = todayLogs.find { it.habitId == habit.habitId },
                    last30DaysLogs = last30DaysLogs,
                    onBack = { viewModel.navigateTo(HabitoSubScreen.LIST) },
                    onEdit = { viewModel.navigateTo(HabitoSubScreen.ADD_EDIT, habit) },
                    onMarkCompleted = {
                        viewModel.logHabit(habit.habitId, HabitStatus.COMPLETED)
                        viewModel.navigateTo(HabitoSubScreen.LIST)
                    },
                    modifier = modifier
                )
            }
        }
    }
}

// ─── Habit List Screen ────────────────────────────────────────────────────────

@Composable
private fun HabitListScreen(
    habits: List<Habit>,
    todayLogs: List<HabitLog>,
    userName: String,
    profileImageUri: String?,
    onOpenProfile: () -> Unit,
    onAddHabit: () -> Unit,
    onHabitClick: (Habit) -> Unit,
    onToggleComplete: (Int) -> Unit,
    onDeleteHabit: (Int) -> Unit,
    onMarkToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitRows = habits.chunked(2)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Greeting header
        item {
            GreetingHeader(
                userName = userName,
                profileImageUri = profileImageUri,
                onOpenProfile = onOpenProfile,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // Week date strip
        item {
            Spacer(Modifier.height(12.dp))
            WeekDateStrip(modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(16.dp))
        }

        // Today's overview card
        item {
            TodayOverviewCard(
                habits = habits,
                todayLogs = todayLogs,
                onTap = onMarkToday,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // Habit grid — chunked into rows of 2
        if (habits.isEmpty()) {
            item {
                EmptyHabitsState(
                    onAddHabit = onAddHabit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        } else {
            itemsIndexed(habitRows, key = { _, row -> row.first().habitId }) { _, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { habit ->
                        val log = todayLogs.find { it.habitId == habit.habitId }
                        HabitGridCard(
                            habit = habit,
                            todayStatus = log?.status,
                            onCardClick = { onHabitClick(habit) },
                            onToggleComplete = { onToggleComplete(habit.habitId) },
                            onDelete = { onDeleteHabit(habit.habitId) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ─── Greeting Header ──────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(
    userName: String,
    profileImageUri: String?,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName.ifBlank { "there" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = { /* notifications — phase 2 */ }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenProfile) {
                if (profileImageUri != null) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(profileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = "Profile")
                }
            }
        }
    }
}

// ─── Week Date Strip ──────────────────────────────────────────────────────────

@Composable
private fun WeekDateStrip(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    // Show: today-2, today-1, today, today+1, today+2
    val days = remember { (-2..2).map { today.plusDays(it.toLong()) } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            val isToday = day == today
            val isPast  = day.isBefore(today)

            val bgColor = when {
                isToday -> Color(0xFF1A1A1A)
                isPast  -> Color(0xFF4DD631)
                else    -> Color.White
            }
            val dateColor = when {
                isToday -> Color.White
                else    -> Color(0xFF0D0D0D)   // black for both past and future
            }
            val dayColor = when {
                isToday -> Color.White.copy(alpha = 0.75f)
                else    -> Color(0xFF0D0D0D).copy(alpha = 0.7f)
            }

            // weight(1f) distributes equally; horizontal padding slims each card; vertical padding stretches height
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(100.dp),
                color = bgColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = dateColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = dayColor
                    )
                }
            }
        }
    }
}

// ─── Today's Overview Card ────────────────────────────────────────────────────

@Composable
private fun TodayOverviewCard(
    habits: List<Habit>,
    todayLogs: List<HabitLog>,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = todayLogs.count { it.status == HabitStatus.COMPLETED }
    val total = habits.size
    val progress = if (total > 0) completedCount.toFloat() / total else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(34.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$completedCount of $total habits completed",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(Color(0xFF4DD631))
                )
            }
            if (completedCount == 0 && total > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "You haven't completed any habits yet today. Let's get started!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Habit Grid Card ──────────────────────────────────────────────────────────

@Composable
private fun HabitGridCard(
    habit: Habit,
    todayStatus: HabitStatus?,
    onCardClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isCompleted = todayStatus == HabitStatus.COMPLETED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = runCatching { Color(android.graphics.Color.parseColor("#${habit.cardColor}")) }
                .getOrDefault(Color.White)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Top row: icon (left) · delete + checkbox (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = habit.category.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF333333)
                        )
                    }
                }

                // Delete + checkbox grouped on the right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF1A1A1A)
                        )
                    }

                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Uncheck",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(1.5.dp, Color(0xFF1A1A1A), CircleShape)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Subtitle
            val subtitle = when {
                habit.isStepBased && habit.stepTarget != null -> "%,d steps".format(habit.stepTarget)
                habit.reminderEnabled && habit.reminderTime != null -> formatTime(habit.reminderTime)
                else -> habit.category.label()
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Bottom chips: reminder time + streak
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (habit.reminderEnabled && habit.reminderTime != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Alarm, null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = formatTime(habit.reminderTime),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment, null,
                        modifier = Modifier.size(15.dp),
                        tint = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = habit.streakCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1A1A1A)
                    )
                }
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

// ─── Habit Details Screen ─────────────────────────────────────────────────────

@Composable
private fun HabitDetailsScreen(
    habit: Habit,
    todayLog: HabitLog?,
    last30DaysLogs: List<HabitLog>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMarkCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedInLast30 = last30DaysLogs.count {
        it.habitId == habit.habitId && it.status == HabitStatus.COMPLETED
    }
    val completionRate = (completedInLast30 / 30f).coerceAtMost(1f)
    val completionPercent = (completionRate * 100).toInt()
    val isCompletedToday = todayLog?.status == HabitStatus.COMPLETED

    Column(modifier = modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Habit Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Hero card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD6E4FF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = habit.category.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFF333333)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1A1A1A)
                        )
                        if (habit.reminderEnabled && habit.reminderTime != null) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = Color.White.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Alarm, null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF555555)
                                    )
                                    Text(
                                        text = "${formatTime(habit.reminderTime)} • ${habit.category.label()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF555555)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overview: current streak + best streak
            item {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current streak
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = if (habit.streakCount > 0) Color(0xFFFF6B35)
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = habit.streakCount.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Best streak
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = habit.bestStreak.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Best Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Completion rate
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Filled.TrackChanges,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Completion Rate",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Last 30 days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "$completionPercent%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { completionRate },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Mark as Completed CTA
        Button(
            onClick = onMarkCompleted,
            enabled = !isCompletedToday,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A1A1A),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Filled.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isCompletedToday) "Already Completed" else "Mark as Completed",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyHabitsState(onAddHabit: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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

private val habitCardColorOptions = listOf("4DD631", "FCB932", "FFFFFF", "81AEFC")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditHabitScreen(
    editingHabit: Habit?,
    onSave: (String, HabitCategory, Boolean, Int?, Boolean, String?, String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = editingHabit != null

    var title by rememberSaveable { mutableStateOf(editingHabit?.title ?: "") }
    var category by rememberSaveable { mutableStateOf(editingHabit?.category ?: HabitCategory.HEALTH) }
    var selectedColor by rememberSaveable { mutableStateOf(editingHabit?.cardColor ?: "FFFFFF") }
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

        item {
            Text(
                "Card color",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                habitCardColorOptions.forEach { hex ->
                    val color = runCatching {
                        Color(android.graphics.Color.parseColor("#$hex"))
                    }.getOrDefault(Color.White)
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF333333) else Color(0xFFCCCCCC),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                            )
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF333333),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

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
                        onSave(title, category, isStepBased, stepTarget, reminderEnabled, reminderTime, selectedColor)
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

private fun HabitCategory.cardColor(): Color = when (this) {
    HabitCategory.HEALTH      -> Color(0xFFD6E8FF)
    HabitCategory.SLEEP       -> Color(0xFFF5F5F5)
    HabitCategory.FITNESS     -> Color(0xFFD4EDDA)
    HabitCategory.MINDFULNESS -> Color(0xFFFFF3CD)
    HabitCategory.CUSTOM      -> Color(0xFFEEEEEE)
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
