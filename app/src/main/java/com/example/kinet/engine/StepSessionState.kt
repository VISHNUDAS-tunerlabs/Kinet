package com.example.kinet.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-wide singleton that holds the live walking session state.
 * Updated by [com.example.kinet.service.StepTrackingService] on every step detector
 * event; observed by [com.example.kinet.ui.dashboard.DashboardViewModel].
 */
object StepSessionState {
    private val _isWalking = MutableStateFlow(false)
    val isWalking = _isWalking.asStateFlow()

    fun update(walking: Boolean) {
        _isWalking.value = walking
    }
}
