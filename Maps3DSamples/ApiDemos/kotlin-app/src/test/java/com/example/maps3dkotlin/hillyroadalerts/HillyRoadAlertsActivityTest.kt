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

package com.example.maps3dkotlin.hillyroadalerts

import androidx.lifecycle.ViewModelProvider
import com.example.maps3d.common.AlertSeverity
import com.example.maps3d.common.HillyRoadAlertViewModel
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.robolectric.maps3d.Maps3DRobolectricTestRunner
import com.google.maps.android.robolectric.maps3d.requireMap3DView
import com.google.maps.android.robolectric.maps3d.shadow
import com.google.maps.android.robolectric.maps3d.truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLooper

/**
 * Robolectric unit tests for [HillyRoadAlertsActivity] verifying map scene synchronization
 * across initial readiness, configuration changes (Activity rotation/recreation), and teardown.
 */
@RunWith(Maps3DRobolectricTestRunner::class)
class HillyRoadAlertsActivityTest {

    @Test
    fun onMapReady_afterActivityRecreationWhenViewModelAlreadyReady_rendersSceneOnNewMap() {
        val controller = Robolectric.buildActivity(HillyRoadAlertsActivity::class.java).setup()
        ShadowLooper.idleMainLooper()

        val initialActivity = controller.get()
        val initialMap = initialActivity.requireMap3DView().shadow.triggerMapReady()
        assertThat(initialMap).hasPolyline(HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID)
        assertThat(initialMap).hasModel(HillyRoadAlertViewModel.VEHICLE_MODEL_ID)

        // Advance simulation into the WARNING zone (250m) prior to Activity recreation
        val viewModel = ViewModelProvider(initialActivity)[HillyRoadAlertViewModel::class.java]
        viewModel.seekToDistance(250.0)
        assertThat(viewModel.currentState.terrainAlert.severity).isEqualTo(AlertSeverity.WARNING)

        // Recreate the Activity (simulating rotation): old Activity pauses & destroys,
        // new Activity inherits the surviving ViewModel with isMapReady == true
        controller.recreate()
        ShadowLooper.idleMainLooper()

        // Old map overlays were cleaned up on destroy
        assertThat(initialMap).hasNoOverlays()

        // New Map3DView fires onMapReady -> recreated Activity renders the surviving scene
        val recreatedActivity = controller.get()
        val recreatedMap = recreatedActivity.requireMap3DView().shadow.triggerMapReady()

        assertThat(viewModel.currentState.currentDistanceMeters).isWithin(0.001).of(250.0)
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.BASE_ROUTE_POLYLINE_ID)
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.TRAVELED_ROUTE_POLYLINE_ID)
        assertThat(recreatedMap).hasPolyline(HillyRoadAlertViewModel.HAZARD_ROUTE_POLYLINE_ID)
        assertThat(recreatedMap).hasModel(HillyRoadAlertViewModel.VEHICLE_MODEL_ID)
        assertThat(recreatedMap).hasModelNear(
            HillyRoadAlertViewModel.VEHICLE_MODEL_ID,
            viewModel.currentState.vehiclePosition.latitude,
            viewModel.currentState.vehiclePosition.longitude,
        )
    }
}
