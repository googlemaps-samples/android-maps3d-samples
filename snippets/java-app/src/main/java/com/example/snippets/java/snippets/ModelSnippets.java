/*
 * Copyright 2025 Google LLC
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

import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Model;
import com.google.android.gms.maps3d.model.ModelOptions;
import com.google.android.gms.maps3d.model.Orientation;
import com.google.android.gms.maps3d.model.Vector3D;

public class ModelSnippets {

    private final GoogleMap3D map;

    public ModelSnippets(GoogleMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_model_add_java]
    /**
     * Adds a basic 3D model (GLB) to the map from a URL.
     */
    public void addBasicModel() {
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 0.0);
        
        ModelOptions options = new ModelOptions();
        options.setPosition(position);
        options.setUrl("https://example.com/model.glb");
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        
        Model model = map.addModel(options);
    }
    // [END maps_android_3d_model_add_java]

    // [START maps_android_3d_model_options_java]
    /**
     * Adds a 3D model with advanced configuration (scale, orientation).
     */
    public void addAdvancedModel() {
        LatLngAltitude position = new LatLngAltitude(37.4220, -122.0841, 10.0);

        ModelOptions options = new ModelOptions();
        options.setPosition(position);
        options.setUrl("file:///android_asset/my_model.glb");
        options.setScale(new Vector3D(2.0, 2.0, 2.0));
        options.setOrientation(new Orientation(0.0, 45.0, 0.0)); // heading, tilt, roll
        options.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        
        Model model = map.addModel(options);
    }
    // [END maps_android_3d_model_options_java]
}
