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

package com.example.snippets.kotlin.snippets

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.OnMap3DClickListener
import com.google.android.gms.maps3d.model.LatLngAltitude

class PlaceSnippets(private val map: GoogleMap3D) {


    // [START maps_android_3d_place_click_kt]
    /**
     * Listens for clicks on 3D Places (buildings, POIs).
     */
    fun listenToPlaceClicks() {
        map.setMap3DClickListener(object : OnMap3DClickListener {
            override fun onMap3DClick(location: LatLngAltitude, placeId: String?) {
                if (placeId != null) {
                    // Handle place click
                }
            }
        })
    }
    // [END maps_android_3d_place_click_kt]
}
