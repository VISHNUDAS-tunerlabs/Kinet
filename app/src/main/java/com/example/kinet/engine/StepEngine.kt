package com.example.kinet.engine

/**
 * Processes raw sensor values into today's accurate step count.
 *
 * Two responsibilities:
 *  1. Delta calculation — converts cumulative TYPE_STEP_COUNTER values into today's count.
 *  2. Timing validation — processes TYPE_STEP_DETECTOR events to detect genuine walking
 *     sessions and reject false positives (vibration, vehicle, phone drop).
 *
 * The counter (TYPE_STEP_COUNTER) is the source of truth for the final count.
 * The detector (TYPE_STEP_DETECTOR) feeds the session/rhythm state used to gate saving.
 */
class StepEngine {

    // region --- Delta logic (TYPE_STEP_COUNTER) ---

    private var baseSteps: Long = -1L

    /**
     * Called when TYPE_STEP_COUNTER fires with its cumulative value.
     * Returns today's step count relative to the base established at session start.
     */
    fun process(sensorValue: Long): Int {
        if (baseSteps < 0) {
            baseSteps = sensorValue
            return 0
        }
        return (sensorValue - baseSteps).toInt().coerceAtLeast(0)
    }

    fun restoreBase(base: Long) {
        baseSteps = base
    }

    fun getBase(): Long = baseSteps

    fun reset() {
        baseSteps = -1L
        clearTimingState()
    }

    // endregion

    // region --- Timing validation (TYPE_STEP_DETECTOR) ---

    private val stepTimestampsMs = ArrayDeque<Long>()

    private var _isWalkingSession = false
    private var lastStepTimeMs: Long = 0L

    /** True when a genuine walking session is in progress. */
    val isWalkingSession: Boolean get() = _isWalkingSession

    /**
     * Called when TYPE_STEP_DETECTOR fires.
     * @param eventTimeNs Sensor event timestamp in nanoseconds (event.timestamp).
     */
    fun onStepDetected(eventTimeNs: Long) {
        val tsMs = eventTimeNs / 1_000_000L
        updateTimestamps(tsMs)
        updateSession(tsMs)
        lastStepTimeMs = tsMs
    }

    private fun updateTimestamps(tsMs: Long) {
        stepTimestampsMs.addLast(tsMs)
        if (stepTimestampsMs.size > WINDOW_SIZE) stepTimestampsMs.removeFirst()
    }

    private fun updateSession(tsMs: Long) {
        val gap = if (lastStepTimeMs > 0L) tsMs - lastStepTimeMs else 0L

        if (gap == 0L || gap < SESSION_GAP_MS) {
            // Steps are close enough together — start a session once we have enough evidence
            if (!isWalkingSession && stepTimestampsMs.size >= SESSION_MIN_STEPS) {
                _isWalkingSession = true
            }
        } else {
            // Too long a gap — user stopped walking
            _isWalkingSession = false
            stepTimestampsMs.clear()
        }
    }

    /**
     * Returns the intervals (ms) between consecutive detected steps in the current window.
     */
    fun getStepIntervals(): List<Long> =
        stepTimestampsMs.zipWithNext { a, b -> b - a }

    /**
     * Mean interval between steps in the current window (ms), or 0 if insufficient data.
     */
    fun avgStepIntervalMs(): Long {
        val intervals = getStepIntervals()
        if (intervals.isEmpty()) return 0L
        return intervals.average().toLong()
    }

    private fun clearTimingState() {
        stepTimestampsMs.clear()
        _isWalkingSession = false
        lastStepTimeMs = 0L
    }

    // endregion

    companion object {
        /** How many steps to observe before declaring a walking session */
        private const val SESSION_MIN_STEPS = 5

        /** Max gap between steps before the session is considered ended (ms) */
        private const val SESSION_GAP_MS = 3_000L

        /** Rolling window of recent step timestamps */
        private const val WINDOW_SIZE = 10
    }
}
