package com.example.kinet.ui.calibration

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
import com.example.kinet.engine.CalibrationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CalibrationViewModel(
    context: Context,
    private val repository: ActivityRepository
) : ViewModel() {

    sealed class State {
        /** Pre-walk instruction screen */
        object Instruction : State()

        /** Active walk — show live step count */
        data class Active(val steps: Int) : State()

        /** Walk finished — ask for distance and show calculated stride */
        data class Result(val steps: Int) : State()

        /** Stride saved successfully */
        object Saved : State()
    }

    private val _state = MutableStateFlow<State>(State.Instruction)
    val state: StateFlow<State> = _state.asStateFlow()

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetector: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val hasStepDetector: Boolean = stepDetector != null

    private var stepCount = 0
    private var calibrationEngine: CalibrationEngine? = null

    // Timestamps (ms) of each detected step — used for cadence calculation
    private val stepTimestampsMs = mutableListOf<Long>()
    private var walkAvgIntervalMs = 0L

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfile().first()
            calibrationEngine = CalibrationEngine(profile.strideLengthCm)
        }
    }

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                stepCount++
                stepTimestampsMs.add(event.timestamp / 1_000_000L)
                _state.value = State.Active(stepCount)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    fun startWalk() {
        stepCount = 0
        stepTimestampsMs.clear()
        walkAvgIntervalMs = 0L
        _state.value = State.Active(0)
        stepDetector?.let {
            sensorManager.registerListener(
                stepListener, it, SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    fun stopWalk() {
        sensorManager.unregisterListener(stepListener)

        // Compute average step interval from recorded timestamps
        if (stepTimestampsMs.size >= 2) {
            val totalTime = stepTimestampsMs.last() - stepTimestampsMs.first()
            walkAvgIntervalMs = totalTime / (stepTimestampsMs.size - 1)
        }

        if (stepCount > 0) {
            _state.value = State.Result(stepCount)
        } else {
            _state.value = State.Instruction
        }
    }

    /**
     * Saves the calibrated stride using [CalibrationEngine.calibrate] (exact measurement),
     * then feeds cadence into [CalibrationEngine.updateStepInterval] for adaptive tracking.
     * Preserves existing height and weight.
     */
    fun saveStride(distanceMeters: Float) {
        val steps = (_state.value as? State.Result)?.steps ?: return
        if (distanceMeters <= 0f || steps <= 0) return

        val engine = calibrationEngine
        val newStrideCm = if (engine != null) {
            engine.calibrate(steps, distanceMeters)
            if (walkAvgIntervalMs > 0L) {
                engine.updateStepInterval(walkAvgIntervalMs)
            }
            engine.getStrideLengthCm()
        } else {
            (distanceMeters * 100f) / steps
        }

        viewModelScope.launch {
            val current = repository.getUserProfile().first()
            repository.saveUserProfile(current.copy(strideLengthCm = newStrideCm))
            _state.value = State.Saved
        }
    }

    fun retry() {
        _state.value = State.Instruction
    }

    override fun onCleared() {
        sensorManager.unregisterListener(stepListener)
        super.onCleared()
    }
}
