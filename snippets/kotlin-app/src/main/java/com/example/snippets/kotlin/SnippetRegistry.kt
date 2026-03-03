package com.example.snippets.kotlin

import android.content.Context
import com.example.snippets.kotlin.snippets.CameraControlSnippets
import com.example.snippets.kotlin.snippets.MapInitSnippets
import com.example.snippets.kotlin.snippets.MarkerSnippets
import com.example.snippets.kotlin.snippets.ModelSnippets
import com.example.snippets.kotlin.snippets.PlaceSnippets
import com.example.snippets.kotlin.snippets.PolygonSnippets
import com.example.snippets.kotlin.snippets.PolylineSnippets
import com.example.snippets.kotlin.snippets.PopoverSnippets
import com.google.android.gms.maps3d.GoogleMap3D


data class Snippet(
    val title: String,
    val description: String,
    val action: (Context, GoogleMap3D) -> Unit
)

object SnippetRegistry {
    val snippets = mapOf(
        "Map Initialization - Listeners" to Snippet(
            "Map Initialization - Listeners",
            "Initializes a 3D map and logs events when the scene is ready (100% loaded) and steady (camera stopped moving).",
            { context, map -> MapInitSnippets().setupMapListeners(context, map) }
        ),
        
        "Camera - Fly To" to Snippet(
            "Camera - Fly To",
            "Animates the camera to a specific position (Lat: 37.422, Lng: -122.084, Alt: 100m) with a 45-degree tilt and 90-degree heading over 5 seconds.",
            { _, map -> CameraControlSnippets(map).flyCameraToPosition() }
        ),
        "Camera - Fly Around" to Snippet(
            "Camera - Fly Around",
            "Rotates the camera 360 degrees around a specific location (Lat: 37.422, Lng: -122.084) over 10 seconds.",
            { _, map -> CameraControlSnippets(map).flyCameraAroundLocation() }
        ),
        "Camera - Stop Animation" to Snippet(
            "Camera - Stop Animation",
            "Stops any currently running camera animation immediately.",
            { _, map -> CameraControlSnippets(map).stopAnimation() }
        ),
        "Camera - Listen Events" to Snippet(
            "Camera - Listen Events",
            "Logs camera change events to the console, printing the center coordinates as the camera moves.",
            { _, map -> CameraControlSnippets(map).listenToCameraEvents() }
        ),

        "Markers - Basic" to Snippet(
            "Markers - Basic",
            "Adds a standard marker at Lat: 37.422, Lng: -122.084, Alt: 10m.",
            { _, map -> MarkerSnippets(map).addBasicMarker() }
        ),
        "Markers - Advanced" to Snippet(
            "Markers - Advanced",
            "Adds a 'Priority Marker' at Lat: 37.422, Lng: -122.084, Alt: 10m (Relative to Ground) that is extruded and collides with other markers.",
            { _, map -> MarkerSnippets(map).addAdvancedMarker() }
        ),
        "Markers - Click" to Snippet(
            "Markers - Click",
            "Adds a marker at Lat: 37.42, Lng: -122.08 that logs a message when clicked.",
            { _, map -> MarkerSnippets(map).handleMarkerClick() }
        ),

        "Polygons - Basic" to Snippet(
            "Polygons - Basic",
            "Draws a red polygon with a blue stroke around a small area near Lat: 37.42, Lng: -122.08.",
            { _, map -> PolygonSnippets(map).addBasicPolygon() }
        ),
        "Polygons - Extruded" to Snippet(
            "Polygons - Extruded",
            "Draws a semi-transparent red extruded polygon (height 50m) around a small area near Lat: 37.42, Lng: -122.08.",
            { _, map -> PolygonSnippets(map).addExtrudedPolygon() }
        ),

        "Polylines - Basic" to Snippet(
            "Polylines - Basic",
            "Draws a thick red polyline connecting three points near Lat: 37.42, Lng: -122.08.",
            { _, map -> PolylineSnippets(map).addBasicPolyline() }
        ),
        "Polylines - Styled" to Snippet(
            "Polylines - Styled",
            "Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points.",
            { _, map -> PolylineSnippets(map).addStyledPolyline() }
        ),

        "Models - Basic" to Snippet(
            "Models - Basic",
            "Loads a GLB model from a URL (https://example.com/model.glb) and places it clamped to the ground at Lat: 37.422, Lng: -122.084.",
            { _, map -> ModelSnippets(map).addBasicModel() }
        ),
        "Models - Advanced" to Snippet(
            "Models - Advanced",
            "Loads a GLB model from assets (my_model.glb), scales it by 2x, rotates it, and places it at 10m relative altitude.",
            { _, map -> ModelSnippets(map).addAdvancedModel() }
        ),

        "Popovers - Marker" to Snippet(
            "Popovers - Marker",
            "Adds a 'Hello Popover!' text bubble anchored to a marker at Lat: 37.422, Lng: -122.084.",
            { context, map -> PopoverSnippets(context, map).addPopoverToMarker() }
        ),
        "Popovers - Configured" to Snippet(
            "Popovers - Configured",
            "Adds an 'Info' popover anchored to a marker at [0,0] with auto-close enabled and auto-pan disabled.",
            { context, map -> PopoverSnippets(context, map).addConfiguredPopover() }
        ),
        
        "Places - Listen Clicks" to Snippet(
            "Places - Listen Clicks",
            "Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI.",
            { _, map -> PlaceSnippets(map).listenToPlaceClicks() }
        )
    )
}
