package com.example.kinet.engine

/**
 * Computes and refines stride length for accurate distance calculation.
 *
 * Two calibration modes:
 *  1. Manual — user walks a known distance; [calibrate] sets stride exactly.
 *  2. Adaptive — [updateStride] / [updateStepInterval] apply an exponential moving
 *     average so the estimate improves gradually over normal usage.
 */
class CalibrationEngine(private var strideLengthCm: Float) {

    // Adaptive running averages (EMA: 80 % old + 20 % new)
    private var avgStrideLengthCm: Float = strideLengthCm
    private var avgStepIntervalMs: Long = 0L

    /**
     * Manual calibration — user walked [actualDistanceMeters] and the sensor counted [steps].
     * Replaces the current stride length and resets the adaptive average to the new value.
     * Returns the new stride length in cm, or the existing value if inputs are invalid.
     */
    fun calibrate(steps: Int, actualDistanceMeters: Float): Float {
        if (steps <= 0 || actualDistanceMeters <= 0f) return strideLengthCm
        val newStride = (actualDistanceMeters * 100f) / steps
        strideLengthCm = newStride
        avgStrideLengthCm = newStride      // reset EMA to the measured value
        return strideLengthCm
    }

    /**
     * Adaptive update — nudges the stride estimate toward [newStrideCm] using EMA.
     * Call this after each calibration walk session or whenever a reliable measurement
     * is available (e.g., GPS-assisted distance in a future phase).
     */
    fun updateStride(newStrideCm: Float) {
        avgStrideLengthCm = avgStrideLengthCm * EMA_WEIGHT_OLD + newStrideCm * EMA_WEIGHT_NEW
        strideLengthCm = avgStrideLengthCm
    }

    /**
     * Adaptive update — tracks the user's average cadence for future use
     * (e.g., activity-type detection or pace-based stride adjustment in Phase 2).
     */
    fun updateStepInterval(newIntervalMs: Long) {
        avgStepIntervalMs = if (avgStepIntervalMs == 0L) {
            newIntervalMs
        } else {
            (avgStepIntervalMs * EMA_WEIGHT_OLD + newIntervalMs * EMA_WEIGHT_NEW).toLong()
        }
    }

    fun getStrideLengthCm(): Float = strideLengthCm
    fun getAvgStepIntervalMs(): Long = avgStepIntervalMs

    companion object {
        private const val EMA_WEIGHT_OLD = 0.8f
        private const val EMA_WEIGHT_NEW = 0.2f
    }
}
