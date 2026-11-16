# Field Incident Reporter

A native Android application for field technicians to report, track, and manage physical incidents. The backend is a local Node.js mock REST API intended for local development and evaluation only.

---

## Features

- **Incident list** — Tabbed home screen: Active (OPEN, IN_PROGRESS), Completed, and Cancelled
- **Create incident** — Submit a new incident with title, description, category, location, and photos
- **Incident details** — View full incident information and available status actions
- **Edit incident** — Update editable fields and manage attached photos
- **Status lifecycle** — Transition incidents through OPEN → IN_PROGRESS → COMPLETED; cancel from OPEN or IN_PROGRESS
- **Photo management** — Capture or select up to 5 photos per incident; preview and delete individual photos
- **Confirmation dialogs** — Required before cancellation and permanent deletion
- **Error and empty states** — Loading, empty, error, and retry states on all list and detail screens

---

## Tech Stack

### Android

| Component | Library / Version |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01) |
| Architecture pattern | MVVM + Clean Architecture |
| Async | Kotlin Coroutines & Flow 1.11.0 |
| Dependency injection | Hilt 2.60.1 |
| Networking | Retrofit 3.0.0 + OkHttp 4.12.0 |
| Image loading | Coil 2.7.0 |
| Navigation | Navigation Compose 2.10.2 |
| HTTP inspector (debug only) | Chucker 4.3.1 |

### Mock API

| Component | Technology |
|---|---|
| Runtime | Node.js >= 18 |
| Framework | Express 4.x |
| Persistence | JSON Server 0.17.4 (file-backed via `db.json`) |
| File upload | Multer 1.4.5-lts.2 |

---

## Architecture Overview

The Android app follows MVVM with Clean Architecture principles, organised into four logical layers:

```
Presentation  ──►  Domain  ◄──  Data
                                 │
                              Core (network infrastructure)
```

- **Presentation** — Jetpack Compose screens and ViewModels. Consumes use cases via Hilt injection. Exposes `StateFlow` for UI state and `SharedFlow`/`Channel` for one-time events.
- **Domain** — Business models, repository interfaces, and use cases. No Android or Retrofit dependencies.
- **Data** — Retrofit-backed repository implementations, DTOs, and mappers. Fulfils Domain repository contracts.
- **Core** — Shared network infrastructure: `NetworkClient`, `ApiResponseHandler`, `ApiException`.

---

## Project Structure

```
field-incident-reporter/
├── android/                        # Native Android application
│   └── app/src/main/java/com/example/incidentreporter/
│       ├── data/
│       │   ├── remote/
│       │   │   ├── api/            # Retrofit service interfaces
│       │   │   ├── dto/            # Request and response DTOs
│       │   │   └── mapper/         # DTO → domain model mappers
│       │   └── repository/         # Repository implementations
│       ├── di/                     # Hilt modules
│       ├── domain/
│       │   ├── model/              # Domain models
│       │   ├── repository/         # Repository interfaces
│       │   └── usecase/            # Use cases
│       ├── network/                # Core: NetworkClient, ApiResponseHandler
│       └── presentation/
│           ├── createincident/
│           ├── editincident/
│           ├── incidentdetails/
│           ├── incidentlist/
│           ├── navigation/
│           └── theme/
├── mock-api/                       # Local Node.js mock REST API
│   ├── server.js
│   └── src/
│       ├── incidentRoutes.js
│       ├── photoRoutes.js
│       └── ...
├── .cursor/
│   └── rules/                      # Tracked project development rules
└── README.md
```

---

## Prerequisites

### Mock API

- Node.js 18 or later
- npm (bundled with Node.js)

### Android

- Android Studio Panda 4 (2025.3.4) or later
- **JDK 21** — required by the Gradle daemon (`gradle/gradle-daemon-jvm.properties` pins `toolchainVersion=21`). The project includes foojay toolchain resolver support (`settings.gradle.kts`), which can provision JDK 21 automatically via the Foojay Disco API if it is not already installed on the machine. Ensure network access is available on first build if JDK 21 is not yet present.
- Android SDK with API level 37 (compileSdk) and 24 (minSdk) installed via SDK Manager

> The Gradle wrapper downloads Gradle 9.4.1 automatically on first build.

---

## Backend / Mock API Setup

```bash
cd mock-api
npm install
npm start
```

The server starts at `http://localhost:3000`.

For development with auto-reload:

```bash
npm run dev
```

Incident data is stored in `mock-api/db.json` and persists across server restarts. Uploaded photo files are stored in `mock-api/uploads/` (gitignored) and also persist across restarts.

Refer to [`mock-api/README.md`](mock-api/README.md) for the full API reference, endpoint listing, response envelopes, and photo upload details.

---

## Android App Setup

1. Open the `android/` directory in Android Studio as a Gradle project (not the repository root).
2. Allow Gradle to sync and download dependencies.
3. Start the mock API (see above) before running the app.
4. Build and install the app:

```bash
cd android
./gradlew assembleDebug
```

Install the generated APK from `app/build/outputs/apk/debug/`, or run directly from Android Studio via the **Run** button.

---

## API Base URL Configuration

The Android app connects to the mock API via `BuildConfig.BASE_URL`, which is set at build time via the `API_BASE_URL` Gradle property (`app/build.gradle.kts`).

### Android Emulator (default — no configuration required)

The default URL is `http://10.0.2.2:3000/`. The address `10.0.2.2` is the emulator's alias for the host machine's localhost, so no additional configuration is needed.

### Physical Device

The device must be on the same local network as the machine running the mock API.

**Step 1 — Find the host machine's LAN IP address.**

**Step 2 — Build the app with the IP address as a Gradle property:**

```bash
cd android
./gradlew assembleDebug -PAPI_BASE_URL=http://YOUR_HOST_IP:3000/
```

**Step 3 — Set `PUBLIC_BASE_URL` when starting the mock API.**

The mock API builds photo URLs from the incoming request host. When the Android emulator calls the server at `http://10.0.2.2:3000`, the server uses that address in photo URLs, which are then reachable from the emulator. When a physical device calls the server at `http://<HOST_IP>:3000`, the server similarly uses that address. If the two do not match (for example, the server is behind a proxy or the detected host differs from the device-reachable address), set `PUBLIC_BASE_URL` explicitly:

```bash
PUBLIC_BASE_URL=http://YOUR_HOST_IP:3000 npm start
```

Do not commit LAN IP addresses to source control.

---

## Unit Test Execution

Run all JVM unit tests from the `android/` directory:

```bash
cd android
./gradlew test
```

### Test scope

| Layer | Test classes |
|---|---|
| Data — mapper | `IncidentMapperTest` |
| Data — repository | `IncidentRepositoryImplTest` |
| Core — network | `ApiResponseHandlerTest` |
| Domain — use cases | `CancelIncidentUseCaseTest`, `GetIncidentByIdUseCaseTest` |
| Presentation — ViewModels | `CreateIncidentViewModelTest`, `EditIncidentViewModelTest`, `IncidentDetailsViewModelTest`, `ActiveIncidentViewModelTest`, `CancelledIncidentViewModelTest`, `CompletedIncidentViewModelTest` |
| Logic | `IncidentStatusTransitionsTest`, `IncidentDetailsTimelineTest`, `IncidentErrorMappingTest` |

Tests use JUnit 4, MockK, and `kotlinx-coroutines-test`. Fakes (`FakeIncidentRepository`, `FakeIncidentApiService`, `FakePhotoApiService`) replace real network calls.

Instrumented (on-device) UI tests are not included in this project.

---

## Sample Workflow

1. Start the mock API: `cd mock-api && npm start`
2. Launch the app on an emulator or configured physical device.
3. The home screen opens on the **Active** tab (OPEN and IN_PROGRESS incidents).
4. Tap **+** to create a new incident. Fill in the required fields and optionally attach photos. Submit.
5. On success, the app navigates to the new incident's details screen.
6. From the details screen, advance the status (OPEN → IN_PROGRESS → COMPLETED) or cancel the incident.
7. Completed incidents appear on the **Completed** tab; cancelled incidents appear on the **Cancelled** tab.
8. Swipe an incident card on the Active tab to delete it permanently (confirmation required).

---

## Known Limitations

- **Local mock API only.** There is no remote or production backend. All incident data is stored in `mock-api/db.json` and is reset if that file is deleted or replaced.
- **No authentication.** The API and app have no user identity or access control.
- **No offline support.** The app requires the mock API to be running and reachable. Network errors surface as error states with a retry action.
- **No local database.** Incidents are not cached on the device; every screen load fetches from the API.
- **Uploaded photos are not cleaned up automatically.** Files uploaded but never attached to an incident (for example after a failed create) remain in `mock-api/uploads/` and must be deleted manually.
- **Photo URLs are host-relative.** The mock API constructs photo URLs from the request host. Using an emulator and a physical device interchangeably against the same server instance may produce unreachable image URLs unless `PUBLIC_BASE_URL` is set explicitly on the server.
- **Single module.** The Android project is a single-module app. Multi-module separation is not implemented.
- **No CI/CD pipeline.** There is no automated build or test pipeline configured in this repository.
