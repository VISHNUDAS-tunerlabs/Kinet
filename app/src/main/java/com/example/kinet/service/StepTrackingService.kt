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
import com.example.kinet.R
import com.example.kinet.data.local.KinetDatabase
import com.example.kinet.data.repository.ActivityRepositoryImpl
import com.example.kinet.domain.model.DailyActivity
import com.example.kinet.engine.MetricsEngine
import com.example.kinet.engine.StepEngine
import com.example.kinet.engine.StepSessionState
import com.example.kinet.engine.TrackingState
import com.example.kinet.sensor.StepSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepTrackingService : Service() {

    private lateinit var sensorManager: StepSensorManager
    private lateinit var repository: ActivityRepositoryImpl
    private val stepEngine = StepEngine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Guard against re-starting collectors on repeated onStartCommand calls
    private var collectingActivity = false

    // Latest snapshot used for notification updates triggered by control actions
    private var lastActivity: DailyActivity? = null
    private var lastGoal: Int = 10_000
    private var isPaused: Boolean = false

    // Last raw sensor value — needed to advance the step base on pause/resume/reset
    private var lastSensorValue: Long = -1L

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
        // startForeground MUST be called on every onStartCommand — if Android kills and
        // restarts the service (START_STICKY) with a control-action intent, skipping this
        // call causes an immediate crash on API 26+.
        startForeground(NOTIFICATION_ID, buildNotification(paused = isPaused))

        when (intent?.action) {
            ACTION_PAUSE  -> { handlePause();  return START_STICKY }
            ACTION_RESUME -> { handleResume(); return START_STICKY }
            ACTION_RESET  -> { handleReset();  return START_STICKY }
        }

        // Normal start / START_STICKY restart (no action)
        if (!collectingActivity) {
            collectingActivity = true
            scope.launch {
                combine(
                    repository.getTodayActivity(),
                    repository.getUserProfile()
                ) { activity, profile -> Pair(activity, profile) }
                .collect { (activity, profile) ->
                    lastActivity = activity
                    lastGoal = profile.dailyStepGoal
                    pushNotification()
                }
            }
        }

        sensorManager.start(
            onStepCount = { sensorValue ->
                lastSensorValue = sensorValue
                val todaySteps = stepEngine.process(sensorValue)
                persistStepBase(sensorValue)
                if (!isPaused) {
                    scope.launch { repository.updateTodaySteps(todaySteps) }
                }
            },
            onStepDetected = { eventTimeNs ->
                stepEngine.onStepDetected(eventTimeNs)
                if (!isPaused) StepSessionState.update(stepEngine.isWalkingSession)
            }
        )
        return START_STICKY
    }

    // region Control actions

    private fun handlePause() {
        if (isPaused) return
        isPaused = true
        if (lastSensorValue >= 0) stepEngine.pause(lastSensorValue)
        TrackingState.update(true)
        StepSessionState.update(false)
        pushNotification()
    }

    private fun handleResume() {
        if (!isPaused) return
        isPaused = false
        if (lastSensorValue >= 0) stepEngine.resume(lastSensorValue)
        TrackingState.update(false)
        pushNotification()
    }

    private fun handleReset() {
        isPaused = false
        if (lastSensorValue >= 0) stepEngine.resetToday(lastSensorValue)
        TrackingState.update(false)
        StepSessionState.update(false)
        prefs().edit().remove(KEY_BASE_STEPS).remove(KEY_BASE_DATE).apply()
        scope.launch { repository.resetTodayActivity() }
        pushNotification()
    }

    private fun pushNotification() {
        val a = lastActivity
        getSystemService(NotificationManager::class.java)?.notify(
            NOTIFICATION_ID,
            buildNotification(
                steps          = a?.steps         ?: 0,
                calories       = a?.calories      ?: 0f,
                distanceMeters = a?.distanceMeters ?: 0f,
                activeMinutes  = a?.activeMinutes  ?: 0,
                goal           = lastGoal,
                paused         = isPaused
            )
        )
    }

    // endregion

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

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, StepTrackingService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, requestCode, intent,
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

    private fun buildNotification(
        steps: Int = 0,
        calories: Float = 0f,
        distanceMeters: Float = 0f,
        activeMinutes: Int = 0,
        goal: Int = 10_000,
        paused: Boolean = false
    ): Notification {
        ensureChannel()

        val distanceKm = distanceMeters / 1000f
        val goalCapped = goal.coerceAtLeast(1)
        val progress = steps.coerceAtMost(goalCapped)

        val title = when {
            paused        -> "Kinet — Paused"
            steps >= goal -> "Goal reached! Keep it up"
            else          -> "Kinet — Step Tracker"
        }
        val contentLine = "%,d / %,d steps  ·  %.0f kcal".format(steps, goal, calories)
        val expandedText = buildString {
            append("%,d / %,d steps\n".format(steps, goal))
            append("%.2f km  ·  %d min active  ·  %.0f kcal".format(distanceKm, activeMinutes, calories))
            if (paused) append("\n⏸ Tracking paused")
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentLine)
            .setSmallIcon(R.drawable.ic_notification_steps)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(tapIntent())
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setProgress(goalCapped, progress, false)
            .addAction(
                0,
                if (paused) "Resume" else "Pause",
                actionIntent(if (paused) ACTION_RESUME else ACTION_PAUSE, 10)
            )
            .addAction(0, "Reset", actionIntent(ACTION_RESET, 11))
            .build()
    }

    // endregion

    companion object {
        const val NOTIFICATION_ID = 100
        const val ACTION_PAUSE  = "com.example.kinet.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.kinet.ACTION_RESUME"
        const val ACTION_RESET  = "com.example.kinet.ACTION_RESET"
        private const val CHANNEL_ID   = "kinet_step_tracking"
        private const val PREFS_NAME   = "kinet_step_prefs"
        private const val KEY_BASE_DATE  = "base_date"
        private const val KEY_BASE_STEPS = "base_steps"
    }
}
