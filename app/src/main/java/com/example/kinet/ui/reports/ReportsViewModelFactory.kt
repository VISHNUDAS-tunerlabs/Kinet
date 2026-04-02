package com.example.kinet.ui.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.data.repository.HabitRepositoryImpl
import com.example.kinet.engine.MetricsEngine

class ReportsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = KinetDatabase.getInstance(context)
        val activityRepo = ActivityRepositoryImpl(
            activityDao = db.dailyActivityDao(),
            profileDao = db.userProfileDao(),
            metricsEngine = MetricsEngine()
        )
        val habitRepo = HabitRepositoryImpl(db.habitDao())
        return ReportsViewModel(activityRepo, habitRepo) as T
    }
}
