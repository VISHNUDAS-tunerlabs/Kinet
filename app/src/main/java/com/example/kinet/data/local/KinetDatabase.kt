package com.example.kinet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kinet.data.local.dao.DailyActivityDao
import com.example.kinet.data.local.dao.UserProfileDao
import com.example.kinet.data.local.entity.DailyActivityEntity
import com.example.kinet.data.local.entity.UserProfileEntity

@Database(
    entities = [DailyActivityEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KinetDatabase : RoomDatabase() {

    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: KinetDatabase? = null

        fun getInstance(context: Context): KinetDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KinetDatabase::class.java,
                    "kinet_db"
                ).build().also { INSTANCE = it }
            }
    }
}
