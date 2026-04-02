package com.example.kinet.ui.habito

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.data.repository.HabitRepositoryImpl
import com.example.kinet.engine.MetricsEngine

class HabitoViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = KinetDatabase.getInstance(context)
        val habitRepository = HabitRepositoryImpl(db.habitDao())
        val activityRepository = ActivityRepositoryImpl(
            activityDao = db.dailyActivityDao(),
            profileDao = db.userProfileDao(),
            metricsEngine = MetricsEngine()
        )
        return HabitoViewModel(
            habitRepository = habitRepository,
            activityRepository = activityRepository,
            appContext = context.applicationContext
        ) as T
    }
}
