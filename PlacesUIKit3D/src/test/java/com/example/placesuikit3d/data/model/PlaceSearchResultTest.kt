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

package com.example.placesuikit3d.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [PlaceSearchResult].
 */
class PlaceSearchResultTest {

    @Test
    fun toCameraTarget_convertsLocationTo3DTarget() {
        val place = PlaceSearchResult(
            id = "test_1",
            name = "Test Flatirons",
            address = "Boulder, CO",
            location = latLngAltitude {
                latitude = 39.9880
                longitude = -105.2930
                altitude = 2100.0
            },
            rating = 4.9,
            userRatingsTotal = 4000,
            category = "🏛️ Landmarks",
        )

        val target = place.toCameraTarget(heading = 45.0, tilt = 60.0, range = 350.0)
        assertThat(target.latitude).isEqualTo(39.9880)
        assertThat(target.longitude).isEqualTo(-105.2930)
        assertThat(target.altitude).isEqualTo(2100.0)
        assertThat(target.heading).isEqualTo(45.0)
        assertThat(target.tilt).isEqualTo(60.0)
        assertThat(target.range).isEqualTo(350.0)
    }

    @Test
    fun fromPlace_mapsAttributesCorrectly() {
        val googlePlace = Place.builder()
            .setId("place_xyz")
            .setDisplayName("Pearl Street")
            .setFormattedAddress("Pearl St, Boulder")
            .setLocation(LatLng(40.0177, -105.2819))
            .setRating(4.8)
            .setUserRatingCount(1200)
            .build()

        val result = PlaceSearchResult.fromPlace(googlePlace, category = "🏛️ Landmarks", isSelected = true)
        assertThat(result.id).isEqualTo("place_xyz")
        assertThat(result.name).isEqualTo("Pearl Street")
        assertThat(result.address).isEqualTo("Pearl St, Boulder")
        assertThat(result.location.latitude).isEqualTo(40.0177)
        assertThat(result.location.longitude).isEqualTo(-105.2819)
        assertThat(result.rating).isEqualTo(4.8)
        assertThat(result.userRatingsTotal).isEqualTo(1200)
        assertThat(result.category).isEqualTo("🏛️ Landmarks")
        assertThat(result.isSelected).isTrue()
    }

    @Test
    fun toCameraTarget_usesDefaultValuesCorrectly() {
        val place = PlaceSearchResult(
            id = "test_default",
            name = "Default Test",
            address = "San Francisco, CA",
            location = latLngAltitude {
                latitude = 37.7749
                longitude = -122.4194
                altitude = 0.0
            },
        )

        val target = place.toCameraTarget()
        assertThat(target.heading).isEqualTo(25.0)
        assertThat(target.tilt).isEqualTo(45.0)
        assertThat(target.range).isEqualTo(1000.0)
        assertThat(target.altitude).isEqualTo(0.0) // Resolves to datum for non-Colorado locations
    }

    @Test
    fun toCameraTarget_resolvesAltitudeForColoradoLocations() {
        val place = PlaceSearchResult(
            id = "test_co",
            name = "Colorado Spot",
            address = "Boulder, CO",
            location = latLngAltitude {
                latitude = 40.0150
                longitude = -105.2700
                altitude = 0.0
            },
        )

        val target = place.toCameraTarget()
        assertThat(target.altitude).isEqualTo(1650.0) // Resolves to Colorado plateau elevation
    }
}
