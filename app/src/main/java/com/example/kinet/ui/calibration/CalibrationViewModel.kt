package com.example.kinet.ui.calibration

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinet.data.repository.ActivityRepository
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

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                stepCount++
                _state.value = State.Active(stepCount)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
    }

    fun startWalk() {
        stepCount = 0
        _state.value = State.Active(0)
        stepDetector?.let {
            sensorManager.registerListener(
                stepListener, it, SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    fun stopWalk() {
        sensorManager.unregisterListener(stepListener)
        if (stepCount > 0) {
            _state.value = State.Result(stepCount)
        } else {
            // No steps counted — back to instructions
            _state.value = State.Instruction
        }
    }

    /**
     * Saves the new stride derived from [distanceMeters] / [stepCount] to the user profile.
     * Preserves existing height and weight.
     */
    fun saveStride(distanceMeters: Float) {
        val steps = (_state.value as? State.Result)?.steps ?: return
        if (distanceMeters <= 0f || steps <= 0) return

        val newStrideCm = (distanceMeters * 100f) / steps

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
