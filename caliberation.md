Here’s your content converted into a clean, structured **`.md` file** 👇

---

```md id="step-engine-calibration-md"
# 🧠 Step Accuracy & Calibration Strategy

## 📌 Overview

This document defines the **step validation, calibration, and sensor handling strategy** for improving pedometer accuracy.

The goal is to eliminate false positives (e.g., phone shaking) and ensure reliable step counting using a **multi-layer validation approach**.

---

## 🏗️ Processing Flow

```

StepCounter → StepEngine
↓
Interval Filter
Rhythm Check
Session Check
Motion Check
↓
Valid Step
↓
CalibrationEngine
↓
MetricsEngine → DB → UI

````

---

# 🧩 StepEngine (Core Accuracy Layer)

## Responsibilities

- Track step timing  
- Validate walking rhythm  
- Detect walking sessions  
- Accept / reject steps  

---

## 🔹 Step Interval Tracking

```kotlin
private val stepTimestamps = ArrayDeque<Long>()
private const val MAX_WINDOW = 10

fun onStepDetected(timestamp: Long): Boolean {
    stepTimestamps.addLast(timestamp)
    if (stepTimestamps.size > MAX_WINDOW) {
        stepTimestamps.removeFirst()
    }

    return validateStep()
}
````

---

## 🔹 Rhythm Validation

```kotlin
private fun getIntervals(): List<Long> {
    return stepTimestamps.zipWithNext { a, b -> b - a }
}

private fun isRhythmic(): Boolean {
    val intervals = getIntervals()
    if (intervals.size < 3) return false

    val avg = intervals.average()
    val variance = intervals.map { (it - avg) * (it - avg) }.average()

    return variance < 20000
}
```

---

## 🔹 Step Frequency Filter

```kotlin
private fun isValidFrequency(): Boolean {
    val intervals = getIntervals()
    if (intervals.isEmpty()) return false

    val last = intervals.last()
    return last in 300..2000
}
```

---

## 🔹 Session Detection

```kotlin
private var isWalkingSession = false
private var sessionStartTime: Long = 0
private var lastStepTime: Long = 0

private fun updateSession(timestamp: Long) {
    val gap = timestamp - lastStepTime

    if (gap < 2000) {
        if (!isWalkingSession && stepTimestamps.size >= 5) {
            isWalkingSession = true
            sessionStartTime = timestamp
        }
    } else {
        isWalkingSession = false
        stepTimestamps.clear()
    }

    lastStepTime = timestamp
}
```

---

## 🔹 Final Step Validation

```kotlin
private fun validateStep(): Boolean {
    if (!isValidFrequency()) return false
    if (!isRhythmic()) return false
    if (!isWalkingSession) return false

    return true
}
```

---

# 🧠 CalibrationEngine (Personalization Layer)

## 🔹 Manual Calibration Flow

### User Flow

1. Ask user to walk a fixed number of steps (e.g., 100)
2. Start tracking
3. Stop after completion

---

### Stride Calculation

```kotlin
fun calculateStride(distanceMeters: Float, steps: Int): Float {
    return distanceMeters / steps
}
```

---

### User Profile

```kotlin
data class UserProfile(
    val height: Float,
    val weight: Float,
    val strideLength: Float
)
```

---

## 🔹 Adaptive Learning

### Running Averages

```kotlin
private var avgStepInterval: Long = 0
private var avgStride: Float = 0f
```

---

### Update Logic

```kotlin
fun updateStride(newStride: Float) {
    avgStride = (avgStride * 0.8f) + (newStride * 0.2f)
}

fun updateStepInterval(newInterval: Long) {
    avgStepInterval = ((avgStepInterval * 0.8) + (newInterval * 0.2)).toLong()
}
```

---

# 🧠 StepSensorManager (Input Layer)

## Strategy

| Sensor        | Role            |
| ------------- | --------------- |
| Step Counter  | Primary         |
| Accelerometer | Validation only |

---

## 🔹 Step Counter Handling

```kotlin
override fun onSensorChanged(event: SensorEvent) {
    if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
        val steps = event.values[0].toInt()
        listener.onStepCounterChanged(steps)
    }
}
```

---

## 🔹 Accelerometer (Validation Only)

```kotlin
val magnitude = sqrt(x*x + y*y + z*z)
listener.onMotionDetected(magnitude)
```

---

## 🔹 Motion Filtering

```kotlin
if (currentMagnitude < MOTION_THRESHOLD) {
    return false
}
```

---

# 🔥 Implementation Phases

## ✅ Phase 1 (High Priority)

* Step interval filtering
* Session detection
* Basic rhythm validation

---

## 🔜 Phase 2

* Adaptive calibration
* Motion validation

---

## 🚀 Phase 3

* Activity Recognition API
* ML-based step classification

---

# ⚠️ Common Mistakes to Avoid

* Counting raw accelerometer peaks
* No interval filtering
* No session detection
* No calibration
* Direct sensor → UI coupling

---

# 🟢 Summary

This approach ensures:

* High accuracy
* Reduced false positives
* Personalized tracking
* Future scalability


