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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.snippets.java.TrackedMap3D;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnMap3DClickListener;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;

@SnippetGroup(
    title = "Places",
    description = "Snippets demonstrating Place (POI/Building) interaction algorithms."
)
public class PlaceSnippets {

    private final TrackedMap3D map;

    public PlaceSnippets(TrackedMap3D map) {
        this.map = map;
    }

    /**
     * Listens for clicks on 3D Places (buildings, POIs).
     */
    @SuppressWarnings("unused")
    @SnippetItem(
        title = "1. Listen Clicks",
        description = "Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI."
    )
    public void listenToPlaceClicks() {
        // [START maps_android_3d_place_click_java]
        map.setMap3DClickListener(new OnMap3DClickListener() {
            @Override
            public void onMap3DClick(@NonNull LatLngAltitude location, @Nullable String placeId) {
                if (placeId != null) {
                    // Handle place click
                }
            }
        });
        // [END maps_android_3d_place_click_java]
    }
}
