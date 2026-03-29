package com.example.kinet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.domain.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun saveProfile(heightCm: Float, weightKg: Float, strideLengthCm: Float) {
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(heightCm, weightKg, strideLengthCm))
        }
    }
}
