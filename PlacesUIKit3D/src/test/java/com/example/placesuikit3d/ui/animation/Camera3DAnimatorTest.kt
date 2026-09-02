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

package com.example.placesuikit3d.ui.animation

import com.example.placesuikit3d.data.model.PlaceSearchResult
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [Camera3DAnimator] mathematical algorithms.
 */
class Camera3DAnimatorTest {

    @Test
    fun calculateCentroid_singlePlace_returnsExactLocation() {
        val place = PlaceSearchResult(
            id = "p1",
            name = "Place 1",
            location = latLngAltitude {
                latitude = 40.0
                longitude = -105.0
                altitude = 1500.0
            },
        )

        val centroid = Camera3DAnimator.calculateCentroid(listOf(place))
        assertThat(centroid.latitude).isEqualTo(40.0)
        assertThat(centroid.longitude).isEqualTo(-105.0)
        assertThat(centroid.altitude).isEqualTo(1500.0)
    }

    @Test
    fun calculateCentroid_multiplePlaces_computesAverageCoordinates() {
        val p1 = PlaceSearchResult(
            id = "p1",
            name = "Place 1",
            location = latLngAltitude {
                latitude = 39.0
                longitude = -105.0
                altitude = 1000.0
            },
        )
        val p2 = PlaceSearchResult(
            id = "p2",
            name = "Place 2",
            location = latLngAltitude {
                latitude = 41.0
                longitude = -103.0
                altitude = 2000.0
            },
        )

        val centroid = Camera3DAnimator.calculateCentroid(listOf(p1, p2))
        assertThat(centroid.latitude).isEqualTo(40.0)
        assertThat(centroid.longitude).isEqualTo(-104.0)
        assertThat(centroid.altitude).isEqualTo(1500.0)
    }

    @Test
    fun calculateOptimalRange_spreadOutPlaces_scalesAppropriately() {
        val p1 = PlaceSearchResult(
            id = "p1",
            name = "Place 1",
            location = latLngAltitude {
                latitude = 39.99
                longitude = -105.28
                altitude = 1600.0
            },
        )
        val p2 = PlaceSearchResult(
            id = "p2",
            name = "Place 2",
            location = latLngAltitude {
                latitude = 40.08
                longitude = -105.22
                altitude = 1600.0
            },
        )

        val range = Camera3DAnimator.calculateOptimalRange(listOf(p1, p2))
        assertThat(range).isGreaterThan(1500.0)
        assertThat(range).isAtMost(15000.0)
    }

    @Test
    fun flyToTarget_whenMapIsNull_doesNotCrash() {
        val animator = Camera3DAnimator(googleMap3D = null)
        val target = com.example.placesuikit3d.data.model.Camera3DTarget.DEFAULT

        // Should safely queue target and not throw NullPointerException
        animator.flyToTarget(target)
        animator.stopOrbit()
    }
}
