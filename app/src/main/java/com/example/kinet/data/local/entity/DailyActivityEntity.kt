package com.example.kinet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kinet.domain.model.DailyActivity

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val distanceMeters: Float,
    val calories: Float,
    val activeMinutes: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

fun DailyActivityEntity.toDomain() = DailyActivity(
    date = date,
    steps = steps,
    distanceMeters = distanceMeters,
    calories = calories,
    activeMinutes = activeMinutes
)
