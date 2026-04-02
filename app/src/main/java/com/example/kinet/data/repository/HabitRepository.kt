package com.example.kinet.data.repository

import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getActiveHabits(): Flow<List<Habit>>
    fun getTodayLogs(): Flow<List<HabitLog>>
    fun getLogsSince(startDate: String): Flow<List<HabitLog>>
    suspend fun saveHabit(habit: Habit): Long
    suspend fun deleteHabit(habitId: Int)
    suspend fun logHabit(habitId: Int, status: HabitStatus)
}
