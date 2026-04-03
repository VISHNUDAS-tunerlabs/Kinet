package com.example.kinet.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide singleton that holds the tracking paused/running state.
 * Updated by [com.example.kinet.service.StepTrackingService] on pause/resume/reset;
 * observed by [com.example.kinet.ui.dashboard.DashboardViewModel].
 */
object TrackingState {
    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    fun update(paused: Boolean) {
        _isPaused.value = paused
    }
}
