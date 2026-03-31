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

    private val stepDetectorSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var onStepCount: ((Long) -> Unit)? = null
    private var onStepDetected: ((Long) -> Unit)? = null

    /**
     * @param onStepCount    Fires with the cumulative step count from TYPE_STEP_COUNTER.
     * @param onStepDetected Fires with the sensor event timestamp (ns) from TYPE_STEP_DETECTOR,
     *                       once per detected step. Used for timing/rhythm validation.
     */
    fun start(
        onStepCount: (Long) -> Unit,
        onStepDetected: ((Long) -> Unit)? = null
    ) {
        this.onStepCount = onStepCount
        this.onStepDetected = onStepDetected

        val counterSensor = if (hasHardwareStepCounter) stepCounterSensor else accelerometerSensor
        counterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Register detector separately — it fires once per step with a timestamp
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        onStepCount = null
        onStepDetected = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                onStepCount?.invoke(event.values[0].toLong())
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // event.timestamp is nanoseconds since boot; values[0] == 1.0 means one step
                onStepDetected?.invoke(event.timestamp)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Fallback stub — peak detection not implemented in Phase 1
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
}
