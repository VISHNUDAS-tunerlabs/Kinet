package com.example.kinet.domain.usecase

import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.domain.model.DailyActivity
import kotlinx.coroutines.flow.Flow

class GetTodayActivityUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<DailyActivity> = repository.getTodayActivity()
}
