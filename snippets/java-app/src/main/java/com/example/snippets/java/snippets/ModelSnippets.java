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

package com.example.snippets.java.snippets;

import com.google.android.gms.maps3d.model.AltitudeMode;
import com.example.snippets.java.TrackedMap3D;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Models",
    description = "Snippets demonstrating 3D Model (GLB) integration and configuration."
)
public class ModelSnippets {

    public static final String SAUCER_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb";

    private final TrackedMap3D map;

    public ModelSnippets(TrackedMap3D map) {
        this.map = map;
    }

    /**
     * Adds a basic 3D model (GLB) to the map from a URL.
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "1. Basic",
        description = "Loads a GLB model from a URL and places it clamped to the ground."
    )
    public void addBasicModel() {
        // [START maps_android_3d_model_add_java]
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 100.0);
        
        ModelOptions options = new ModelOptions();
        options.setPosition(position);
        options.setUrl(SAUCER_URL);
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);
        options.setOrientation(new Orientation(0.0, 90.0, 0.0)); // heading, tilt, roll
        options.setScale(new Vector3D(10.0, 10.0, 10.0));
        
        Model model = map.addModel(options);
        // [END maps_android_3d_model_add_java]

        // Position camera to see the model
        Camera targetCamera = new Camera(position, 0.0, 45.0, 0.0, 300.0);
        FlyToOptions flyToOptions = new FlyToOptions(targetCamera, 2000L);
        map.flyCameraTo(flyToOptions);
    }
}
