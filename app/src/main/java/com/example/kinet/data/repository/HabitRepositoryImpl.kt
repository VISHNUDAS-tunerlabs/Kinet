package com.example.kinet.data.repository

import com.example.kinet.data.local.dao.HabitDao
import com.example.kinet.data.local.entity.HabitEntity
import com.example.kinet.data.local.entity.HabitLogEntity
import com.example.kinet.data.local.entity.toDomain
import com.example.kinet.domain.model.Habit
import com.example.kinet.domain.model.HabitLog
import com.example.kinet.domain.model.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepositoryImpl(private val dao: HabitDao) : HabitRepository {

    private fun todayDate() =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun getActiveHabits(): Flow<List<Habit>> =
        dao.getActiveHabits().map { list -> list.map { it.toDomain() } }

    override fun getTodayLogs(): Flow<List<HabitLog>> =
        dao.getLogsByDate(todayDate()).map { list -> list.map { it.toDomain() } }

    override fun getLogsSince(startDate: String): Flow<List<HabitLog>> =
        dao.getLogsSince(startDate).map { list -> list.map { it.toDomain() } }

    override suspend fun saveHabit(habit: Habit): Long =
        dao.insertOrReplace(
            HabitEntity(
                habitId = habit.habitId,
                title = habit.title,
                category = habit.category.name,
                isCustom = habit.isCustom,
                isStepBased = habit.isStepBased,
                stepTarget = habit.stepTarget,
                reminderEnabled = habit.reminderEnabled,
                reminderTime = habit.reminderTime,
                isActive = habit.isActive,
                updatedAt = System.currentTimeMillis(),
                streakCount = habit.streakCount
            )
        )

    override suspend fun deleteHabit(habitId: Int) = dao.softDelete(habitId)

    override suspend fun logHabit(habitId: Int, status: HabitStatus) {
        dao.insertOrReplaceLog(
            HabitLogEntity(habitId = habitId, date = todayDate(), status = status.name)
        )
        recalculateStreak(habitId)
    }

    private suspend fun recalculateStreak(habitId: Int) {
        val logs = dao.getLogsByHabitId(habitId)
            .associateBy { it.date }   // date → log

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        var streak = 0
        // Walk backwards day-by-day starting from today, up to 365 days
        for (i in 0 until 365) {
            val dateStr = fmt.format(cal.time)
            val log = logs[dateStr]
            if (log != null && log.status == HabitStatus.COMPLETED.name) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val existing = dao.getById(habitId) ?: return
        val best = maxOf(existing.bestStreak, streak)
        dao.updateStreaks(habitId, streak, best)
    }
}
