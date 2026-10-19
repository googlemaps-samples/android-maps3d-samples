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
import com.google.maps.android.compose3d.utils.isValidCameraAltitudeMode
import com.google.maps.android.compose3d.utils.toValidCamera
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataModelTest {

    @Test
    fun targetCamera_mapsSupportedModes_andFallsBackForClampToGround() {
        val groundCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND)
        val meshCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)
        val clampCamera = DataModel.mtRainierCamera(AltitudeMode.CLAMP_TO_GROUND)
        val absoluteCamera = DataModel.mtRainierCamera(AltitudeMode.ABSOLUTE)

        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, groundCamera.altitudeMode)
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, meshCamera.altitudeMode)
        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, clampCamera.altitudeMode)
        assertEquals(0.0, clampCamera.center.altitude, 1e-6)
        assertEquals(AltitudeMode.ABSOLUTE, absoluteCamera.altitudeMode)
    }

    @Test
    fun targetBeaconConfig_mapsAllFourModes_andExtrudesOnlyNonClampedModes() {
        val groundBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "Ground",
            mode = AltitudeMode.RELATIVE_TO_GROUND,
        )
        val meshBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "Mesh",
            mode = AltitudeMode.RELATIVE_TO_MESH,
        )
        val clampBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "Clamp",
            mode = AltitudeMode.CLAMP_TO_GROUND,
        )
        val absoluteBeacon = DataModel.targetBeaconConfig(
            isMountain = false,
            label = "Absolute",
            mode = AltitudeMode.ABSOLUTE,
        )

        assertEquals(AltitudeMode.RELATIVE_TO_GROUND, groundBeacon.altitudeMode)
        assertTrue(groundBeacon.isExtruded)

        assertEquals(AltitudeMode.RELATIVE_TO_MESH, meshBeacon.altitudeMode)
        assertTrue(meshBeacon.isExtruded)

        assertEquals(AltitudeMode.CLAMP_TO_GROUND, clampBeacon.altitudeMode)
        assertFalse(clampBeacon.isExtruded)

        assertEquals(AltitudeMode.ABSOLUTE, absoluteBeacon.altitudeMode)
        assertTrue(absoluteBeacon.isExtruded)
    }

    @Test
    fun toValidCamera_and_copy_preserveSupportedCameraAltitudeModes() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)
        val validated = camera.toValidCamera()
        val copied = validated.copy(roll = 15.0)

        assertTrue(isValidCameraAltitudeMode(AltitudeMode.RELATIVE_TO_MESH))
        assertFalse(isValidCameraAltitudeMode(AltitudeMode.CLAMP_TO_GROUND))
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, validated.altitudeMode)
        assertEquals(AltitudeMode.RELATIVE_TO_MESH, copied.altitudeMode)
        assertEquals(15.0, copied.roll ?: 0.0, 1e-6)
    }
}
