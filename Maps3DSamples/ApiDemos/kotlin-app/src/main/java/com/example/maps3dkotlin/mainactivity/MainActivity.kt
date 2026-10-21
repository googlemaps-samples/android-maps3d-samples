// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.maps3dkotlin.mainactivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.advancedcameraanimation.AdvancedCameraAnimationActivity
import com.example.maps3dkotlin.animatingmodels.AnimatingModelsActivity
import com.example.maps3dkotlin.cameracontrols.CameraControlsActivity
import com.example.maps3dkotlin.camerarestrictions.CameraRestrictionsActivity
import com.example.maps3dkotlin.cloudstyling.CloudStylingActivity
import com.example.maps3dkotlin.datavisualization.DataVisualizationActivity
import com.example.maps3dkotlin.fieldofview.FieldOfViewActivity
import com.example.maps3dkotlin.flightsimulator.FlightSimulatorActivity
import com.example.maps3dkotlin.hellomap.HelloMapActivity
import com.example.maps3dkotlin.mapinteractions.MapInteractionsActivity
import com.example.maps3dkotlin.mapslocalization.MapsLocalizationActivity
import com.example.maps3dkotlin.markers.MarkersActivity
import com.example.maps3dkotlin.models.ModelsActivity
import com.example.maps3dkotlin.pathfollowing.PathFollowingActivity
import com.example.maps3dkotlin.pathstyling.PathStylingActivity
import com.example.maps3dkotlin.placedetails.PlaceDetailsActivity
import com.example.maps3dkotlin.polygons.PolygonsActivity
import com.example.maps3dkotlin.polylines.PolylinesActivity
import com.example.maps3dkotlin.popovers.PopoversActivity
import com.example.maps3dkotlin.projection3d.Projection3DActivity
import com.example.maps3dkotlin.roadmapmode.RoadmapModeActivity
import com.example.maps3dkotlin.routes.RoutesActivity
import com.example.maps3dkotlin.storelocator.StoreLocatorActivity
import com.example.maps3dkotlin.theme.Maps3DSamplesTheme
import com.example.maps3dkotlin.uicontrols.UiControlsActivity
import kotlinx.coroutines.launch

/**
 * A data class to represent a single sample in the list.
 *
 * @param titleResId The string resource ID for the sample's title.
 * @param activityClass The activity to launch for this sample. Null if not implemented.
 * @param is2DOrAbstractSample True if the sample highlights 2D / Abstract Map (`ROADMAP`) workflows.
 */
data class Sample(
    @param:StringRes @get:StringRes val titleResId: Int,
    val activityClass: Class<out Activity>?,
    val is2DOrAbstractSample: Boolean = false,
)

/**
 * The main activity of the 3D Maps SDK Samples application.
 * This activity displays a list of available samples that users can select from.
 */
class MainActivity : ComponentActivity() {

    private val samples = listOf(
        // Canonical 2D / Abstract Map Samples (Map3DMode.ROADMAP & Hybrid Overlays)
        Sample(R.string.feature_title_store_locator, StoreLocatorActivity::class.java, is2DOrAbstractSample = true),
        Sample(R.string.feature_title_routes_api, RoutesActivity::class.java, is2DOrAbstractSample = true),
        Sample(R.string.feature_title_roadmap_mode, RoadmapModeActivity::class.java, is2DOrAbstractSample = true),
        Sample(R.string.feature_title_cloud_styling, CloudStylingActivity::class.java, is2DOrAbstractSample = true),
        Sample(R.string.feature_title_data_visualization, DataVisualizationActivity::class.java, is2DOrAbstractSample = true),
        Sample(R.string.feature_title_ui_controls, UiControlsActivity::class.java, is2DOrAbstractSample = false),
        // Core 3D Maps SDK Samples
        Sample(R.string.feature_title_overview_hello_3d_map, HelloMapActivity::class.java),
        Sample(R.string.feature_title_camera_controls, CameraControlsActivity::class.java),
        Sample(R.string.feature_title_markers, MarkersActivity::class.java),
        Sample(R.string.feature_title_polygons, PolygonsActivity::class.java),
        Sample(R.string.feature_title_polylines, PolylinesActivity::class.java),
        Sample(R.string.feature_title_3d_models, ModelsActivity::class.java),
        Sample(R.string.feature_title_popovers, PopoversActivity::class.java),
        Sample(R.string.feature_title_map_interactions, MapInteractionsActivity::class.java),
        Sample(R.string.feature_title_camera_restrictions, CameraRestrictionsActivity::class.java),
        Sample(R.string.feature_title_flight_simulator, FlightSimulatorActivity::class.java),
        Sample(R.string.feature_title_path_following, PathFollowingActivity::class.java),
        Sample(R.string.feature_title_path_styling, PathStylingActivity::class.java),
        Sample(R.string.feature_title_animating_models, AnimatingModelsActivity::class.java),
        Sample(R.string.feature_title_place_details, PlaceDetailsActivity::class.java),
        Sample(R.string.feature_title_advanced_camera_animation, AdvancedCameraAnimationActivity::class.java),
        Sample(R.string.feature_title_field_of_view, FieldOfViewActivity::class.java),
        Sample(R.string.feature_title_projection_3d, Projection3DActivity::class.java),
        Sample(R.string.feature_title_maps_localization, MapsLocalizationActivity::class.java),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Maps3DSamplesTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text(stringResource(R.string.samples_menu_title)) })
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    SampleMenuList(
                        samples = samples,
                        modifier = Modifier.padding(innerPadding),
                    ) { sample ->
                        val activityClass = sample.activityClass
                        if (activityClass != null) {
                            startActivity(Intent(this, activityClass))
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = getString(R.string.feature_not_implemented),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A Composable function that displays the list of samples.
 */
@Composable
fun SampleMenuList(
    samples: List<Sample>,
    modifier: Modifier = Modifier,
    onItemClick: (Sample) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(samples) { sample ->
                SampleListItem(sample = sample) {
                    onItemClick(sample)
                }
                HorizontalDivider()
            }
        }
    }
}

/**
 * A Composable function for a single item in the sample list, including a `2D / ROADMAP` badge
 * for canonical 2D & abstract map samples.
 */
@Composable
fun SampleListItem(sample: Sample, onClick: () -> Unit) {
    val isEnabled = sample.activityClass != null
    val color = if (isEnabled) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(sample.titleResId),
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = Modifier.weight(1f),
        )
        if (sample.is2DOrAbstractSample) {
            Text(
                text = "2D / ROADMAP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
