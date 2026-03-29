package com.example.kinet.engine

/**
 * Processes raw step counter sensor values into today's step count.
 *
 * The Android TYPE_STEP_COUNTER sensor reports cumulative steps since the last device reboot.
 * This engine tracks a per-day base value so it can compute today's steps correctly across
 * reboots and app restarts (base value is persisted externally via SharedPreferences).
 */
class StepEngine {

    private var baseSteps: Long = -1L

    /**
     * Called when the sensor fires with the latest cumulative value.
     * Returns today's step count relative to the base established at session start.
     */
    fun process(sensorValue: Long): Int {
        if (baseSteps < 0) {
            baseSteps = sensorValue
            return 0
        }
        return (sensorValue - baseSteps).toInt().coerceAtLeast(0)
    }

    /**
     * Restore a persisted base from a previous session (same day).
     */
    fun restoreBase(base: Long) {
        baseSteps = base
    }

    fun getBase(): Long = baseSteps

    fun reset() {
        baseSteps = -1L
    }
}
