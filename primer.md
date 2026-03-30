# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 architecture is complete and buildable. The app tracks steps via a foreground service, persists data, and has a clean onboarding + dashboard flow.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API, Room (local DB), SharedPreferences.

**Key files:**
- `MainActivity.kt` — routing hub; no NavController, uses StateFlow-driven `when` branching
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `showProfileEdit`
- `StepTrackingService.kt` — foreground service with sensor management
- `ui/profile/ProfileSetupScreen.kt` — first-run onboarding
- `ui/profile/ProfileEditScreen.kt` — edit existing profile (added this session)
- `ui/dashboard/DashboardScreen.kt` — main dashboard with metrics
- `data/repository/ActivityRepositoryImpl.kt` — Room + SharedPreferences persistence

---

## 2. What Was Accomplished This Session

### Notification tap action (`StepTrackingService.kt`)
- Added `PendingIntent` targeting `MainActivity` with `FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP`
- Tapping the foreground notification now brings the app to the foreground
- Used `FLAG_IMMUTABLE` (required Android 12+)

### `onTaskRemoved` handler (`StepTrackingService.kt`)
- Added `onTaskRemoved()` override to schedule a self-restart via `AlarmManager.set()`
- Restarts the service 1 second after the user swipes the app from recents
- Uses `AlarmManager.ELAPSED_REALTIME` (no wakelock needed) + `FLAG_ONE_SHOT | FLAG_IMMUTABLE`
- No `SCHEDULE_EXACT_ALARM` permission needed (uses inexact `set()`)

### Profile edit screen (new feature)
- Created `ProfileEditScreen.kt` — pre-filled with current profile values
- TopAppBar with back arrow (cancel), side-by-side Cancel + Save buttons
- Save button disabled until fields are valid **and** at least one value has changed
- Stride length shows "hint: height × 0.415" and auto-populates unless manually edited
- `MainViewModel` updated: added `userProfile: StateFlow<UserProfile>`, `showProfileEdit: StateFlow<Boolean>`, `openProfileEdit()`, `closeProfileEdit()`
- `saveProfile()` now also calls `closeProfileEdit()` after persisting
- `DashboardScreen` updated: header row has a `Person` icon button (top-right) → opens edit screen
- `MainActivity` updated: when profile is set, branches on `showProfileEdit` to show either dashboard or edit screen

---

## 3. Immediate Next Steps

1. **Test on a physical device** — emulators don't fire the step counter sensor; all sensor logic needs real hardware validation
2. **Handle step base on service restart** — confirm `restoreStepBase()` correctly handles gaps when `onTaskRemoved` triggers a restart
3. **Daily reset logic** — verify step counter resets at midnight (sensor gives cumulative count since reboot; app must diff against stored base)
4. **Notification content update** — currently shows "Tracking your steps" statically; update dynamically to show live step count
5. **WeeklyChart data** — confirm `weeklyActivities` is correctly queried and displayed on the dashboard

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — temporary KSP workaround, leave as-is
- Accelerometer fallback in sensor layer is a stub — acceptable for Phase 1
- No Room migration strategy yet — if schema changes, users lose data (Phase 1 acceptable)

---

## 5. Important Decisions This Session

- **No NavController** — routing stays StateFlow-driven in `MainActivity` (`when` on sealed state). Simple enough for Phase 1; revisit if screens grow beyond 4-5.
- **`onTaskRemoved` uses inexact alarm** — avoids needing `SCHEDULE_EXACT_ALARM` permission; 1s delay is acceptable for a pedometer.
- **Save disables until changed** — `ProfileEditScreen` prevents no-op saves by comparing current vs. stored values before enabling the Save button.
