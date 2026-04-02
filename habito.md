# Habito — Habit Tracking Feature Reference

## Overview

A lightweight, offline-first habit tracking module integrated into the existing Kinet tab navigation. Helps users build daily routines with a calm, motivating, and low-friction experience — usable in under 30 seconds per day.

---

## Architectural Constraints (from current app)

- **No NavController** — sub-navigation inside the Habito tab must follow the same StateFlow-driven `when` routing pattern used in `MainActivity`. A `HabitoViewModel` will own the current sub-screen state.
- **Room v3** — habit data goes into `KinetDatabase` as new entities with a `MIGRATION_3_4`.
- **Offline-first** — no network calls, no AI suggestions in Phase 1.
- **Local notifications only** — Android `NotificationChannel` (the app already holds notification permission via the foreground step service).
- **Step integration** — step-based habits (e.g. "Walk 8,000 steps") can be auto-evaluated using live step data already available from `StepEngine` / `ActivityRepository`.

---

## Phase 1 Scope

### Screens (sub-navigation via `HabitoViewModel` state)

| State | Screen |
|---|---|
| `HabitoScreen.LIST` | My Habits — default landing |
| `HabitoScreen.ADD` | Add / Edit Habit |
| `HabitoScreen.DAILY_LOG` | Mark Daily Status |

The Habit Assistant is scoped to **local reminder notifications only** in Phase 1 — not a separate screen.

---

## Screens

### 1. My Habits (Landing)

Default view when user taps the Habito tab.

**Layout:**
- Header row: "My Habits" title + "+" icon button (navigates to ADD screen)
- "Mark Today's Habits" CTA button at top — navigates to DAILY_LOG
- List of `HabitCard` items (scrollable `LazyColumn`)
- Empty state illustration + "Add your first habit" text when list is empty

**HabitCard shows:**
- Habit title + category icon
- Reminder time (if enabled), e.g. "Reminder: 8:00 AM"
- Today's status badge: Completed / Skipped / Pending
- Streak placeholder (e.g. "— day streak") — value always 0 in Phase 1, field is there for Phase 2
- Trailing: edit icon + delete (swipe-to-dismiss or long-press menu)

---

### 2. Add / Edit Habit

**Fields:**
- **Title** — text input (required); pre-filled when editing
- **Category** — single-select chip row: Health, Sleep, Fitness, Mindfulness, Custom
- **Habit type** — toggle: Manual (user marks it) vs Step-Based (auto-evaluated from step count)
  - If Step-Based: numeric input for step target (default: 8000)
- **Reminder** — toggle; if enabled: time picker (hour/minute) using `TimePickerDialog`
- **Save / Cancel** buttons

**Built-in suggestions** (shown as tappable chips above the input when title is empty):
- Drink a glass of water after waking up
- Go to bed before 11 PM
- Walk 8,000 steps *(auto-sets type to Step-Based, target 8000)*
- Read for 15 minutes
- Morning stretching

Tapping a suggestion pre-fills the form; user can still edit before saving.

---

### 3. Mark Daily Status

Shown when user taps "Mark Today's Habits" or taps a reminder notification.

**Layout:**
- Progress header: "X of Y completed today" with a linear progress bar
- Card-based flow: one `HabitLogCard` per active habit with Pending status today
- Already-logged habits shown in a collapsed section below ("Done for today")

**HabitLogCard interactions:**
- "Completed" button (primary, filled)
- "Skip" button (secondary, outlined)
- Step-based habits show live progress inline: "6,240 / 8,000 steps" with auto-complete when target is reached (no manual tap needed)

**Positive reinforcement:**
- When all habits are marked: "All done for today!" success state with a checkmark illustration

---

## Habit Assistant (Phase 1: Reminders Only)

No separate screen. Implemented as a background `BroadcastReceiver` + `AlarmManager`.

**Behavior:**
- At the user's set reminder time, fire a local notification
- Notification text: "Time to check in on your habits — X pending today"
- Tapping the notification opens the app directly to the DAILY_LOG sub-screen
- No network, no AI, no nudges beyond this in Phase 1

**Future scope (Phase 2+):**
- Streak protection reminders ("You're on a 5-day streak — don't break it!")
- Missed habit recovery prompts
- Smart timing suggestions based on past completion patterns

---

## Data Model

### Room Entity: `HabitEntity`

```kotlin
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val habitId: Int = 0,
    val title: String,
    val category: String,           // "HEALTH" | "SLEEP" | "FITNESS" | "MINDFULNESS" | "CUSTOM"
    val isCustom: Boolean,
    val isStepBased: Boolean,
    val stepTarget: Int?,           // null if not step-based
    val reminderEnabled: Boolean,
    val reminderTime: String?,      // "HH:mm" format, null if disabled
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    // Phase 2 fields — stored now, not surfaced in UI yet
    val streakCount: Int = 0,
    val bestStreak: Int = 0
)
```

### Room Entity: `HabitLogEntity`

```kotlin
@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val habitId: Int,               // FK → HabitEntity.habitId
    val date: String,               // "yyyy-MM-dd"
    val status: String              // "COMPLETED" | "SKIPPED" | "PENDING"
)
```

### Migration

`MIGRATION_3_4` in `KinetDatabase`:
- `CREATE TABLE habits (...)`
- `CREATE TABLE habit_logs (...)`

### Domain Model

```kotlin
data class Habit(
    val habitId: Int,
    val title: String,
    val category: HabitCategory,
    val isStepBased: Boolean,
    val stepTarget: Int?,
    val reminderEnabled: Boolean,
    val reminderTime: String?,
    val isActive: Boolean,
    val streakCount: Int
)

data class HabitLog(
    val logId: Int,
    val habitId: Int,
    val date: String,
    val status: HabitStatus
)

enum class HabitCategory { HEALTH, SLEEP, FITNESS, MINDFULNESS, CUSTOM }
enum class HabitStatus { COMPLETED, SKIPPED, PENDING }
```

---

## ViewModel & Navigation

```
HabitoViewModel
 ├── habitSubScreen: StateFlow<HabitoSubScreen>
 ├── habits: StateFlow<List<Habit>>
 ├── todayLogs: StateFlow<List<HabitLog>>
 ├── todaySteps: StateFlow<Int>           // from ActivityRepository — for step-based auto-eval
 └── editingHabit: StateFlow<Habit?>      // null = new habit
```

```kotlin
enum class HabitoSubScreen { LIST, ADD, DAILY_LOG }
```

`HabitoScreen.kt` does a `when(habitSubScreen)` switch — mirrors the pattern in `MainActivity`.

`HabitoViewModelFactory` receives `HabitRepository` and `ActivityRepository` (for live steps).

---

## Step-Based Habit Auto-Evaluation

- On each step count update, `HabitoViewModel` checks all active step-based habits for today
- If `todaySteps >= habit.stepTarget` and today's log is still `PENDING`, auto-insert a `COMPLETED` log
- No user action needed — surfaces in the DAILY_LOG card as "Auto-completed"

This reuses the existing `ActivityRepository` / `DashboardViewModel` step data — no new sensor work.

---

## Design Principles

- Calm, positive, non-judgmental tone
- Rounded cards, soft elevation — consistent with existing app `DashboardScreen` card style
- Empty states with helpful prompt (not blank screens)
- Streak placeholders rendered now (value = 0) so Phase 2 only needs to populate the number
- Animation-ready layout slots (use `AnimatedVisibility` where natural, nothing forced)
- Entire interaction for daily check-in completable in under 30 seconds

---

## Phase 2 Scope (not in Phase 1)

- Streak calculation + best streak tracking
- Monthly completion rate analytics
- Reports tab integration (habit trend charts)
- Partial completion status
- Streak protection notifications
- Social accountability
- Reward badges
- AI-generated routine suggestions

---

## Files to Create

```
ui/habito/HabitoScreen.kt              (replace placeholder)
ui/habito/HabitoViewModel.kt
ui/habito/HabitoViewModelFactory.kt
ui/habito/components/HabitCard.kt
ui/habito/components/HabitLogCard.kt
ui/habito/screens/HabitListScreen.kt
ui/habito/screens/AddEditHabitScreen.kt
ui/habito/screens/DailyLogScreen.kt
data/local/entity/HabitEntity.kt
data/local/entity/HabitLogEntity.kt
data/local/dao/HabitDao.kt
data/repository/HabitRepository.kt
data/repository/HabitRepositoryImpl.kt
domain/model/Habit.kt
domain/model/HabitLog.kt
service/HabitReminderReceiver.kt       (BroadcastReceiver for AlarmManager)
```
