# Codelab: Aloha Explorer — Building with the 3D Maps SDK for Android

Welcome to the future of mobile mapping! In this codelab, we are going to build **Aloha Explorer**, a travel app designed to showcase the beauty and history of Honolulu using the **Google Maps 3D SDK for Android**.

Unlike standard 2D or "2.5D" maps, the 3D SDK allows you to treat the world as a true 3D environment. You will learn how to fly cameras through mountain passes, animate models in the water, and highlight historic landmarks with 3D volumes.

### What you’ll learn

* How to initialize the 3D Map view.
* How to orchestrate cinematic camera movements.
* How to use **Clamp Modes** to place objects accurately in 3D space.
* How to extrude 2D polygons into 3D volumes.
* How to animate 3D models (GLTF) along complex paths.
* How to wrap the 3D SDK for use in **Jetpack Compose**.

---

## 1. Hello, 3D Honolulu!

Before we can explore the islands, we need to get the "engine" started. This first step ensures your development environment is correctly configured and the 3D renderer is active.

### The API Key (Don't Panic!)

The most common hurdle in map development is the API key. To make this work, you need a key with the correct permissions.

1. Follow the [standard Google Maps setup steps here](https://developers.google.com/maps/documentation/android-sdk/get-api-key) to create a project and a key.
2. **Crucial Step:** You must explicitly enable the **"Maps 3D SDK for Android"** in the [Google Cloud Console Library](https://console.cloud.google.com/apis/library/mapsandroid.googleapis.com). Without this, the map will remain a blank canvas.

### Objective

Initialize the `Map3DView` in your layout and set the initial camera position to coordinates over Honolulu.

### Conclusion

If you see the vibrant blue of the Pacific and the detailed 3D mesh of the city buildings, congratulations! Your SDK is authenticated and ready for action.

---

## 2. The Royal Flyover (Camera Mastery)

Now that we’re in Hawaii, let's head to the historic **Iolani Palace**. In this section, we move from simple "teleporting" to cinematic storytelling.

### Objective

*   **The Snap:** Use `CameraUpdate` to move the camera instantly to the Palace.
*   **The Smooth Glide:** Implement `animateCamera` to transition from the ocean to the Palace with a dramatic tilt.
*   **The Orbit:** Script a 360-degree rotation around the building to show off its 3D depth.
*   **Timing the Scene:** Use `delay()` to sequence these animations. It's simple, but is it robust?

### Conclusion

You’ve mastered the 3D camera. You can now direct a scene like a movie, moving from shot to shot with cinematic grace.

---

## 3. The Professional Touch (Wait for It...)

Using specific time delays (e.g., `delay(5000)`) is easy, but it's risky. What if the user has a slow connection and the map isn't loaded yet? What if the device is slow and the animation takes longer than expected?

In this section, we replace "guessing" with "listening."

### Objective

*   **The Steady Listener:** Use `setOnMapSteadyListener` to detect when the 3D map has fully streamed in the surrounding tiles.
*   **The Animation Listener:** Use `setCameraAnimationEndListener` to trigger the "Orbit" only after the "Glide" has truly finished.
*   **The "Suspending" Wrapper:** Write your own suspending functions, `awaitMapSteady()` and `awaitCameraAnimation()`, to wrap these callbacks. This allows you to keep your clean, readable coroutine code while getting the robustness of event-driven programming.

### Conclusion

Your app is now "network resilient." It doesn't just run on a timer; it reacts to the state of the world. This is the difference between a prototype and a production-ready app.

---

## 4. Sticking the Landing (Markers & Clamping)

Objects in a 3D world need to know where they sit on the "Z-axis." If you place a marker on a mountain, does it float in the air or sink into the rocks?

### Objective

Place a marker at the Palace entrance and experiment with the three primary **Clamp Modes**:

* **Draped:** The marker "paints" onto the ground surface.
* **Relative to Ground:** The marker hovers a specific distance above the terrain.
* **Absolute:** The marker stays at a fixed altitude, regardless of the hills below it.

### Conclusion

You now understand **Clamping**. This is the secret to making sure your UI elements feel like they are part of the physical world.

---

## 5. Highlighting History (2D to 3D Polygons)

Sometimes a single marker isn't enough. We want to highlight the entire Palace grounds to show its significance.

### Objective

* **Draw a Perimeter:** Create a flat `Polygon3D` that traces the boundaries of the Palace.
* **The Holographic Highlight:** Instead of a simple flat shape, we'll "extrude" that polygon into a **3D Rectangular Prism**. By making it translucent, you create a 3D volume of light that draws attention to the area without obscuring the beautiful 3D buildings inside it.

### Conclusion

You’ve moved beyond points and lines. You can now define 3D volumes in space, a powerful tool for highlighting districts or historic sites.

---

## 6. Tapping the Turf (Interaction)

A great app is interactive. We want users to be able to "touch" the 3D world.

### Objective

Implement an `OnElementClickListener`. When a user taps your 3D Palace prism, trigger a UI event—like a snackbar or a detail card—that tells them about the history of the Hawaiian Monarchy.

### Conclusion

Your map is now alive. You’ve bridged the gap between the 3D map renderer and the Android UI, allowing users to "query" the world around them.

---

## 7. Beach Bound & "Jumping the Shark"

It’s time for some fun. We’re moving from the Palace to the beach, and we’re bringing some animation with us.

### Objective

* **The Route:** Draw a `Polyline3D` from the Palace to Waikiki Beach.
* **Follow the Path:** Code a camera sequence that follows this line, simulating a commute to the shore.
* **The Fin:** Place a `Model3D` (a shark fin) in the bay.
* **The Grand Finale:** Animate the fin's position to swim in a circle. Then, script a **"Jump the Shark"** sequence where the model rises, tilts forward, and dives back into the water using a parabolic path.

### Conclusion

You’ve pushed the SDK to its limits! You can now animate complex 3D assets through space and time, opening the door for games or real-time transit tracking.

---

## 8. The "Cool Kids" Corner (Jetpack Compose)

Modern Android development is all about **Jetpack Compose**. While the 3D SDK is View-based, it plays perfectly with Compose.

### Objective

Wrap your `Map3DView` inside an `AndroidView` composable. You will see a high-level demonstration of how to maintain the map's lifecycle and trigger a simple camera movement from a Compose `Button`.

### Conclusion

You’ve modernized your stack. You can now build beautiful, reactive UIs in Compose that sit right on top of your immersive 3D world. For a richer implementation, you can always refer to the full samples in GitHub.

---

## Finish & Next Steps

Mahalo for completing the **Aloha Explorer** codelab! You’ve gone from a blank map to a cinematic, interactive 3D experience.

Now that you have the tools, what will you build? Here are a few ideas to try on your own:

* **Hiker’s Helper:** Use `ClampMode` to trace a ridge-line hike across the Oahu mountains.
* **City Transit:** Animate 3D bus or train models along real-world routes.
* **Real Estate Tour:** Use 3D prisms to show the "view" from various floors of a proposed building.

**The world is no longer flat—go build something amazing!**

> **Ready to dive into the code?** [Link to the full GitHub Sample Repository]
