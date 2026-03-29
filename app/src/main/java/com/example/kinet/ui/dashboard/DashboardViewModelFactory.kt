package com.example.kinet.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.domain.usecase.GetTodayActivityUseCase
import com.example.kinet.domain.usecase.GetWeeklyActivitiesUseCase
import com.example.kinet.engine.MetricsEngine

class DashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = KinetDatabase.getInstance(context)
        val repository = ActivityRepositoryImpl(
            activityDao = db.dailyActivityDao(),
            profileDao = db.userProfileDao(),
            metricsEngine = MetricsEngine()
        )
        return DashboardViewModel(
            getTodayActivity = GetTodayActivityUseCase(repository),
            getWeeklyActivities = GetWeeklyActivitiesUseCase(repository)
        ) as T
    }
}
