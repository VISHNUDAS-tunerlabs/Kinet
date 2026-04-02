package com.example.kinet.ui.habito

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.data.repository.HabitRepository
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitCategory
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus
import com.example.kinet.service.HabitReminderReceiver
import com.example.kinet.service.HabitReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HabitoSubScreen { LIST, ADD_EDIT, DAILY_LOG }

class HabitoViewModel(
    private val habitRepository: HabitRepository,
    private val activityRepository: ActivityRepository,
    private val appContext: Context
) : ViewModel() {

    private val _subScreen = MutableStateFlow(HabitoSubScreen.LIST)
    val subScreen: StateFlow<HabitoSubScreen> = _subScreen.asStateFlow()

    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit.asStateFlow()

    val habits: StateFlow<List<Habit>> = habitRepository.getActiveHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayLogs: StateFlow<List<HabitLog>> = habitRepository.getTodayLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todaySteps: StateFlow<Int> = activityRepository.getTodayActivity()
        .map { it.steps }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        HabitReminderReceiver.ensureChannel(appContext)
        // Auto-complete step-based habits when step target is reached
        viewModelScope.launch {
            combine(habits, todayLogs, todaySteps) { h, l, s -> Triple(h, l, s) }
                .collect { (activeHabits, logs, steps) ->
                    activeHabits
                        .filter { it.isStepBased && it.stepTarget != null }
                        .forEach { habit ->
                            val existing = logs.find { it.habitId == habit.habitId }
                            val alreadyDone = existing != null && existing.status != HabitStatus.PENDING
                            if (!alreadyDone && steps >= (habit.stepTarget ?: Int.MAX_VALUE)) {
                                habitRepository.logHabit(habit.habitId, HabitStatus.COMPLETED)
                            }
                        }
                }
        }
    }

    fun navigateTo(screen: HabitoSubScreen, habit: Habit? = null) {
        _editingHabit.value = habit
        _subScreen.value = screen
    }

    fun saveHabit(
        title: String,
        category: HabitCategory,
        isStepBased: Boolean,
        stepTarget: Int?,
        reminderEnabled: Boolean,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val existing = _editingHabit.value
            val habit = Habit(
                habitId = existing?.habitId ?: 0,
                title = title.trim(),
                category = category,
                isCustom = true,
                isStepBased = isStepBased,
                stepTarget = if (isStepBased) stepTarget else null,
                reminderEnabled = reminderEnabled,
                reminderTime = if (reminderEnabled) reminderTime else null,
                isActive = true,
                streakCount = existing?.streakCount ?: 0
            )
            val savedId = habitRepository.saveHabit(habit).toInt()

            // Cancel any existing alarm for this habit, then reschedule if needed
            HabitReminderScheduler.cancel(appContext, savedId)
            if (reminderEnabled && reminderTime != null) {
                HabitReminderScheduler.schedule(appContext, savedId, reminderTime)
            }

            _editingHabit.value = null
            _subScreen.value = HabitoSubScreen.LIST
        }
    }

    fun deleteHabit(habitId: Int) {
        viewModelScope.launch {
            HabitReminderScheduler.cancel(appContext, habitId)
            habitRepository.deleteHabit(habitId)
        }
    }

    fun logHabit(habitId: Int, status: HabitStatus) {
        viewModelScope.launch {
            habitRepository.logHabit(habitId, status)
        }
    }
}
