# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 is feature-complete. All four tabs are live with real data. The app tracks steps via a foreground service, persists data with Room (v5), and has full navigation with bottom tabs, onboarding, calibration, profile (name/photo/edit/streaks), a fully functional Habito module with live streaks and reminders that survive reboot, a Reports tab with a GitHub-style habit heatmap, a rich foreground notification with live stats, and pause/resume/reset controls on the dashboard.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API, Room v5, SharedPreferences.

**Key files:**

### App shell
- `MainActivity.kt` — StateFlow-driven `when` routing; no NavController; passes typed ViewModels to all screens
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `currentTab`, `showProfile`, `showProfileEdit`, `showCalibration`, `appTheme`, `currentStreak`, `bestStreak`
- `MainViewModelFactory.kt` — wires `ActivityRepositoryImpl` + `HabitRepositoryImpl` + SharedPreferences

### Sensor & engine
- `service/StepTrackingService.kt` — foreground service; NOTIFICATION_ID = 100; handles `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_RESET` intents; `startForeground` called on EVERY `onStartCommand` (required to avoid crash on API 26+ when service is restarted with a control-action intent); `collectingActivity` guard; `combine(getTodayActivity, getUserProfile)` drives live notification; stores `lastSensorValue` for pause/resume base adjustment; `isPaused` service field mirrors `TrackingState`
- `service/HabitReminderReceiver.kt` — BroadcastReceiver; NOTIFICATION_ID = 200
- `service/BootReceiver.kt` — re-schedules all reminders on boot via `HabitReminderScheduler`
- `engine/StepEngine.kt` — delta calculation + timing/session detection; supports `pause(sensorValue)`, `resume(sensorValue)`, `resetToday(sensorValue)`; `frozenStepCount` holds the step count displayed while paused; `resume()` advances `baseSteps` by steps accumulated during pause to skip them
- `engine/StepSessionState.kt` — process-wide singleton `MutableStateFlow<Boolean>`; updated by service
- `engine/TrackingState.kt` — process-wide singleton `MutableStateFlow<Boolean>` for `isPaused`; updated by service, observed by DashboardViewModel
- `engine/CalibrationEngine.kt` — manual calibration + adaptive EMA (80/20)
- `engine/MetricsEngine.kt` — distance, calories, active minutes formulas

### Data layer
- `data/local/KinetDatabase.kt` — Room v5; MIGRATION_1_2 through MIGRATION_4_5
- `data/local/dao/DailyActivityDao.kt` — `getByDate()`, `getLastSevenDays()`, `upsert()`, `resetForDate()` (zeros steps/distance/calories/activeMinutes for a date)
- `data/local/dao/HabitDao.kt` — `getActiveHabits()`, `getHabitsWithReminders()`, `getById()`, `insertOrReplace()`, `softDelete()`, `getLogsByDate()`, `getLogsByHabitId()`, `getLogsSince()`, `insertOrReplaceLog()`, `updateStreaks()`
- `data/repository/ActivityRepository.kt` — interface includes `resetTodayActivity()`
- `data/repository/ActivityRepositoryImpl.kt` — `resetTodayActivity()` calls `activityDao.resetForDate(todayDate())`
- `data/repository/HabitRepositoryImpl.kt` — `logHabit()` triggers `recalculateStreak()`

### UI — Dashboard (Steps tab)
- `ui/dashboard/DashboardViewModel.kt` — exposes `todayActivity`, `stepGoal`, `isWalkingSession`, `isPaused`; `pause()`, `resume()`, `resetSteps()` send intents to service via `appContext`; constructor takes `appContext: Context`
- `ui/dashboard/DashboardViewModelFactory.kt` — passes `context.applicationContext` as `appContext`
- `ui/dashboard/DashboardScreen.kt` — `StepGoalCard` has Pause/Resume toggle (`FilledTonalButton`) + Reset button (`FilledTonalButton`); "Paused" badge (red) animates in place of "Walking" badge; progress bar turns error-red when paused; reset shows `AlertDialog` confirmation

### UI — Notification
- Rich foreground notification: collapsed shows `"N,NNN / 10,000 steps · NNN kcal"`; expanded shows distance + active minutes; progress bar toward goal; "Pause/Resume" + "Reset" action buttons; title changes to "Kinet — Paused" / "Goal reached!" appropriately
- Icon: `res/drawable/ic_notification_steps.xml` — monochrome vector footprints (no `android:tint` — causes resource linking error)

### UI — Calibration
- `ui/calibration/CalibrationViewModel.kt` — records step timestamps during walk; `saveStride()` calls `engine.calibrate()` + `engine.updateStepInterval()`

### UI — Profile
- `ui/profile/ProfileSetupScreen.kt` — onboarding with step goal field
- `ui/profile/ProfileViewScreen.kt` — Habit Streaks section (hidden when both = 0)
- `ui/profile/ProfileEditScreen.kt` — name, height, weight, stride, goal fields

### UI — Habito
- `ui/habito/HabitoScreen.kt` — LIST / ADD_EDIT / DAILY_LOG sub-screens; streak badges; reminder time display
- `ui/habito/HabitoViewModel.kt` — step-based auto-evaluation via `combine`

### UI — Reports
- `ui/reports/ReportsViewModel.kt` — 16-week heatmap grid; per-habit 7-day stats; weekly step summaries
- `ui/reports/ReportsScreen.kt` — step chart + 6 stat cards + heatmap + per-habit completion cards
- `ui/reports/HabitHeatmap.kt` — Canvas; 16×7 grid; 5 colour levels; month/day labels; legend

---

## 2. What Was Accomplished This Session

1. **Rich foreground notification** — notification now shows `"N,NNN / goal steps · NNN kcal"` (collapsed) and `"distance · active mins · kcal"` (expanded); progress bar toward daily goal; title changes on goal reached / paused; uses custom footstep vector icon (`ic_notification_steps.xml`); `combine(getTodayActivity, getUserProfile)` drives all updates
2. **Pause / Resume / Reset** — full implementation across all layers:
   - `StepEngine`: `pause()`, `resume()` (skips steps during pause by advancing base), `resetToday()` (re-anchors base to current sensor value)
   - `DailyActivityDao`: `resetForDate()` SQL query
   - `ActivityRepository` + `ActivityRepositoryImpl`: `resetTodayActivity()`
   - `StepTrackingService`: handles `ACTION_PAUSE/RESUME/RESET` intents; stores `lastSensorValue`; notification has action buttons; `TrackingState` singleton updated
   - `DashboardViewModel`: exposes `isPaused`; `pause()`, `resume()`, `resetSteps()` methods
   - `DashboardScreen`: Pause/Resume toggle + Reset button in `StepGoalCard`; reset confirmation dialog; "Paused" badge; progress bar color change
3. **Bug fix — service crash on action intent restart** — `startForeground` moved to top of `onStartCommand` (before action dispatch), preventing crash when Android restarts a killed service with a control-action intent
4. **Bug fix — wrong button component** — replaced `FilledTonalIconButton` (icon-only, fixed 40dp) with `FilledTonalButton` (supports icon+text, respects `weight()` modifier)
5. **Bug fix — drawable XML** — removed `android:tint="?attr/colorControlNormal"` (not resolvable in drawable XML context) and fixed malformed `<vector>` tag (missing `>` left by the edit)

---

## 3. Immediate Next Steps

1. **Physical device testing** — validate step tracking, pause/resume/reset accuracy, notification action buttons, sensor behavior across app kill/restart cycles
2. **Daily reset validation** — confirm step base resets correctly at midnight and after reboot; verify `resetForDate` doesn't affect previous days' data
3. **Home tab content** — only remaining placeholder; plan: daily step ring, today's habit summary (X/Y done), current streak highlight, motivational copy
4. **Persist pause state across service restart** — currently if the service is killed while paused and restarted, `isPaused` resets to `false` and steps during the pause are counted; fix: persist `sensorValueAtPause` to SharedPreferences

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — KSP workaround, leave as-is
- Accelerometer fallback in `StepSensorManager` is a stub — acceptable for Phase 1
- **Pause state lost on service restart** — `TrackingState.isPaused` and `StepEngine.isPaused` both reset to `false` if the process is killed while paused; steps accumulated during pause will be counted on next run
- `AlarmManager.setInexactRepeating` may fire a few minutes late — acceptable for habit reminders

---

## 5. Important Decisions This Session

- **`startForeground` always called first** — Android API 26+ requires `startForeground` within 5s of any `onStartCommand`; moving it before the action dispatch prevents a crash if the service is killed and restarted with a control-action intent queued
- **`FilledTonalButton` over `FilledTonalIconButton` for Pause/Reset** — `FilledTonalIconButton` is fixed 40×40dp and not designed for icon+text; `FilledTonalButton` fills `weight(1f)` correctly
- **`lastSensorValue` stored in service** — `pause()`, `resume()`, and `resetToday()` all need the current sensor value; storing it in `onStepCount` callback gives accurate timing without passing it through intent extras
- **`combine(getTodayActivity, getUserProfile)`** — single combined flow drives notification updates; guarantees step goal is always current when building the progress bar
- **`resetForDate` SQL (not delete)** — zeroes columns instead of deleting the row; preserves the date entry in Room so the Flow emits an update (delete + re-insert would require an upsert trigger); dashboard updates to 0 immediately
- **No tint on notification vector drawable** — `?attr/colorControlNormal` is a theme attribute not resolvable in a raw drawable XML; notification system renders small icons as monochrome white automatically
