package com.example.kinet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kinet.data.local.entity.HabitEntity
import com.example.kinet.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1 AND reminderEnabled = 1 AND reminderTime IS NOT NULL")
    suspend fun getHabitsWithReminders(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE habitId = :habitId")
    suspend fun getById(habitId: Int): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(habit: HabitEntity): Long

    @Query("UPDATE habits SET isActive = 0, updatedAt = :now WHERE habitId = :habitId")
    suspend fun softDelete(habitId: Int, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsByDate(date: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date >= :startDate ORDER BY date ASC")
    fun getLogsSince(startDate: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getLogsByHabitId(habitId: Int): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceLog(log: HabitLogEntity)

    @Query("UPDATE habits SET streakCount = :streak, bestStreak = :best WHERE habitId = :habitId")
    suspend fun updateStreaks(habitId: Int, streak: Int, best: Int)
}
