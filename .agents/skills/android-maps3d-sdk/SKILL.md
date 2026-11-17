---
name: android-maps3d-sdk
description: >-
  You MUST load this skill whenever the user asks to create, style, or modify
  any map, marker, custom marker style (PinConfiguration, Glyph, MarkerView),
  pin, popover, camera position/animation (flyCameraTo, flyCameraAround, Camera,
  UiControlSettings), click listener (setMap3DClickListener, Map3DClickEvent),
  3D glTF/GLB model (addModel), polygon, or polyline—even if the prompt does NOT
  mention "3D" or "Google Maps"—as well as whenever
  com.google.android.gms:play-services-maps3d, GoogleMap3D, Map3DView, or
  Map3DInitConfig is used. Only skip this skill if the user or build.gradle.kts
  explicitly requests 2D play-services-maps or maps-compose.
license: Apache-2.0
metadata:
  author: googlemaps-samples
  version: "2.0.0"
  maven_artifact: "com.google.android.gms:play-services-maps3d:16.0.1"
  secrets_plugin: "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:2.0.1"
  min_sdk: "24"
  min_kotlin: "2.2.0"
  supported_compile_sdk: "35-36"
---

# Android Maps 3D SDK — Deterministic Workflow (`play-services-maps3d:16.0.1`)

## Step 1: Verify `minSdk >= 24`, `compileSdk`, Kotlin `>= 2.2.0`, & Manifest API Key

1. **Inspect `gradle/libs.versions.toml` and module `build.gradle.kts` / `build.gradle`**:
   - **Enforce `minSdk >= 24` Safely**: `play-services-maps3d:16.0.1` requires `minSdk >= 24` (declared in the AAR manifest). If `minSdk < 24`:
     - On a **brand-new starter project** (*Empty Activity*): Update `minSdk = 24` and inform the user.
     - On an **existing app** with custom screens/users: **Ask user confirmation first** before raising `minSdk` to `24`.
   - **Respect Pinned `compileSdk`, Kotlin `>= 2.2.0`, & Java-Only Skip**: Ensure `compileSdk` is `35` or `36` (never downgrade `36` to `35`). For Kotlin projects, verify Kotlin is `>= 2.2.0` (`play-services-maps3d:16.0.1` pulls `kotlin-stdlib:2.3.0`, requiring KGP `2.2.0+` under Kotlin's `N+1` metadata rule, or an explicit `kotlin-stdlib` constraint on older KGP). On **Java-only** projects (no Kotlin plugin), skip `min_kotlin`.
   - **Verify Maven Artifact (Never Downgrade) & Secrets Plugin**:
     - **New project (not yet installed):** Add `com.google.android.gms:play-services-maps3d:16.0.1`.
     - **Existing project on `< 16.0.1`** (`0.1.x`, `0.2.x`, or broken `16.0.0` which crashes JDK 21 `javac` and Hilt/KAPT on `Map3DControl.class`): Upgrade to `16.0.1`.
     - **Existing project already on `> 16.0.1`:** **Keep the project's newer version (NEVER downgrade)** and notify the user in chat that they can manually update this skill (`npx skills update` or `git pull`) — never fetch remote files or execute update commands automatically.
     - NEVER use 2D `play-services-maps`, NEVER use dynamic Gradle versions (`16.+`), and NEVER add a non-existent Maven `com.google.maps.android:maps3d-compose` artifact (only use `project(":maps3d-compose")` if `settings.gradle.kts` includes it). Configure `com.google.android.libraries.mapsplatform.secrets-gradle-plugin` with `secrets.properties` and `local.defaults.properties` at the **project root**.
   - **Verify `AndroidManifest.xml` `<meta-data>` Key**: Use `com.google.android.geo.maps3d.API_KEY` (NEVER 2D Maps' `com.google.android.geo.API_KEY`, and never hardcode `AIza...` keys):
     ```xml
     <meta-data
         android:name="com.google.android.geo.maps3d.API_KEY"
         android:value="${MAPS3D_API_KEY}" />
     ```
2. **Credentials Setup & Demo Key Quickstart** (if `secrets.properties` is missing or still uses a placeholder key — never hardcode `AIza...` keys in source or `AndroidManifest.xml`):
   - **Quick Prototyping (Maps Demo Key — No Billing Card):** Guide the user to open `https://mapsplatform.google.com/maps-demo-key` in their browser, sign in with a personal Google account (Workspace accounts are not eligible), accept the Maps Demo Project Terms, generate the key, and paste it into project-root `secrets.properties` as `MAPS3D_API_KEY=<key>`. *(Expectation check: Demo Keys have daily quotas and limited API coverage; if the app hits an API restriction/auth error on Maps 3D SDK for Android, switch to a standard Cloud project key below.)*
   - **Production & Full Maps 3D Android Support (Standard Google Cloud API Key):**
     1. Open Google Cloud Console (`https://console.cloud.google.com/`), select or create a billing-enabled project, and enable **Maps 3D SDK for Android** under **APIs & Services > Library** (plus **Places API (New)** if using default POI popovers or `PlacesUIKit3D`).
     2. Create an API key under **APIs & Services > Credentials** and restrict it to **Android apps** (package name + SHA-1 fingerprint) and **Maps 3D SDK for Android**.
     3. Save `MAPS3D_API_KEY=<key>` in project-root `secrets.properties` (backed by `secrets-gradle-plugin` + `local.defaults.properties`) and confirm `secrets.properties` is in `.gitignore`.
3. **Replit / Headless Container Check**:
   - If `$REPL_ID` is set or running in a low-RAM headless container, read [references/replit-headless-gradle.md](references/replit-headless-gradle.md) before running `./gradlew`.

## Step 2: 2D-to-3D Function Translation & Selective Reference Router

**NEVER** load all files in `references/` upfront. Load **only** the 1–2 reference file(s) required for the active task:

| Developer Intent (Day 1 / Day 2 / Day 3) | ❌ 2D Maps API (DO NOT USE) | ✅ Maps 3D SDK `16.0.1` (`com.google.android.gms.maps3d.*`) | Reference to Load |
| :--- | :--- | :--- | :--- |
| **Gradle Dependency** | `play-services-maps` / `maps-compose` | `com.google.android.gms:play-services-maps3d:16.0.1` | `references/01a-setup-compose.md` or `01b-setup-xml-views.md` |
| **Manifest API Key** | `com.google.android.geo.API_KEY` | `com.google.android.geo.maps3d.API_KEY` (`${MAPS3D_API_KEY}`) | `references/01a-setup-compose.md` or `01b-setup-xml-views.md` |
| **Map View (Jetpack Compose)** | `GoogleMap()` (Compose 2D) | `AndroidView` (`Modifier.fillMaxSize()`) + `Map3DView` + `Map3DInitConfig.create(...)` (or in-repo `:maps3d-compose`) | `references/01a-setup-compose.md` |
| **Map View (XML Views)** | `SupportMapFragment`, `MapView` | `<com.google.android.gms.maps3d.Map3DView>` + `OnMap3DViewReadyCallback` + `TrackedMap3D` | `references/01b-setup-xml-views.md` |
| **Map Mode / Theme / Locale / UI** | `googleMap.mapType`, `uiSettings` | `setMapMode(Map3DMode.*)`, `setMapColorScheme(...)`, `setLocale(LocaleOptions)`, `getUiControlSettings()` | `references/01a-setup-compose.md` or `05-scenarios-and-utilities.md` |
| **3D Coordinates** | `LatLng(lat, lng)` | `latLngAltitude { latitude = ...; longitude = ...; altitude = ... }` | Any loaded reference |
| **Instant Camera Pose** | `moveCamera(CameraUpdateFactory)` | `googleMap3D.setCamera(camera { center = ...; heading = ...; tilt = ...; range = ... })` | `references/02-camera-flyto-animations.md` |
| **Smooth Fly-To / Orbit Tour** | `animateCamera(...)` | `flyCameraTo(flyToOptions { ... })`, `flyCameraAround(flyAroundOptions { ... })`, `stopCameraAnimation()` | `references/02-camera-flyto-animations.md` |
| **Camera Bounds & Projection** | `setLatLngBoundsForCameraTarget()` | `setCameraRestriction(cameraRestriction { ... })`, `setOnMapSteadyListener`, `Projection3D` | `references/02-camera-flyto-animations.md` |
| **Map & POI Click Listener** | `setOnMapClickListener { latLng -> }` | `setMap3DClickListener { event: Map3DClickEvent -> false }` (`event.location`, `event.placeId` on Main UI thread) | `references/03-markers-models-popups.md` |
| **3D Marker / Pin / Custom View** | `addMarker(MarkerOptions().title())` | `addMarker(markerOptions { ...; style = pinConfiguration { ... } })`, `addMarkerView(MarkerViewOptions().apply { ... })` | `references/03-markers-models-popups.md` |
| **3D `.glb` Model & Popover** | `GroundOverlay`, `InfoWindowAdapter` | `addModel(modelOptions { url = ...; position = ...; scale = ... })`, `@UiThread addPopover(popoverOptions { ... })` | `references/03-markers-models-popups.md` |
| **3D Polyline / Extruded Polygon** | `addPolyline()`, `addPolygon()` | `addPolyline(polylineOptions { path = ... })`, `addPolygon(polygonOptions { extruded = true; innerPaths = ... })` | `references/04-polygons-and-polylines.md` |
| **Tours, Path Following, Places 3D** | Custom 2D loop / `LatLngBounds` | `Scenario`, `Units`/`Utilities`, `PathEngine`, `RoadTerrainAnalyzer`, `TourPlaybackController`, `PlacesUIKit3D` | `references/05-scenarios-and-utilities.md` |
| **Java Project (`*.java`)** | `new MarkerOptions().position(...)` | Constructors + setters (`new MarkerOptions()`, `new Camera(...)`, `Map3DInitConfig.create(...)`, `PinConfiguration.builder()`) | `references/java-interop-builders.md` |

- **Fallback When a Capability Is Not in Local `references/`** (strictly local / already-installed tools — never fetch unverified remote Markdown or prompt the user to install an MCP server mid-task):
  1. If `search_documents` (Developer Knowledge MCP) or `android docs` is already present in active tools, query it for the `com.google.android.gms.maps3d.*` class.
  2. Otherwise inspect the downloaded `play-services-maps3d` AAR/JAR classes and signatures directly in the local Gradle cache/classpath.

## Step 3: Critical API & Runtime Guardrails (Override Stale Training Data)

1. **Shared `LatLngAltitude`, Meter Units, & Nullable Returns**:
   - `Camera.range` and `LatLngAltitude.altitude` are in **meters** (never 2D zoom levels), and always specify `AltitudeMode` (`ABSOLUTE`, `CLAMP_TO_GROUND`, `RELATIVE_TO_GROUND`, `RELATIVE_TO_MESH`).
   - `addMarker()`, `addMarkerView()`, `getCamera()`, `getCameraRestriction()`, and `getUiControlSettings()` return `@Nullable` types — always use `?.` in Kotlin or `!= null` in Java.
   - `addPopover(...)` and `Popover.show()/hide()/toggle()/remove()` are strictly `@UiThread`.
   - For custom Android `View` markers via `addMarkerView(...)`, instantiate `MarkerViewOptions().apply { ... }` (the `markerViewOptions { }` DSL is `@Hide` in `16.0.1`).
   - `LocaleOptions` also has no top-level DSL; instantiate `LocaleOptions().apply { language = ...; region = ... }`.
2. **Current `Map3DClickEvent` Signature on GA `16.0.1` (Main-Thread Dispatched)**:
   - Do **NOT** use the deprecated 2-argument `{ location, placeId -> }` callback:
     ```kotlin
     googleMap3D.setMap3DClickListener { event: Map3DClickEvent ->
         // Runs on Main UI thread; return false to allow default Places popover, true to suppress
         false
     }
     ```
   - Default POI popovers (returning `false`) require `FragmentActivity` (`AppCompatActivity`) and `com.google.android.libraries.places:places`.
3. **Compose Sizing, `update = {}` Recomposition, `Map3DMode`, & Single-Instance Rule**:
   - Always pass `Modifier.fillMaxSize()` (or explicit non-zero size) to `AndroidView` wrapping `Map3DView` — a `0x0` layout blocks Maps 3D auth.
   - Always pass `Map3DMode.HYBRID` / `SATELLITE` / `ROADMAP` explicitly via `Map3DInitConfig.create(..., mapMode = Map3DMode.HYBRID)` or `Map3DView(context, Map3DMode.HYBRID)` (do NOT use the removed `0.1.0` `Map3DOptions`).
   - **Never** call imperative methods (`addMarker`, `flyCameraTo`, `setCamera`) inside `AndroidView`'s `update = { ... }` lambda; run them inside `onMap3DViewReady` or `LaunchedEffect`.
   - Only **one** `Map3DView` instance may be active per process; track and `.remove()` added objects before unmounting/destroying.
   - **Zero wildcard imports** (`import com.google.android.gms.maps3d.model.*` is forbidden).

## Step 4: Day-1 vs. Day-2 Compile & Verify (Mandatory — 0 Tokens on Pass)

Always pass `--console=plain -q` (emits 0 tokens of stdout on pass; prints only actionable errors to stderr on fail):
1. **Day-1 Setup** (modified `build.gradle.kts`, `AndroidManifest.xml`, `secrets.properties`, or `res/layout/*.xml`):
   ```bash
   ./gradlew processDebugMainManifest processDebugResources compileDebugKotlin --console=plain -q
   ```
2. **Day-2 / Day-3 Code Edits** (only `.kt` or `.java` files modified):
   ```bash
   ./gradlew compileDebugKotlin --console=plain -q   # or compileDebugJavaWithJavac for Java modules
   ```
