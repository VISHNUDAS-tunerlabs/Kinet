package com.example.kinet.domain.model

data class Habit(
    val habitId: Int = 0,
    val title: String,
    val category: HabitCategory,
    val isCustom: Boolean,
    val isStepBased: Boolean,
    val stepTarget: Int?,
    val reminderEnabled: Boolean,
    val reminderTime: String?,   // "HH:mm" format
    val isActive: Boolean,
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val cardColor: String = "FFFFFF"  // hex without '#'
)

data class HabitLog(
    val habitId: Int,
    val date: String,            // "yyyy-MM-dd" format
    val status: HabitStatus
)

enum class HabitCategory { HEALTH, SLEEP, FITNESS, MINDFULNESS, CUSTOM }

enum class HabitStatus { COMPLETED, SKIPPED, PENDING }
