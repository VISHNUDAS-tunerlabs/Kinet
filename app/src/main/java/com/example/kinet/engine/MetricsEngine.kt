package com.example.kinet.engine

/**
 * Pure calculation engine — no Android dependencies.
 * All formulas are standard fitness approximations.
 */
class MetricsEngine {

    /**
     * Distance in meters.
     * steps × stride_length_cm / 100
     */
    fun calculateDistanceMeters(steps: Int, strideLengthCm: Float): Float =
        (steps * strideLengthCm) / 100f

    /**
     * Calories burned (kcal).
     * Approximation: steps × weightKg / 1400
     * (equivalent to ~100 kcal per mile for a 70 kg person at 2000 steps/mile)
     */
    fun calculateCalories(steps: Int, weightKg: Float): Float =
        (steps * weightKg) / 1400f

    /**
     * Active minutes.
     * Assumes ~100 steps per minute at a moderate walking pace.
     */
    fun calculateActiveMinutes(steps: Int): Int =
        (steps / 100).coerceAtLeast(0)
}
