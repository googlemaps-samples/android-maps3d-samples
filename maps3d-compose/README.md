# Maps 3D Compose Wrapper (Experimental)

This library provides a Jetpack Compose wrapper for the Google Maps 3D SDK for Android.

> [!WARNING]
> **Status**: This implementation is a **Work In Progress (WIP) experiment** and serves as a **reference implementation**. It is not intended for production use. APIs may change significantly in the future.

## What's Inside

This module contains the source code for the Compose wrapper:
- **`GoogleMap3D`**: The main Composable function to display a 3D map.
- **Declarative State**: Support for adding Markers, Polylines, Polygons, 3D Models, and Popovers via standard Compose state lists.
- **Camera Hoisting**: Ability to hoist the camera state for animations and position tracking.

## Current Coverage

For a detailed list of supported features and missing APIs, see [compose_api_coverage.md](./compose_api_coverage.md).
