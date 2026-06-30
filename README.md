# DecideForMe

A personal decision engine that eliminates daily decision fatigue. Pre-load your options, tap one button, and the app tells you what to do. No thinking required.

## Features

### Core
- **One-Tap Decide** — Big animated button that instantly picks for you
- **Smart Decision Engine** — Learns your preferences over time with weighted scoring
- **Contextual Awareness** — Factors in time of day, day of week, weather, mood, and recent history
- **Decision Categories** — Meals, Workouts, Outfits, Movies/Shows, Date Night, Weekend Activities, plus custom categories
- **Shake to Decide** — Shake your phone for a fun randomized decision with slot-machine animation

### Intelligence
- **Learning Engine** — Tracks accepts vs rejects, boosts options you love, deprioritizes ones you skip
- **Smart Suggestions** — 10-15 pre-built suggestions per category with contextual tags
- **Mood Tracker** — Infers your energy level from decision patterns
- **Contextual Tags** — Options tagged with mood, budget, weather, time, and day type

### Social
- **Couple/Group Mode** — Share a category via QR code, merge preferences, decide together
- **Export/Import** — Full JSON backup, import on new device

### UI/UX
- **Material You** — Dynamic color theming with 4 curated palettes (Ocean, Sunset, Forest, Lavender)
- **Dark Mode + AMOLED Black** — Full dark mode support
- **Spring Animations** — Smooth bouncy animations on every interaction
- **Confetti Celebrations** — Streak milestones trigger confetti
- **Haptic Feedback** — Tactile response on decisions
- **Home Screen Widget** — Glance-based widget for instant decisions

### Tracking
- **Decision History** — List view and calendar view of all decisions
- **Streaks & Stats** — Current streak, longest streak, accept rate, favorites, weekly recap
- **Calendar View** — Visual overview of decision activity by day

### Identity
- **Google Sign-In** — Optional, for personalized experience only
- **Continue Without** — Fully functional offline with no account needed

## Architecture

```
app/src/main/java/com/decideforme/
├── data/
│   ├── model/          # @Serializable data classes (entire JSON schema)
│   └── repository/     # Single JSON file CRUD with Mutex + StateFlow
├── domain/
│   ├── DecisionEngine  # Weighted random selection with contextual scoring
│   ├── ShakeDetector   # Accelerometer-based shake detection
│   ├── MoodTracker     # Pattern analysis for mood inference
│   ├── SmartSuggestions # Pre-built option suggestions per category
│   └── NotificationScheduler # Local daily reminders
├── di/                 # Hilt dependency injection module
└── presentation/
    ├── auth/           # Google Sign-In (optional)
    ├── categories/     # Category & option management
    ├── components/     # Reusable UI (confetti, slot machine, mood selector, etc.)
    ├── history/        # Decision history + calendar view
    ├── home/           # Main decide screen
    ├── navigation/     # NavHost + bottom navigation
    ├── onboarding/     # 4-page guided setup
    ├── settings/       # Theme, haptics, export/import
    ├── sharing/        # Couple/group mode with QR
    ├── stats/          # Streaks, rankings, weekly recap
    ├── theme/          # Material 3 theming system
    └── widget/         # Glance home screen widget
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| DI | Hilt (KSP) |
| Storage | Local JSON file (Kotlin Serialization) |
| Async | Coroutines + StateFlow |
| Navigation | Compose Navigation |
| Animations | Compose Animation + Lottie |
| Widgets | Glance AppWidget |
| QR Codes | ZXing |
| Identity | Google Sign-In SDK (optional) |

## Constraints

- **Zero cost** — No paid services, no cloud backend, no Firebase, no API keys
- **Fully offline** — All data stored locally in a single JSON file
- **No internet required** for core functionality
- **All dependencies are free/open-source**

## Build

### Requirements
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 34

### Steps
```bash
git clone <this-repo>
cd DecideForMe
# Open in Android Studio, sync Gradle, run on device/emulator
```

Or from command line:
```bash
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

## Data Model

Everything lives in a single `decideforme_data.json` file:

```json
{
  "userProfile": { "displayName": "", "onboardingCompleted": true },
  "categories": [
    {
      "id": "meals",
      "name": "Meals",
      "options": [
        { "id": "...", "name": "Pasta", "weight": 1.2, "timesAccepted": 5, "tags": {...} }
      ]
    }
  ],
  "decisionHistory": [...],
  "streaks": { "currentStreak": 7, "longestStreak": 14, "totalDecisions": 42 },
  "settings": { "themeMode": "system", "colorPalette": "dynamic" }
}
```

## License

MIT License — see [LICENSE](LICENSE)
