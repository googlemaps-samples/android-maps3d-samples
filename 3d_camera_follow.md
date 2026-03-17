# 3d Camera Follow Learnings \- Maps JS API

Mar 12, 2026  [Ryan Baumann](mailto:ryanbaumann@google.com)

**Example:** [https://serve-dot-zipline.googleplex.com/asset/1f36aa09-5981-5344-afa0-6eae2553ce80/](https://serve-dot-zipline.googleplex.com/asset/1f36aa09-5981-5344-afa0-6eae2553ce80/)   
**Screencast:** [http://screencast/cast/NTAwMDg4MzcxNTcwMjc4NHxhYjQ1ZDFmNS0zYg](http://screencast/cast/NTAwMDg4MzcxNTcwMjc4NHxhYjQ1ZDFmNS0zYg)   
**Code: [https://source.cloud.google.com/gmp-demos-ryanbaumann/gmp-demos/+/main:strava-3d-route-viz/README.md](https://source.cloud.google.com/gmp-demos-ryanbaumann/gmp-demos/+/main:strava-3d-route-viz/README.md)** 

# 3D Camera Smoothing & Tracking on Google Maps Platform JS API

Creating a continuously tracking, cinematic 3D drone camera over arbitrary, jagged GPS polyline data is notoriously difficult. The native event-watcher implementations built on `flyCameraTo()` produce choppy, geometric movements that constantly brake to zero velocity at every point.

Here is the final, hardened TL;DR architecture implemented to solve continuous 3D tracking:

## 1\. Ditching Waypoints for Continuous Rendering

- **The Problem:** Resolving Promises or `gmp-animationend` events on native `Map3DElement.flyCameraTo(point)` commands inherently forces the camera to decelerate and **stop** at every coordinate.  
- **The Solution:** We entirely stripped out waypoint logic and replaced it with a `requestAnimationFrame(timestamp)` engine. The engine uses uniform spatial math based on current playback speed (`timestamp * baseSpeedInKmMs`) to continuously calculate and set the exact interpolated position of `map3d.center` and `map3d.heading` frame-by-frame, keeping velocity perfectly uniform.

## 2\. Distance-Based Array Caching & Resampling

- **The Problem:** Raw GPS coordinates contain jagged vertices. Previously, calculating math algorithms (like Haversine spatial distance) on 10,000+ coordinates directly inside the visual `requestAnimationFrame` loop destroyed framerates.  
- **The Solution:** We implemented a two-pass pre-computation algorithm in `createSmoothRoute()`:  
  1. We process the entire GPS route up front, calculating the true cumulative distance of every single point. We physically bind this entire data array to the memory object as a `Float64Array` (`e._cumDists`).  
  2. The run-time camera engine now executes an ultra-lightweight integer scan (`while(e._cumDists[i+1] < t)`) to instantly find its spatial location. This permanently eliminates complex real-time geometric math from the animation loop.  
  3. We then apply a simple rolling average across a window of 15 points (a 75-meter strike length) to artificially round and sweep the harsh geometric edges into organic, swooping curves.

## 3\. Physics-Based Inertia via Interpolation (LERP & SLERP)

- **The Problem:** Even with fast distance lookups, snapping the drone mathematically to the exact polyline coordinate on every frame makes the camera movement feel mechanical and stiff.  
- **The Solution:** We overlayed physics-based **Lerp (Linear Interpolation)** and **Slerp (Spherical Linear Interpolation)** routing inside the engine:  
  1. The engine holds a persistent `smoothedState` memory object for physical position (`lat`, `lng`, `altitude`, `range`, `tilt`).  
  2. At every frame, we calculate the delta between the *current physical camera* and the *target mathematical coordinate*. We then force the physical camera to only travel a fraction (`10%`) of that distance. This creates beautiful "rubber banding" drone inertia.  
  3. **Heading SLERP:** For camera rotation, we implemented a specialized shortest-path wrapper for the `heading` attribute. This guarantees that when the route pivots past True North (359 degrees to 0 degrees), the camera smoothly rotates forward \+1 degree instead of violently spinning \-359 degrees backward.  
  4. The interpolation stiffness is actively variable via the `currentLerpFactor` parameter, which is bound to a real-time UI slider.

## 3\. Cinematic Inertia View (Time-Based Future Bearing)

- **The Problem:** If a camera simply stares exactly at the point it is currently positioned over, the viewport "whips" wildly the exact millisecond the target changes heading. Think of a bumper car rounding a corner.  
- **The Solution:** We implemented a predictive time horizon.  
  1. The engine calculates an anchor point exactly **8 seconds in the future** (adjusted dynamically for current playback speed).  
  2. The camera computes the strict compass `heading` between its *current* position and the *future* 8-second position.  
  3. This produces drone-like inertia: as the route curves, the camera smoothly pans to "anticipate" and "lean into" the incoming turn well before reaching the corner.

## 4\. Ground-Relative Leader Lines (Volumetric Markers)

- **The Problem:** Standard 2D UI pins (`<gmp-advanced-marker>`) or focal alignments float strangely when rendered against complex 3D meshes (skyscrapers, steep canyons, urban forestry) because they inherit relative scene depths poorly.  
- **The Solution:** We swapped to a true WebGL `Marker3DElement`.  
  1. The `altitudeMode` is strictly bound to `'RELATIVE_TO_GROUND'`, aggressively refusing to snap to the top vertices of building or tree meshes.  
  2. We enabled `extruded: true` and boosted the absolute position strictly to `terrainAltitude + 5m`. This creates a beautiful, volumetric 5-meter red laser "Leader Line" piercing straight down from the camera's focus point directly into the exact terrain surface, perfectly anchoring the viewer.

## 5\. Aggressive Spatial Elevation Anti-Clipping

- **The Problem:** When tracking uphill into a mountain range, simply boosting the focal point to `focalElevation + 50m` fails. By the time the focal point is halfway up the cliff face, the camera lens (which is trailing behind the focal point horizontally by \~1000 meters) has completely smashed into the base geometry of the mountain.  
- **The Solution (Part 1 \- Spatial Gradients):** At every frame, the engine scans the absolute delta of terrain variance spanning roughly the upcoming **500 meters**. If a high variance (steep climb) is immediately oncoming, it triggers highly aggressive multipliers:  
  1. It scales coordinate altitude aggressively based on the gradient (pushing the offset **150 meters ABOVE the absolute maximum peak** inside that 500m blast radius).  
  2. It aggressively inflates the camera `range` (zooming backwards by `30x` the variance, easily scaling into the thousands).  
  3. It violently drops the camera's `tilt` dynamically—slammed from a scenic horizontal (`65` degrees) all the way down to a steep orbital drop (`20` degrees) proportional to the steepness of the impending climb.  
- **The Solution (Part 2 \- Physical Footprint Polling API):** If the 3D ground geometry *behind* the target is higher than the target (like panning horizontally along a sheer cliff in Peru), simply raising the target altitude isn't enough; the physical lens still smashes into the cliff. We built a background polling loop leveraging `google.maps.geometry.spherical.computeOffset` to dynamically cast a ray projecting exactly where the camera payload physically sits in the real world. We poll that projected location against `google.maps.ElevationService`. If the physical camera ever falls below the true ground elevation of its coordinate, the system forcibly lifts the entire `cameraAltitude` to maintain a rigid `+250m` absolute floor clearance above the bounding terrain.

## 6\. Decoupling Temporal and Spatial Rendering (Position Scrubbing)

- **The Problem:** If the camera rendering variables (`center`, `tilt`, `range`) exist strictly inside the temporal loop (`if (timestamp - start > T) updateMap`), then manual timeline scrubbing is impossible.  
- **The Solution:** We abstracted the camera setter into a pure, stateless spatial function: `drawCameraAtDistance(distance_in_meters)`. The temporal loop simply accumulates elapsed distance over time and calls that function. By exposing the same spatial function to a standard HTML `<input type="range" id="scrubber">`, users can simply grab the slider and scrub forwards/backwards millions of frames at will, and the engine flawlessly redraws the cinematic camera state **even if playback is fully paused.**

## 7\. "High Altitude Orbital" Mode

- As an ultimate escape hatch, a manual UI toggle bypasses all granular gradients and permanently enforces an extreme drone shot. The engine scrapes the **absolute total highest elevation of the entire route file**. It permanently locks the base altitude `+1200m` above that global maximum peak, forces the camera back `2500m`, and locks the tilt completely down to `20` degrees. This produces a majestic, top-down orbital tracking shot of the entire journey that is mathematically guaranteed to never hit the surface in any topography on earth.

## 8\. Safe Primitive Coordinate Parsing (Preventing Call Stack Crawls)

- **The Problem:** When dynamically injecting `altitude` data directly into Maps SDK `LatLng` route objects via JavaScript `array.map()` closures (e.g. `{ lat: () => e.lat() }`), subsequent route updates created recursive wrapper chains that eventually exploded the browser memory with `Maximum call stack size exceeded` errors.  
- **The Solution:** Custom geometry algorithms (like Haversine distance and bearing calculations) must be rewritten to strictly check and forcefully eagerly-evaluate coordinate properties down to pure numeric primitives `typeof e.lat === 'function' ? e.lat() : e.lat`. Never maintain closures over volatile Maps SDK class instances.

```javascript
// BAD: Creates infinite recursion loop when clicking start multiple times
e = trueElevationPoints.map(p => ({
    lat: () => p.lat(), // Closure traps the array reference
    lng: () => p.lng()
}));

// GOOD: Eagerly evaluates coordinates to absolute primitives
let plat = typeof e[i].lat === 'function' ? e[i].lat() : e[i].lat;
let plng = typeof e[i].lng === 'function' ? e[i].lng() : e[i].lng;

trueElevationPoints.push({
    lat: plat,
    lng: plng,
    altitude: currAlt
});
```

## 9\. Absolute Surface Marker Clamping

- **The Problem:** 3D Interactive Markers placed at high altitudes (like a mountain peak in Peru at 5,000m) were floating an additional 5,000m into the sky. This occurred because the route processing pipeline was supplying the true API elevation to the marker payload (`altitude: 5000`), but the marker was *also* configured to `altitudeMode: RELATIVE_TO_GROUND`, causing it to double-stack the altitude ($5000\\text{m offset} \+ 5000\\text{m mesh relative} \= 10,000\\text{m total}$).  
- **The Solution:** For map-anchored points of interest (like Photo Pins or Trailheads), completely strip the manual `altitude` property from the payload entirely and assign `altitudeMode: CLAMP_TO_GROUND`. This defers absolute positioning strictly to the WebGL rendering engine, safely super-gluing the 3D element directly onto the mesh geometry regardless of its global elevation.

```javascript
// BAD: Double stacking absolute elevation + relative to ground
const marker = new Marker3DInteractiveElement({
    position: { lat: r, lng: a, altitude: 5000 },
    altitudeMode: 'RELATIVE_TO_GROUND'
});

// GOOD: Delegate altitude firmly to the WebGL mesh
const marker = new Marker3DInteractiveElement({
    position: { lat: r, lng: a }, // Strip altitude
    altitudeMode: 'CLAMP_TO_GROUND'
});
```

## 10\. Restoring Absolute Camera Animation Targets

- **The Problem:** After successfully clamping a `Marker3D` to the ground (Section 9), clicking that marker to trigger a cinematic map fly-to (`flyCameraTo`) causes the camera to violently plunge to 0 meters (Sea Level). Why? Because `CLAMP_TO_GROUND` elements inherently discard their `altitude` property, meaning `element.position` returns `{ lat, lng, altitude: undefined }`. When the camera engine receives an `undefined` target altitude, it defaults to `0`.  
- **The Solution:** When mapping `gmp-click` events on clamped markers, you cannot blindly pass the `marker.position` payload back to the map camera. You must maintain the true API ground elevation in memory via closure, and manually re-inject it into the target payload (`{ ...marker.position, altitude: trueElevation + 50 }`) before calling the camera animation engine.

```javascript
// The ground elevation was calculated earlier via ElevationService
const trueElevation = elevationResults[i];

marker.addEventListener("gmp-click", () => {
    // Attempting to fly to marker.position alone will crash into Sea Level (0m)
    // We must manually re-inject the absolute elevation + 50m overhead clearance
    flyCameraTo({ 
        ...marker.position, 
        altitude: trueElevation + 50 
    });
});
```

## 11\. Custom HTML Markers in 3D (Marker3DElement vs AdvancedMarkerElement)

- **The Problem:** 3D `<gmp-map-3d>` elements **do not support** DOM-bound `AdvancedMarkerElement` instances (they require a 2D `<gmp-map>` parent). Furthermore, injecting complex CSS-styled HTML sub-trees directly into a `Marker3DElement` via a `<template>` tag fails with a `maps3d.js` type error.  
- **The Engine Constraint:** The native `Marker3DElement` strictly rasterizes elements directly into the WebGL scene canvas for maximum raw geometry performance. Because it rasterizes, it strictly only supports raw `HTMLImageElement`, simple `SVGElement` payloads, or native API elements.  
- **The Solution:** Import the GMP `Marker3DElement` class directly. We bypass `PinElement` and external DOM structures entirely for the camera marker.  
  1. We simply instantiate a raw `new Marker3DElement()`.  
  2. We set `altitudeMode: 'RELATIVE_TO_GROUND'` and force the extrusion `altitude: 5`. This cleanly ensures the WebGL engine receives a format it handles perfectly, safely drawing a basic volumetric stalk directly hovering over the ground mesh geometry without any 2D artifact rendering bugs or clipping issues.  
  3. **Implementation Warning:** When injecting this marker into the scene during `requestAnimationFrame` initialization, ensure the cleanup function does not execute `removeChild()` on the *newly passed* marker instance. Maintain an `activeCameraMarker` singleton reference inside the closure to safely wipe old markers without accidentally detaching the new tracking element before the loop even starts.

## 12\. Velocity Slope Clamping (Jank Prevention)

- **The Problem:** The temporal LERP physics defined in Section 3 flawlessly smooth out raw geometric turns. However, when the playback speed multiplier is cranked (e.g. `16x` speed), the target elevation spikes massively frame-to-frame as the 500m blast radius sweeps over sharp alpine peaks. This causes the target physical drone altitude to teleport vertically faster than the LERP can practically dampen, resulting in jarring "vertical pop-in".  
- **The Solution:** We implemented a physical maximum rate-of-change (slope) clamp on the target camera elevation.  
  1. At exactly `1.0x` speed, the camera is physically forbidden from altering its target altitude by more than `4.0` meters per frame (\~240m/s climb/dive rate at 60fps).  
  2. This delta actively scales smoothly via the user's `multiplier` variable so that high playback speeds scale linearly without trailing deeply into the ground.  
  3. Consequently, extreme topological variance is converted into a smooth, constrained glide slope *before* entering the LERP physics, providing cinematic panning across mountains.

