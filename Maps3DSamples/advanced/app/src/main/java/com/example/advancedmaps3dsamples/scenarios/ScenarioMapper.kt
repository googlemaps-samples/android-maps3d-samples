// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.scenarios

import android.graphics.Color
import android.util.Log
import com.example.advancedmaps3dsamples.utils.toHeading
import com.example.advancedmaps3dsamples.utils.toRange
import com.example.advancedmaps3dsamples.utils.toRoll
import com.example.advancedmaps3dsamples.utils.toTilt
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.PolygonOptions
import com.google.android.gms.maps3d.Map3DOptions
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import com.google.android.gms.maps3d.model.Hole
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polygonOptions
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.google.maps.android.ktx.utils.toLatLngList
import java.util.UUID
import androidx.core.graphics.toColorInt

private const val TAG = "ScenarioMapper"

/**
 * Parses a comma-separated string of "key=value" pairs into a map. Example:
 * "lat=39.65,lng=-105.02,alt=550.2" -> {"lat": "39.65", "lng": "-105.02", "alt": "550.2"}
 */
fun String.toAttributesMap(): Map<String, String> {
  // Trim whitespace around the string and commas/semicolons just in case
  val trimmedString = this.trim().trimEnd(';').trimEnd(',')
  if (trimmedString.isBlank()) {
    return emptyMap()
  }
  return trimmedString
    .split(",")
    .map { it.trim() }
    .filter { it.contains("=") }
    .associate { part ->
      val (key, value) = part.split("=", limit = 2)
      key.trim() to value.trim()
    }
}

/** Helper to safely get a Double value from the attributes map. */
fun Map<String, String>.getDouble(key: String, default: Double = 0.0): Double {
    return this[key]?.toDoubleOrNull() ?: default
}

/** Helper to safely get a Long value from the attributes map. */
fun Map<String, String>.getLong(key: String, default: Long = 0L): Long {
    return this[key]?.toLongOrNull() ?: default
}

/** Helper to safely get a Double value from the attributes map. */
fun Map<String, String>.getInt(key: String, default: Int = 0): Int {
    return this[key]?.toIntOrNull() ?: default
}

/** Helper to safely get a String value from the attributes map. */
fun Map<String, String>.getString(key: String, default: String = ""): String = this[key] ?: default

// --- Object Converters ---

fun Map<String, String>.toLatLngAltitude(): LatLngAltitude {
    return latLngAltitude {
        latitude = getDouble("lat")
        longitude = getDouble("lng")
        // Use 'alt' directly now, assuming it represents the final desired altitude
        altitude = getDouble("alt", 0.0) // Provide a default altitude if needed
    }
}

fun Map<String, String>.toCamera(): Camera {
    return camera {
        center = this@toCamera.toLatLngAltitude()
        heading = getDouble("hdg").toHeading()
        tilt = getDouble("tilt", 45.0).toTilt()
        range = getDouble("range", 1500.0).toRange()
        roll = getDouble("roll", 0.0).toRoll()
    }
}

fun String.toCamera(): Camera {
    return this.toAttributesMap().toCamera()
}

fun Map<String, String>.toDuration(): Long {
    // Use "dur" as the key for duration
    return getLong("dur", 1000) // Default duration 1000ms
}

fun String.toFlyTo(): FlyToOptions {
    val attributes = this.toAttributesMap()
    return flyToOptions {
        endCamera = attributes.toCamera()
        durationInMillis = attributes.toDuration()
    }
}

fun String.toFlyAround(): FlyAroundOptions {
    val attributes = this.toAttributesMap()
    return flyAroundOptions {
        center = attributes.toCamera() // Fly around the camera position defined in the string
        durationInMillis = attributes.toDuration()
        // Use "count" as the key for rounds
        rounds = attributes.getDouble("count", 1.0) // Default 1 round
    }
}

fun String.toTimeout(): Long {
    val attributes = this.toAttributesMap()
    return attributes["timeout"]?.toLong() ?: 0L
}

fun String.toDelay(): Long {
    val attributes = this.toAttributesMap()
    return attributes.toDuration()
}

@Map3DMode
fun String.toMap3DMode(): Int {
    return when (this.lowercase()) {
        "hybrid" -> Map3DMode.HYBRID
        "satellite" -> Map3DMode.SATELLITE
        else -> {
            Log.w(TAG, "Unsupported map mode '$this', defaulting to SATELLITE.")
            Map3DMode.SATELLITE
        }
    }
}

fun String.toAnimation(): List<AnimationStep> {
    val stepsString = this.trim().trimEnd(';')
    if (stepsString.isBlank()) {
        return emptyList()
    }
    return buildList<AnimationStep> {
        stepsString.split(";").forEach { step ->
            val trimmedStep = step.trim()
            if (trimmedStep.contains('=')) {
                val (key, value) = trimmedStep.split("=", limit = 2)
                when (key.trim().lowercase()) {
                    "flyto" -> add(FlyToStep(value.toFlyTo()))
                    "delay" -> add(DelayStep(value.toDelay()))
                    "flyaround" -> add(FlyAroundStep(value.toFlyAround()))
                    "waituntilthemapissteady" -> {
                        add(WaitUntilTheMapIsSteadyStep(value.toTimeout()))
                    }
                    else -> Log.w(TAG, "Unsupported animation step type: $key")
                }
            } else {
                when (trimmedStep.lowercase()) {
                    "waituntilthemapissteady" -> {
                        add(WaitUntilTheMapIsSteadyStep(0L))
                    }
                    else -> Log.w(TAG, "Ignoring invalid animation step format: $step")
                }
            }
        }
    }
}

fun String.toMaps3DOptions(): Map3DOptions {
    var camera: Camera? = null
    var mode: Int = Map3DMode.SATELLITE

    val optionsString = this.trim().trimEnd(';')
    if (optionsString.isBlank()) {
        Log.w(TAG, "Empty initialState string provided for Map3DOptions")
        // Return default options
        return Map3DOptions(defaultUiDisabled = true)
    }

    optionsString.split(";").forEach { option ->
        val trimmedOption = option.trim()
        if (trimmedOption.contains('=')) {
            val (label, value) = trimmedOption.split("=", limit = 2)
            when (label.trim().lowercase()) {
                "mode" -> {
                    mode = value.trim().toMap3DMode()
                }

                "camera" -> {
                    camera = value.trim().toCamera()
                }

                else -> Log.w(TAG, "Unsupported Map3DOption key: $label")
            }
        } else {
            Log.w(TAG, "Ignoring invalid Map3DOption format: $option")
        }
    }

    // Use defaults if camera is not specified in the string
    val defaultCameraPos = latLngAltitude {
        latitude = 0.0
        longitude = 0.0
        altitude = 10_000_000.0
    }

    return Map3DOptions(
        defaultUiDisabled = true,
        centerLat = camera?.center?.latitude ?: defaultCameraPos.latitude,
        centerLng = camera?.center?.longitude ?: defaultCameraPos.longitude,
        centerAlt = camera?.center?.altitude ?: defaultCameraPos.altitude,
        heading = camera?.heading ?: 0.0,
        tilt = camera?.tilt ?: 0.0, // Default tilt 0 for initial state
        roll = camera?.roll ?: 0.0,
        range = camera?.range ?: 10_000_000.0, // Default range wide view
        minHeading = 0.0,
        maxHeading = 360.0,
        minTilt = 0.0,
        maxTilt = 90.0,
        bounds = null,
        mapMode = mode,
        mapId = null,
    )
}

fun String.toMarkers(): List<MarkerOptions> {
    val markersString = this.trim().trimEnd(';')
    if (markersString.isBlank()) {
        return emptyList()
    }
    return markersString.split(";").mapNotNull { markerStr ->
        val attributes = markerStr.toAttributesMap()
        val markerId = attributes["id"]
        // Basic validation: requires lat, lng
        if (attributes.containsKey("lat") && attributes.containsKey("lng")) {
            markerOptions {
                if (markerId != null) {
                    id = markerId
                }
                collisionBehavior = CollisionBehavior.REQUIRED_AND_HIDES_OPTIONAL
                position = attributes.toLatLngAltitude()
                isExtruded = true
                isDrawnWhenOccluded = true // Consider if this is always desired
                label = attributes.getString("label", "Marker") // Default label
                zIndex = attributes.getInt("z", 1) // Default zIndex
                this.altitudeMode = parseAltitudeMode(attributes.getString("altMode"))
            }
        } else {
            Log.w(TAG, "Skipping invalid marker definition (missing lat/lng): $markerStr")
            null // Skip invalid marker definitions
        }
    }
}

fun String.toModels(): List<ModelOptions> {
    val modelsString = this.trim().trimEnd(';')
    if (modelsString.isBlank()) {
        return emptyList()
    }
    return modelsString.split(";").mapNotNull { modelStr ->
        val attributes = modelStr.toAttributesMap()
        // Basic validation: require id, lat, lng, url
        val modelId = attributes.getString("id")
        val url = attributes.getString("url")
        if (
            modelId.isNotBlank() &&
            url.isNotBlank() &&
            attributes.containsKey("lat") &&
            attributes.containsKey("lng")
        ) {
            try {
                modelOptions {
                    id = modelId // Set the ID within the options
                    position = attributes.toLatLngAltitude()
                    this.url = url // Use 'this.url' to avoid conflict if needed
                    altitudeMode = parseAltitudeMode(attributes.getString("altMode"))
                    // Parse scale - allow uniform scale via 'scale' or individual axes
                    val uniformScale = attributes.getDouble("scale", -1.0) // Use -1 to check if set
                    scale =
                        if (uniformScale >= 0) {
                            vector3D {
                                x = uniformScale
                                y = uniformScale
                                z = uniformScale
                            }
                        } else {
                            vector3D {
                                x = attributes.getDouble("scaleX", 1.0) // Default scale 1.0
                                y = attributes.getDouble("scaleY", 1.0)
                                z = attributes.getDouble("scaleZ", 1.0)
                            }
                        }
                    // Parse orientation
                    orientation = orientation {
                        heading = attributes.getDouble("hdg", 0.0) // Default 0
                        tilt = attributes.getDouble("tilt", 0.0) // Default 0
                        roll = attributes.getDouble("roll", 0.0) // Default 0
                    }
                    // Add other ModelOptions properties here if needed
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing model definition: $modelStr", e)
                null // Skip models that cause parsing errors
            }
        } else {
            Log.w(
                TAG,
                "Skipping invalid model definition (missing id, url, lat, or lng): $modelStr"
            )
            null // Skip invalid model definitions
        }
    }
}

fun String.toPolylines(): List<PolylineOptions> {
    return split(";").flatMap { it.toPolyline() }
}

/**
 * Parses a polygon description string into a list of PolygonOptions.
 *
 * The input string should contain key=value pairs separated by commas.
 * Keys include:
 * - outer: A list of outer polygon coordinates separated by "|", each coordinate as "lat:lng[:alt]".
 * - inner: (Optional) A list of hole coordinates separated by "|", same format as outer.
 * - fill: (Optional) Fill color string (e.g., "#FF0000").
 * - stroke: (Optional) Stroke color string.
 * - width: (Optional) Stroke width as a double.
 * - altMode: (Optional) Altitude mode string.
 *
 * Returns an empty list if no valid outer coordinates are found.
 */
fun String.toPolygons(): List<PolygonOptions> {
    val attributes = this.toAttributesMap()

    val outerRaw = attributes["outer"] ?: return emptyList()
    val outerCoordinates = outerRaw.split("|").mapNotNull { it.toLatLngAltitudeOrNull() }
    if (outerCoordinates.isEmpty()) return emptyList()

    val innerCoordinates = attributes["inner"]
        ?.split("|")
        ?.mapNotNull { it.toLatLngAltitudeOrNull() }
        ?.let { listOf(Hole(it)) }
        ?: emptyList()

    val fillColor = attributes.getColor("fill", Color.argb(70, 255, 255, 0))
    val strokeColor = attributes.getColor("stroke", Color.GREEN)
    val strokeWidth = attributes.getDouble("width", 3.0)
    val altitudeMode = parseAltitudeMode(attributes["altMode"].toString())

    return listOf(
        polygonOptions {
            this.path = outerCoordinates
            this.innerPaths = innerCoordinates
            this.fillColor = fillColor
            this.strokeColor = strokeColor
            this.strokeWidth = strokeWidth
            this.altitudeMode = altitudeMode
        }
    )
}

/**
 * Retrieves a color integer value from the map by key.
 *
 * The map is expected to contain color values as strings (e.g., "#FF0000").
 * If the key is not present or the value is not a valid color string,
 * the specified default color integer is returned.
 *
 * @receiver Map<String, String> containing key-value pairs where values might be color strings.
 * @param key The key to look up in the map.
 * @param default The default color integer to return if the key is missing or invalid.
 * @return Int The parsed color integer or the default color if parsing fails.
 */
fun Map<String, String>.getColor(key: String, default: Int): Int {
    val value = this[key] ?: return default
    return try {
        value.toColorInt()
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        default
    }
}

/**
 * Parses a string representing geographic coordinates with optional altitude into a [LatLngAltitude] object.
 *
 * The input string should have the format: "latitude:longitude" or "latitude:longitude:altitude",
 * where latitude and longitude are required and altitude is optional.
 *
 * Examples:
 * - "39.7508987:-104.9565381" -> LatLngAltitude(39.7508987, -104.9565381, 0.0)
 * - "39.7508987:-104.9565381:100.0" -> LatLngAltitude(39.7508987, -104.9565381, 100.0)
 *
 * Returns null if the input does not have at least latitude and longitude,
 * or if any of the numeric parts cannot be parsed as Double.
 *
 * @receiver String containing the coordinate data separated by colons.
 * @return [LatLngAltitude]? Parsed coordinate with altitude or null if invalid.
 */
fun String.toLatLngAltitudeOrNull(): LatLngAltitude? {
    val parts = this.split(":").map { it.trim() }
    if (parts.size < 2) return null
    val lat = parts[0].toDoubleOrNull()
    val lng = parts[1].toDoubleOrNull()
    val alt = if (parts.size >= 3) parts[2].toDoubleOrNull() else 0.0
    return if (lat != null && lng != null && alt != null) LatLngAltitude(lat, lng, alt) else null
}


/**
 * Parses an encoded polyline string into a List containing a single PolylineOptions object.
 * Assumes the input string 'this' is a valid encoded polyline compatible with
 * the assumed 'String.toLatLngList()' decoder function.
 *
 * @return A List containing one PolylineOptions object, or an empty list if
 * the input is blank, decoding fails, or the decoded polyline has fewer than 2 points.
 */
fun String.toPolyline(idp: String? = null): List<PolylineOptions> {
    val id = idp ?: UUID.randomUUID().toString()

    val encodedPolyline = this.trim()
    if (encodedPolyline.isBlank()) {
        Log.w(TAG, "Input polyline string is blank.")
        return emptyList()
    }

    // 1. Decode the encoded string using the assumed extension function
    val decodedLatLngs: List<LatLng> = try {
        // IMPORTANT: Replace this with the actual call to your decoder function if the import path is different
        encodedPolyline.toLatLngList()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to decode polyline string: '$encodedPolyline'", e)
        return emptyList()
    }

    // 2. Validate decoded points
    if (decodedLatLngs.size < 2) {
        Log.w(
            TAG,
            "Decoded polyline has fewer than 2 points (${decodedLatLngs.size}). Cannot create PolylineOptions."
        )
        return emptyList()
    }

    // 3. Convert LatLng to LatLngAltitude (assuming Clamp to Mesh behavior)
    // Altitude value is often ignored when using CLAMP_TO_GROUND or CLAMP_TO_MESH
    val points3d: List<LatLngAltitude> = decodedLatLngs.map { latLng ->
        latLngAltitude {
            latitude = latLng.latitude
            longitude = latLng.longitude
            altitude = 0.0 // Altitude typically ignored for clamping modes
        }
    }

    Log.w(TAG, "Decoded polyline with ${points3d.size} points.")
    Log.w(TAG, "Decoded polyline [$this]")

    val color = 0xff_0f_53_ff.toInt()

    // 4. Create PolylineOptions
    val polylineOptions = polylineOptions {
        this.id = id
        this.path = points3d
        strokeColor = color
        strokeWidth = 7.0
        altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        zIndex = 5
        outerColor = color
        outerWidth = 1.0
    }

    val polylineOptionsBackground = polylineOptions {
        this.id = id + "_background"
        this.path = points3d
        strokeColor = Color.argb(128, 0, 0, 0)
        strokeWidth = 13.0
        altitudeMode = AltitudeMode.CLAMP_TO_GROUND
        zIndex = 2
        outerColor = color
        outerWidth = 1.0
    }

    // 5. Return a list containing the single PolylineOptions
    Log.d(TAG, "Successfully created PolylineOptions with ${points3d.size} points.")
    return listOf(polylineOptions, polylineOptionsBackground)
}

// --- Helper Functions ---

@AltitudeMode
private fun parseAltitudeMode(altModeString: String): Int {
    return when (altModeString.lowercase()) {
        "absolute" -> AltitudeMode.ABSOLUTE
        "relative_to_ground" -> AltitudeMode.RELATIVE_TO_GROUND
        "relative_to_mesh" -> AltitudeMode.RELATIVE_TO_MESH
        "clamp_to_ground" -> AltitudeMode.CLAMP_TO_GROUND
        else -> {
            if (altModeString.isNotEmpty()) {
                Log.w(
                    TAG,
                    "Ignoring unrecognized altitude mode '$altModeString', defaulting to CLAMP_TO_GROUND",
                )
            }
            AltitudeMode.CLAMP_TO_GROUND
        }
    }
}
