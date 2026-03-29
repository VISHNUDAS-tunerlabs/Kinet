package com.example.kinet.data.repository

import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getTodayActivity(): Flow<DailyActivity>
    fun getWeeklyActivities(): Flow<List<DailyActivity>>
    fun getUserProfile(): Flow<UserProfile>
    fun isProfileSet(): Flow<Boolean>
    suspend fun updateTodaySteps(steps: Int)
    suspend fun saveUserProfile(profile: UserProfile)
}
