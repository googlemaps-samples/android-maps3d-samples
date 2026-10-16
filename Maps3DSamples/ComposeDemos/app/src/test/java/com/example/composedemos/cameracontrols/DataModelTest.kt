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

package com.example.composedemos.cameracontrols

import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.maps.android.compose3d.utils.copy
import com.google.maps.android.compose3d.utils.toValidCamera
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataModelTest {

    @Test
    fun mtRainierCamera_relativeToGround_usesUnifiedClearanceAltitude() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND)

        assertEquals(DataModel.MT_RAINIER_LATITUDE, camera.center.latitude, 1e-6)
        assertEquals(DataModel.MT_RAINIER_LONGITUDE, camera.center.longitude, 1e-6)
        assertEquals(DataModel.RELATIVE_CLEARANCE_METERS, camera.center.altitude, 1e-6)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, camera.altitudeMode)
        assertEquals(DataModel.RELATIVE_GROUND_HEADING, camera.heading ?: 0.0, 1e-6)
        assertEquals(DataModel.RELATIVE_GROUND_TILT, camera.tilt ?: 0.0, 1e-6)
        assertEquals(DataModel.MT_RAINIER_BASE_RANGE_METERS, camera.range ?: 0.0, 1e-6)
    }

    @Test
    fun mtRainierCamera_relativeToMesh_usesUnifiedMeshClearanceAboveSurface() {
        val groundCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND)
        val meshCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)

        assertEquals(DataModel.RELATIVE_MESH_CLEARANCE_METERS, meshCamera.center.altitude, 1e-6)
        assertTrue(meshCamera.center.altitude > groundCamera.center.altitude)
        assertEquals(
            DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR,
            meshCamera.range ?: 0.0,
            1e-6,
        )
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, meshCamera.altitudeMode)
    }

    @Test
    fun mtRainierCamera_clampToGround_anchorsTargetAtZeroGroundClearance() {
        val clampCamera = DataModel.mtRainierCamera(AltitudeMode.CLAMP_TO_GROUND)

        assertEquals(DataModel.CLAMP_ALTITUDE_METERS, clampCamera.center.altitude, 1e-6)
        assertEquals(DataModel.MT_RAINIER_BASE_RANGE_METERS, clampCamera.range ?: 0.0, 1e-6)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, clampCamera.altitudeMode)
    }

    @Test
    fun mtRainierCamera_absolute_usesSeaLevelSummitElevationAboveGround() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.ABSOLUTE)

        assertEquals(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS, camera.center.altitude, 1e-6)
        assertEquals(
            DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR,
            camera.range ?: 0.0,
            1e-6,
        )
        assertEquals(AltitudeMode.ABSOLUTE, camera.altitudeMode)
    }

    @Test
    fun esbCamera_and_beacon_shareUnifiedAltitudeModeLogic() {
        val groundEsb = DataModel.esbCamera(AltitudeMode.RELATIVE_TO_GROUND)
        val meshEsb = DataModel.esbCamera(AltitudeMode.RELATIVE_TO_MESH)
        val clampEsb = DataModel.esbCamera(AltitudeMode.CLAMP_TO_GROUND)
        val absoluteEsb = DataModel.esbCamera(AltitudeMode.ABSOLUTE)

        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, groundEsb.altitudeMode)
        assertEquals(DataModel.RELATIVE_CLEARANCE_METERS, groundEsb.center.altitude, 1e-6)
        assertEquals(DataModel.ESB_BASE_RANGE_METERS, groundEsb.range ?: 0.0, 1e-6)

        assertEquals(AltitudeMode.RELATIVE_TO_MESH, meshEsb.altitudeMode)
        assertEquals(DataModel.RELATIVE_MESH_CLEARANCE_METERS, meshEsb.center.altitude, 1e-6)
        assertEquals(
            DataModel.ESB_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR,
            meshEsb.range ?: 0.0,
            1e-6,
        )

        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, clampEsb.altitudeMode)
        assertEquals(DataModel.CLAMP_ALTITUDE_METERS, clampEsb.center.altitude, 1e-6)
        assertEquals(DataModel.ESB_BASE_RANGE_METERS, clampEsb.range ?: 0.0, 1e-6)

        assertEquals(AltitudeMode.ABSOLUTE, absoluteEsb.altitudeMode)
        assertEquals(DataModel.ESB_ABSOLUTE_ALTITUDE_METERS, absoluteEsb.center.altitude, 1e-6)
        assertEquals(
            DataModel.ESB_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR,
            absoluteEsb.range ?: 0.0,
            1e-6,
        )

        val esbMeshBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "ESB Spire (+360m DSM Mesh)",
            mode = AltitudeMode.RELATIVE_TO_MESH,
            refreshToken = 2,
        )
        assertEquals(DataModel.RELATIVE_MESH_CLEARANCE_METERS, esbMeshBeacon.position.altitude, 1e-6)
        assertTrue(esbMeshBeacon.position.altitude > DataModel.ESB_ABSOLUTE_ALTITUDE_METERS)
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, esbMeshBeacon.altitudeMode)
        assertTrue(esbMeshBeacon.isExtruded)

        val esbClampBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "ESB Clamped (0m DTM)",
            mode = AltitudeMode.CLAMP_TO_GROUND,
            refreshToken = 1,
        )
        assertEquals(DataModel.CLAMP_ALTITUDE_METERS, esbClampBeacon.position.altitude, 1e-6)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, esbClampBeacon.altitudeMode)
        assertFalse(esbClampBeacon.isExtruded)
        assertNotEquals(esbClampBeacon.key, esbMeshBeacon.key)
    }

    @Test
    fun mtRainierBeaconConfig_configuresExtrudedSummitMarkerForAllModes() {
        val groundBeacon = DataModel.mtRainierBeaconConfig(
            label = "Mt. Rainier (+100m DTM Ground)",
            mode = AltitudeMode.RELATIVE_TO_GROUND,
        )
        assertEquals(DataModel.MT_RAINIER_LATITUDE, groundBeacon.position.latitude, 1e-6)
        assertEquals(DataModel.MT_RAINIER_LONGITUDE, groundBeacon.position.longitude, 1e-6)
        assertEquals(DataModel.RELATIVE_CLEARANCE_METERS, groundBeacon.position.altitude, 1e-6)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, groundBeacon.altitudeMode)
        assertTrue(groundBeacon.isExtruded)
        assertTrue(groundBeacon.isDrawnWhenOccluded)

        val meshBeacon = DataModel.mtRainierBeaconConfig(
            label = "Mt. Rainier (+150m DSM Mesh)",
            mode = AltitudeMode.RELATIVE_TO_MESH,
        )
        assertEquals(DataModel.RELATIVE_MESH_CLEARANCE_METERS, meshBeacon.position.altitude, 1e-6)
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, meshBeacon.altitudeMode)
        assertTrue(meshBeacon.isExtruded)

        val clampBeacon = DataModel.mtRainierBeaconConfig(
            label = "Mt. Rainier Clamped (0m DTM)",
            mode = AltitudeMode.CLAMP_TO_GROUND,
        )
        assertEquals(DataModel.CLAMP_ALTITUDE_METERS, clampBeacon.position.altitude, 1e-6)
        assertEquals(AltitudeMode.CLAMP_TO_GROUND, clampBeacon.altitudeMode)
        assertFalse(clampBeacon.isExtruded)

        val absoluteBeacon = DataModel.mtRainierBeaconConfig(
            label = "Mt. Rainier (4,392m MSL Abs)",
            mode = AltitudeMode.ABSOLUTE,
        )
        assertEquals(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS, absoluteBeacon.position.altitude, 1e-6)
        assertEquals(AltitudeMode.ABSOLUTE, absoluteBeacon.altitudeMode)
        assertTrue(absoluteBeacon.isExtruded)
    }

    @Test
    fun toValidCamera_and_copy_preserveAltitudeMode() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)
        val validated = camera.toValidCamera()
        val copied = validated.copy(roll = 15.0)

        assertEquals(AltitudeMode.RELATIVE_TO_MESH, validated.altitudeMode)
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, copied.altitudeMode)
        assertEquals(15.0, copied.roll ?: 0.0, 1e-6)
    }
}
