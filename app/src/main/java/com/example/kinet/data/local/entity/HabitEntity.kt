package com.example.kinet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitCategory

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val habitId: Int = 0,
    val title: String,
    val category: String,
    val isCustom: Boolean,
    val isStepBased: Boolean,
    val stepTarget: Int?,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val isActive: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val streakCount: Int = 0,
    val bestStreak: Int = 0
)

fun HabitEntity.toDomain() = Habit(
    habitId = habitId,
    title = title,
    category = HabitCategory.entries.find { it.name == category } ?: HabitCategory.CUSTOM,
    isCustom = isCustom,
    isStepBased = isStepBased,
    stepTarget = stepTarget,
    reminderEnabled = reminderEnabled,
    reminderTime = reminderTime,
    isActive = isActive,
    streakCount = streakCount,
    bestStreak = bestStreak
)
