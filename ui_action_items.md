# UI Action Items — Based on Reference Designs

> Reference images: `uireference/HomeDashboard.png`, `uireference/HabitDetails.png`
> Current state: Habito tab has a plain `HabitListScreen` (LIST/ADD_EDIT/DAILY_LOG). No details screen. Bottom nav is a plain 4-tab NavigationBar. The separate `HomeScreen` (AppTab.HOME) is a stub and is out of scope here.

---

## Action 1 — Redesign HabitListScreen (Habito Tab)

**File:** `app/src/main/java/com/example/kinet/ui/habito/HabitoScreen.kt`
**Status:** `HabitListScreen` currently shows a plain `LazyColumn` of `HabitCard`s with a "Mark Today's Habits" button and an "Add Habit" button at the bottom. Reference image (`HomeDashboard.png`) shows this same Habito tab — redesign it to match.

### 1a. Personalized Greeting Header (inside HabitListScreen)
- Replace nothing — add a new header `Column` at the top of `HabitListScreen` (above the habit grid), inside the existing scroll
- Show: `"Good morning,"` (labelLarge, muted) + `"[UserName]"` (headlineMedium, bold) on the left
- Right side: notification bell `IconButton` (no-op for now) + circular profile `AsyncImage` avatar
- **Data needed:** pass `userName: String` and `profileImageUri: String?` as params into `HabitListScreen`
- Pass these down from `HabitoScreen` → needs `MainViewModel` wired into the Habito tab in `MainActivity.kt`

### 1b. Week Date Strip
- Horizontal `LazyRow` of 7 day chips spanning the current week (Mon–Sun)
- Each chip: day abbreviation (Mon/Tue…) + date number (11/12…)
- Today: filled black circle with white text; other days: gray outlined circle
- Visual only — no tap navigation for Phase 1

### 1c. Today's Overview Card
- Dark (`Color(0xFF1A1A1A)` or `onBackground`) full-width `Card` with 16dp rounded corners
- Title: `"Today's Overview"` (white, titleMedium bold)
- Subtitle: `"X of Y habits completed"` (white, bodySmall muted)
- Top-right: `Icons.Filled.BarChart` icon (white, no-op)
- `LinearProgressIndicator` below subtitle (progress = completedCount / total, track color white 20% alpha, indicator white)
- Empty state text when 0 completed: `"You haven't completed any habits yet today. Let's get started!"` (white, bodySmall)

### 1d. Habit Cards Grid (2-column) — replaces current LazyColumn
- Swap `LazyColumn` for `LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp))`
- Each card:
  - Background color per category:
    - `HEALTH` → `Color(0xFFD6E8FF)` (blue)
    - `SLEEP` → `MaterialTheme.colorScheme.surface` (white/light)
    - `FITNESS` → `Color(0xFFD6F0D6)` (green)
    - `MINDFULNESS` → `Color(0xFFFFF0C2)` (yellow)
    - `CUSTOM` → `MaterialTheme.colorScheme.surfaceVariant`
  - Top row: category icon in small rounded square (left) · `Icons.Filled.MoreVert` `IconButton` (no-op, center) · circle checkbox `IconButton` (right — filled `Icons.Filled.CheckCircle` in primary if completed, else `Icons.Outlined.RadioButtonUnchecked`)
  - Habit `title` — bodyLarge bold, max 2 lines
  - Subtitle line: step target (e.g., `"8,000 steps"`) if step-based, else category label (e.g., `"After waking up"` — use `reminderTime` formatted, fallback to category label)
  - Bottom row chips: time chip (if `reminderEnabled`) + flame icon + `streakCount` label
- Card tap → `onHabitClick(habit)` → navigates to `HABIT_DETAILS` (Action 2)
- Checkbox tap → `onMarkToday(Int)` → logs COMPLETED immediately without opening DAILY_LOG

### 1e. Floating Add Button — replaces bottom "Add Habit" button
- Remove the full-width `Button("Add Habit")` at the bottom
- Use a `FloatingActionButton` anchored via `Scaffold`'s `floatingActionButton` slot in `HabitListScreen`'s parent, or pass `onAddHabit` up and handle in MainActivity's Scaffold FAB slot
- The center FAB in Action 3 can serve double duty here

### 1f. Pass userName + profileImageUri into HabitListScreen
- In `HabitoScreen.kt`, add `userName: String` and `profileImageUri: String?` params to `HabitoScreen`
- In `MainActivity.kt`, collect `userProfile` from `mainViewModel` and pass `userProfile.name` and `userProfile.profileImageUri` to `HabitoScreen`

---

## Action 2 — Add HabitDetails Sub-Screen to Habito

**File:** `app/src/main/java/com/example/kinet/ui/habito/HabitoScreen.kt`
**File:** `app/src/main/java/com/example/kinet/ui/habito/HabitoViewModel.kt`

### 2a. Add HABIT_DETAILS to HabitoSubScreen enum
```kotlin
enum class HabitoSubScreen { LIST, ADD_EDIT, DAILY_LOG, HABIT_DETAILS }
```

### 2b. HabitoViewModel changes
- Add `selectedHabit: StateFlow<Habit?>` — set when navigating to details
- `navigateTo(HabitoSubScreen.HABIT_DETAILS, habit)` stores the selected habit
- Add `fun getCompletionRate(habitId: Int): Float` — queries `habitLogs` for last 30 days, returns completedCount / 30f (clamped 0–1)
- Expose `selectedHabitLogs: StateFlow<List<HabitLog>>` filtered to selected habit

### 2c. HabitDetails Screen Composable

#### Header
- Row: `IconButton` back arrow (left) · `"Habit Details"` centered title · `IconButton` pencil edit (right, navigates to ADD_EDIT for this habit)

#### Hero Card
- Large lavender/primaryContainer `Card` with 16dp rounded corners, takes ~35% of screen height
- Centered content: large category icon in a 56dp rounded square container
- Habit `title` below — headline bold, centered
- Reminder pill below title: clock icon + `"HH:MM AM • [category label]"` — shown only if `reminderEnabled`

#### Overview Section
- `"Overview"` label (labelLarge)
- Two side-by-side `Card`s in a `Row`:
  - Left: flame icon + `currentStreak` (displayLarge) + `"Current Streak"` (bodySmall)
  - Right: trophy icon (`Icons.Filled.EmojiEvents`) + `bestStreak` + `"Best Streak"`

#### Completion Rate Card
- Full-width `Card`
- Left: bullseye/target icon (`Icons.Filled.TrackChanges`) + `"Completion Rate"` (bodyLarge bold) + `"Last 30 days"` (bodySmall muted)
- Right: `"85%"` (headlineLarge, colored primary)
- Full-width green `LinearProgressIndicator` at the bottom of the card

#### Mark as Completed CTA
- Full-width black rounded `Button` (use `ButtonDefaults.buttonColors(containerColor = Color.Black)`)
- Label: `"✓ Mark as Completed"`
- Disabled + grey if already logged today as COMPLETED
- On click: `viewModel.logHabit(habitId, HabitStatus.COMPLETED)` then pop back to LIST

### 2d. Wire HABIT_DETAILS into HabitoScreen entry point
In `HabitoScreen` `when(subScreen)`:
```kotlin
HabitoSubScreen.HABIT_DETAILS -> HabitDetailsScreen(
    habit = selectedHabit,
    currentStreak = ...,
    bestStreak = ...,
    completionRate = ...,
    todayLog = ...,
    onBack = { viewModel.navigateTo(HabitoSubScreen.LIST) },
    onEdit = { viewModel.navigateTo(HabitoSubScreen.ADD_EDIT, selectedHabit) },
    onMarkCompleted = { viewModel.logHabit(it, HabitStatus.COMPLETED) }
)
```

### 2e. Habit card tap → HabitDetails
- In `HabitListScreen`, make each `HabitCard` clickable (add `onClick: () -> Unit` param)
- Tap routes to `HABIT_DETAILS` with that habit

---

## Action 3 — Add FAB for "Add Habit" (bottom-right, above nav bar)

**File:** `app/src/main/java/com/example/kinet/MainActivity.kt`

### What to change
- Keep the existing 4-tab `NavigationBar` as-is (Home · Steps · Habito · Reports)
- Add a standard `FloatingActionButton` in the `Scaffold`'s `floatingActionButton` slot
- FAB is only visible when `currentTab == AppTab.HABITO` and `subScreen == HabitoSubScreen.LIST` — hide on all other tabs and Habito sub-screens
- FAB action: `viewModel.navigateTo(HabitoSubScreen.ADD_EDIT)` — same as the old "Add Habit" button
- Use `Icons.Filled.Add` icon
- Remove the full-width `Button("Add Habit")` currently at the bottom of `HabitListScreen` (Action 1e) — FAB replaces it

### Implementation approach
```kotlin
// In Scaffold:
floatingActionButton = {
    if (currentTab == AppTab.HABITO && habitoSubScreen == HabitoSubScreen.LIST) {
        FloatingActionButton(onClick = { habitoViewModel.navigateTo(HabitoSubScreen.ADD_EDIT) }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Habit")
        }
    }
}
// floatingActionButtonPosition = FabPosition.End (default — bottom-right)
```

- Expose `subScreen: StateFlow<HabitoSubScreen>` from `HabitoViewModel` up to `MainActivity` (it's already a StateFlow — just collect it in MainActivity alongside `currentTab`)

---

## Action 4 — TopAppBar: Context-Aware Header

**File:** `app/src/main/java/com/example/kinet/MainActivity.kt`

- When `currentTab == AppTab.HOME`: do NOT render the generic `TopAppBar` (HomeScreen has its own embedded header per reference)
- All other tabs: keep existing `TopAppBar` with tab label + profile avatar
- Use a `if (currentTab != AppTab.HOME)` guard around the `topBar` `Scaffold` param, or pass `topBar = {}` lambda conditionally

---

## Dependency Map

```
Action 1 (Redesign HabitListScreen)
  └─ requires: userName + profileImageUri passed into HabitoScreen (MainActivity change)
  └─ requires: Action 4 (hide TopAppBar on HABITO tab — it has its own header now)
  └─ requires: Action 2 (HABIT_DETAILS) for card tap to work

Action 2 (HabitDetails)
  └─ requires: new HABIT_DETAILS enum value
  └─ requires: HabitoViewModel.selectedHabit + completionRate logic
  └─ unlocks: tap-to-details from Action 1 habit grid

Action 3 (Bottom-right FAB)
  └─ standalone — can be done independently
  └─ replaces the removed "Add Habit" bottom button from Action 1
  └─ requires HabitoViewModel's subScreen StateFlow collected in MainActivity

Action 4 (TopAppBar — hide on HABITO tab)
  └─ required by Action 1 (HabitListScreen owns its own header)
```

---

## Order of Execution

1. **Action 4** — hide TopAppBar for HABITO tab (1-line change, quick prerequisite)
2. **Action 2a + 2b** — add `HABIT_DETAILS` enum + ViewModel changes (no UI yet, unblocks card taps)
3. **Action 2c + 2d + 2e** — build HabitDetails screen
4. **Action 1** — redesign HabitListScreen (greeting, week strip, overview card, 2-col grid)
5. **Action 3** — bottom-right FAB (standalone, replaces removed Add button)

---

## Files to Touch (Summary)

| File | Change |
|------|--------|
| `ui/habito/HabitoScreen.kt` | Redesign `HabitListScreen` (greeting header, week strip, overview card, 2-col grid) + add `HabitDetailsScreen` + wire `HABIT_DETAILS` into `when(subScreen)` |
| `ui/habito/HabitoViewModel.kt` | Add `selectedHabit`, `completionRate()`, `HABIT_DETAILS` navigation |
| `MainActivity.kt` | Hide TopAppBar on HABITO tab + pass `userProfile.name`/`profileImageUri` to `HabitoScreen` + center FAB bottom nav |
| `ui/home/HomeScreen.kt` | Out of scope for this session — keep as stub |
