package com.example.kinet.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.engine.MetricsEngine
import com.example.kinet.engine.StepEngine
import com.example.kinet.sensor.StepSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackingService : Service() {

    private lateinit var sensorManager: StepSensorManager
    private lateinit var repository: ActivityRepositoryImpl
    private val stepEngine = StepEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = KinetDatabase.getInstance(this)
        repository = ActivityRepositoryImpl(
            activityDao = db.dailyActivityDao(),
            profileDao = db.userProfileDao(),
            metricsEngine = MetricsEngine()
        )
        sensorManager = StepSensorManager(this)
        restoreStepBase()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        sensorManager.start { sensorValue ->
            val todaySteps = stepEngine.process(sensorValue)
            persistStepBase(sensorValue)
            scope.launch {
                repository.updateTodaySteps(todaySteps)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // region Step base persistence (SharedPreferences)

    private fun todayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun prefs() = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    private fun restoreStepBase() {
        val prefs = prefs()
        val savedDate = prefs.getString(KEY_BASE_DATE, null)
        val savedBase = prefs.getLong(KEY_BASE_STEPS, -1L)
        if (savedDate == todayDate() && savedBase >= 0) {
            stepEngine.restoreBase(savedBase)
        }
        // If date changed, StepEngine will set a new base on first sensor event
    }

    private fun persistStepBase(sensorValue: Long) {
        val base = stepEngine.getBase()
        if (base < 0) return
        prefs().edit()
            .putString(KEY_BASE_DATE, todayDate())
            .putLong(KEY_BASE_STEPS, base)
            .apply()
    }

    // endregion

    // region Notification

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Tracks your steps in the background" }
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kinet")
            .setContentText("Tracking your steps")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    // endregion

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "kinet_step_tracking"
        private const val PREFS_NAME = "kinet_step_prefs"
        private const val KEY_BASE_DATE = "base_date"
        private const val KEY_BASE_STEPS = "base_steps"
    }
}
