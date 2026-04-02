# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 is feature-complete. All four tabs are live with real data. The app tracks steps via a foreground service, persists data with Room (v5), and has full navigation with bottom tabs, onboarding, calibration, profile (name/photo/edit/streaks), a fully functional Habito module with live streaks and reminders that survive reboot, and a Reports tab with a GitHub-style habit heatmap.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API, Room v5, SharedPreferences.

**Key files:**

### App shell
- `MainActivity.kt` — StateFlow-driven `when` routing; no NavController; passes typed ViewModels to all screens; collects `currentStreak` + `bestStreak` from MainViewModel for ProfileViewScreen
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `currentTab`, `showProfile`, `showProfileEdit`, `showCalibration`, `appTheme`, `currentStreak`, `bestStreak`; depends on both `ActivityRepository` and `HabitRepository`
- `MainViewModelFactory.kt` — wires `ActivityRepositoryImpl` + `HabitRepositoryImpl` + SharedPreferences

### Sensor & engine
- `service/StepTrackingService.kt` — foreground service; NOTIFICATION_ID = 100; `collectingActivity` guard; live notification `"%,d steps · %.0f kcal"`; calls `StepSessionState.update()` on every STEP_DETECTOR event
- `service/HabitReminderReceiver.kt` — BroadcastReceiver; NOTIFICATION_ID = 200; `HabitReminderScheduler` object inside
- `service/BootReceiver.kt` — `ACTION_BOOT_COMPLETED` receiver; re-schedules all reminders via `HabitReminderScheduler.schedule()` using `getHabitsWithReminders()` DAO query
- `engine/StepEngine.kt` — delta calculation + timing/session detection via STEP_DETECTOR
- `engine/StepSessionState.kt` — process-wide singleton `MutableStateFlow<Boolean>`; updated by service, observed by DashboardViewModel
- `engine/CalibrationEngine.kt` — manual calibration + adaptive EMA (80/20) for stride and cadence
- `engine/MetricsEngine.kt` — distance, calories, active minutes formulas

### Data layer
- `data/local/KinetDatabase.kt` — Room v5; MIGRATION_1_2 through MIGRATION_4_5
- `data/local/dao/HabitDao.kt` — `getActiveHabits()`, `getHabitsWithReminders()`, `getById()`, `insertOrReplace()`, `softDelete()`, `getLogsByDate()`, `getLogsByHabitId()`, `getLogsSince()`, `insertOrReplaceLog()`, `updateStreaks()`
- `data/repository/HabitRepositoryImpl.kt` — `logHabit()` triggers `recalculateStreak()`; streak walks back up to 365 days of COMPLETED logs; updates `streakCount` + `bestStreak`; `getLogsSince()` exposed for Reports

### UI — Dashboard (Steps tab)
- `ui/dashboard/DashboardViewModel.kt` — observes `StepSessionState.isWalking` as `isWalkingSession`
- `ui/dashboard/DashboardScreen.kt` — animated "Walking" badge (fadeIn/fadeOut) on StepGoalCard; calibration entry card at bottom

### UI — Calibration
- `ui/calibration/CalibrationViewModel.kt` — creates `CalibrationEngine` in `init`; records step timestamps during walk; `stopWalk()` computes avg interval; `saveStride()` calls `engine.calibrate()` + `engine.updateStepInterval()`

### UI — Profile
- `ui/profile/ProfileSetupScreen.kt` — onboarding; optional name field first
- `ui/profile/ProfileViewScreen.kt` — hero with name/avatar; Body Metrics cards; **Habit Streaks section** (current + best streak, only shown when > 0); Appearance theme chips; Edit Profile button
- `ui/profile/ProfileEditScreen.kt` — name, height, weight, stride, goal fields

### UI — Habito
- `ui/habito/HabitoScreen.kt` — three sub-screens: LIST, ADD_EDIT, DAILY_LOG; streak shown as `"N day streak"` in primary color; category icons; reminder time display
- `ui/habito/HabitoViewModel.kt` — sub-screen nav; step-based auto-evaluation via `combine`; save/delete/log
- `ui/habito/HabitoViewModelFactory.kt`

### UI — Reports
- `ui/reports/ReportsViewModel.kt` — combines `weeklyActivities + activeHabits + getLogsSince(16weeksAgo)`; computes step summaries, `HabitWeekStats` (7-day per-habit), and `heatmapWeeks` (16×7 grid of `HeatmapDay`)
- `ui/reports/ReportsScreen.kt` — This Week: step chart + 6 stat cards; Habit Activity: heatmap card + per-habit cards with completion bar
- `ui/reports/HabitHeatmap.kt` — Canvas composable; 16 cols × 7 rows; month labels top; M/W/F day labels left; 5 colour levels (surfaceVariant → primary at 4 intensities); legend bottom-right; theme-aware colours resolved before Canvas
- `ui/reports/ReportsViewModelFactory.kt`

### UI — Components
- `ui/components/WeeklyChart.kt` — Canvas bar chart; reused on Dashboard and Reports
- `ui/components/MetricCard.kt`

---

## 2. What Was Accomplished This Session

1. **Habit reminder reboot survival** — `BootReceiver` re-schedules all reminders on `ACTION_BOOT_COMPLETED`; `RECEIVE_BOOT_COMPLETED` permission added to manifest; `HabitDao.getHabitsWithReminders()` added
2. **Reports tab (full)** — replaced placeholder with live data: weekly step chart, 6 summary stats, GitHub-style 16-week habit heatmap, per-habit 7-day completion cards with streak badges
3. **GitHub-style heatmap** — `HabitHeatmap.kt` Canvas composable; `ReportsViewModel` builds precise 16×7 date grid (always starts on Monday); `HabitDao.getLogsSince()` Flow added; `HabitRepository.getLogsSince()` interface + impl added
4. **Streak on Profile** — `Habit` domain model gains `bestStreak`; `HabitEntity.toDomain()` maps it; `MainViewModel` exposes `currentStreak` + `bestStreak` StateFlows (max across all active habits); `ProfileViewScreen` shows "Habit Streaks" section with fire + trophy stat cards (hidden when both = 0)

---

## 3. Immediate Next Steps

1. **Physical device testing** — sensors don't fire on emulator; validate step tracking, session badge, calibration, habit reminders + reboot survival, streak calculation on real hardware
2. **Home tab content** — only remaining placeholder; plan: daily step ring, today's habit summary (X/Y done), current streak highlight, motivational copy
3. **Daily reset validation** — confirm step base resets correctly at midnight across reboots and `onTaskRemoved` restarts

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — KSP workaround, leave as-is
- Accelerometer fallback in `StepSensorManager` is a stub — acceptable for Phase 1
- `TYPE_STEP_DETECTOR` registered twice if CalibrationScreen is open while service runs — harmless; Android delivers to both listeners independently
- `AlarmManager.setInexactRepeating` may fire a few minutes late — acceptable for daily habit reminders

---

## 5. Important Decisions This Session

- **`BootReceiver` uses `goAsync()`** — ensures the coroutine that re-schedules alarms is not killed before it finishes; required because `onReceive` has a ~10s budget on the main thread
- **Heatmap always starts on a Monday** — `buildHeatmapGrid()` snaps to the Monday of the current week then subtracts 15 weeks, guaranteeing aligned columns regardless of what day today is
- **`getLogsSince(16weeksAgo)` shared between heatmap and 7-day stats** — single DB query serves both; 7-day per-habit stats filter in-memory; avoids a second Flow subscription
- **`bestStreak` added to `Habit` domain model** — previously only in `HabitEntity`; needed to flow through to MainViewModel for profile display without a raw DAO call from the ViewModel layer
- **Heatmap colours resolved outside Canvas** — `MaterialTheme.colorScheme` can't be called inside `DrawScope`; all 5 level colours captured as Composable-scope vals before the Canvas lambda
- **Profile streak section hidden when both values = 0** — avoids showing an empty section for users who haven't set up habits yet; no hard-coded zero-state placeholder needed
