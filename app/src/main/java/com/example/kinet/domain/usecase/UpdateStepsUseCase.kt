package com.example.kinet.domain.usecase

import com.example.kinet.data.repository.ActivityRepository

class UpdateStepsUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(steps: Int) = repository.updateTodaySteps(steps)
}
