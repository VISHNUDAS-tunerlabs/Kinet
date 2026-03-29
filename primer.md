# Kinet — Session Primer

## Current State of the Project
Phase 1 architecture is complete and fully wired. The project **builds and syncs successfully** in Android Studio. A debug APK can be generated via `./gradlew assembleDebug`.

First-run onboarding (profile setup screen) is implemented and reactive — the app routes itself based on DB state with no manual navigation logic.

### File Structure
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
    MainViewModel.kt, MainViewModelFactory.kt
    profile/        ProfileSetupScreen.kt
    dashboard/      DashboardScreen.kt, DashboardViewModel.kt, DashboardViewModelFactory.kt
    components/     MetricCard.kt, WeeklyChart.kt
    theme/          Theme.kt, Color.kt, Type.kt
  MainActivity.kt
```

## What Was Accomplished This Session
- Added `isProfileSet(): Flow<Boolean>` to `ActivityRepository` and `ActivityRepositoryImpl` — maps `UserProfileDao.get()` null-check to distinguish first-run from returning user
- Created `MainViewModel` — exposes `isProfileSet: StateFlow<Boolean?>` (null = loading, false = needs setup, true = ready) and `saveProfile(heightCm, weightKg, strideLengthCm)` coroutine
- Created `MainViewModelFactory` — wires Room DB → repository → MainViewModel
- Created `ProfileSetupScreen` — clean onboarding form with:
  - Height, weight, stride length fields (decimal keyboard)
  - Stride auto-calculated from height (× 0.415) and pre-filled; stops auto-updating once user edits it manually
  - "Get Started" button disabled until all fields are valid (positive numbers)
- Updated `MainActivity` to route reactively: blank (loading) → `ProfileSetupScreen` → `DashboardScreen` based on `isProfileSet` StateFlow — no NavController needed

## Immediate Next Steps (Specific & Actionable)
1. **Test on a physical device** — install `app-debug.apk`; verify: first launch shows profile screen → save navigates to dashboard → re-launch skips profile screen; emulators don't fire `TYPE_STEP_COUNTER`
2. **Add notification tap action** — add a `PendingIntent` in `StepTrackingService.buildNotification()` to open `MainActivity` on notification tap
3. **Handle `onTaskRemoved` in service** — decide whether tracking stops when user swipes the app away from recents
4. **Profile edit screen** — no way to change profile after first setup without clearing app data; add a settings/edit entry point in the dashboard
5. **Watch for KSP update** — when KSP fully supports AGP 9.x `android.sourceSets`, remove `android.disallowKotlinSourceSets=false` from `gradle.properties`

## Open Blockers / Unresolved Issues
- `android.disallowKotlinSourceSets=false` in `gradle.properties` is a temporary suppression flag — intentional, documented, will be removed when KSP migrates
- Accelerometer fallback in `StepSensorManager` is a stub — no real peak-detection algorithm; acceptable for Phase 1 since most devices have hardware step counter
- No profile edit UI — user must clear app data to re-enter profile values

## Important Decisions Made This Session
- **Routing via StateFlow** — `isProfileSet: StateFlow<Boolean?>` in `MainViewModel` drives screen routing in `setContent`; `null` initial value renders a blank frame while Room query resolves, avoiding a flash of the wrong screen
- **Stride auto-fill** — stride = height × 0.415 pre-populated but always editable; a `strideManuallyEdited` flag prevents the auto-fill from overwriting user input when height changes afterward
- **No NavController** — a simple `when (isProfileSet)` in `setContent` handles the two-screen flow cleanly; NavController would be over-engineering for Phase 1

## Key Architecture Reminders
- `StepEngine` reads `TYPE_STEP_COUNTER` (cumulative since reboot), subtracts a daily baseline stored in SharedPreferences → today's steps
- `MetricsEngine` derives distance (steps × stride), calories (MET formula), active minutes from step count
- Hardware step counter ignores shaking — detects rhythmic walking patterns only
- `StepTrackingService` writes to Room DB on every sensor event; ViewModel reads via Flow — no direct coupling
- `UserProfileEntity` uses a singleton row (`id = 1`) with `OnConflictStrategy.REPLACE` — safe to upsert on every profile save
- `updatedAt` on all entities is reserved for Phase 2 last-write-wins sync
- AGP 9.x bundles Kotlin natively — do NOT apply `org.jetbrains.kotlin.android` plugin; `kotlinOptions` DSL is gone, use `compileOptions` only
