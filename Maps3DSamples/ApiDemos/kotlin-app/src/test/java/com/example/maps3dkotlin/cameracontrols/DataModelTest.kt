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
import com.example.maps3d.common.toValidCamera
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataModelTest {

    @Test
    fun mtRainierCamera_relativeToGround_usesUnifiedClearanceAltitude() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND)

        assertThat(camera.center.latitude).isWithin(1e-6).of(DataModel.MT_RAINIER_LATITUDE)
        assertThat(camera.center.longitude).isWithin(1e-6).of(DataModel.MT_RAINIER_LONGITUDE)
        assertThat(camera.center.altitude).isWithin(1e-6).of(DataModel.RELATIVE_CLEARANCE_METERS)
        assertThat(camera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(camera.heading).isWithin(1e-6).of(DataModel.RELATIVE_GROUND_HEADING)
        assertThat(camera.tilt).isWithin(1e-6).of(DataModel.RELATIVE_GROUND_TILT)
        assertThat(camera.range).isWithin(1e-6).of(DataModel.MT_RAINIER_BASE_RANGE_METERS)
    }

    @Test
    fun mtRainierCamera_relativeToMesh_usesUnifiedMeshClearanceAboveSurface() {
        val groundCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND)
        val meshCamera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)

        assertThat(meshCamera.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS)
        assertThat(meshCamera.center.altitude).isGreaterThan(groundCamera.center.altitude)
        assertThat(meshCamera.range)
            .isWithin(1e-6)
            .of(DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR)
        assertThat(meshCamera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
    }

    @Test
    fun mtRainierCamera_absolute_usesSeaLevelSummitElevationAboveGround() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.ABSOLUTE)

        assertThat(camera.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS)
        assertThat(camera.range)
            .isWithin(1e-6)
            .of(DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR)
        assertThat(camera.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
    }

    @Test
    fun mtRainierCamera_clampToGround_anchorsTargetAtZeroGroundClearance() {
        val clampCamera = DataModel.mtRainierCamera(AltitudeMode.CLAMP_TO_GROUND)

        assertThat(clampCamera.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.CLAMP_ALTITUDE_METERS)
        assertThat(clampCamera.range).isWithin(1e-6).of(DataModel.MT_RAINIER_BASE_RANGE_METERS)
        assertThat(clampCamera.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
    }

    @Test
    fun esbCamera_and_beacon_shareUnifiedAltitudeModeLogic() {
        val groundEsb = DataModel.esbCamera(AltitudeMode.RELATIVE_TO_GROUND)
        val meshEsb = DataModel.esbCamera(AltitudeMode.RELATIVE_TO_MESH)
        val clampEsb = DataModel.esbCamera(AltitudeMode.CLAMP_TO_GROUND)
        val absoluteEsb = DataModel.esbCamera(AltitudeMode.ABSOLUTE)

        assertThat(groundEsb.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(groundEsb.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.RELATIVE_CLEARANCE_METERS)
        assertThat(groundEsb.range).isWithin(1e-6).of(DataModel.ESB_BASE_RANGE_METERS)

        assertThat(meshEsb.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(meshEsb.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS)
        assertThat(meshEsb.range)
            .isWithin(1e-6)
            .of(DataModel.ESB_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR)

        assertThat(clampEsb.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(clampEsb.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.CLAMP_ALTITUDE_METERS)
        assertThat(clampEsb.range).isWithin(1e-6).of(DataModel.ESB_BASE_RANGE_METERS)

        assertThat(absoluteEsb.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
        assertThat(absoluteEsb.center.altitude)
            .isWithin(1e-6)
            .of(DataModel.ESB_ABSOLUTE_ALTITUDE_METERS)
        assertThat(absoluteEsb.range)
            .isWithin(1e-6)
            .of(DataModel.ESB_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR)

        val esbMeshBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "ESB Spire (+360m DSM Mesh)",
            mode = AltitudeMode.RELATIVE_TO_MESH,
            refreshToken = 2,
        )
        assertThat(esbMeshBeacon.position.altitude)
            .isWithin(1e-6)
            .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS)
        assertThat(esbMeshBeacon.position.altitude)
            .isGreaterThan(DataModel.ESB_ABSOLUTE_ALTITUDE_METERS)
        assertThat(esbMeshBeacon.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(esbMeshBeacon.isExtruded).isTrue()

        val esbClampBeacon = DataModel.targetBeaconOptions(
            isMountain = false,
            label = "ESB Clamped (0m DTM)",
            mode = AltitudeMode.CLAMP_TO_GROUND,
            refreshToken = 1,
        )
        assertThat(esbClampBeacon.position.altitude)
            .isWithin(1e-6)
            .of(DataModel.CLAMP_ALTITUDE_METERS)
        assertThat(esbClampBeacon.altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)
        assertThat(esbClampBeacon.isExtruded).isFalse()
        assertThat(esbClampBeacon.id).isNotEqualTo(esbMeshBeacon.id)
    }

    @Test
    fun mtRainierBeaconOptions_configuresExtrudedSummitMarkerForAllModes() {
        val groundBeacon = DataModel.mtRainierBeaconOptions(
            label = "Mt. Rainier (+100m DTM Ground)",
            mode = AltitudeMode.RELATIVE_TO_GROUND,
        )
        assertThat(groundBeacon.position.latitude).isWithin(1e-6).of(DataModel.MT_RAINIER_LATITUDE)
        assertThat(groundBeacon.position.longitude).isWithin(1e-6).of(DataModel.MT_RAINIER_LONGITUDE)
        assertThat(groundBeacon.position.altitude).isWithin(1e-6).of(DataModel.RELATIVE_CLEARANCE_METERS)
        assertThat(groundBeacon.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND)
        assertThat(groundBeacon.isExtruded).isTrue()
        assertThat(groundBeacon.isDrawnWhenOccluded).isTrue()

        val meshBeacon = DataModel.mtRainierBeaconOptions(
            label = "Mt. Rainier (+150m DSM Mesh)",
            mode = AltitudeMode.RELATIVE_TO_MESH,
        )
        assertThat(meshBeacon.position.altitude)
            .isWithin(1e-6)
            .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS)
        assertThat(meshBeacon.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(meshBeacon.isExtruded).isTrue()

        val clampBeacon = DataModel.mtRainierBeaconOptions(
            label = "Mt. Rainier Clamped (0m DTM)",
            mode = AltitudeMode.CLAMP_TO_GROUND,
        )
        assertThat(clampBeacon.position.altitude).isWithin(1e-6).of(DataModel.CLAMP_ALTITUDE_METERS)
        assertThat(clampBeacon.altitudeMode).isEqualTo(AltitudeMode.CLAMP_TO_GROUND)
        assertThat(clampBeacon.isExtruded).isFalse()

        val absoluteBeacon = DataModel.mtRainierBeaconOptions(
            label = "Mt. Rainier (4,392m MSL Abs)",
            mode = AltitudeMode.ABSOLUTE,
        )
        assertThat(absoluteBeacon.position.altitude)
            .isWithin(1e-6)
            .of(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS)
        assertThat(absoluteBeacon.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
        assertThat(absoluteBeacon.isExtruded).isTrue()
    }

    @Test
    fun toValidCamera_and_copy_preserveAltitudeMode() {
        val camera = DataModel.mtRainierCamera(AltitudeMode.RELATIVE_TO_MESH)
        val validated = camera.toValidCamera()
        val copied = validated.copy(roll = 15.0)

        assertThat(validated.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(copied.altitudeMode).isEqualTo(AltitudeMode.RELATIVE_TO_MESH)
        assertThat(copied.roll).isWithin(1e-6).of(15.0)
    }
}
