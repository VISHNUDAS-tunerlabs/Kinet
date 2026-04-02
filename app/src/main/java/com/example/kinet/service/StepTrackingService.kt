package com.example.kinet.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.kinet.MainActivity
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.engine.MetricsEngine
import com.example.kinet.engine.StepEngine
import com.example.kinet.engine.StepSessionState
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

    // Guard against starting multiple notification-update collectors on repeated
    // onStartCommand calls (START_STICKY restart, task-removed restart, etc.)
    private var collectingActivity = false

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

        // Collect live activity → update foreground notification with steps + calories
        if (!collectingActivity) {
            collectingActivity = true
            scope.launch {
                repository.getTodayActivity().collect { activity ->
                    getSystemService(NotificationManager::class.java)
                        ?.notify(NOTIFICATION_ID, buildNotification(activity.steps, activity.calories))
                }
            }
        }

        sensorManager.start(
            onStepCount = { sensorValue ->
                val todaySteps = stepEngine.process(sensorValue)
                persistStepBase(sensorValue)
                scope.launch {
                    repository.updateTodaySteps(todaySteps)
                }
            },
            onStepDetected = { eventTimeNs ->
                stepEngine.onStepDetected(eventTimeNs)
                // Publish walking session state for the dashboard badge
                StepSessionState.update(stepEngine.isWalkingSession)
            }
        )
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.stop()
        StepSessionState.update(false)
        scope.cancel()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartIntent = Intent(applicationContext, StepTrackingService::class.java)
        val pendingIntent = PendingIntent.getService(
            applicationContext, 1, restartIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(AlarmManager::class.java)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1_000L,
            pendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // region Step base persistence

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

    private fun tapIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your steps in the background"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds the foreground notification.
     *
     * When [steps] > 0 the content line shows live progress:
     *   "8,432 steps  ·  312 kcal"
     * Otherwise shows a ready-state placeholder.
     */
    private fun buildNotification(steps: Int = 0, calories: Float = 0f): Notification {
        ensureChannel()
        val contentText = if (steps > 0) {
            "%,d steps  ·  %.0f kcal".format(steps, calories)
        } else {
            "Ready to track your steps"
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kinet")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(tapIntent())
            .build()
    }

    // endregion

    companion object {
        const val NOTIFICATION_ID = 100   // foreground service — kept low to avoid collision
        private const val CHANNEL_ID = "kinet_step_tracking"
        private const val PREFS_NAME = "kinet_step_prefs"
        private const val KEY_BASE_DATE = "base_date"
        private const val KEY_BASE_STEPS = "base_steps"
    }
}
