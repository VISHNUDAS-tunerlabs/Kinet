package com.example.kinet.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.data.repository.HabitRepository
import com.example.kinet.domain.model.UserProfile
import com.example.kinet.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: ActivityRepository,
    private val habitRepository: HabitRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

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

    /** Highest current streak across all active habits. */
    val currentStreak: StateFlow<Int> = habitRepository.getActiveHabits()
        .map { habits -> habits.maxOfOrNull { it.streakCount } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** All-time best streak across all active habits. */
    val bestStreak: StateFlow<Int> = habitRepository.getActiveHabits()
        .map { habits -> habits.maxOfOrNull { it.bestStreak } ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Profile view (details page)
    private val _showProfile = MutableStateFlow(false)
    val showProfile: StateFlow<Boolean> = _showProfile.asStateFlow()

    fun openProfile() { _showProfile.value = true }
    fun closeProfile() { _showProfile.value = false }

    // Profile edit (edit form, launched from profile view)
    private val _showProfileEdit = MutableStateFlow(false)
    val showProfileEdit: StateFlow<Boolean> = _showProfileEdit.asStateFlow()

    fun openProfileEdit() { _showProfileEdit.value = true }
    fun closeProfileEdit() { _showProfileEdit.value = false }

    // Calibration
    private val _showCalibration = MutableStateFlow(false)
    val showCalibration: StateFlow<Boolean> = _showCalibration.asStateFlow()

    fun openCalibration() { _showCalibration.value = true }
    fun closeCalibration() { _showCalibration.value = false }

    // App theme (persisted in SharedPreferences)
    private val _appTheme = MutableStateFlow(
        AppTheme.entries.getOrElse(prefs.getInt("app_theme", 0)) { AppTheme.DYNAMIC }
    )
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _appTheme.value = theme
        prefs.edit().putInt("app_theme", theme.ordinal).apply()
    }

    fun saveProfile(name: String, heightCm: Float, weightKg: Float, strideLengthCm: Float, dailyStepGoal: Int) {
        viewModelScope.launch {
            repository.saveUserProfile(
                userProfile.value.copy(
                    name = name,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    strideLengthCm = strideLengthCm,
                    dailyStepGoal = dailyStepGoal
                )
            )
            _showProfileEdit.value = false
        }
    }

    fun saveProfileImage(uri: String) {
        viewModelScope.launch {
            repository.saveUserProfile(userProfile.value.copy(profileImageUri = uri))
        }
    }
}
