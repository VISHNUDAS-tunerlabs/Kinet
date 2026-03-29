# Kinet — Session Primer

## Current State of the Project
Phase 1 architecture is fully scaffolded. All layers are in place: domain, data, engine, sensor, service, and UI. The app is ready to sync with Android Studio and attempt a first build.

### File Structure Built
```
app/src/main/java/com/example/kinet/
  domain/
    model/          DailyActivity.kt, UserProfile.kt
    usecase/        GetTodayActivityUseCase.kt, GetWeeklyActivitiesUseCase.kt, UpdateStepsUseCase.kt
  data/
    local/
      entity/       DailyActivityEntity.kt, UserProfileEntity.kt
      dao/          DailyActivityDao.kt, UserProfileDao.kt
      KinetDatabase.kt
    repository/     ActivityRepository.kt (interface), ActivityRepositoryImpl.kt
  engine/           MetricsEngine.kt, StepEngine.kt, CalibrationEngine.kt
  sensor/           StepSensorManager.kt
  service/          StepTrackingService.kt
  ui/
    dashboard/      DashboardScreen.kt, DashboardViewModel.kt, DashboardViewModelFactory.kt
    components/     MetricCard.kt, WeeklyChart.kt
  MainActivity.kt
```

## What Was Accomplished This Session
- Reviewed and understood both Phase 1 (offline pedometer) and Phase 2 (cloud sync) plans
- Added Room 2.7.1, KSP 2.2.10-1.0.31, ViewModel Compose 2.8.7 to `libs.versions.toml`
- Added `kotlin.android` and `ksp` plugins to root and app `build.gradle.kts`
- Built full domain → data → engine → sensor → service → UI stack from scratch
- `StepTrackingService` is a ForegroundService (type: health) that registers the step counter sensor, computes daily steps via `StepEngine`, and writes to Room on every sensor event
- `DashboardScreen` shows: step goal card with linear progress indicator, 4 metric cards (steps, distance, calories, active time), and a Canvas-based weekly bar chart
- `MainActivity` requests `ACTIVITY_RECOGNITION` + `POST_NOTIFICATIONS` at runtime, then starts the service
- All Room entities include `updatedAt: Long` — required for Phase 2 incremental sync
- Repository pattern with interface — Phase 2 sync engine swaps impl without touching domain

## Immediate Next Steps (Specific & Actionable)
1. **Verify KSP version** — `2.2.10-1.0.31` may not exist yet for Kotlin `2.2.10`. Check https://github.com/google/ksp/releases and update `libs.versions.toml` if needed
2. **Sync and build in Android Studio** — resolve any compile errors (import issues, annotation processing)
3. **Test on a physical device** — emulators don't always fire `TYPE_STEP_COUNTER`; use a real phone to verify the sensor → DB → UI flow
4. **Add notification tap action** — `PendingIntent` in `StepTrackingService.buildNotification()` to open `MainActivity` on tap
5. **Add first-run user profile screen** — height, weight, and stride length input; currently `UserProfile.Default` (170cm / 70kg / 75cm) is used until a profile is saved to DB
6. **Handle `onTaskRemoved` in service** — decide whether tracking should stop when user swipes the app away

## Open Blockers / Unresolved Issues
- KSP version compatibility with Kotlin `2.2.10` must be confirmed before the project will build
- Accelerometer fallback in `StepSensorManager` is a stub only — no real peak-detection algorithm; fine for Phase 1 since most devices have hardware step counter
- No settings or profile screen exists — metrics will use default values until built

## Important Decisions Made This Session
- **No Hilt** — manual `ViewModelProvider.Factory` keeps Phase 1 dependency-free; Hilt is a Phase 2 addition
- **Service writes to DB, ViewModel reads via Flow** — no direct ViewModel ↔ Service coupling; clean and testable
- **`updatedAt` on all entities from day one** — non-negotiable for Phase 2 last-write-wins conflict resolution
- **`StepEngine` manages the cumulative sensor base** — persisted in SharedPreferences keyed by date so it survives service restarts and day rollovers correctly
