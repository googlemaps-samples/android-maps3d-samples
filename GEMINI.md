# Android Maps 3D Samples - AI Pair Programming Guidelines & Architecture Rules

You are working on the **Google Maps 3D Android Samples** repository (`android-maps3d-samples`). Apply the architecture and engineering standards defined below to guide code generation, feature development, and refactoring.

---

## ⚖️ Pragmatic Execution & Incremental Adoption (Non-Blocking Rules)

This repository contains both production-grade showcase architectures (like `PlacesUIKit3D` and new 3-way parity samples) and pre-existing legacy demos or minimal API snippets. To maintain real-world development velocity:

1. **New Features & Explicit Refactors:**
   - Follow the full Clean Architecture, MVVM, UDF, and testing standards defined below.
2. **Maintenance, Bug Fixes & Iterations on Existing Code:**
   - **Never block user requests:** If an existing feature or activity does not yet follow all layers or test tiers, do NOT refuse the task, demand a massive rewrite, or introduce unnecessary overhead.
   - **Targeted Scope:** Focus directly and cleanly on what the user requested to change or fix.
   - **Pragmatic Boy Scout Rule:** Leave touched lines cleaner (e.g., spotless formatting, no wildcards, no new anti-patterns) without disrupting working implementations or touching unrelated legacy code.
3. **Minimal API Demos vs Showcase Apps:**
   - Standalone API snippets designed to demonstrate single SDK calls do not require multi-module decomposition unless explicitly requested.

---

## 🏛️ Architecture & Separation of Concerns (SoC)

Refer to [ARCHITECTURE.md](ARCHITECTURE.md) for full architectural specifications.

### 1. Architectural Scopes
- **`Maps3DSamples` (Feature Demos):** Maintain **100% 3-way language parity** across:
  - **Kotlin Views** (`Maps3DSamples/ApiDemos/kotlin-app`)
  - **Java Views** (`Maps3DSamples/ApiDemos/java-app`)
  - **Jetpack Compose** (`Maps3DSamples/ComposeDemos/app`)
- **Standalone Showcase Modules (e.g., `PlacesUIKit3D`):** Decouple into Repository (Data), ViewModel (Presentation/UDF), Stateless Composables (UI), and Math/Kinematics helpers.

### 2. Mandatory Layering for Architecture Features
For Clean Architecture and MVVM modules, decouple business logic, math calculations, and repository fetching from Activity or Composable files:
1. **Domain Data & Models (`*Data.kt`, `*Model.kt`):** Immutable datasets, waypoints, constants, decoupled from raw SDK/network DTOs.
2. **Repositories (`*Repository.kt`):** Abstract external SDK and data access behind interfaces with injectable `CoroutineDispatcher`.
3. **Pure Math Engine (`*Engine.kt`, `*Animator.kt`):** Pure deterministic trigonometric, altitude, and kinematic functions (Zero Android UI / View / Context dependencies).
4. **Pure State Machine Controller (`*Controller.kt`):** Pure Kotlin state machine managing progress, time integration, and step transitions.
5. **Presentation ViewModel (`*ViewModel.kt`, `*State.kt`):** Android ViewModel exposing immutable UI state via `StateFlow` (for Kotlin/Compose) and `LiveData` (for Java).
6. **Thin View Layer (`*Activity.kt`, `*Activity.java`, Composable Screen):** Thin views (< 250 lines) that only observe ViewModel state, forward user clicks, and apply map rendering updates.

---

## 🧪 Testing Suite Standards

Whenever creating new features or undertaking architectural refactors, maintain all applicable tiers of tests. For bug fixes or minor updates on existing code, focus tests on the touched area without requiring retroactive test suites:

1. **Tier 1: Engine & Math Unit Tests (`*EngineTest.kt`, `*AnimatorTest.kt`):** Tests angle normalization (360°/0° wrap-around), spherical math, coordinate bounds framing, and dynamic altitude calculation.
2. **Tier 2: Controller & Repository Unit Tests (`*ControllerTest.kt`, `*RepositoryTest.kt`):** Tests state transitions, step sequencing, dwell delays, play/pause toggles, arrival conditions, and repository data mapping with test dispatchers.
3. **Tier 3: ViewModel Unit Tests (`*ViewModelTest.kt`):** Tests `StateFlow` & `LiveData` emissions, UI intent dispatches (`selectPlace`, `flyToCommand`, `onFlyToCompleted`, `onFrameTick`), and debounced flows using `runTest` and `StandardTestDispatcher`.
4. **Tier 4: Visual Regression Tests (`*VisualTest.kt` & `*VisualTest.java`):** Automates UI Automator tests capturing live 3D map scene screenshots and validating visual correctness using the Gemini vision multimodal API.

---

## ⚠️ Language, Concurrency & Maps 3D Implementation Constraints

- **Kotlin Cross-Module Smart Casts:** When referencing nullable properties of state classes defined in `common` or external models, always capture to a local `val` first:
  ```kotlin
  val flyToCommand = state.flyToCommand
  if (flyToCommand != null) {
      executeNativeFlyTo(flyToCommand)
  }
  ```
- **Injectable Coroutine Dispatchers:** Never hardcode `Dispatchers.IO` or `Dispatchers.Default`. Always provide an injectable constructor parameter defaulting to `Dispatchers.IO`:
  ```kotlin
  class PlacesRepositoryImpl(
      private val placesClient: PlacesClient,
      private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
  ) : PlacesRepository
  ```
- **Consumable Camera Commands:** One-time camera animations (e.g. fly-to, auto-frame) must be consumed by the View and cleared immediately in the ViewModel via `onFlyToCompleted()` to prevent animation loops on recomposition.
- **Explicit Altitude Modes:** Always specify `AltitudeMode` (`CLAMP_TO_GROUND`, `RELATIVE_TO_GROUND`, or `ABSOLUTE`). Never assume ground altitude is `0.0` over 3D terrain meshes.
- **Hardware-Synchronized Tickers:** Use `Choreographer.FrameCallback` in Views and `withFrameNanos` / `withFrameMillis` in Compose for smooth 60/120fps animations.
- **Resilient LiveData Updates in ViewModel:**
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

---

## 🧹 Code Generation Hygiene & Linter Standards

When generating, refactoring, or editing code, strictly adhere to these rules:

1. **Zero Wildcard Imports:**
   - ❌ `import com.google.android.gms.maps.model.*` is **strictly forbidden**.
   - ✅ Every imported symbol must be explicitly listed to conform to `ktlint` and `spotless`.
2. **Spotless Compliance:**
   - Always verify and run `./gradlew spotlessApply` on touched modules before finishing a task.
3. **Modern Kotlin Idioms & Pragmatic Warning Hygiene:**
   - Prefer modern standard library idioms where applicable (e.g., `kotlin.time.Duration` with `delay(100.milliseconds)` instead of legacy millisecond `Long`s).
   - Prefer Android KTX extensions (e.g., `hexColorString.toColorInt()`) over legacy utility methods.
   - Keep KDoc references clean by importing or fully qualifying symbols in brackets (e.g., `[PlaceSearch3DScreenState]`).
   - For unavoidable warnings (e.g., experimental Compose APIs or external SDK deprecations), use targeted `@OptIn(...)` or `@Suppress(...)` with an explanatory comment rather than forcing brittle workarounds.
4. **Resource & Design Token Discipline:**
   - Externalize user-facing strings to `res/values/strings.xml`.
   - Prefer theme tokens (`MaterialTheme.colorScheme.*`) over hardcoded hex values. Document any canonical brand colors (e.g. `#F4B400` Google yellow) with an explanatory comment.
   - Compose state collections must use `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose`.

