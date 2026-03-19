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

import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem

@SnippetGroup(
    title = "Places",
    description = "Snippets demonstrating Place (POI/Building) interaction algorithms."
)
class PlaceSnippets(private val map: TrackedMap3D) {
    /**
     * Listens for clicks on 3D Places (buildings, POIs).
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Listen Clicks",
        description = "Sets up a listener that logs the Place ID when a user clicks on a 3D building or POI."
    )
    fun listenToPlaceClicks() {
        // [START maps_android_3d_place_click_kt]
        map.setMap3DClickListener { location, placeId ->
            if (placeId != null) {
                // Handle place click
            }
        }
        // [END maps_android_3d_place_click_kt]
    }
}
