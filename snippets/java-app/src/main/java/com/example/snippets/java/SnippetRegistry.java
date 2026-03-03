package com.example.snippets.java;

import com.example.snippets.java.snippets.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class SnippetRegistry {
    public static final Map<String, Snippet> snippets = new LinkedHashMap<>();

    static {
        registerSnippet("Map Initialization - Listeners",
                "Initializes a 3D map and logs events when the scene is ready (100% loaded) and steady (camera stopped moving).",
                (context, map) -> new MapInitSnippets().setupMapListeners(context, map));

        registerSnippet("Camera - Fly To",
                "Animates the camera to a specific position (Lat: 37.422, Lng: -122.084, Alt: 100m) with a 45-degree tilt and 90-degree heading over 5 seconds.",
                (context, map) -> new CameraControlSnippets(map).flyCameraToPosition());

        registerSnippet("Camera - Fly Around",
                "Rotates the camera 360 degrees around a specific location (Lat: 37.422, Lng: -122.084) over 10 seconds.",
                (context, map) -> new CameraControlSnippets(map).flyCameraAroundLocation());

        registerSnippet("Camera - Stop Animation",
                "Stops any currently running camera animation immediately.",
                (context, map) -> new CameraControlSnippets(map).stopAnimation());

        registerSnippet("Camera - Listen Events",
                "Logs camera change events to the console, printing the center coordinates as the camera moves.",
                (context, map) -> new CameraControlSnippets(map).listenToCameraEvents());

        registerSnippet("Markers - Basic",
                "Adds a standard marker at Lat: 37.422, Lng: -122.084, Alt: 10m.",
                (context, map) -> new MarkerSnippets(map).addBasicMarker());

        registerSnippet("Markers - Advanced",
                "Adds a 'Priority Marker' at Lat: 37.422, Lng: -122.084, Alt: 10m (Relative to Ground) that is extruded and collides with other markers.",
                (context, map) -> new MarkerSnippets(map).addAdvancedMarker());

        registerSnippet("Markers - Click",
                "Adds a marker at Lat: 37.42, Lng: -122.08 that logs a message when clicked.",
                (context, map) -> new MarkerSnippets(map).handleMarkerClick());

        registerSnippet("Polygons - Basic",
                "Draws a red polygon with a blue stroke around a small area near Lat: 37.42, Lng: -122.08.",
                (context, map) -> new PolygonSnippets(map).addBasicPolygon());

        registerSnippet("Polygons - Extruded",
                "Draws a semi-transparent red extruded polygon (height 50m) around a small area near Lat: 37.42, Lng: -122.08.",
                (context, map) -> new PolygonSnippets(map).addExtrudedPolygon());

        registerSnippet("Polylines - Basic",
                "Draws a thick red polyline connecting three points near Lat: 37.42, Lng: -122.08.",
                (context, map) -> new PolylineSnippets(map).addBasicPolyline());

        registerSnippet("Polylines - Styled",
                "Draws a magenta polyline with a green outline, extruded and following the ground curvature (geodesic), connecting two points.",
                (context, map) -> new PolylineSnippets(map).addStyledPolyline());

        registerSnippet("Models - Basic",
                "Loads a GLB model from a URL (https://example.com/model.glb) and places it clamped to the ground at Lat: 37.422, Lng: -122.084.",
                (context, map) -> new ModelSnippets(map).addBasicModel());

        registerSnippet("Models - Advanced",
                "Loads a GLB model from assets (my_model.glb), scales it by 2x, rotates it, and places it at 10m relative altitude.",
                (context, map) -> new ModelSnippets(map).addAdvancedModel());

        registerSnippet("Popovers - Marker",
                "Adds a 'Hello Popover!' text bubble anchored to a marker at Lat: 37.422, Lng: -122.084.",
                (context, map) -> new PopoverSnippets(context, map).addPopoverToMarker());

        registerSnippet("Popovers - Configured",
                "Adds an 'Info' popover anchored to a marker at [0,0] with auto-close enabled and auto-pan disabled.",
                (context, map) -> new PopoverSnippets(context, map).addConfiguredPopover());

        registerSnippet("Places - Listen Clicks",
                "Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI.",
                (context, map) -> new PlaceSnippets(map).listenToPlaceClicks());
    }

    private static void registerSnippet(String title, String description, SnippetAction action) {
        snippets.put(title, new Snippet(title, description, action));
    }
}
