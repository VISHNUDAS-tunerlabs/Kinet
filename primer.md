# Kinet — Session Primer

## 1. Current State of the Project

Phase 1 architecture is complete. The app tracks steps via a foreground service, persists data with Room (v3), and has full navigation with bottom tabs, onboarding, calibration, a premium profile view page, and profile edit.

**Stack:** Kotlin, Jetpack Compose, Android Sensor API, Room v3, SharedPreferences, Coil 2.7.0 (image loading).

**Key files:**
- `MainActivity.kt` — StateFlow-driven `when` routing; no NavController; hosts bottom nav scaffold; `ProfileAvatar` composable for TopAppBar icon
- `MainViewModel.kt` — owns `isProfileSet`, `userProfile`, `currentTab`, `showProfile`, `showProfileEdit`, `showCalibration`, `appTheme`
- `MainViewModelFactory.kt` — passes `SharedPreferences` ("kinet_prefs") to `MainViewModel`
- `StepTrackingService.kt` — foreground service; registers both TYPE_STEP_COUNTER and TYPE_STEP_DETECTOR
- `engine/StepEngine.kt` — delta calculation + timing/session validation via STEP_DETECTOR
- `engine/CalibrationEngine.kt` — manual calibration + adaptive EMA for stride/cadence
- `engine/MetricsEngine.kt` — distance, calories, active minutes formulas
- `sensor/StepSensorManager.kt` — wraps sensor API; dual-sensor start with two callbacks
- `ui/AppTab.kt` — enum with 4 tabs (HOME, STEPS, HABITO, REPORTS)
- `ui/theme/AppTheme.kt` — enum: DYNAMIC, OCEAN, FOREST, SUNSET
- `ui/theme/Theme.kt` — KinetTheme accepts AppTheme param; full light/dark color schemes per theme
- `ui/theme/Color.kt` — palettes for Default (Purple), Ocean, Forest, Sunset
- `ui/profile/ProfileSetupScreen.kt` — premium onboarding (Height, Weight, Daily Step Goal)
- `ui/profile/ProfileViewScreen.kt` — premium profile details page (see layout below)
- `ui/profile/ProfileEditScreen.kt` — full profile edit (Height, Weight, Stride, Goal)
- `ui/dashboard/DashboardScreen.kt` — Steps tab; metrics + CalibrationCard entry point
- `ui/calibration/CalibrationScreen.kt` — 3-step guided walk: instruction → active → result/save
- `ui/home/HomeScreen.kt` — placeholder
- `ui/habito/HabitoScreen.kt` — placeholder
- `ui/reports/ReportsScreen.kt` — placeholder report cards
- `data/local/KinetDatabase.kt` — Room v3 with MIGRATION_1_2 + MIGRATION_2_3
- `data/repository/ActivityRepositoryImpl.kt` — Room persistence layer

---

## 2. What Was Accomplished This Session

### Profile View Screen (new premium page)
- Navigation flow: person icon in TopAppBar → `ProfileViewScreen` (view page), not directly to edit
- Back from ProfileViewScreen → main scaffold
- "Edit Profile" button inside → `ProfileEditScreen`; back/save from edit → back to ProfileViewScreen

**ProfileViewScreen layout:**
- **Hero**: gradient Box (primary → secondary), height wraps content with `padding(vertical = 32.dp)` + top inset padding — no fixed height, so no dead space below text
- Inside hero: 108dp circular avatar (white border, tappable) with 32dp camera badge overlay; "Kinet Athlete" title + "Fitness Tracker · Phase 1" subtitle
- `Spacer(24.dp)` between hero box and body (outside the box, in the outer Column)
- **Body Metrics**: 2×2 stat card grid — Height, Weight, Stride, Daily Goal — each with icon + large value + colored container (primaryContainer, secondaryContainer, tertiaryContainer, surfaceVariant)
- **Appearance**: 4 ThemeChips (Dynamic, Ocean, Forest, Sunset) with colored dot + selected border — live theme switching
- **Edit Profile** full-width button at bottom

### Profile image in TopAppBar
- `ProfileAvatar` composable in `MainActivity`: shows `AsyncImage` (32dp circle, Coil) when `profileImageUri` is set, falls back to `Person` icon when null
- Image is the same URI saved from the photo picker — consistent across profile view and main nav

### Profile image upload
- `PickVisualMedia` photo picker in `ProfileViewScreen`
- `takePersistableUriPermission` called (wrapped in `runCatching`) for persistence across restarts
- `MainViewModel.saveProfileImage(uri)` saves URI while preserving all other profile fields

### App theming infrastructure
- `AppTheme` enum (DYNAMIC, OCEAN, FOREST, SUNSET) in `ui/theme/AppTheme.kt`
- Full light + dark `ColorScheme` per theme in `Theme.kt`
- `KinetTheme` accepts `appTheme: AppTheme` — applied at root in `MainActivity`
- Theme persisted via SharedPreferences key `"app_theme"` (ordinal int)
- `MainViewModel.setTheme()` updates both StateFlow and SharedPreferences

### Data layer
- `UserProfile`: added `profileImageUri: String? = null`
- `UserProfileEntity`: added nullable `profileImageUri TEXT` column
- `KinetDatabase` bumped to **version 3**, `MIGRATION_2_3`: `ALTER TABLE user_profile ADD COLUMN profileImageUri TEXT`
- `saveProfile()` preserves existing `profileImageUri` when measurements are updated
- `saveProfileImage()` dedicated function for image-only updates

### Coil
- `coil = "2.7.0"` in `libs.versions.toml`, `coil-compose` in `build.gradle.kts`

---

## 3. Immediate Next Steps

1. **Physical device testing** — step counter and step detector don't fire on emulators; all accuracy and image picker work needs real hardware
2. **Profile name field** — hero hardcodes "Kinet Athlete"; add `name: String` to `UserProfile` + MIGRATION_3_4 so users can set their own name (editable in `ProfileEditScreen`)
3. **Session gate on DashboardScreen** — `StepEngine.isWalkingSession` is available but not surfaced in UI; consider a subtle "walking now" badge on the Steps tab
4. **Adaptive calibration hookup** — `CalibrationEngine.updateStride()` and `updateStepInterval()` exist but are not yet called after calibration in `CalibrationViewModel`
5. **Notification live step count** — foreground notification shows static text; update to reflect live count
6. **Daily reset validation** — confirm step base resets correctly at midnight across reboots and `onTaskRemoved` restarts
7. **Home tab content** — currently placeholder; plan content (streak, weekly summary, motivational card?)
8. **Habito + Reports** — fully placeholder; plan scope for Phase 2

---

## 4. Open Blockers / Unresolved Issues

- `android.disallowKotlinSourceSets=false` in `gradle.properties` — KSP workaround, leave as-is
- Accelerometer fallback in `StepSensorManager` is a stub — acceptable for Phase 1
- `TYPE_STEP_DETECTOR` registered twice if CalibrationScreen is open while the service runs — harmless, Android delivers to both listeners independently
- Profile name in hero is hardcoded as "Kinet Athlete" until a name field is added

---

## 5. Important Decisions This Session

- **Hero uses wrapping height, not fixed** — removed `height(290.dp)` in favour of `padding(vertical = 32.dp)` + outer `Spacer(24.dp)`, so the gradient card never has dead space below the subtitle text
- **ProfileAvatar in TopAppBar** — same Coil `AsyncImage` as the profile page; consistent identity across the app with zero extra state
- **Appearance section kept** — theming chips remain in `ProfileViewScreen` even after a brief removal; theming infrastructure is live and functional
- **STEP_COUNTER stays the count source of truth** — STEP_DETECTOR used only for timing/session and calibration
- **No NavController** — routing stays StateFlow-driven `when` in `MainActivity`; revisit if screen count grows beyond Phase 1
- **Room MIGRATION_2_3 with nullable column** — `profileImageUri TEXT` (no DEFAULT needed since nullable)
