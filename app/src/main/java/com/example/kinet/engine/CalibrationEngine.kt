package com.example.kinet.engine

/**
 * Computes a calibrated stride length from a known distance and step count.
 * Designed for guided calibration walks (Phase 1: manual; Phase 2: adaptive).
 */
class CalibrationEngine(private var strideLengthCm: Float) {

    /**
     * Calibrate using a known measured distance and the steps taken to cover it.
     * Returns the new stride length in cm, or the existing value if input is invalid.
     */
    fun calibrate(steps: Int, actualDistanceMeters: Float): Float {
        if (steps <= 0 || actualDistanceMeters <= 0f) return strideLengthCm
        strideLengthCm = (actualDistanceMeters * 100f) / steps
        return strideLengthCm
    }

    fun getStrideLengthCm(): Float = strideLengthCm
}
