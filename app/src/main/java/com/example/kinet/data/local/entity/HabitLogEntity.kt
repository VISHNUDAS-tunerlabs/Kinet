package com.example.kinet.data.local.entity

import androidx.room.Entity
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus

@Entity(
    tableName = "habit_logs",
    primaryKeys = ["habitId", "date"]
)
data class HabitLogEntity(
    val habitId: Int,
    val date: String,    // "yyyy-MM-dd"
    val status: String   // HabitStatus.name
)

fun HabitLogEntity.toDomain() = HabitLog(
    habitId = habitId,
    date = date,
    status = HabitStatus.entries.find { it.name == status } ?: HabitStatus.PENDING
)
