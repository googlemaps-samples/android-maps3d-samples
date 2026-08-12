/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.composedemos.pathfollowing

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.PolylineConfig
import kotlinx.coroutines.delay

class PathFollowingActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    windowInsetsController.systemBarsBehavior =
      WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          PathFollowingScreen()
        }
      }
    }
  }

  companion object {
    // Urban Path (New York City - Central Park Block Circuit)
    val URBAN_PATH = listOf(
      LatLng(40.7783119, -73.9627630),
      LatLng(40.7776355, -73.9611664),
      LatLng(40.7770011, -73.9616294),
      LatLng(40.7776743, -73.9632228),
      LatLng(40.7783119, -73.9627630)
    )

    // Rural Path
    val RURAL_PATH = listOf(
      LatLng(37.254529, -122.380897),
      LatLng(37.255065, -122.381627),
      LatLng(37.257540, -122.383720),
      LatLng(37.261200, -122.383950),
      LatLng(37.264780, -122.388210),
      LatLng(37.268520, -122.392450),
      LatLng(37.272110, -122.397640),
      LatLng(37.276430, -122.401120),
      LatLng(37.280850, -122.403560),
      LatLng(37.286018, -122.405072),
      LatLng(37.291040, -122.404210),
      LatLng(37.295800, -122.401980),
      LatLng(37.300120, -122.399540),
      LatLng(37.304550, -122.397210),
      LatLng(37.309200, -122.395100),
      LatLng(37.313450, -122.392840),
      LatLng(37.317200, -122.390510),
      LatLng(37.320850, -122.388740),
      LatLng(37.323540, -122.387600),
      LatLng(37.325269, -122.386728)
    )
  }
}

enum class EnvironmentType(val label: String) {
  URBAN("Urban"),
  RURAL("Rural")
}

@Composable
fun PathFollowingScreen() {
  var isMapSteady by remember { mutableStateOf(false) }

  // Environment Selection State
  var selectedEnv by remember { mutableStateOf(EnvironmentType.URBAN) }
  var currentPath by remember { mutableStateOf(PathFollowingActivity.URBAN_PATH) }

  // Path Calculations State
  val pathCalculations = remember(currentPath) {
    val cumulative = DoubleArray(currentPath.size)
    var total = 0.0
    cumulative[0] = 0.0
    for (i in 1 until currentPath.size) {
      val dist = SphericalUtil.computeDistanceBetween(currentPath[i - 1], currentPath[i])
      total += dist
      cumulative[i] = total
    }
    Pair(cumulative, total)
  }
  val cumulativeDistances = pathCalculations.first
  val totalDistance = pathCalculations.second

  // Animation Controls State
  var isPlaying by remember { mutableStateOf(false) }
  var progress by remember { mutableFloatStateOf(0.0f) }
  var isUserScrubbing by remember { mutableStateOf(false) }
  var elapsedDistance by remember { mutableDoubleStateOf(0.0) }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
        isPlaying = false
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Camera Parameter Sliders State
  var cameraRange by remember { mutableFloatStateOf(300f) }
  var groundAltitude by remember { mutableFloatStateOf(20f) }
  var headingOffset by remember { mutableFloatStateOf(0f) }
  var cameraTilt by remember { mutableFloatStateOf(70f) }
  var followSpeedMps by remember { mutableFloatStateOf(30f) }

  // Interpolated Position & Heading
  var currentLatLng by remember(currentPath) { mutableStateOf(currentPath.first()) }
  var currentHeading by remember { mutableStateOf<Double?>(null) }
  var targetHeading by remember { mutableDoubleStateOf(120.0) }

  // Helper to calculate position for given elapsed distance
  fun updatePositionForDistance(dist: Double) {
    if (currentPath.isEmpty()) return
    var index = 0
    while (index < cumulativeDistances.size - 1 && cumulativeDistances[index + 1] < dist) {
      index++
    }
    val p1 = currentPath[index]
    val p2 = if (index < currentPath.size - 1) currentPath[index + 1] else p1

    val segStartDist = cumulativeDistances[index]
    val segEndDist =
      if (index < cumulativeDistances.size - 1) cumulativeDistances[index + 1] else totalDistance
    val segLen = segEndDist - segStartDist

    val fraction = if (segLen > 0) ((dist - segStartDist) / segLen).coerceIn(0.0, 1.0) else 0.0
    currentLatLng = SphericalUtil.interpolate(p1, p2, fraction)
    val bearing = SphericalUtil.computeHeading(p1, p2)

    val targetHeadingRaw = (bearing + headingOffset + 360.0) % 360.0
    val computedHeading = if (currentHeading == null || isUserScrubbing || !isPlaying) {
      targetHeadingRaw
    } else {
      var diff = (targetHeadingRaw - currentHeading!!) % 360.0
      if (diff > 180.0) diff -= 360.0
      if (diff < -180.0) diff += 360.0
      (currentHeading!! + diff * 0.12 + 360.0) % 360.0
    }
    currentHeading = computedHeading
    targetHeading = computedHeading
  }

  // Switch Environment logic
  fun switchEnvironment(env: EnvironmentType) {
    selectedEnv = env
    isPlaying = false
    currentHeading = null
    progress = 0f
    elapsedDistance = 0.0
    if (env == EnvironmentType.RURAL) {
      currentPath = PathFollowingActivity.RURAL_PATH
      cameraRange = 450f
      groundAltitude = 40f
      cameraTilt = 75f
    } else {
      currentPath = PathFollowingActivity.URBAN_PATH
      cameraRange = 300f
      groundAltitude = 20f
      cameraTilt = 70f
    }
    updatePositionForDistance(0.0)
  }

  // Animation Loop
  LaunchedEffect(isPlaying) {
    if (!isPlaying) return@LaunchedEffect
    val frameDurationMs = 16L
    while (isPlaying) {
      val stepDistance = followSpeedMps * (frameDurationMs / 1000.0)
      elapsedDistance += stepDistance

      if (elapsedDistance >= totalDistance) {
        elapsedDistance = 0.0
      }

      if (!isUserScrubbing && totalDistance > 0) {
        progress = (elapsedDistance / totalDistance).toFloat().coerceIn(0f, 1f)
      }
      updatePositionForDistance(elapsedDistance)

      delay(frameDurationMs)
    }
  }

  // Dynamic Camera State
  val dynamicCamera =
    remember(currentLatLng, targetHeading, cameraTilt, cameraRange, groundAltitude) {
      camera {
        center = latLngAltitude {
          latitude = currentLatLng.latitude
          longitude = currentLatLng.longitude
          altitude = groundAltitude.toDouble()
        }
        heading = targetHeading
        tilt = cameraTilt.toDouble()
        range = cameraRange.toDouble()
        roll = 0.0
      }
    }

  // Polyline Config
  val polylineConfig = remember(currentPath) {
    PolylineConfig(
      key = "path_following_polyline",
      points = currentPath.map { latLng ->
        latLngAltitude {
          latitude = latLng.latitude
          longitude = latLng.longitude
          altitude = 5.0
        }
      },
      color = Color.BLUE,
      width = 10f,
      altitudeMode = AltitudeMode.RELATIVE_TO_GROUND,
    )
  }

  Box(
    modifier = Modifier
        .fillMaxSize()
        .semantics { contentDescription = if (isMapSteady) "MapSteady" else "MapLoading" },
  ) {
    // 1. Full Screen 3D Map
    GoogleMap3D(
      camera = dynamicCamera,
      polylines = listOf(polylineConfig),
      modifier = Modifier.fillMaxSize(),
      onMapSteady = {
        isMapSteady = true
      },
    )

    // 2. Control Panel Card at Bottom
    Card(
      modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(16.dp)
          .fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier
            .padding(16.dp)
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState()),
      ) {
        // Environment Selector
        Text(
          text = "Path Environment:",
          style = MaterialTheme.typography.labelLarge,
        )

        Row(
          modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          EnvironmentType.entries.forEach { env ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                  .weight(1f)
                  .selectable(
                      selected = (selectedEnv == env),
                      onClick = { switchEnvironment(env) },
                      role = Role.RadioButton,
                  ),
            ) {
              RadioButton(
                selected = (selectedEnv == env),
                onClick = null,
              )
              Text(
                text = env.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp),
              )
            }
          }
        }

        // Play / Pause and Progress Slider Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth(),
        ) {
          IconButton(
            onClick = { isPlaying = !isPlaying },
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
              contentDescription = "Play or Pause animation",
            )
          }

          Slider(
            value = progress,
            onValueChange = { newValue ->
              isUserScrubbing = true
              progress = newValue
              elapsedDistance = totalDistance * newValue
              updatePositionForDistance(elapsedDistance)
            },
            onValueChangeFinished = {
              isUserScrubbing = false
            },
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Path Progress Slider" },
          )
        }

        // Camera Controls Sliders
        Text(
          text = "Camera Range: ${cameraRange.toInt()}m",
          style = MaterialTheme.typography.labelMedium,
        )
        Slider(
          value = cameraRange,
          onValueChange = {
            cameraRange = it
            updatePositionForDistance(elapsedDistance)
          },
          valueRange = 50f..1000f,
          modifier = Modifier.semantics { contentDescription = "Camera Range Slider" },
        )

        val maxAltitude = if (selectedEnv == EnvironmentType.RURAL) 2000f else 200f
        Text(
          text = "Ground Altitude: ${groundAltitude.toInt()}m",
          style = MaterialTheme.typography.labelMedium,
        )
        Slider(
          value = groundAltitude.coerceIn(2f, maxAltitude),
          onValueChange = {
            groundAltitude = it
            updatePositionForDistance(elapsedDistance)
          },
          valueRange = 2f..maxAltitude,
          modifier = Modifier.semantics { contentDescription = "Ground Altitude Slider" },
        )

        Text(
          text = "Heading Offset: ${headingOffset.toInt()}°",
          style = MaterialTheme.typography.labelMedium,
        )
        Slider(
          value = headingOffset,
          onValueChange = {
            headingOffset = it
            updatePositionForDistance(elapsedDistance)
          },
          valueRange = -180f..180f,
          modifier = Modifier.semantics { contentDescription = "Heading Offset Slider" },
        )

        Text(
          text = "Camera Tilt: ${cameraTilt.toInt()}°",
          style = MaterialTheme.typography.labelMedium,
        )
        Slider(
          value = cameraTilt,
          onValueChange = {
            cameraTilt = it
            updatePositionForDistance(elapsedDistance)
          },
          valueRange = 0f..85f,
          modifier = Modifier.semantics { contentDescription = "Camera Tilt Slider" },
        )

        Text(
          text = "Follow Speed: ${followSpeedMps.toInt()} m/s",
          style = MaterialTheme.typography.labelMedium,
        )
        Slider(
          value = followSpeedMps,
          onValueChange = { followSpeedMps = it },
          valueRange = 5f..100f,
          modifier = Modifier.semantics { contentDescription = "Follow Speed Slider" },
        )
      }
    }
  }
}
