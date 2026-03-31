package com.example.kinet.data.repository

import com.example.kinet.data.local.dao.DailyActivityDao
import com.example.kinet.data.local.dao.UserProfileDao
import com.example.kinet.data.local.entity.DailyActivityEntity
import com.example.kinet.data.local.entity.UserProfileEntity
import com.example.kinet.data.local.entity.toDomain
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.domain.model.UserProfile
import com.example.kinet.engine.MetricsEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityRepositoryImpl(
    private val activityDao: DailyActivityDao,
    private val profileDao: UserProfileDao,
    private val metricsEngine: MetricsEngine
) : ActivityRepository {

    private fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun getTodayActivity(): Flow<DailyActivity> =
        activityDao.getByDate(todayDate()).map { entity ->
            entity?.toDomain() ?: DailyActivity(todayDate(), 0, 0f, 0f, 0)
        }

    override fun getWeeklyActivities(): Flow<List<DailyActivity>> =
        activityDao.getLastSevenDays().map { list -> list.map { it.toDomain() } }

    override fun getUserProfile(): Flow<UserProfile> =
        profileDao.get().map { it?.toDomain() ?: UserProfile.Default }

    override fun isProfileSet(): Flow<Boolean> =
        profileDao.get().map { it != null }

    override suspend fun updateTodaySteps(steps: Int) {
        val profile = profileDao.get().first()?.toDomain() ?: UserProfile.Default
        val distance = metricsEngine.calculateDistanceMeters(steps, profile.strideLengthCm)
        val calories = metricsEngine.calculateCalories(steps, profile.weightKg)
        val activeMinutes = metricsEngine.calculateActiveMinutes(steps)
        activityDao.upsert(
            DailyActivityEntity(
                date = todayDate(),
                steps = steps,
                distanceMeters = distance,
                calories = calories,
                activeMinutes = activeMinutes,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        profileDao.upsert(
            UserProfileEntity(
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                strideLengthCm = profile.strideLengthCm,
                dailyStepGoal = profile.dailyStepGoal,
                profileImageUri = profile.profileImageUri,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
