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

package com.example.maps3djava.cameracontrols;

import static com.example.maps3d.common.UtilitiesKt.copy;
import static com.example.maps3d.common.UtilitiesKt.toValidCamera;
import static com.google.common.truth.Truth.assertThat;

import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.MarkerOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DataModelTest {

    @Test
    public void mtRainierCamera_relativeToGround_usesUnifiedClearanceAltitude() {
        Camera camera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND);

        assertThat(camera.getCenter().getLatitude()).isWithin(1e-6).of(DataModel.MT_RAINIER_LATITUDE);
        assertThat(camera.getCenter().getLongitude()).isWithin(1e-6).of(DataModel.MT_RAINIER_LONGITUDE);
        assertThat(camera.getCenter().getAltitude()).isWithin(1e-6).of(DataModel.RELATIVE_CLEARANCE_METERS);
        assertThat(camera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(camera.getHeading()).isWithin(1e-6).of(DataModel.RELATIVE_GROUND_HEADING);
        assertThat(camera.getTilt()).isWithin(1e-6).of(DataModel.RELATIVE_GROUND_TILT);
        assertThat(camera.getRange()).isWithin(1e-6).of(DataModel.MT_RAINIER_BASE_RANGE_METERS);
    }

    @Test
    public void mtRainierCamera_relativeToMesh_usesUnifiedMeshClearanceAboveSurface() {
        Camera groundCamera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND);
        Camera meshCamera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_MESH);

        assertThat(meshCamera.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS);
        assertThat(meshCamera.getCenter().getAltitude())
                .isGreaterThan(groundCamera.getCenter().getAltitude());
        assertThat(meshCamera.getRange())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR);
        assertThat(meshCamera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
    }

    @Test
    public void mtRainierCamera_absolute_usesSeaLevelSummitElevationAboveGround() {
        Camera camera = DataModel.getMtRainierCamera(AltitudeMode.ABSOLUTE);

        assertThat(camera.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS);
        assertThat(camera.getRange())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR);
        assertThat(camera.getAltitudeMode()).isEqualTo(AltitudeMode.ABSOLUTE);
    }

    @Test
    public void mtRainierCamera_clampToGround_anchorsTargetAtZeroGroundClearance() {
        Camera clampCamera = DataModel.getMtRainierCamera(AltitudeMode.CLAMP_TO_GROUND);

        assertThat(clampCamera.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.CLAMP_ALTITUDE_METERS);
        assertThat(clampCamera.getRange()).isWithin(1e-6).of(DataModel.MT_RAINIER_BASE_RANGE_METERS);
        assertThat(clampCamera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
    }

    @Test
    public void esbCamera_and_beacon_shareUnifiedAltitudeModeLogic() {
        Camera groundEsb = DataModel.getEsbCamera(AltitudeMode.RELATIVE_TO_GROUND);
        Camera meshEsb = DataModel.getEsbCamera(AltitudeMode.RELATIVE_TO_MESH);
        Camera clampEsb = DataModel.getEsbCamera(AltitudeMode.CLAMP_TO_GROUND);
        Camera absoluteEsb = DataModel.getEsbCamera(AltitudeMode.ABSOLUTE);

        assertThat(groundEsb.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(groundEsb.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_CLEARANCE_METERS);
        assertThat(groundEsb.getRange()).isWithin(1e-6).of(DataModel.ESB_BASE_RANGE_METERS);

        assertThat(meshEsb.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(meshEsb.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS);
        assertThat(meshEsb.getRange())
                .isWithin(1e-6)
                .of(DataModel.ESB_BASE_RANGE_METERS * DataModel.RELATIVE_MESH_RANGE_FACTOR);

        assertThat(clampEsb.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(clampEsb.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.CLAMP_ALTITUDE_METERS);
        assertThat(clampEsb.getRange()).isWithin(1e-6).of(DataModel.ESB_BASE_RANGE_METERS);

        assertThat(absoluteEsb.getAltitudeMode()).isEqualTo(AltitudeMode.ABSOLUTE);
        assertThat(absoluteEsb.getCenter().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.ESB_ABSOLUTE_ALTITUDE_METERS);
        assertThat(absoluteEsb.getRange())
                .isWithin(1e-6)
                .of(DataModel.ESB_BASE_RANGE_METERS * DataModel.ABSOLUTE_RANGE_FACTOR);

        MarkerOptions esbMeshBeacon =
                DataModel.getTargetBeaconOptions(
                        false, "ESB Spire (+360m DSM Mesh)", AltitudeMode.RELATIVE_TO_MESH, 2);
        assertThat(esbMeshBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS);
        assertThat(esbMeshBeacon.getPosition().getAltitude())
                .isGreaterThan(DataModel.ESB_ABSOLUTE_ALTITUDE_METERS);
        assertThat(esbMeshBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(esbMeshBeacon.isExtruded()).isTrue();

        MarkerOptions esbClampBeacon =
                DataModel.getTargetBeaconOptions(
                        false, "ESB Clamped (0m DTM)", AltitudeMode.CLAMP_TO_GROUND, 1);
        assertThat(esbClampBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.CLAMP_ALTITUDE_METERS);
        assertThat(esbClampBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.CLAMP_TO_GROUND);
        assertThat(esbClampBeacon.isExtruded()).isFalse();
        assertThat(esbClampBeacon.getId()).isNotEqualTo(esbMeshBeacon.getId());
    }

    @Test
    public void mtRainierBeaconOptions_configuresExtrudedSummitMarkerForAllModes() {
        MarkerOptions groundBeacon =
                DataModel.getMtRainierBeaconOptions(
                        "Mt. Rainier (+100m DTM Ground)", AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(groundBeacon.getPosition().getLatitude())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_LATITUDE);
        assertThat(groundBeacon.getPosition().getLongitude())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_LONGITUDE);
        assertThat(groundBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_CLEARANCE_METERS);
        assertThat(groundBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(groundBeacon.isExtruded()).isTrue();
        assertThat(groundBeacon.isDrawnWhenOccluded()).isTrue();

        MarkerOptions meshBeacon =
                DataModel.getMtRainierBeaconOptions(
                        "Mt. Rainier (+150m DSM Mesh)", AltitudeMode.RELATIVE_TO_MESH);
        assertThat(meshBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.RELATIVE_MESH_CLEARANCE_METERS);
        assertThat(meshBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(meshBeacon.isExtruded()).isTrue();

        MarkerOptions clampBeacon =
                DataModel.getMtRainierBeaconOptions(
                        "Mt. Rainier Clamped (0m DTM)", AltitudeMode.CLAMP_TO_GROUND);
        assertThat(clampBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.CLAMP_ALTITUDE_METERS);
        assertThat(clampBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.CLAMP_TO_GROUND);
        assertThat(clampBeacon.isExtruded()).isFalse();

        MarkerOptions absoluteBeacon =
                DataModel.getMtRainierBeaconOptions(
                        "Mt. Rainier (4,392m MSL Abs)", AltitudeMode.ABSOLUTE);
        assertThat(absoluteBeacon.getPosition().getAltitude())
                .isWithin(1e-6)
                .of(DataModel.MT_RAINIER_GROUND_ELEVATION_METERS);
        assertThat(absoluteBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.ABSOLUTE);
        assertThat(absoluteBeacon.isExtruded()).isTrue();
    }

    @Test
    public void toValidCamera_and_copy_preserveAltitudeMode() {
        Camera camera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_MESH);
        Camera validated = toValidCamera(camera);
        Camera copied = copy(validated, validated.getCenter(), validated.getHeading(), validated.getTilt(), validated.getRange(), 15.0);

        assertThat(validated.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(copied.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(copied.getRoll()).isWithin(1e-6).of(15.0);
    }
}
