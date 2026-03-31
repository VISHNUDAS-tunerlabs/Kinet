package com.example.kinet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ActivityRepository) : ViewModel() {

    // null = loading, false = needs setup, true = ready
    val isProfileSet: StateFlow<Boolean?> = repository.isProfileSet()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val userProfile: StateFlow<UserProfile> = repository.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserProfile.Default
        )

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    private val _showProfileEdit = MutableStateFlow(false)
    val showProfileEdit: StateFlow<Boolean> = _showProfileEdit.asStateFlow()

    fun openProfileEdit() {
        _showProfileEdit.value = true
    }

    fun closeProfileEdit() {
        _showProfileEdit.value = false
    }

    private val _showCalibration = MutableStateFlow(false)
    val showCalibration: StateFlow<Boolean> = _showCalibration.asStateFlow()

    fun openCalibration() {
        _showCalibration.value = true
    }

    fun closeCalibration() {
        _showCalibration.value = false
    }

    fun saveProfile(heightCm: Float, weightKg: Float, strideLengthCm: Float, dailyStepGoal: Int) {
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(heightCm, weightKg, strideLengthCm, dailyStepGoal))
            _showProfileEdit.value = false
        }
    }
}
