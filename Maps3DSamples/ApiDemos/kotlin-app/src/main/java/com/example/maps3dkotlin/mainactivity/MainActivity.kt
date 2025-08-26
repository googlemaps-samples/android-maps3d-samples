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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.cameracontrols.CameraControlsActivity
import com.example.maps3dkotlin.hellomap.HelloMapActivity
import com.example.maps3dkotlin.mapinteractions.MapInteractionsActivity
import com.example.maps3dkotlin.markers.MarkersActivity
import com.example.maps3dkotlin.models.ModelsActivity
import com.example.maps3dkotlin.polygons.PolygonsActivity
import com.example.maps3dkotlin.polylines.PolylinesActivity
import com.example.maps3dkotlin.theme.Maps3DSamplesTheme
import kotlinx.coroutines.launch

/**
 * A data class to represent a single sample in the list.
 * Using a data class provides type safety and makes the code more readable
 * compared to using a Map or a Pair.
 *
 * @param titleResId The string resource ID for the sample's title.
 * @param activityClass The activity to launch for this sample. Null if not implemented.
 */
data class Sample(
    @StringRes val titleResId: Int,
    val activityClass: Class<out Activity>?
)

/**
 * The main activity of the 3D Maps SDK Samples application.
 * This activity displays a list of available samples that users can select from.
 */
class MainActivity : ComponentActivity() {

    // A list of all the samples to be displayed in the app.
    // This approach is more idiomatic and type-safe than using a map.
    private val samples = listOf(
        Sample(R.string.feature_title_overview_hello_3d_map, HelloMapActivity::class.java),
        Sample(R.string.feature_title_camera_controls, CameraControlsActivity::class.java),
        Sample(R.string.feature_title_markers, MarkersActivity::class.java),
        Sample(R.string.feature_title_polygons, PolygonsActivity::class.java),
        Sample(R.string.feature_title_polylines, PolylinesActivity::class.java),
        Sample(R.string.feature_title_3d_models, ModelsActivity::class.java),
        Sample(R.string.feature_title_map_interactions, MapInteractionsActivity::class.java),
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
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    SampleMenuList(
                        samples = samples,
                        modifier = Modifier.padding(innerPadding)
                    ) { sample ->
                        val activityClass = sample.activityClass
                        if (activityClass != null) {
                            startActivity(Intent(this, activityClass))
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = getString(R.string.feature_not_implemented)
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
 * This function is responsible for rendering the list of available map samples.
 * It uses a `LazyColumn` for efficient rendering of the list.
 *
 * @param samples The list of samples to display.
 * @param modifier A `Modifier` to be applied to the layout.
 * @param onItemClick A callback that is invoked when a sample is clicked.
 */
@Composable
fun SampleMenuList(
    samples: List<Sample>,
    modifier: Modifier = Modifier,
    onItemClick: (Sample) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
 * A Composable function for a single item in the sample list.
 * This function displays the title of the sample and handles click events.
 * The item's appearance changes based on whether the feature is enabled.
 *
 * @param sample The sample to display.
 * @param onClick A callback that is invoked when the item is clicked.
 */
@Composable
fun SampleListItem(sample: Sample, onClick: () -> Unit) {
    val isEnabled = sample.activityClass != null
    val color = if (isEnabled) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    }

    Text(
        text = stringResource(sample.titleResId),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        fontSize = 18.sp,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
    )
}