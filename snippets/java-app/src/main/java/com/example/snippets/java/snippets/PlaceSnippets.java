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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DClickListener;
import com.google.android.gms.maps3d.model.LatLngAltitude;

public class PlaceSnippets {

    private final GoogleMap3D map;

    public PlaceSnippets(GoogleMap3D map) {
        this.map = map;
    }

    // [START maps_android_3d_place_click_java]
    /**
     * Listens for clicks on 3D Places (buildings, POIs).
     */
    public void listenToPlaceClicks() {
        map.setMap3DClickListener(new OnMap3DClickListener() {
            @Override
            public void onMap3DClick(@NonNull LatLngAltitude location, @Nullable String placeId) {
                if (placeId != null) {
                    // Handle place click
                }
            }
        });
    }
    // [END maps_android_3d_place_click_java]
}
