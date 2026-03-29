# 🏃 Smart Pedometer App (Phase 1)

## 📌 Overview

An **offline-first, low-battery pedometer app** that accurately tracks:

* Steps
* Distance
* Calories burned
* Active time

Designed to be **privacy-focused** and **independent of network connectivity**, while being architected for future cloud sync.

---

## 🎯 Goals

* High accuracy step tracking
* Minimal battery usage
* Fully offline functionality
* Clean and simple user experience
* Future-ready architecture for backend integration

---

## 🏗️ Architecture

```
Sensors → Step Engine → Metrics Engine → Local DB → UI
```

---

## 🧠 Core Components

### 🔹 Sensor Layer

* Uses Android step counter sensor (primary)
* Accelerometer as fallback

---

### 🔹 Step Engine

* Processes step data
* Validates and corrects inconsistencies

---

### 🔹 Metrics Engine

* Distance calculation
* Calorie estimation
* Active time tracking

---

### 🔹 Calibration Engine

* Manual calibration (guided steps)
* Adaptive stride learning over time

---

### 🔹 Local Database

* Room (SQLite)

---

## 🗄️ Database Schema

### DailyActivity

* date
* steps
* distance_meters
* calories
* active_minutes

---

### ActivitySession (optional)

* start_time
* end_time
* steps
* distance

---

### UserProfile

* height
* weight
* stride_length

---

## 📱 Features

* Real-time step tracking
* Daily dashboard
* Weekly insights (graphs)
* Calibration system
* Offline data storage

---

## 📊 Dashboard

* Summary cards (steps, distance, calories, time)
* Daily progress indicator
* Weekly trend graphs

---

## 🔋 Battery Optimization

* Uses hardware step sensor
* Avoids continuous GPS
* Low-frequency sensor polling
* Background processing optimized

---

## 📡 Network Usage

❌ No network required
✅ Fully offline

---

## 🧠 Future Readiness (Phase 2)

The app is designed with:

* Repository pattern
* `updated_at` timestamps
* Modular architecture

👉 Enables seamless addition of:

* Cloud sync
* Backup & restore
* Multi-device support

---

## 🚀 Tech Stack

* Kotlin
* Jetpack Compose
* Room (SQLite)
* Android Sensor API

---

## 🟢 Status

MVP-ready architecture
Focused on accuracy and performance

---

# ☁️ Smart Pedometer App (Phase 2 - Cloud Sync)

## 📌 Overview

Phase 2 extends the offline pedometer app into a **cloud-enabled SaaS system** with:

* Multi-device sync
* Backup & restore
* User accounts
* Analytics & insights

---

## 🎯 Goals

* Seamless data sync across devices
* Reliable offline-first syncing
* Secure user authentication
* Scalable backend architecture

---

## 🏗️ Architecture

```
Mobile App
   ↓
Local DB ↔ Sync Engine ↔ Backend API
                                ↓
                             MongoDB
```

---

## 🧠 Core Components

### 🔹 Sync Engine (Mobile)

* Push local changes to server
* Pull remote updates
* Handles retries and failures

---

### 🔹 Backend API

* Built with Node.js + Express
* REST-based APIs

---

### 🔹 Database

* MongoDB
* Stores user activity and sessions

---

## 🔄 Sync Flow

1. Collect unsynced local data
2. Send to backend
3. Backend stores data
4. Backend returns updates
5. Merge into local DB

---

## 🔐 Authentication

* JWT-based authentication
* Secure API access

---

## 🗄️ Backend Data Model

### User

* email
* password
* profile (height, weight, stride)

---

### DailyActivity

* user_id
* date
* steps
* distance
* calories

---

### Session

* start_time
* end_time
* activity_type

---

### Device

* device_id
* last_sync

---

## ⚖️ Conflict Resolution

* Strategy: **Last write wins**
* Based on `updated_at` timestamp

---

## 🔁 Sync Strategy

* Incremental sync using timestamps
* Background sync using WorkManager

---

## 📡 API Endpoints

### Auth

* POST /login
* POST /signup

---

### Sync

* POST /sync
* GET /sync

---

### Activity

* GET /activity
* POST /activity

---

## ⚡ Performance

* Batch sync requests
* Indexed queries (user_id, date)
* Efficient data transfer

---

## 🔋 Mobile Considerations

* Sync only when needed
* Retry with exponential backoff
* Works offline-first

---

## 🚀 Tech Stack

### Backend

* Node.js
* Express
* MongoDB
* Mongoose

---

### Mobile Additions

* WorkManager
* SyncQueue

---

## 🟢 Status

Phase 2 enables:

* Cloud sync
* Backup
* Multi-device usage

---

## 🔮 Future Enhancements

* Leaderboards
* Social features
* Advanced analytics
* AI-based insights

---
