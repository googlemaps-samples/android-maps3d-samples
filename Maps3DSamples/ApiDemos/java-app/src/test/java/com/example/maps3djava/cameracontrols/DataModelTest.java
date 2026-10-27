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
import static com.example.maps3d.common.UtilitiesKt.isValidCameraAltitudeMode;
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
    public void targetCamera_mapsSupportedModes_andFallsBackForClampToGround() {
        Camera groundCamera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_GROUND);
        Camera meshCamera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_MESH);
        Camera clampCamera = DataModel.getMtRainierCamera(AltitudeMode.CLAMP_TO_GROUND);
        Camera absoluteCamera = DataModel.getMtRainierCamera(AltitudeMode.ABSOLUTE);

        assertThat(groundCamera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(meshCamera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(clampCamera.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(clampCamera.getCenter().getAltitude()).isWithin(1e-6).of(0.0);
        assertThat(absoluteCamera.getAltitudeMode()).isEqualTo(AltitudeMode.ABSOLUTE);
    }

    @Test
    public void targetBeaconOptions_mapsAllFourModes_andExtrudesOnlyNonClampedModes() {
        MarkerOptions groundBeacon =
                DataModel.getTargetBeaconOptions(
                        false, "Ground", AltitudeMode.RELATIVE_TO_GROUND, 0);
        MarkerOptions meshBeacon =
                DataModel.getTargetBeaconOptions(false, "Mesh", AltitudeMode.RELATIVE_TO_MESH, 0);
        MarkerOptions clampBeacon =
                DataModel.getTargetBeaconOptions(false, "Clamp", AltitudeMode.CLAMP_TO_GROUND, 0);
        MarkerOptions absoluteBeacon =
                DataModel.getTargetBeaconOptions(false, "Absolute", AltitudeMode.ABSOLUTE, 0);

        assertThat(groundBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_GROUND);
        assertThat(groundBeacon.isExtruded()).isTrue();

        assertThat(meshBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(meshBeacon.isExtruded()).isTrue();

        assertThat(clampBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.CLAMP_TO_GROUND);
        assertThat(clampBeacon.isExtruded()).isFalse();

        assertThat(absoluteBeacon.getAltitudeMode()).isEqualTo(AltitudeMode.ABSOLUTE);
        assertThat(absoluteBeacon.isExtruded()).isTrue();
    }

    @Test
    public void toValidCamera_and_copy_preserveSupportedCameraAltitudeModes() {
        Camera camera = DataModel.getMtRainierCamera(AltitudeMode.RELATIVE_TO_MESH);
        Camera validated = toValidCamera(camera);
        Camera copied =
                copy(
                        validated,
                        validated.getCenter(),
                        validated.getHeading(),
                        validated.getTilt(),
                        validated.getRange(),
                        15.0);

        assertThat(isValidCameraAltitudeMode(AltitudeMode.RELATIVE_TO_MESH)).isTrue();
        assertThat(isValidCameraAltitudeMode(AltitudeMode.CLAMP_TO_GROUND)).isFalse();
        assertThat(validated.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(copied.getAltitudeMode()).isEqualTo(AltitudeMode.RELATIVE_TO_MESH);
        assertThat(copied.getRoll()).isWithin(1e-6).of(15.0);
    }
}
