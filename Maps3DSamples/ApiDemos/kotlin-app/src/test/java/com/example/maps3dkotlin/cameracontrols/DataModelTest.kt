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

package com.example.maps3dkotlin.cameracontrols

import com.example.maps3d.common.copy
import com.example.maps3d.common.isValidCameraAltitudeMode
import com.example.maps3d.common.toValidCamera
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.common.truth.Truth.assertThat
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

        assertThat(groundCamera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(meshCamera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(clampCamera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(clampCamera.center.altitude).isWithin(1e-6).of(0.0)
        assertThat(absoluteCamera.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
    }

    @Test
    fun targetBeaconOptions_mapsAllFourModes_andExtrudesOnlyNonClampedModes() {
        val groundBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "Ground",
            mode = AltitudeMode.RELATIVE_TO_GROUND,
        )
        val meshBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "Mesh",
            mode = AltitudeMode.RELATIVE_TO_MESH,
        )
        val clampBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "Clamp",
            mode = AltitudeMode.CLAMP_TO_GROUND,
        )
        val absoluteBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "Absolute",
            mode = AltitudeMode.ABSOLUTE,
        )

        assertThat(groundBeacon.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(groundBeacon.isExtruded).isTrue()

        assertThat(meshBeacon.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(meshBeacon.isExtruded).isTrue()

        assertThat(clampBeacon.altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)
        assertThat(clampBeacon.isExtruded).isFalse()

        assertThat(absoluteBeacon.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
        assertThat(absoluteBeacon.isExtruded).isTrue()
    }

    @Test
    fun toValidCamera_and_copy_preserveSupportedCameraAltitudeModes() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)
        val validated = camera.toValidCamera()
        val copied = validated.copy(roll = 15.0)

        assertThat(isValidCameraAltitudeMode(AltitudeMode.RELATIVE_TO_MESH)).isTrue()
        assertThat(isValidCameraAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)).isFalse()
        assertThat(validated.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(copied.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(copied.roll).isWithin(1e-6).of(15.0)
    }
}
