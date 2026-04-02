package com.example.kinet.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.data.repository.HabitRepository
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HabitWeekStats(
    val habit: Habit,
    /** One entry per day, oldest first. Null = no log for that day. */
    val weekLogs: List<HabitStatus?>,
    val completedDays: Int,
    val completionRate: Float
)

/**
 * Represents one cell in the GitHub-style heatmap grid.
 * @param date         ISO "yyyy-MM-dd"
 * @param completed    number of habits completed this day
 * @param total        total active habits (used as denominator)
 * @param rate         0..1; NaN-safe — 0 when total == 0
 * @param isFuture     true for dates after today (drawn as empty)
 */
data class HeatmapDay(
    val date: String,
    val completed: Int,
    val total: Int,
    val rate: Float,
    val isFuture: Boolean
)

data class ReportsUiState(
    val weeklyActivities: List<DailyActivity> = emptyList(),
    val totalSteps: Int = 0,
    val avgStepsPerDay: Int = 0,
    val bestDaySteps: Int = 0,
    val totalDistanceKm: Float = 0f,
    val totalCalories: Float = 0f,
    val totalActiveMinutes: Int = 0,
    val habitStats: List<HabitWeekStats> = emptyList(),
    /** 16 columns (weeks) × 7 rows (Mon–Sun), oldest column first. */
    val heatmapWeeks: List<List<HeatmapDay>> = emptyList()
)

class ReportsViewModel(
    activityRepository: ActivityRepository,
    habitRepository: HabitRepository
) : ViewModel() {

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Date ranges ────────────────────────────────────────────────────────────

    /** 7 date strings: 6 days ago → today, for per-habit week stats. */
    private val weekDates: List<String> = buildDateRange(daysBack = 6)

    /**
     * The 16-week heatmap grid as week-columns.
     * Each column = List<String> of 7 ISO dates (Mon..Sun).
     * Col 0 is oldest; col 15 contains today.
     */
    private val heatmapGrid: List<List<String>> = buildHeatmapGrid()

    /** Earliest date in the heatmap — used as the Flow start date. */
    private val heatmapStartDate: String = heatmapGrid.first().first()

    // ── Streams ────────────────────────────────────────────────────────────────

    val uiState: StateFlow<ReportsUiState> = combine(
        activityRepository.getWeeklyActivities(),
        habitRepository.getActiveHabits(),
        habitRepository.getLogsSince(heatmapStartDate)
    ) { weekly, habits, logs ->

        // Step summaries
        val activeDays = weekly.filter { it.steps > 0 }
        val totalSteps = weekly.sumOf { it.steps }
        val avgSteps = if (activeDays.isNotEmpty()) totalSteps / activeDays.size else 0
        val bestDay = weekly.maxOfOrNull { it.steps } ?: 0
        val totalDistKm = weekly.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f
        val totalCal = weekly.sumOf { it.calories.toDouble() }.toFloat()
        val totalActive = weekly.sumOf { it.activeMinutes }

        // Group all logs by habitId → date
        val logsByHabit: Map<Int, Map<String, HabitLog>> = logs
            .groupBy { it.habitId }
            .mapValues { (_, list) -> list.associateBy { it.date } }

        // Per-habit last-7-day stats
        val habitStats = habits.map { habit ->
            val habitLogs = logsByHabit[habit.habitId] ?: emptyMap()
            val weekLogs = weekDates.map { date -> habitLogs[date]?.status }
            val completedDays = weekLogs.count { it == HabitStatus.COMPLETED }
            HabitWeekStats(
                habit = habit,
                weekLogs = weekLogs,
                completedDays = completedDays,
                completionRate = completedDays / 7f
            )
        }

        // Heatmap: group logs by date, count COMPLETED per day
        val completedByDate: Map<String, Int> = logs
            .filter { it.status == HabitStatus.COMPLETED }
            .groupBy { it.date }
            .mapValues { (_, list) -> list.size }

        val today = fmt.format(Date())
        val totalHabits = habits.size

        val heatmapWeeks = heatmapGrid.map { weekDays ->
            weekDays.map { date ->
                val completed = completedByDate[date] ?: 0
                val isFuture = date > today
                val rate = if (!isFuture && totalHabits > 0)
                    (completed.toFloat() / totalHabits).coerceAtMost(1f)
                else 0f
                HeatmapDay(date, completed, totalHabits, rate, isFuture)
            }
        }

        ReportsUiState(
            weeklyActivities = weekly,
            totalSteps = totalSteps,
            avgStepsPerDay = avgSteps,
            bestDaySteps = bestDay,
            totalDistanceKm = totalDistKm,
            totalCalories = totalCal,
            totalActiveMinutes = totalActive,
            habitStats = habitStats,
            heatmapWeeks = heatmapWeeks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState()
    )

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun buildDateRange(daysBack: Int): List<String> {
        val cal = Calendar.getInstance()
        return (daysBack downTo 0).map { offset ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            fmt.format(cal.time)
        }
    }

    /**
     * Builds a 16 × 7 grid of ISO date strings.
     * Col 0 starts on the Monday that is exactly 15 weeks before the
     * Monday of the current week, so col 15 always contains today's week.
     */
    private fun buildHeatmapGrid(): List<List<String>> {
        val cal = Calendar.getInstance()
        // Snap to Monday of the current week
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val toMonday = when (dayOfWeek) {
            Calendar.SUNDAY -> -6
            else -> -(dayOfWeek - Calendar.MONDAY)
        }
        cal.add(Calendar.DAY_OF_YEAR, toMonday)
        // Go back 15 more weeks to get the start of the 16-week window
        cal.add(Calendar.WEEK_OF_YEAR, -15)

        return (0 until 16).map { _ ->
            val week = (0 until 7).map { day ->
                val date = fmt.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 1)
                date
            }
            week
        }
    }
}
