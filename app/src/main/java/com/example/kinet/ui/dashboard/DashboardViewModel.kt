package com.example.kinet.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.usecase.GetTodayActivityUseCase
import com.example.kinet.domain.usecase.GetWeeklyActivitiesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    getTodayActivity: GetTodayActivityUseCase,
    getWeeklyActivities: GetWeeklyActivitiesUseCase
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
}
