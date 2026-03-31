# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 architecture is complete. The app tracks steps via a foreground service, persists data with Room, and has a full navigation structure with bottom tabs, onboarding, calibration, and profile management.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API (Step Counter + Step Detector), Room v2, SharedPreferences.

**Key files:**
- `MainActivity.kt` — StateFlow-driven `when` routing; no NavController; hosts bottom nav scaffold
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `currentTab`, `showProfileEdit`, `showCalibration`
- `StepTrackingService.kt` — foreground service; registers both TYPE_STEP_COUNTER and TYPE_STEP_DETECTOR
- `engine/StepEngine.kt` — delta calculation + timing/session validation via STEP_DETECTOR
- `engine/CalibrationEngine.kt` — manual calibration + adaptive EMA for stride/cadence
- `engine/MetricsEngine.kt` — distance, calories, active minutes formulas
- `sensor/StepSensorManager.kt` — wraps sensor API; dual-sensor start with two callbacks
- `ui/AppTab.kt` — enum with 4 tabs (HOME, STEPS, HABITO, REPORTS)
- `ui/profile/ProfileSetupScreen.kt` — premium onboarding (Height, Weight, Daily Step Goal)
- `ui/profile/ProfileEditScreen.kt` — full profile edit (adds Stride field)
- `ui/dashboard/DashboardScreen.kt` — Steps tab; metrics + CalibrationCard entry point
- `ui/calibration/CalibrationScreen.kt` — 3-step guided walk: instruction → active → result/save
- `ui/home/HomeScreen.kt` — placeholder
- `ui/habito/HabitoScreen.kt` — placeholder
- `ui/reports/ReportsScreen.kt` — 2 placeholder report cards
- `data/local/KinetDatabase.kt` — Room v2 with MIGRATION_1_2
- `data/repository/ActivityRepositoryImpl.kt` — Room persistence layer

---

## 2. What Was Accomplished This Session

### Step accuracy — sensor layer upgrade
- `StepSensorManager` now registers **both** `TYPE_STEP_DETECTOR` (per-step timestamps) and `TYPE_STEP_COUNTER` (cumulative count) simultaneously
- Added `onStepDetected` callback to `start()` — fires with `event.timestamp` (nanoseconds) for each detected step
- `StepEngine` gained a full **timing validation layer**: 10-step rolling timestamp window, session detection (requires 5 consecutive steps within 3s gaps, clears state if gap > 3s), `isWalkingSession` public property, `avgStepIntervalMs()` for cadence
- `CalibrationEngine` gained adaptive EMA methods: `updateStride(newStrideCm)` and `updateStepInterval(newIntervalMs)` (80/20 blend)
- `StepTrackingService` wired up the new `onStepDetected` callback

### CalibrationScreen (new screen)
- 3-state machine: `Instruction → Active → Result → Saved`
- Registers `TYPE_STEP_DETECTOR` directly in `CalibrationViewModel` during guided walk
- `CalibrationEngine.calibrate(steps, distanceMeters)` computes new stride and saves to `UserProfile` via repository
- `TopAppBar` with back arrow; `onBack` and `onDone` both route to `closeCalibration()` in `MainViewModel`
- Entry point: `CalibrationCard` tappable card inside the Steps (Dashboard) tab

### Bottom navigation (4 tabs)
- `AppTab` enum: HOME, STEPS, HABITO, REPORTS — each carries its label and `ImageVector` icon
- `material-icons-extended` added to `libs.versions.toml` + `build.gradle.kts`
- `MainActivity` restructured: shared `TopAppBar` (tab title + profile icon) + `NavigationBar` + tab content switching
- Profile edit and CalibrationScreen remain full-screen overlays above the scaffold
- `MainViewModel` gained `currentTab: StateFlow<AppTab>` and `setTab()`

### Premium onboarding redesign
- `ProfileSetupScreen` fully redesigned: "Let's personalize your journey" header, illustration placeholder slot (marked for future Lottie/asset), 3 rounded-card input fields, large "Start Tracking" CTA
- Fields: Height, Weight, Daily Step Goal (stride auto-calculated from height silently)
- `ProfileEditScreen` updated: added `dailyStepGoal` field, rounded field shapes consistent with onboarding

### dailyStepGoal — new field across the stack
- `UserProfile` domain model: added `dailyStepGoal: Int = 10_000`
- `UserProfileEntity`: added `dailyStepGoal` column
- `KinetDatabase`: bumped to **version 2**, `MIGRATION_1_2` runs `ALTER TABLE user_profile ADD COLUMN dailyStepGoal INTEGER NOT NULL DEFAULT 10000`
- `ActivityRepositoryImpl.saveUserProfile` persists it
- `MainViewModel.saveProfile` updated to 4-param signature
- `DashboardViewModel` exposes `stepGoal: StateFlow<Int>` from user profile
- `DashboardScreen` uses live `stepGoal` — hardcoded constant removed

---

## 3. Immediate Next Steps

1. **Physical device testing** — step counter and step detector sensors don't fire on emulators; all accuracy work needs real hardware validation
2. **Session gate on DashboardScreen** — `StepEngine.isWalkingSession` is available but not yet surfaced in UI; consider a subtle "walking now" indicator on the Steps tab
3. **Adaptive calibration hookup** — `CalibrationEngine.updateStride()` and `updateStepInterval()` exist but are not yet called after each calibration; wire them in `CalibrationViewModel.saveStride()`
4. **Notification live step count** — foreground notification currently shows static "Tracking your steps"; update to show live count
5. **Daily reset validation** — confirm step base resets correctly at midnight across reboots and `onTaskRemoved` restarts
6. **Home tab content** — placeholder screen; plan what "personalized features" go here (Phase 2 scope)

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — KSP workaround, leave as-is
- Accelerometer fallback in `StepSensorManager` is a stub — acceptable for Phase 1
- `TYPE_STEP_DETECTOR` is registered twice on the same `SensorManager` instance if CalibrationScreen is open while the service is running — this is harmless (Android delivers events to both listeners independently) but worth noting
- Habito and Reports screens are full placeholders — no functionality yet

---

## 5. Important Decisions This Session

- **STEP_COUNTER stays the count source of truth** — STEP_DETECTOR is used only for timing/session validation and calibration counting, not for the main daily step total. Android's hardware counter is more reliable for sustained background tracking.
- **No NavController** — routing stays StateFlow-driven `when` branching in `MainActivity`. Bottom nav is handled via `MainViewModel.currentTab`. Revisit if screen count grows beyond Phase 1.
- **Stride hidden from onboarding** — ProfileSetupScreen only asks Height/Weight/StepGoal; stride auto-calculated as `height × 0.415`. Stride remains editable in ProfileEditScreen and via the CalibrationScreen.
- **Room MIGRATION_1_2 with DEFAULT** — chose `ALTER TABLE` with `DEFAULT 10000` rather than destructive migration; existing users keep their profile data.
- **Illustration slot marked, not implemented** — `IllustrationPlaceholder()` in `ProfileSetupScreen` is a clearly-commented `Box` sized for a future Lottie animation or graphic asset.
