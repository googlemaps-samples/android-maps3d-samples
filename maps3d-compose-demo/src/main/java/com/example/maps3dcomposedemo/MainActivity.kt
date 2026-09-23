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

package com.example.maps3dcomposedemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CatalogScreen()
                }
            }
        }
    }
}

@Composable
fun CatalogScreen() {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        item {
            Text(
                text = stringResource(R.string.catalog_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        item {
            SampleItem(R.string.sample_title_basic_map) {
                context.startActivity(Intent(context, BasicMapActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_hello_map) {
                context.startActivity(Intent(context, HelloMapActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_camera_controls) {
                context.startActivity(Intent(context, CameraControlsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_map_interactions) {
                context.startActivity(Intent(context, MapInteractionsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_markers) {
                context.startActivity(Intent(context, MarkersActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_custom_markers) {
                context.startActivity(Intent(context, CustomMarkersActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_place_clicks) {
                context.startActivity(Intent(context, PlaceClickActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_models) {
                context.startActivity(Intent(context, ModelsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_polygons) {
                context.startActivity(Intent(context, PolygonsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_polylines) {
                context.startActivity(Intent(context, PolylinesActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_popovers) {
                context.startActivity(Intent(context, PopoversActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_map_options) {
                context.startActivity(Intent(context, MapOptionsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_camera_animations) {
                context.startActivity(Intent(context, CameraAnimationsActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_camera_changed) {
                context.startActivity(Intent(context, CameraChangedActivity::class.java))
            }
        }
        item {
            SampleItem(R.string.sample_title_projection_3d) {
                context.startActivity(Intent(context, Projection3DActivity::class.java))
            }
        }
    }
}

@Composable
fun SampleItem(@StringRes titleRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
