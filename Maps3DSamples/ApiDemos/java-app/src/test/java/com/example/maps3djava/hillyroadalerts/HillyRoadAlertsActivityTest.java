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

package com.example.maps3djava.hillyroadalerts;

import static com.google.common.truth.Truth.assertThat;
import static com.google.maps.android.robolectric.maps3d.truth.Maps3DTruth.assertThat;

import androidx.lifecycle.ViewModelProvider;
import com.example.maps3d.common.AlertSeverity;
import com.example.maps3d.common.HillyRoadAlertViewModel;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.maps.android.robolectric.maps3d.Map3DTestHarness;
import com.google.maps.android.robolectric.maps3d.Maps3DRobolectricTestRunner;
import com.google.maps.android.robolectric.maps3d.Maps3DShadows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;

/**
 * Robolectric unit tests for {@link HillyRoadAlertsActivity} verifying map scene synchronization
 * across initial readiness, configuration changes (Activity rotation/recreation), and teardown.
 */
@RunWith(Maps3DRobolectricTestRunner.class)
public class HillyRoadAlertsActivityTest {

    @Test
    public void onMapReady_afterActivityRecreationWhenViewModelAlreadyReady_rendersSceneOnNewMap() {
        ActivityController<HillyRoadAlertsActivity> controller =
                Robolectric.buildActivity(HillyRoadAlertsActivity.class).setup();
        ShadowLooper.idleMainLooper();

        HillyRoadAlertsActivity initialActivity = controller.get();
        Map3DView initialMapView = Map3DTestHarness.requireMap3DView(initialActivity);
        GoogleMap3D initialMap = Maps3DShadows.shadowOf(initialMapView).triggerMapReady();
        assertThat(initialMap).hasPolyline(HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID);
        assertThat(initialMap).hasModel(HillyRoadAlertViewModel.VEHICLE_MODEL_ID);

        // Advance simulation into the WARNING zone (250m) prior to Activity recreation
        HillyRoadAlertViewModel viewModel =
                new ViewModelProvider(initialActivity).get(HillyRoadAlertViewModel.class);
        viewModel.seekToDistance(250.0);
        assertThat(viewModel.getCurrentState().terrainAlert.severity)
                .isEqualTo(AlertSeverity.WARNING);

        // Recreate the Activity (simulating rotation): old Activity pauses & destroys,
        // new Activity inherits the surviving ViewModel with isMapReady == true
        controller.recreate();
        ShadowLooper.idleMainLooper();

        // Old map overlays were cleaned up on destroy
        assertThat(initialMap).hasNoOverlays();

        // New Map3DView fires onMapReady -> recreated Activity renders the surviving scene
        HillyRoadAlertsActivity recreatedActivity = controller.get();
        Map3DView recreatedMapView = Map3DTestHarness.requireMap3DView(recreatedActivity);
        GoogleMap3D recreatedMap = Maps3DShadows.shadowOf(recreatedMapView).triggerMapReady();

        assertThat(viewModel.getCurrentState().currentDistanceMeters).isWithin(0.001).of(250.0);
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID);
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.TRAVELED_ROUTE_POLYLINE_ID);
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.HAZARD_ROUTE_POLYLINE_ID);
        assertThat(recreatedMap).hasModel(HillyRoadAlertViewModel.VEHICLE_MODEL_ID);
        assertThat(recreatedMap)
                .hasModelNear(
                        HillyRoadAlertViewModel.VEHICLE_MODEL_ID,
                        viewModel.getCurrentState().vehiclePosition.getLatitude(),
                        viewModel.getCurrentState().vehiclePosition.getLongitude());
    }
}
