# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 is feature-complete. All four tabs are live with real data. The app tracks steps via a foreground service, persists data with Room (v6), and has full navigation with bottom tabs, onboarding, calibration, profile (name/photo/edit/streaks), a fully functional Habito module with live streaks and reminders that survive reboot, a Reports tab with a GitHub-style habit heatmap, a rich foreground notification with live stats, and pause/resume/reset controls on the dashboard.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API, Room v6, SharedPreferences.

**Key files:**

### App shell
- `MainActivity.kt` — StateFlow-driven `when` routing; no NavController; passes typed ViewModels to all screens; `habitoViewModel` hoisted here for FAB + TopAppBar visibility control
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `currentTab`, `showProfile`, `showProfileEdit`, `showCalibration`, `appTheme`, `currentStreak`, `bestStreak`
- `MainViewModelFactory.kt` — wires `ActivityRepositoryImpl` + `HabitRepositoryImpl` + SharedPreferences

### Sensor & engine
- `service/StepTrackingService.kt` — foreground service; NOTIFICATION_ID = 100; handles `ACTION_PAUSE`, `ACTION_RESUME`, `ACTION_RESET` intents; `startForeground` called on EVERY `onStartCommand`; `isPaused` service field mirrors `TrackingState`
- `service/HabitReminderReceiver.kt` — BroadcastReceiver; NOTIFICATION_ID = 200
- `service/BootReceiver.kt` — re-schedules all reminders on boot via `HabitReminderScheduler`
- `engine/StepEngine.kt` — delta calculation + timing/session detection; supports `pause(sensorValue)`, `resume(sensorValue)`, `resetToday(sensorValue)`
- `engine/StepSessionState.kt` — process-wide singleton `MutableStateFlow<Boolean>`
- `engine/TrackingState.kt` — process-wide singleton `MutableStateFlow<Boolean>` for `isPaused`
- `engine/CalibrationEngine.kt` — manual calibration + adaptive EMA (80/20)
- `engine/MetricsEngine.kt` — distance, calories, active minutes formulas

### Data layer
- `data/local/KinetDatabase.kt` — Room **v6**; MIGRATION_1_2 through MIGRATION_5_6
  - MIGRATION_5_6: `ALTER TABLE habits ADD COLUMN cardColor TEXT NOT NULL DEFAULT 'FFFFFF'`
- `data/local/dao/DailyActivityDao.kt` — `getByDate()`, `getLastSevenDays()`, `upsert()`, `resetForDate()`
- `data/local/dao/HabitDao.kt` — `getActiveHabits()`, `getHabitsWithReminders()`, `getById()`, `insertOrReplace()`, `softDelete()`, `getLogsByDate()`, `getLogsByHabitId()`, `getLogsSince()`, `insertOrReplaceLog()`, `updateStreaks()`
- `data/repository/HabitRepositoryImpl.kt` — `logHabit()` triggers `recalculateStreak()`; passes `cardColor` when building `HabitEntity`
- `domain/model/Habit.kt` — `cardColor: String = "FFFFFF"` field added (hex string without `#`)
- `data/local/entity/HabitEntity.kt` — `cardColor: String = "FFFFFF"` column added; mapped in `toDomain()`

### UI — Dashboard (Steps tab)
- `ui/dashboard/DashboardViewModel.kt` — exposes `todayActivity`, `stepGoal`, `isWalkingSession`, `isPaused`; `pause()`, `resume()`, `resetSteps()` send intents to service
- `ui/dashboard/DashboardScreen.kt` — Pause/Resume toggle + Reset button; "Paused" badge; progress bar turns error-red when paused

### UI — Theme
- `ui/theme/Theme.kt` — **app is light-only**; dark scheme removed entirely; dynamic scheme forces `AppBackground` + `AppSurface`
- `ui/theme/Color.kt` — `AppBackground = #F8F9FA`, `TextPrimary = #0D0D0D`, `TextSecondary = #444444`
- `ui/theme/Type.kt` — **Nunito** via Google Fonts for all 15 M3 text styles; `TextPrimary` for display/headline/title, `TextSecondary` for body/label
- `res/values/themes.xml` — `android:windowBackground = @color/app_background` (prevents black flash on launch)
- `res/values/colors.xml` — `app_background = #FFF8F9FA`
- `res/values/font_certs.xml` — GMS Google Fonts provider certificates

### UI — Habito
- `ui/habito/HabitoViewModel.kt`
  - `HabitoSubScreen`: LIST, ADD_EDIT, DAILY_LOG, HABIT_DETAILS
  - `saveHabit()` now accepts `cardColor: String` parameter
  - `last30DaysLogs` StateFlow for 30-day completion rate
  - `_selectedHabit` / `selectedHabit` for HabitDetailsScreen
- `ui/habito/HabitoScreen.kt` — major redesign this session (see below)

#### HabitoScreen current design:
- **HabitListScreen**: LazyColumn with `GreetingHeader` → spacer → `WeekDateStrip` → `TodayOverviewCard` → 2-column habit grid
- **GreetingHeader**: time-aware greeting, avatar, notifications icon
- **WeekDateStrip**: 5-day view (today±2); `RoundedCornerShape(100.dp)` capsule per day; past=`#4DD631`, today=`#1A1A1A`, future=white; `weight(1f)` equal distribution; `headlineSmall` date + `bodyMedium` day name
- **TodayOverviewCard**: dark (`#1A1A1A`) card; `titleLarge` bold title; `bodyMedium` subtitle; custom Box progress bar (`7.dp` height, `RoundedCornerShape(50)`, `#4DD631` fill); BarChart icon `34.dp` with `clip(RoundedCornerShape(6.dp))`; padding `24.dp` horizontal + vertical
- **HabitGridCard**:
  - Card elevation `3.dp` (subtle shadow)
  - Card color from `habit.cardColor` (parsed via `android.graphics.Color.parseColor`)
  - Padding `18.dp` all around
  - Top row: category icon (left) · delete icon + checkbox (right)
  - Delete icon: `20.dp`, `#1A1A1A`, touch target `32.dp`
  - Checkbox border: `#1A1A1A`; completed = `CheckCircle` in primary color
  - Title: `bodyLarge`, Bold, `#1A1A1A`
  - Reminder chip: Alarm icon `13.dp` + `labelMedium` text, both `#1A1A1A`
  - Streak row: fire icon `15.dp` + `labelMedium` count, both `#1A1A1A`
- **AddEditHabitScreen**:
  - Color picker row: 4 circular swatches (`#4DD631`, `#FCB932`, `#FFFFFF`, `#81AEFC`); selected = dark border + checkmark
  - `onSave` lambda: `(String, HabitCategory, Boolean, Int?, Boolean, String?, String) -> Unit` (7th param = `cardColor`)
- **HabitDetailsScreen**: hero card, streak stat cards, 30-day completion rate, "Mark as Completed" CTA
- TopAppBar hidden on HABITO tab; FAB shown only on LIST sub-screen (bottom-right)

### UI — Reports
- `ui/reports/ReportsViewModel.kt` — 16-week heatmap grid; per-habit 7-day stats; weekly step summaries
- `ui/reports/ReportsScreen.kt` — step chart + 6 stat cards + heatmap + per-habit completion cards
- `ui/reports/HabitHeatmap.kt` — Canvas; 16×7 grid; 5 colour levels; month/day labels; legend

---

## 2. What Was Accomplished This Session

1. **TodayOverviewCard polish** — larger text (`titleLarge`/`bodyMedium`), more padding (`24.dp`), custom rounded Box progress bar (`7.dp`, `#4DD631`), BarChart icon enlarged to `34.dp` with `RoundedCornerShape(6.dp)` clip; increased gap between date strip and greeting header
2. **Habit card color picker** — 4 color options (`#4DD631`, `#FCB932`, `#FFFFFF`, `#81AEFC`) in Add/Edit screen; stored in `cardColor` field; propagated through domain model → entity → DB (MIGRATION_5_6) → ViewModel → UI
3. **Habit card UI refinements**:
   - Padding `14dp` → `18dp`
   - Title `bodyMedium` → `bodyLarge`
   - Delete icon larger (`20dp`), black (`#1A1A1A`)
   - Checkbox border black (`#1A1A1A`)
   - Reminder + fire icons/text: larger sizes, all black (`#1A1A1A`)
   - Elevation `0dp` → `3dp` for subtle shadow
4. **BarChart icon fix** — removed unnecessary Box wrapper; `clip(RoundedCornerShape(6.dp))` applied directly to icon modifier

---

## 3. Immediate Next Steps

1. **Physical device testing** — validate card color persists across app restart; confirm MIGRATION_5_6 runs cleanly on existing installs
2. **Home tab content** — still a placeholder; plan: daily step ring, today's habit summary (X/Y done), current streak highlight
3. **Habit card white color contrast** — white card on `#F8F9FA` background may be hard to distinguish; consider a subtle border when `cardColor == "FFFFFF"`
4. **Persist pause state across service restart** — if service is killed while paused, `isPaused` resets to `false`; fix: persist `sensorValueAtPause` to SharedPreferences

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — KSP workaround, leave as-is
- Accelerometer fallback in `StepSensorManager` is a stub — acceptable for Phase 1
- **Pause state lost on service restart** — steps accumulated during pause counted on next run
- `AlarmManager.setInexactRepeating` may fire a few minutes late — acceptable for habit reminders
- White habit card (`#FFFFFF`) on white-ish app background (`#F8F9FA`) has low visual separation — no border added yet

---

## 5. Important Decisions This Session

- **`cardColor` stored as hex string without `#`** (e.g. `"4DD631"`) — parsed at render time with `android.graphics.Color.parseColor("#$cardColor")`; default `"FFFFFF"`
- **Room v5 → v6** — `MIGRATION_5_6` is a simple `ALTER TABLE ADD COLUMN`; existing habits get `cardColor = 'FFFFFF'` (white) automatically
- **Progress bar as custom Box** — `LinearProgressIndicator` doesn't support rounded ends in M3 without workarounds; custom nested `Box` with `RoundedCornerShape(50)` gives full control over height, color, and shape
- **Fire icon always black** — removed the conditional orange/grey; black is more consistent with the card's design language regardless of streak count
- **`clip()` directly on Icon modifier** — avoids extra layout node; `RoundedCornerShape(6.dp)` clips the icon's rectangular bounding box for a slightly softened look
