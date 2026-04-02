package com.example.kinet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kinet.data.local.dao.DailyActivityDao
import com.example.kinet.data.local.dao.HabitDao
import com.example.kinet.data.local.dao.UserProfileDao
import com.example.kinet.data.local.entity.DailyActivityEntity
import com.example.kinet.data.local.entity.HabitEntity
import com.example.kinet.data.local.entity.HabitLogEntity
import com.example.kinet.data.local.entity.UserProfileEntity

@Database(
    entities = [
        DailyActivityEntity::class,
        UserProfileEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class KinetDatabase : RoomDatabase() {

    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: KinetDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_profile ADD COLUMN dailyStepGoal INTEGER NOT NULL DEFAULT 10000"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_profile ADD COLUMN profileImageUri TEXT"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits (
                        habitId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isCustom INTEGER NOT NULL,
                        isStepBased INTEGER NOT NULL,
                        stepTarget INTEGER,
                        reminderEnabled INTEGER NOT NULL,
                        reminderTime TEXT,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        streakCount INTEGER NOT NULL DEFAULT 0,
                        bestStreak INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habit_logs (
                        habitId INTEGER NOT NULL,
                        date TEXT NOT NULL,
                        status TEXT NOT NULL,
                        PRIMARY KEY (habitId, date)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_profile ADD COLUMN name TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): KinetDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KinetDatabase::class.java,
                    "kinet_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
    }
}
