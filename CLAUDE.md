# 🏃 Smart Pedometer App

## 📌 Overview

A **lightweight, offline-first Android pedometer app** designed to accurately track:

- Steps
- Distance traveled
- Calories burned
- Active time

The app focuses on **high accuracy**, **low battery consumption**, and **privacy**, with no dependency on network connectivity in Phase 1.

---

## 🎯 Goals

- Accurate step tracking using device sensors  
- Minimal battery usage  
- Fully offline functionality  
- Simple and clean user experience  
- Scalable architecture for future enhancements  

---

## 🏗️ Architecture

The app follows a **lean clean architecture** approach to ensure scalability without over-engineering.

---

## 📂 Project Structure
/app
MainActivity.kt

/domain
/model
/usecase

/data
/repository

/sensor
/engine


---

## 🧠 Core Components

### 🔹 Sensor Layer

- Uses Android **Step Counter Sensor**
- Fallback to **Accelerometer** if needed  
- Responsible only for collecting raw step data  

---

### 🔹 Engine Layer

Core processing logic:

- Step validation and correction  
- Distance calculation  
- Calorie estimation  
- Activity duration tracking  

---

### 🔹 Domain Layer

Contains:

#### Models:
- `DailyActivity`
- `Session`
- `UserProfile`

#### Use Cases:
- `GetStepsUseCase`
- `CalculateMetricsUseCase`

---

### 🔹 Data Layer

- Uses a **Repository pattern**
- Handles data storage and retrieval  
- Phase 1: In-memory or SharedPreferences  

---

### 🔹 UI Layer

- Built using **Jetpack Compose**  
- Displays:
  - Steps  
  - Distance  
  - Calories  
- Simple layout for Phase 1  

---

## 📊 Features (Phase 1)

- Real-time step tracking  
- Distance calculation  
- Calorie estimation  
- Active time tracking  
- Basic dashboard UI  
- Fully offline functionality  

---

## 🗄️ Data Model

### DailyActivity

- date  
- steps  
- distance_meters  
- calories  
- active_minutes  

---

### Session (Optional)

- start_time  
- end_time  
- steps  
- distance  

---

### UserProfile

- height  
- weight  
- stride_length  

---

## 🔋 Battery Optimization

- Uses hardware step sensor  
- Avoids GPS usage  
- Efficient background processing  
- Minimal sensor polling  

---

## 📡 Network Usage

- ❌ No network required  
- ✅ Fully offline  

---

## 🚀 Tech Stack

- Kotlin  
- Jetpack Compose  
- Android Sensor API  
- (Optional) SharedPreferences  

---

## 🧠 Future Scope (Phase 2)

Planned enhancements:

- Room Database (local persistence)  
- Cloud sync & backup  
- Multi-device support  
- Advanced analytics (graphs & trends)  
- Improved UI/UX (modern design, animations)  

---

## ⚠️ Limitations (Phase 1)

- Basic UI only  
- Limited historical data storage  
- No cloud backup  

---

## 🟢 Status

Phase 1: Prototype with clean architecture  
Focus on **accuracy, performance, and simplicity**

## UI / UX Rules

* Mobile-first responsive design is mandatory
* Keep design minimal and professional
* Use consistent spacing and typography
* Add subtle hover effects and transitions
* Ensure good color contrast (accessibility)

## Never Do

* Never include unnecessary dependencies
* Never break responsiveness
* Never hardcode absolute URLs for assets
* Never clutter UI with too many elements



## SESSION MANAGEMENT (CRITICAL)

At the end of EVERY session, you must rewrite `primer.md` completely.

Include:

1. Current state of the project
2. What was accomplished this session
3. Immediate next steps (specific, actionable)
4. Any open blockers or unresolved issues
5. Any important decisions made this session



## Output Expectations

* Always generate clean, production-ready code
* Ensure responsiveness before completing tasks
* Prefer simplicity over complexity
* Code should be easy to understand and modify



## Instructions for Claude

* Act as a senior mobile app developer
* Focus on clean design and usability
* Validate responsiveness in every change
* Avoid overengineering
* Deliver complete, working solutions only
