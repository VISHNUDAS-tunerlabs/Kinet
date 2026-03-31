package com.example.kinet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kinet.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, // singleton row
    val heightCm: Float,
    val weightKg: Float,
    val strideLengthCm: Float,
    val dailyStepGoal: Int = 10_000,
    val profileImageUri: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toDomain() = UserProfile(
    heightCm = heightCm,
    weightKg = weightKg,
    strideLengthCm = strideLengthCm,
    dailyStepGoal = dailyStepGoal,
    profileImageUri = profileImageUri
)
