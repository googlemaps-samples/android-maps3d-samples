# Gemini Code Assist Style Guide: android-maps3d-samples

This guide defines the custom code review and generation rules for the `android-maps3d-samples` project.

## Jetpack Compose Guidelines
- **API Guidelines**: Strictly follow the [Jetpack Compose API guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md).
- **Naming**: Composable functions must be PascalCase.
- **State Management**: Lift state. Do not put complex `GoogleMap3D` objects directly into Composables if possible.
- **Modifiers**: The first optional parameter of any Composable should be `modifier: Modifier = Modifier`.

## Kotlin Style
- **Naming**: Use camelCase for variables and functions.
- **Documentation**: Provide KDoc for all public classes, properties, and functions.
- **Safety**: Use null-safe operators and avoid `!!`.

## Maps 3D Specifics
- **Secrets**: Never commit API keys. Ensure they are read from `secrets.properties` via `BuildConfig` or similar. Use the Secrets Gradle Plugin.
- **Maps 3D SDK Integration**:
  - The SDK is currently View-based (`Map3DView`). Use it in Jetpack Compose by wrapping it inside an `AndroidView` composable.
  - Implement the **Double-Wait** pattern (`awaitCameraAnimation` + `awaitSteady`) for cinematic animations in Compose to ensure peak visual quality before triggering UI changes.
  - Animations are fire-and-forget. Use a wrapper suspend function (like `awaitCameraAnimation`) to enable structured concurrency.
  - Be mindful of Z-ordering issues when over-layering Compose elements over the `AndroidView` which uses `SurfaceView` or `TextureView` internally.
  - **Permissions**: Ensure `<uses-permission android:name="android.permission.INTERNET" />` is in `AndroidManifest.xml`.
  - **API Key Metadata**: Requires `<meta-data android:name="com.google.android.geo.maps3d.API_KEY" android:value="${MAPS3D_API_KEY}" />` in `AndroidManifest.xml`.
