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

package com.example.maps3d.common.storelocator

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Filterable store amenities supported in the canonical 2D/3D Store Locator sample.
 */
enum class StoreAmenity(val label: String, val iconEmoji: String) {
    OPEN_NOW("Open Now", "🟢"),
    DRIVE_THRU("Drive-Thru", "🚗"),
    OPEN_24_HOURS("24 Hours", "🕒"),
    EV_CHARGING("EV Charging", "⚡"),
}

/**
 * Domain model representing a physical store location in San Francisco for the Store Locator sample.
 */
data class StoreLocation(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val hours: String,
    val isOpenNow: Boolean,
    val amenities: Set<StoreAmenity>,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val distanceKm: Double = computeDistanceKm(
        REFERENCE_ORIGIN_LAT,
        REFERENCE_ORIGIN_LNG,
        latitude,
        longitude,
    ),
) {
    /** Converts the store's geographic coordinates to a [LatLngAltitude]. */
    fun toLatLngAltitude(): LatLngAltitude =
        latLngAltitude {
            latitude = this@StoreLocation.latitude
            longitude = this@StoreLocation.longitude
            altitude = this@StoreLocation.altitude
        }

    /**
     * Builds the 3D detail camera (`tilt = 55.0`, `range = 650.0`) used when flying from the
     * 2D roadmap overview to inspect a specific store location.
     */
    fun toDetailCamera(): Camera =
        camera {
            center = toLatLngAltitude()
            heading = DETAIL_CAMERA_HEADING
            tilt = DETAIL_CAMERA_TILT
            roll = 0.0
            range = DETAIL_CAMERA_RANGE
        }.toValidCamera()

    /**
     * Builds the 2D low-tilt camera (`tilt = 15.0`, `range = 850.0`) used when inspecting a
     * selected store in 2D roadmap mode.
     */
    fun to2DCamera(): Camera =
        camera {
            center = toLatLngAltitude()
            heading = OVERVIEW_CAMERA_HEADING
            tilt = DETAIL_2D_CAMERA_TILT
            roll = 0.0
            range = DETAIL_2D_CAMERA_RANGE
        }.toValidCamera()

    /** Returns true if this store satisfies all [requiredAmenities]. */
    fun matchesAmenities(requiredAmenities: Set<StoreAmenity>): Boolean =
        amenities.containsAll(requiredAmenities)

    /** Formatted summary of amenities for cards and popovers. */
    fun formattedAmenities(): String =
        amenities.joinToString(" • ") { "${it.iconEmoji} ${it.label}" }

    /** Resolves the marker pin background color resource ID for this store instance. */
    @ColorRes
    fun getPinColorResId(isSelected: Boolean): Int =
        StoreLocation.getPinColorResId(isSelected, isOpenNow)

    /** Resolves the theme-aware marker pin background color for this store instance. */
    @ColorInt
    fun getPinBackgroundColor(context: Context, isSelected: Boolean): Int =
        StoreLocation.getPinBackgroundColor(context, isSelected, isOpenNow)

    /** Resolves the marker pin background color for this store instance under a specific theme mode. */
    @ColorInt
    fun getPinBackgroundColor(isDarkTheme: Boolean, isSelected: Boolean = false): Int =
        StoreLocation.getPinBackgroundColor(isSelected, isOpenNow, isDarkTheme)

    companion object {
        /** Marker pin color constants across light and dark themes. */
        const val COLOR_PIN_SELECTED_LIGHT = 0xFFF4B400.toInt()
        const val COLOR_PIN_SELECTED_DARK = 0xFFFDD663.toInt()
        const val COLOR_PIN_OPEN_LIGHT = 0xFF1A73E8.toInt()
        const val COLOR_PIN_OPEN_DARK = 0xFF8AB4F8.toInt()
        const val COLOR_PIN_CLOSED_LIGHT = 0xFF757575.toInt()
        const val COLOR_PIN_CLOSED_DARK = 0xFFBDC1C6.toInt()

        /**
         * Resolves the marker pin background color resource ID for a store based on its selection
         * and open status.
         */
        @JvmStatic
        @ColorRes
        fun getPinColorResId(isSelected: Boolean, isOpenNow: Boolean): Int = when {
            isSelected -> R.color.store_pin_selected
            isOpenNow -> R.color.store_pin_open
            else -> R.color.store_pin_closed
        }

        /**
         * Resolves the marker pin background color using theme-aware resources from the given [Context].
         */
        @JvmStatic
        @ColorInt
        fun getPinBackgroundColor(context: Context, isSelected: Boolean, isOpenNow: Boolean): Int =
            ContextCompat.getColor(context, getPinColorResId(isSelected, isOpenNow))

        /**
         * Resolves the marker pin background color for a given theme mode ([isDarkTheme]).
         */
        @JvmStatic
        @ColorInt
        fun getPinBackgroundColor(
            isSelected: Boolean,
            isOpenNow: Boolean,
            isDarkTheme: Boolean,
        ): Int = when {
            isSelected -> if (isDarkTheme) COLOR_PIN_SELECTED_DARK else COLOR_PIN_SELECTED_LIGHT
            isOpenNow -> if (isDarkTheme) COLOR_PIN_OPEN_DARK else COLOR_PIN_OPEN_LIGHT
            else -> if (isDarkTheme) COLOR_PIN_CLOSED_DARK else COLOR_PIN_CLOSED_LIGHT
        }
        /** Reference user origin at Union Square, San Francisco. */
        const val REFERENCE_ORIGIN_LAT = 37.7879
        const val REFERENCE_ORIGIN_LNG = -122.4075

        /** 2D top-down roadmap overview camera parameters. */
        const val OVERVIEW_CAMERA_TILT = 0.0
        const val OVERVIEW_CAMERA_RANGE = 4500.0
        const val OVERVIEW_CAMERA_HEADING = 0.0

        /** 3D perspective overview camera parameters. */
        const val OVERVIEW_3D_CAMERA_TILT = 55.0
        const val OVERVIEW_3D_CAMERA_RANGE = 4200.0
        const val OVERVIEW_3D_CAMERA_HEADING = 25.0

        /** 3D store inspection camera parameters when a store card or pin is selected. */
        const val DETAIL_CAMERA_TILT = 55.0
        const val DETAIL_CAMERA_RANGE = 650.0
        const val DETAIL_CAMERA_HEADING = 0.0

        /** 2D store inspection camera parameters when viewing a selected store in 2D mode. */
        const val DETAIL_2D_CAMERA_TILT = 0.0
        const val DETAIL_2D_CAMERA_RANGE = 850.0

        /**
         * Marker pin scale constants.
         */
        const val DEFAULT_PIN_SCALE = 0.8f
        const val SELECTED_PIN_SCALE = 1.0f

        /** Default 2D-style overview camera covering all San Francisco stores. */
        @JvmStatic
        val OVERVIEW_CAMERA: Camera =
            camera {
                center = latLngAltitude {
                    latitude = REFERENCE_ORIGIN_LAT
                    longitude = REFERENCE_ORIGIN_LNG
                    altitude = 0.0
                }
                heading = OVERVIEW_CAMERA_HEADING
                tilt = OVERVIEW_CAMERA_TILT
                roll = 0.0
                range = OVERVIEW_CAMERA_RANGE
            }.toValidCamera()

        /** 3D perspective overview camera covering all San Francisco stores. */
        @JvmStatic
        val OVERVIEW_CAMERA_3D: Camera =
            camera {
                center = latLngAltitude {
                    latitude = REFERENCE_ORIGIN_LAT
                    longitude = REFERENCE_ORIGIN_LNG
                    altitude = 0.0
                }
                heading = OVERVIEW_3D_CAMERA_HEADING
                tilt = OVERVIEW_3D_CAMERA_TILT
                roll = 0.0
                range = OVERVIEW_3D_CAMERA_RANGE
            }.toValidCamera()

        /**
         * Computes great-circle Haversine distance in kilometers between two coordinates.
         */
        @JvmStatic
        fun computeDistanceKm(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
        ): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadiusKm * c
        }

        /**
         * 8 curated San Francisco store locations sorted by distance from Union Square.
         */
        @JvmStatic
        val SAN_FRANCISCO_STORES: List<StoreLocation> = listOf(
            StoreLocation(
                id = "store_union_square",
                name = "Union Square Flagship",
                address = "300 Post St, San Francisco, CA",
                phone = "(415) 555-0101",
                hours = "Open 24 Hours",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                    StoreAmenity.OPEN_24_HOURS,
                    StoreAmenity.EV_CHARGING,
                ),
                latitude = 37.7886,
                longitude = -122.4071,
            ),
            StoreLocation(
                id = "store_market_street",
                name = "Market & 4th Express",
                address = "780 Market St, San Francisco, CA",
                phone = "(415) 555-0102",
                hours = "6:00 AM – 10:00 PM",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                ),
                latitude = 37.7859,
                longitude = -122.4052,
            ),
            StoreLocation(
                id = "store_financial_district",
                name = "Montgomery Station Hub",
                address = "101 Montgomery St, San Francisco, CA",
                phone = "(415) 555-0103",
                hours = "Open 24 Hours",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                    StoreAmenity.OPEN_24_HOURS,
                ),
                latitude = 37.7897,
                longitude = -122.4019,
            ),
            StoreLocation(
                id = "store_soma_ev_plaza",
                name = "SoMa EV & Drive-Thru Plaza",
                address = "450 Harrison St, San Francisco, CA",
                phone = "(415) 555-0104",
                hours = "Open 24 Hours",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                    StoreAmenity.DRIVE_THRU,
                    StoreAmenity.OPEN_24_HOURS,
                    StoreAmenity.EV_CHARGING,
                ),
                latitude = 37.7852,
                longitude = -122.3936,
            ),
            StoreLocation(
                id = "store_ferry_building",
                name = "Embarcadero Waterfront",
                address = "1 Ferry Building, San Francisco, CA",
                phone = "(415) 555-0105",
                hours = "7:00 AM – 8:00 PM",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                    StoreAmenity.EV_CHARGING,
                ),
                latitude = 37.7955,
                longitude = -122.3937,
            ),
            StoreLocation(
                id = "store_civic_center",
                name = "Van Ness Drive-Thru",
                address = "601 Van Ness Ave, San Francisco, CA",
                phone = "(415) 555-0106",
                hours = "5:30 AM – 11:00 PM",
                isOpenNow = true,
                amenities = setOf(
                    StoreAmenity.OPEN_NOW,
                    StoreAmenity.DRIVE_THRU,
                ),
                latitude = 37.7813,
                longitude = -122.4202,
            ),
            StoreLocation(
                id = "store_north_beach",
                name = "Columbus Ave Corner",
                address = "520 Columbus Ave, San Francisco, CA",
                phone = "(415) 555-0107",
                hours = "8:00 AM – 6:00 PM (Closed)",
                isOpenNow = false,
                amenities = setOf(
                    StoreAmenity.EV_CHARGING,
                ),
                latitude = 37.7998,
                longitude = -122.4095,
            ),
            StoreLocation(
                id = "store_mission_bay",
                name = "Mission Bay Drive-Thru & Charge",
                address = "1500 Owens St, San Francisco, CA",
                phone = "(415) 555-0108",
                hours = "7:00 AM – 7:00 PM (Closed)",
                isOpenNow = false,
                amenities = setOf(
                    StoreAmenity.DRIVE_THRU,
                    StoreAmenity.EV_CHARGING,
                ),
                latitude = 37.7689,
                longitude = -122.3928,
            ),
        ).sortedBy { it.distanceKm }
    }
}
