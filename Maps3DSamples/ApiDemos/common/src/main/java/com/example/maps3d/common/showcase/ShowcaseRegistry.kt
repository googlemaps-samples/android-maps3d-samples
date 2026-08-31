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

package com.example.maps3d.common.showcase

/**
 * Master catalog registry of all Google Maps 3D showcase features.
 *
 * Categorizes samples into Beginner, Intermediate, and Advanced tiers,
 * and maintains mappings to their respective Compose, Kotlin Views, and Java Views activities.
 */
object ShowcaseRegistry {

    val SAMPLES: List<ShowcaseSample> = listOf(
        // ==========================================
        // 🟢 BEGINNER TIER
        // ==========================================
        ShowcaseSample(
            id = "hello_map",
            title = "Hello 3D Map",
            subtitle = "Map instantiation, lifecycle management, and initial camera positioning.",
            tier = SampleTier.BEGINNER,
            tags = listOf("#basics", "#init", "#lifecycle", "#quickstart"),
            iconEmoji = "📍",
            composeActivity = "com.example.composedemos.hellomap.HelloMapActivity",
            kotlinActivity = "com.example.maps3dkotlin.hellomap.HelloMapActivity",
            javaActivity = "com.example.maps3djava.hellomap.HelloMapActivity",
        ),
        ShowcaseSample(
            id = "roadmap_mode",
            title = "Map Modes",
            subtitle = "Toggle dynamically between Satellite, Hybrid, and Roadmap 3D rendering modes.",
            tier = SampleTier.BEGINNER,
            tags = listOf("#modes", "#satellite", "#roadmap", "#hybrid", "#styling"),
            iconEmoji = "🛰️",
            composeActivity = "com.example.composedemos.roadmapmode.RoadmapModeActivity",
            kotlinActivity = "com.example.maps3dkotlin.roadmapmode.RoadmapModeActivity",
            javaActivity = "com.example.maps3djava.roadmapmode.RoadmapModeActivity",
        ),
        ShowcaseSample(
            id = "camera_controls",
            title = "Camera Controls",
            subtitle = "Programmatic control of camera center coordinates, range, heading, tilt, and roll.",
            tier = SampleTier.BEGINNER,
            tags = listOf("#camera", "#tilt", "#heading", "#range", "#navigation"),
            iconEmoji = "🎥",
            composeActivity = "com.example.composedemos.cameracontrols.CameraControlsActivity",
            kotlinActivity = "com.example.maps3dkotlin.cameracontrols.CameraControlsActivity",
            javaActivity = "com.example.maps3djava.cameracontrols.CameraControlsActivity",
        ),
        ShowcaseSample(
            id = "camera_restrictions",
            title = "Camera Restrictions",
            subtitle = "Enforce bounding boxes, min/max altitude bounds, and clamped heading/tilt angles.",
            tier = SampleTier.BEGINNER,
            tags = listOf("#camera", "#bounds", "#clamping", "#restrictions"),
            iconEmoji = "🔒",
            composeActivity = "com.example.composedemos.camerarestrictions.CameraRestrictionsActivity",
            kotlinActivity = "com.example.maps3dkotlin.camerarestrictions.CameraRestrictionsActivity",
            javaActivity = "com.example.maps3djava.camerarestrictions.CameraRestrictionsActivity",
        ),

        // ==========================================
        // 🟡 INTERMEDIATE TIER
        // ==========================================
        ShowcaseSample(
            id = "markers",
            title = "3D Markers",
            subtitle = "Place and style 3D markers with altitude modes, drag listeners, and custom icons.",
            tier = SampleTier.INTERMEDIATE,
            tags = listOf("#markers", "#pins", "#altitude", "#overlays", "#interaction"),
            iconEmoji = "🏷️",
            composeActivity = "com.example.composedemos.markers.MarkersActivity",
            kotlinActivity = "com.example.maps3dkotlin.markers.MarkersActivity",
            javaActivity = "com.example.maps3djava.markers.MarkersActivity",
        ),
        ShowcaseSample(
            id = "polygons",
            title = "Extruded Polygons",
            subtitle = "Render 2D surfaces and extruded 3D volumetric buildings with colors and holes.",
            tier = SampleTier.INTERMEDIATE,
            tags = listOf("#polygons", "#extrusion", "#3d-buildings", "#geometry"),
            iconEmoji = "🔷",
            composeActivity = "com.example.composedemos.polygons.PolygonsActivity",
            kotlinActivity = "com.example.maps3dkotlin.polygons.PolygonsActivity",
            javaActivity = "com.example.maps3djava.polygons.PolygonsActivity",
        ),
        ShowcaseSample(
            id = "polylines",
            title = "Polylines & Paths",
            subtitle = "Draw flat, extruded, and terrain-clamped geodesic polylines with custom strokes.",
            tier = SampleTier.INTERMEDIATE,
            tags = listOf("#polylines", "#paths", "#geodesic", "#strokes", "#altitude"),
            iconEmoji = "〰️",
            composeActivity = "com.example.composedemos.polylines.PolylinesActivity",
            kotlinActivity = "com.example.maps3dkotlin.polylines.PolylinesActivity",
            javaActivity = "com.example.maps3djava.polylines.PolylinesActivity",
        ),
        ShowcaseSample(
            id = "models",
            title = "3D Models (GLB)",
            subtitle = "Load and position 3D GLB mesh assets with scale, roll/pitch/yaw orientation.",
            tier = SampleTier.INTERMEDIATE,
            tags = listOf("#models", "#glb", "#3d-mesh", "#orientation", "#ufo"),
            iconEmoji = "🛸",
            composeActivity = "com.example.composedemos.models.ModelsActivity",
            kotlinActivity = "com.example.maps3dkotlin.models.ModelsActivity",
            javaActivity = "com.example.maps3djava.models.ModelsActivity",
        ),
        ShowcaseSample(
            id = "popovers",
            title = "Popovers & Info Windows",
            subtitle = "Anchor rich HTML/View popovers to 3D markers with auto-close and pan behaviors.",
            tier = SampleTier.INTERMEDIATE,
            tags = listOf("#popovers", "#infowindow", "#html", "#ui", "#anchors"),
            iconEmoji = "💬",
            composeActivity = "com.example.composedemos.popovers.PopoversActivity",
            kotlinActivity = "com.example.maps3dkotlin.popovers.PopoversActivity",
            javaActivity = "com.example.maps3djava.popovers.PopoversActivity",
        ),

        // ==========================================
        // 🔴 ADVANCED TIER
        // ==========================================
        ShowcaseSample(
            id = "advanced_camera",
            title = "Cinematic Camera Tours",
            subtitle = "Orchestrate multi-keyframe orbital camera tours with heading & tilt interpolations.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#cinematic", "#camera", "#animation", "#keyframe", "#orbit", "#tour"),
            iconEmoji = "🎢",
            composeActivity = "com.example.composedemos.advancedcameraanimation.AdvancedCameraAnimationActivity",
            kotlinActivity = "com.example.maps3dkotlin.advancedcameraanimation.AdvancedCameraAnimationActivity",
            javaActivity = "com.example.maps3djava.advancedcameraanimation.AdvancedCameraAnimationActivity",
        ),
        ShowcaseSample(
            id = "path_following",
            title = "Path Following (VSYNC)",
            subtitle = "Interactive flight simulation with VSYNC Choreographer, speed multipliers & gestures.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#physics", "#path-following", "#choreographer", "#vsync", "#flight", "#gestures"),
            iconEmoji = "🏎️",
            composeActivity = "com.example.composedemos.pathfollowing.PathFollowingActivity",
            kotlinActivity = "com.example.maps3dkotlin.pathfollowing.PathFollowingActivity",
            javaActivity = "com.example.maps3djava.pathfollowing.PathFollowingActivity",
        ),
        ShowcaseSample(
            id = "data_visualization",
            title = "Real-Time Data Viz",
            subtitle = "Simulate rising water levels and regional flood zones using extruded 3D polygons.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#data-viz", "#flood-fill", "#extrusion", "#elevation", "#simulation"),
            iconEmoji = "🌊",
            composeActivity = "com.example.composedemos.datavisualization.DataVisualizationActivity",
            kotlinActivity = "com.example.maps3dkotlin.datavisualization.DataVisualizationActivity",
            javaActivity = "com.example.maps3djava.datavisualization.DataVisualizationActivity",
        ),
        ShowcaseSample(
            id = "field_of_view",
            title = "Dynamic FOV Lens",
            subtitle = "Manipulate camera lens field-of-view perspective from telephoto zoom to wide-angle.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#fov", "#field-of-view", "#lens", "#perspective", "#zoom"),
            iconEmoji = "🔭",
            composeActivity = "com.example.composedemos.fieldofview.FieldOfViewActivity",
            kotlinActivity = "com.example.maps3dkotlin.fieldofview.FieldOfViewActivity",
            javaActivity = "com.example.maps3djava.fieldofview.FieldOfViewActivity",
        ),
        ShowcaseSample(
            id = "routes",
            title = "Routes API Navigation",
            subtitle = "Compute driving routes via Google Routes API and visualize 3D path corridors.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#routes", "#directions", "#navigation", "#turn-by-turn", "#polyline"),
            iconEmoji = "🛣️",
            composeActivity = "com.example.composedemos.routes.RoutesActivity",
            kotlinActivity = "com.example.maps3dkotlin.routes.RoutesActivity",
            javaActivity = "com.example.maps3djava.routes.RoutesActivity",
        ),
        ShowcaseSample(
            id = "flight_simulator",
            title = "Flight Simulator",
            subtitle = "Interactive first-person flight controls over high-resolution 3D photorealistic mesh.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#flight-simulator", "#physics", "#cockpit", "#3d-controls"),
            iconEmoji = "✈️",
            composeActivity = "com.example.composedemos.flightsimulator.FlightSimulatorActivity",
            kotlinActivity = "com.example.maps3dkotlin.flightsimulator.FlightSimulatorActivity",
            javaActivity = "com.example.maps3djava.flightsimulator.FlightSimulatorActivity",
        ),
        ShowcaseSample(
            id = "animating_models",
            title = "Animating Models",
            subtitle = "Real-time coordinate translations, heading rotations, and speed animations for 3D GLB models.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#models", "#animation", "#glb", "#ufo", "#movement"),
            iconEmoji = "🛸",
            composeActivity = "com.example.composedemos.animatingmodels.AnimatingModelsActivity",
            kotlinActivity = "com.example.maps3dkotlin.animatingmodels.AnimatingModelsActivity",
            javaActivity = "com.example.maps3djava.animatingmodels.AnimatingModelsActivity",
        ),
        ShowcaseSample(
            id = "map_interactions",
            title = "Gestures & Place Clicks",
            subtitle = "Tap listeners on 3D buildings, POIs, and custom touch gesture delegates.",
            tier = SampleTier.ADVANCED,
            tags = listOf("#gestures", "#poi", "#place-click", "#touch", "#listeners"),
            iconEmoji = "👆",
            composeActivity = "com.example.composedemos.mapinteractions.MapInteractionsActivity",
            kotlinActivity = "com.example.maps3dkotlin.mapinteractions.MapInteractionsActivity",
            javaActivity = "com.example.maps3djava.mapinteractions.MapInteractionsActivity",
        ),
    )

    fun getSampleById(id: String): ShowcaseSample? = SAMPLES.find { it.id == id }

    fun filter(
        framework: FrameworkType,
        query: String = "",
        tier: SampleTier? = null,
        tag: String? = null,
    ): List<ShowcaseSample> {
        return SAMPLES.filter { sample ->
            sample.isAvailable(framework) &&
                (tier == null || sample.tier == tier) &&
                (tag == null || sample.tags.any { it.equals(tag, ignoreCase = true) }) &&
                (query.isBlank() ||
                    sample.title.contains(query, ignoreCase = true) ||
                    sample.subtitle.contains(query, ignoreCase = true) ||
                    sample.tags.any { it.contains(query, ignoreCase = true) })
        }
    }

    fun allTags(): List<String> =
        SAMPLES.flatMap { it.tags }.distinct().sorted()
}
