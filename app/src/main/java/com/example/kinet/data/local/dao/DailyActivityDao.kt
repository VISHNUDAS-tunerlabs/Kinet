package com.example.kinet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kinet.data.local.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivityDao {

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun getByDate(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT 7")
    fun getLastSevenDays(): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyActivityEntity)

    @Query("UPDATE daily_activity SET steps = 0, distanceMeters = 0, calories = 0, activeMinutes = 0 WHERE date = :date")
    suspend fun resetForDate(date: String)
}
