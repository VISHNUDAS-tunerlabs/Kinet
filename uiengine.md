Please enhance the app experience by introducing a first-time user onboarding flow for collecting essential fitness profile data, while keeping the design scalable for future UI improvements.

🎯 Goal

When a user opens the app for the first time, they must be guided through a modern onboarding/setup screen before entering the main app.

This onboarding should collect the minimum required profile inputs needed for personalized tracking.

🧾 Required Fields

The onboarding screen must collect:

Height
Weight
Daily Steps Goal

These values should become the initial user profile configuration.

🔁 Profile Edit Support

The same fields must also remain editable later from the Profile Edit section.

This means:

Onboarding = initial setup
Profile Edit = update existing values
Both should reuse the same validation and data model where possible
👤 First-Time User Logic

Implement logic such that:

If user opens app for the first time
→ show onboarding flow
If onboarding is already completed
→ directly open app Home screen

Suggested implementation:

Persist isOnboardingCompleted
Store height, weight, and step goal locally
Use SharedPreferences / DataStore based on current architecture
🎨 UI / UX Requirements

The onboarding screen should feel premium, modern, and aligned with the app’s current visual identity.

Design expectations
Minimal clean layout
Smooth spacing
Rounded input cards/fields
Progress-friendly feel (like a setup wizard)
Large CTA button:
“Continue” / “Start Tracking”
Friendly welcoming header such as:
“Let’s personalize your journey”
🖼️ Future-Ready Visual Design

Even though custom illustrations/graphics are not available right now, the screen layout should be designed with future extensibility in mind.

Please leave intentional space/sections for:

Hero illustration
Progress graphic
Fitness avatar
Motivational visual cards
Animated onboarding assets later

The structure should allow future UI enhancements without redesigning the whole screen.

🧱 Suggested Technical Structure

Recommended scalable approach:

Dedicated OnboardingScreen
Reusable profile form component
Shared validation logic with Profile Edit
Data persistence layer abstraction
Future-ready composable/fragment sections for illustration slots
✨ UX Intent

The onboarding should make users feel that the app is:

personalized
intelligent
health-focused
premium
expandable for future features