# GeoGuessr Android

A GeoGuessr-style multiplayer mobile game for Android built with Kotlin, Firebase, and Google Maps.

## Features

- 🌍 Random Street View locations to guess
- 📍 Interactive map for placing your guess
- ⏱️ Timed rounds (60 seconds per round)
- 👥 Real-time multiplayer with Firebase
- 🏠 Create/join game rooms with a 6-character code
- 🏆 5 rounds per game with a final scoreboard and winner announcement
- 🔐 Firebase Authentication (email/password)
- 👤 Player nicknames and avatars

## Architecture

The project follows **MVVM** (Model-View-ViewModel) architecture:

```
app/src/main/
├── java/com/geoguessr/android/
│   ├── data/            # Firebase repository implementations
│   ├── domain/          # Models, repository interfaces, use cases
│   ├── presentation/    # ViewModels and UI fragments
│   └── di/              # Hilt dependency injection modules
└── res/                 # Layouts, strings, themes
```

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- Google Maps API key
- Firebase project

### Steps

1. **Clone the repository**
2. **Create a Firebase project** at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Authentication (Email/Password)
   - Enable Realtime Database
   - Download `google-services.json` and replace `app/google-services.json`
3. **Get a Google Maps API key** at [console.cloud.google.com](https://console.cloud.google.com)
   - Enable Maps SDK for Android
   - Replace `YOUR_GOOGLE_MAPS_API_KEY` in `app/src/main/res/values/strings.xml`
4. **Build and run** on an Android device or emulator (API 26+)

## Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Primary language |
| Firebase Auth | User authentication |
| Firebase Realtime Database | Multiplayer game state |
| Google Maps SDK | Map guessing interface |
| Hilt | Dependency injection |
| Navigation Component | Fragment navigation |
| Material Design 3 | UI theme |
| Coroutines + Flow | Async operations |
| Retrofit + OkHttp | HTTP client |

## Firebase Database Structure

```json
{
  "users": {
    "<uid>": { "uid", "email", "nickname", "avatar" }
  },
  "rooms": {
    "<roomId>": {
      "roomId", "code", "ownerId", "status",
      "totalRounds", "currentRound",
      "players": { "<uid>": { "userId", "nickname", "roundScores", "totalScore" } },
      "rounds": [ { "roundNumber", "location", "timeLimit", "selectedLocations" } ]
    }
  }
}
```