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

import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.libraries.places.api.model.Place

/**
 * Domain model representing a Place search result with 3D geographic coordinates.
 *
 * @property id Unique Place ID
 * @property name Display name of the place
 * @property address Formatted street address or descriptive location
 * @property location 3D coordinates (latitude, longitude, altitude)
 * @property rating Rating score (0.0 to 5.0)
 * @property userRatingsTotal Total count of user reviews
 * @property types List of place type categories (e.g., restaurant, museum)
 * @property category UI category group (e.g., Landmarks, Cafes, Hotels)
 * @property isSelected Whether this result is currently selected on the map/UI
 */
data class PlaceSearchResult(
    val id: String,
    val name: String,
    val address: String? = null,
    val location: LatLngAltitude,
    val rating: Double? = null,
    val userRatingsTotal: Int? = null,
    val types: List<String> = emptyList(),
    val category: String? = null,
    val isSelected: Boolean = false,
) {
    /**
     * Converts to a [Camera3DTarget] focused on this place with cinematic 3D tilt.
     */
    fun toCameraTarget(
        heading: Double = 25.0,
        tilt: Double = 45.0,
        range: Double = 1000.0,
    ): Camera3DTarget {
        val safeAltitude = when {
            location.altitude > 0.0 -> location.altitude
            location.latitude in 39.0..41.0 && location.longitude in -106.0..-104.0 -> 1650.0
            else -> 0.0
        }
        return Camera3DTarget(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = safeAltitude,
            heading = heading,
            tilt = tilt,
            range = range,
        )
    }

    companion object {
        private val KNOWN_ALTITUDES = mapOf(
            "ChIJfXOTtWbsa4cRmW07qJRB6_8" to 2100.0, // The Flatirons
            "ChIJwd_EEkfsa4cRqy6eShKXFXY" to 1750.0, // Chautauqua Park
            "ChIJk7Q30j_sa4cR6-4G6_0Qv6c" to 1630.0, // Boulder Dushanbe Teahouse
            "ChIJd74X4z_sa4cR7qR1hHq1m9s" to 1625.0, // Pearl Street Mall
            "ChIJ5f9Z8j_sa4cR6L-QJ_qX5wE" to 1625.0, // St Julien Hotel & Spa
            "ChIJ3_0QzD_sa4cRw6xQ3_q8w9A" to 1630.0, // Avanti Food & Beverage
            "ChIJ9Q_e0D_sa4cR_q6L6LqX5wE" to 1630.0, // Ozo Coffee Company
            "ChIJ-Q_e0D_sa4cR_q6L6LqX5wE" to 1630.0, // Boxcar Coffee Roasters
            "ChIJ7Q_e0D_sa4cR_q6L6LqX5wE" to 1630.0, // Wonder Press
        )

        /**
         * Builds a [PlaceSearchResult] from a Google Places SDK [Place] object.
         */
        fun fromPlace(place: Place, category: String? = null, isSelected: Boolean = false): PlaceSearchResult {
            val locationLatLng = place.location
            val lat = locationLatLng?.latitude ?: 39.9880
            val lng = locationLatLng?.longitude ?: -105.2930
            val name = place.displayName ?: "Unknown Place"

            val resolvedAltitude = KNOWN_ALTITUDES[place.id] ?: when {
                lat in 39.0..41.0 && lng in -106.0..-104.0 -> 1650.0
                else -> 0.0
            }

            return PlaceSearchResult(
                id = place.id ?: "",
                name = name,
                address = place.formattedAddress,
                location = latLngAltitude {
                    latitude = lat
                    longitude = lng
                    altitude = resolvedAltitude
                },
                rating = place.rating,
                userRatingsTotal = place.userRatingCount,
                types = place.placeTypes ?: emptyList(),
                category = category,
                isSelected = isSelected,
            )
        }
    }
}
