package com.example.kinet.ui.dashboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.usecase.GetTodayActivityUseCase
import com.example.kinet.domain.usecase.GetWeeklyActivitiesUseCase
import com.example.kinet.engine.StepSessionState
import com.example.kinet.engine.TrackingState
import com.example.kinet.service.StepTrackingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    getTodayActivity: GetTodayActivityUseCase,
    getWeeklyActivities: GetWeeklyActivitiesUseCase,
    repository: ActivityRepository,
    private val appContext: Context
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

    val isWalkingSession: StateFlow<Boolean> = StepSessionState.isWalking
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val isPaused: StateFlow<Boolean> = TrackingState.isPaused
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun pause() = sendAction(StepTrackingService.ACTION_PAUSE)

    fun resume() = sendAction(StepTrackingService.ACTION_RESUME)

    fun resetSteps() = sendAction(StepTrackingService.ACTION_RESET)

    private fun sendAction(action: String) {
        val intent = Intent(appContext, StepTrackingService::class.java).apply {
            this.action = action
        }
        appContext.startService(intent)
    }
}
