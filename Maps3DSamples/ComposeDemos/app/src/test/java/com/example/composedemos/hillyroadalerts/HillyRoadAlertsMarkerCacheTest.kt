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

package com.example.composedemos.hillyroadalerts

import com.example.maps3d.common.ExtremaType
import com.example.maps3d.common.TerrainExtremaPoint
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.maps.android.compose3d.MarkerConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [syncExtremaMarkerConfigs] in [HillyRoadAlertsActivity].
 */
@RunWith(RobolectricTestRunner::class)
class HillyRoadAlertsMarkerCacheTest {

    @Test
    fun syncExtremaMarkerConfigs_reusesMarkerConfigAcrossFrameTicksAndEvictsExitedExtrema() {
        val cache = mutableMapOf<String, MarkerConfig>()
        val crestPos = LatLngAltitude(36.4865, -114.5320, 625.0)
        val dipPos = LatLngAltitude(36.4872, -114.5310, 610.0)

        // Frame 1: crest_10 at 200m ahead, dip_18 at 340m ahead
        val frame1Upcoming = listOf(
            TerrainExtremaPoint(
                id = "crest_10",
                position = crestPos,
                type = ExtremaType.CREST,
                distanceAheadMeters = 200.0,
                gradeDeltaPercent = 16.0,
                approachGradePercent = 8.0,
            ),
            TerrainExtremaPoint(
                id = "dip_18",
                position = dipPos,
                type = ExtremaType.DIP,
                distanceAheadMeters = 340.0,
                gradeDeltaPercent = -14.0,
                approachGradePercent = -7.0,
            ),
        )

        val frame1Markers = syncExtremaMarkerConfigs(
            upcoming = frame1Upcoming,
            cache = cache,
            crestFormat = "Crest (%1$+.0f%%)",
            dipFormat = "Dip (%1$+.0f%%)",
            onMarkerClicked = { _, _, _, _ -> },
        )
        assertEquals(2, frame1Markers.size)
        assertEquals("Crest (+8%)", frame1Markers[0].label)
        assertEquals("Dip (-7%)", frame1Markers[1].label)

        // Frame 2: vehicle advances 15m -> distanceAheadMeters changes, but IDs and positions match
        val frame2Upcoming = listOf(
            frame1Upcoming[0].copy(distanceAheadMeters = 185.0),
            frame1Upcoming[1].copy(distanceAheadMeters = 325.0),
        )
        val frame2Markers = syncExtremaMarkerConfigs(
            upcoming = frame2Upcoming,
            cache = cache,
            crestFormat = "Crest (%1$+.0f%%)",
            dipFormat = "Dip (%1$+.0f%%)",
            onMarkerClicked = { _, _, _, _ -> },
        )

        // Identical MarkerConfig instances must be reused so Map3DState.syncMarkers does not
        // recreate native 3D markers on every frame tick
        assertSame(frame1Markers[0], frame2Markers[0])
        assertSame(frame1Markers[1], frame2Markers[1])

        // Frame 3: crest_10 exits lookahead window; only dip_18 remains
        val frame3Upcoming = listOf(frame2Upcoming[1].copy(distanceAheadMeters = 120.0))
        val frame3Markers = syncExtremaMarkerConfigs(
            upcoming = frame3Upcoming,
            cache = cache,
            crestFormat = "Crest (%1$+.0f%%)",
            dipFormat = "Dip (%1$+.0f%%)",
            onMarkerClicked = { _, _, _, _ -> },
        )
        assertEquals(1, frame3Markers.size)
        assertSame(frame1Markers[1], frame3Markers[0])
        assertTrue("crest_10" !in cache)

        // Route switch with colliding ID ("dip_18") at a different 3D coordinate must replace cached config
        val newRoutePos = LatLngAltitude(37.8760, -122.5990, 95.0)
        val newRouteUpcoming = listOf(
            TerrainExtremaPoint(
                id = "dip_18",
                position = newRoutePos,
                type = ExtremaType.DIP,
                distanceAheadMeters = 210.0,
                gradeDeltaPercent = -18.0,
                approachGradePercent = -11.0,
            ),
        )
        val newRouteMarkers = syncExtremaMarkerConfigs(
            upcoming = newRouteUpcoming,
            cache = cache,
            crestFormat = "Crest (%1$+.0f%%)",
            dipFormat = "Dip (%1$+.0f%%)",
            onMarkerClicked = { _, _, _, _ -> },
        )
        assertEquals(1, newRouteMarkers.size)
        assertNotSame(frame1Markers[1], newRouteMarkers[0])
        assertEquals(newRoutePos, newRouteMarkers[0].position)
        assertEquals("Dip (-11%)", newRouteMarkers[0].label)
    }
}
