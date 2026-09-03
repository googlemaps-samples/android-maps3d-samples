# 🏛️ Android Maps 3D Samples - Clean Architecture & Engineering Standards

This document establishes the mandatory architecture, separation of concerns (SoC), and testing standards across the **Android Maps 3D Samples** repository (`android-maps3d-samples`). All new features, showcase modules, and refactors must strictly adhere to these guidelines.

---

## 🎯 1. Architectural Philosophy & Scope

Features and modules in this repository follow strict **Clean Architecture** and **Unidirectional Data Flow (UDF)**:

1. **`Maps3DSamples` (Feature Demos):**
   Must maintain **100% 3-way language parity** across:
   - **Kotlin Views** (`Maps3DSamples/ApiDemos/kotlin-app`)
   - **Java Views** (`Maps3DSamples/ApiDemos/java-app`)
   - **Jetpack Compose** (`Maps3DSamples/ComposeDemos/app`)
   All business logic, math calculations, and state machines are encapsulated in the shared common module (`Maps3DSamples/ApiDemos/common`).

2. **Standalone Showcase Apps (e.g. `PlacesUIKit3D`):**
   Must decouple **Data (Repository)**, **Domain (Models & Math)**, **Presentation (ViewModel & State)**, and **UI (Compose & Views)**.

3. **Core Philosophy:**
   - **Zero Business Logic in Views:** Activities, Fragments, and Composables are thin rendering layers (< 250 lines).
   - **Immutable State:** Single source of truth exposed via immutable `StateFlow` (and `LiveData` for Java interop).
   - **Deterministic & Testable:** Pure JVM unit testability with zero Android framework dependencies in math and domain layers.

4. **Pragmatic Scope & Incremental Adoption:**
   - Apply this specification fully to all **new features** and **explicit refactors**.
   - When maintaining or modifying **existing legacy code** that does not yet adhere to all layers or test tiers, focus changes strictly on the requested task. Do not block progress or force an unrequested total rewrite. Apply the Boy Scout rule pragmatically (leave the touched code cleaner without destabilizing working code).

---

## 📦 2. Architectural Blueprints

### A. Shared Animation & Feature Demos (`Maps3DSamples`)
```
Maps3DSamples/
├── ApiDemos/
│   ├── common/                                      # 🟢 Shared Pure Domain & ViewModel Layer
│   │   ├── src/main/java/com/example/maps3d/common/<feature>/
│   │   │   ├── <Feature>Data.kt                     # Layer 1: Datasets, waypoints, constants, domain poses
│   │   │   ├── <Feature>Keyframe.kt                 # Layer 1: Sealed keyframe/step hierarchy & enums
│   │   │   ├── <Feature>Engine.kt                   # Layer 2: Pure math & trigonometry (zero Android UI deps)
│   │   │   ├── <Feature>State.kt                    # Layer 4: Immutable UI state & command representations
│   │   │   ├── <Feature>Controller.kt               # Layer 3: Pure Kotlin domain state machine
│   │   │   └── <Feature>ViewModel.kt                # Layer 4: MVVM ViewModel (StateFlow + LiveData)
│   │   └── src/test/java/com/example/maps3d/common/<feature>/
│   │       ├── <Feature>EngineTest.kt               # Tier 1 Test: Math & interpolation unit tests
│   │       ├── <Feature>ControllerTest.kt           # Tier 2 Test: Domain state machine unit tests
│   │       └── <Feature>ViewModelTest.kt            # Tier 3 Test: Presentation & intent dispatching unit tests
│   ├── kotlin-app/
│   │   └── src/main/java/com/example/maps3dkotlin/<feature>/
│   │       └── <Feature>Activity.kt                 # Layer 5: Thin View (<200 lines) collecting StateFlow
│   └── java-app/
│   │   └── src/main/java/com/example/maps3djava/<feature>/
│   │       └── <Feature>Activity.java               # Layer 5: Thin View (<200 lines) observing LiveData
└── ComposeDemos/
    └── app/src/main/java/com/example/composedemos/<feature>/
        └── <Feature>Activity.kt                     # Layer 5: Thin View (<200 lines) observing Compose state
```

### B. Data-Driven Showcase Apps (e.g. `PlacesUIKit3D`)
```
PlacesUIKit3D/
└── src/
    ├── main/java/com/example/placesuikit3d/
    │   ├── data/
    │   │   ├── model/                               # Layer 1: Immutable Domain Models (e.g. PlaceSearchResult)
    │   │   └── repository/                          # Layer 1: Repository Interface & Impl (PlacesRepository)
    │   ├── ui/
    │   │   ├── animation/                           # Layer 2: Math & Camera kinematic helpers (Camera3DAnimator)
    │   │   ├── compose/                             # Layer 5: Reusable Stateless Composables & Widgets
    │   │   ├── viewmodel/                           # Layer 4: MVVM ViewModel & Screen State (StateFlow)
    │   │   └── MainActivity.kt                      # Layer 5: Thin View (<250 lines) wiring UI, Map3D & Fragments
    └── test/java/com/example/placesuikit3d/
        ├── data/repository/                         # Tier 2 Test: Repository unit tests (with TestDispatcher)
        ├── ui/animation/                            # Tier 1 Test: Camera math & framing calculation tests
        └── ui/viewmodel/                            # Tier 3 Test: ViewModel state flow & intent tests
```

---

## 🧱 3. Layer Responsibilities & Standards

### Layer 1: Domain Models & Data Layer (`*Data.kt`, `*Repository.kt`)
- **Immutable Domain Entities:** Lightweight, parcelable (if needed) data classes decoupled from SDK or network DTOs.
- **Repository Pattern:** External SDKs (Places SDK, Geocoding, Network) must be accessed exclusively through an interface:
  ```kotlin
  interface PlacesRepository {
      suspend fun searchPlaces(query: String): Result<List<PlaceSearchResult>>
      suspend fun fetchPlaceDetails(placeId: String): Result<PlaceDetailsSummary>
  }
  ```
- **Dispatcher Injection:** Repositories must accept an injectable `CoroutineDispatcher` defaulting to `Dispatchers.IO`:
  ```kotlin
  class PlacesRepositoryImpl(
      private val placesClient: PlacesClient,
      private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
  ) : PlacesRepository
  ```

### Layer 2: Pure Domain Math & Kinematics Engine (`*Engine.kt`, `*Animator.kt`)
- **Rule:** Contains pure, deterministic mathematical functions (e.g., angle normalization, spherical interpolation, heading predictions, dynamic altitude calculation, coordinate bounds framing).
- **Dependencies:** **Zero Android UI or Lifecycle dependencies.** Must run on pure JVM.

### Layer 3: Pure State Machine & Domain Controllers (`*Controller.kt`)
- **Rule:** Orchestrates time integration, progress ticks (`onFrameTick`), step sequencing, and playback transitions (`play`, `pause`, `reset`).
- **Dependencies:** Zero Android UI / View / Context dependencies.

### Layer 4: Presentation ViewModel Layer (`*ViewModel.kt`, `*State.kt`)
- **Rule:** Subclass of `androidx.lifecycle.ViewModel`. Exposes:
  - `val state: StateFlow<T>` for Kotlin Views and Jetpack Compose.
  - `val liveData: LiveData<T>` for Java Views interoperability.
- **Unidirectional Data Flow (UDF):** Views emit user intents $\rightarrow$ ViewModel processes via Repository/Controller $\rightarrow$ Emits immutable `State` $\rightarrow$ Views render.
- **Dispatcher Injection:** ViewModels performing coroutines must accept an injectable `CoroutineDispatcher` (defaulting to `Dispatchers.Main` or `Dispatchers.IO`) for deterministic testing.
- **Resilience Rule:** LiveData updates must safely support both JVM unit testing and Android Main thread:
  ```kotlin
  private fun updateState(newState: FeatureState) {
      _state.value = newState
      try {
          _liveData.value = newState
      } catch (_: Exception) {
          _liveData.postValue(newState)
      }
  }
  ```

### Layer 5: Thin View Layer (Activities, Fragments & Composables)
- **Rule:** Must be **under 250 lines**. Views are strictly responsible for:
  1. Inflating/rendering UI widgets.
  2. Forwarding user click/drag events to the ViewModel.
  3. Applying camera, marker, and model updates to `GoogleMap3D`.
  4. Driving hardware-synced frame ticks via `Choreographer.FrameCallback` (Views) or `withFrameNanos` (Compose).
- **Cross-Module Smart Cast Rule:** When reading nullable `val` state properties across module boundaries in Kotlin, always assign to a local `val`:
  ```kotlin
  val flyToCommand = state.flyToCommand
  if (flyToCommand != null) {
      executeNativeFlyTo(flyToCommand)
  }
  ```

---

## 🗺️ 4. Google Maps 3D SDK Standards

### 1. Explicit Altitude Modes
When rendering markers, polylines, or polygons in 3D:
- Never assume ground is at altitude `0.0`. Photorealistic 3D terrain and city meshes vary significantly.
- Always be explicit with `AltitudeMode`:
  - `AltitudeMode.CLAMP_TO_GROUND`: Standard for surface markers, footprints, and road overlays.
  - `AltitudeMode.RELATIVE_TO_GROUND`: For objects elevated above terrain (e.g. drones, rooftops).
  - `AltitudeMode.ABSOLUTE`: Only when using verified ellipsoid/sea-level orthometric altitudes.
- For elevated 3D buildings, dynamic altitude calculation or known ground elevation resolution must be applied to prevent markers from clipping inside building geometries.

### 2. Consumable Camera Commands
- Model camera animations (e.g., fly-to, auto-frame, orbit) as consumable one-time commands in the UI state:
  ```kotlin
  data class ScreenState(
      val flyToCommand: Camera3DTarget? = null,
      val selectedPlaceId: String? = null,
  )
  ```
- The View executes the camera movement on the map and immediately dispatches `viewModel.onFlyToCompleted()` to reset `flyToCommand = null`. This prevents repeating camera transitions across configuration changes, screen rotations, or recompositions.

### 3. Lifecycle Forwarding
Every Activity, Fragment, or Composable hosting a `Map3DElement` or `Map3DView` must strictly forward Android lifecycle callbacks:
- `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onDestroy`, and `onLowMemory`.

---

## 🎨 5. Jetpack Compose & State Hoisting Standards

1. **Lifecycle-Aware Collection:**
   Always use `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose` (never `collectAsState()`) to halt flow collection when the Composable is invisible or backgrounded.
2. **Stateless Composables & Hoisting:**
   Composables must accept plain state objects and lambda callbacks (e.g., `onSelectPlace: (PlaceSearchResult) -> Unit`). Never pass the `ViewModel` instance down through child composables.
3. **Interop with Views and Fragments:**
   When hosting legacy views or third-party SDK fragments (e.g., `PlaceDetailsCompactFragment` from Places UI Kit):
   - Use `AndroidViewBinding` or `FragmentContainerView` inside Compose.
   - Keep Fragment transaction and argument binding logic isolated in the View layer.

---

## 🧪 6. Mandatory 4-Tier Testing Suite

Every feature must implement complete testing coverage across all four tiers:

| Tier | Test Type | Target File | Mandated Verification |
| :--- | :--- | :--- | :--- |
| **Tier 1** | **Engine & Math Tests** | `<Feature>EngineTest.kt`, `Camera3DAnimatorTest.kt` | Angle normalization (360°/0° boundary), spherical interpolation, coordinate bounds framing, dynamic altitude math. |
| **Tier 2** | **Controller & Repository Tests** | `<Feature>ControllerTest.kt`, `<Feature>RepositoryTest.kt` | State transitions, step sequences, dwell timers, playback controls, network/SDK failure mapping, test dispatcher isolation. |
| **Tier 3** | **ViewModel Unit Tests** | `<Feature>ViewModelTest.kt` | `StateFlow` and `LiveData` emissions, UI intent dispatches (`selectPlace`, `flyToCommand`, `clearSearch`, `onFrameTick`), coroutine flow debouncing. |
| **Tier 4** | **Visual Regression Tests** | `<Feature>VisualTest.kt` & `.java` | Automated UI Automator test capturing live 3D map scene screenshots and validating visual correctness via Gemini multimodal API. |

---

## 🚫 7. Anti-Patterns & Prohibitions

1. ❌ **No Monolithic Activities or Composables:** Never place calculations, timer handlers, repository calls, or state arrays inside `Activity` or Composable files. Keep views under 250 lines.
2. ❌ **No UI Logic in Controllers, Engines, or Repositories:** Never import `android.view.*`, `android.widget.*`, or `android.content.Context` into Engine, Controller, or Repository classes.
3. ❌ **No Direct Mutability:** Never expose `MutableStateFlow` or `MutableLiveData` directly from ViewModels.
4. ❌ **No Hardcoded Coroutine Dispatchers:** Never use `Dispatchers.IO` or `Dispatchers.Default` directly inside ViewModel/Repository bodies without an injectable constructor parameter.
5. ❌ **No SDK DTO Leakage:** Never expose raw Places SDK or Maps SDK internal models directly in UI states; map them to domain models first.
6. ❌ **No Unconsumed Camera Commands:** Never leave camera fly-to triggers as persistent boolean flags that re-trigger on recomposition.
7. ❌ **No Untested ViewModels or Repositories:** Every ViewModel and Repository must have unit test coverage validating state emissions and error handling.

