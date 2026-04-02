package com.example.kinet.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.usecase.GetTodayActivityUseCase
import com.example.kinet.domain.usecase.GetWeeklyActivitiesUseCase
import com.example.kinet.engine.StepSessionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    getTodayActivity: GetTodayActivityUseCase,
    getWeeklyActivities: GetWeeklyActivitiesUseCase,
    repository: ActivityRepository
) : ViewModel() {

    val todayActivity: StateFlow<DailyActivity> = getTodayActivity()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyActivity("", 0, 0f, 0f, 0)
        )

    val weeklyActivities: StateFlow<List<DailyActivity>> = getWeeklyActivities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val stepGoal: StateFlow<Int> = repository.getUserProfile()
        .map { it.dailyStepGoal }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 10_000
        )

    /** True when the sensor layer is detecting an active walking session. */
    val isWalkingSession: StateFlow<Boolean> = StepSessionState.isWalking
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
}
