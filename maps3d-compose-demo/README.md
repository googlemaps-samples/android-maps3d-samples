# Maps 3D Compose Demo

This module is a sample application that demonstrates how to use the `maps3d-compose` wrapper library.

> [!WARNING]
> **Status**: This application serves as a **reference implementation** for using the experimental Compose wrapper. It is a WIP experiment.

## What's Inside

You can find several sample activities demonstrating different features of the 3D Map in Compose:
- **Basic Map**: A simple map with camera controls.
- **Map Options**: Demonstrates changing map modes (Satellite/Hybrid) and applying camera restrictions.
- **Models**: Shows how to add and interact with 3D models (glTF) on the map, including click listeners.
- **Popovers**: Demonstrates the declarative Popover API, rendering Compose content inside native map popovers.

## How to Run

You can install and run this demo on a connected device or emulator using:
```bash
./gradlew :maps3d-compose-demo:installDebug
```
Then launch any of the activities via the launcher or ADB.
