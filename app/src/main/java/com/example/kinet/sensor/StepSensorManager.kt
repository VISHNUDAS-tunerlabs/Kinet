package com.example.kinet.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Wraps the Android sensor API.
 * Primary: TYPE_STEP_COUNTER (hardware, low battery)
 * Fallback: TYPE_ACCELEROMETER (note: accelerometer-based step detection is a stub here;
 *           a production-quality fallback requires a peak-detection algorithm)
 */
class StepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val accelerometerSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val hasHardwareStepCounter: Boolean = stepCounterSensor != null

    private var onStepCount: ((Long) -> Unit)? = null

    fun start(onStepCount: (Long) -> Unit) {
        this.onStepCount = onStepCount
        val sensor = if (hasHardwareStepCounter) stepCounterSensor else accelerometerSensor
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        onStepCount = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                onStepCount?.invoke(event.values[0].toLong())
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Accelerometer fallback — stub only.
                // Full implementation would apply a low-pass filter + peak detection.
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
