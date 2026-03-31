package com.example.kinet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kinet.data.local.dao.DailyActivityDao
import com.example.kinet.data.local.dao.UserProfileDao
import com.example.kinet.data.local.entity.DailyActivityEntity
import com.example.kinet.data.local.entity.UserProfileEntity

@Database(
    entities = [DailyActivityEntity::class, UserProfileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KinetDatabase : RoomDatabase() {

    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun userProfileDao(): UserProfileDao

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

        fun getInstance(context: Context): KinetDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KinetDatabase::class.java,
                    "kinet_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
