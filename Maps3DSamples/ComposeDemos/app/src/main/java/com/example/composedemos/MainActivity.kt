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

package com.example.composedemos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.composedemos.advancedcameraanimation.AdvancedCameraAnimationActivity
import com.example.composedemos.animatingmodels.AnimatingModelsActivity
import com.example.composedemos.cameracontrols.CameraControlsActivity
import com.example.composedemos.camerarestrictions.CameraRestrictionsActivity
import com.example.composedemos.cloudstyling.CloudStylingActivity
import com.example.composedemos.datavisualization.DataVisualizationActivity
import com.example.composedemos.fieldofview.FieldOfViewActivity
import com.example.composedemos.flightsimulator.FlightSimulatorActivity
import com.example.composedemos.hellomap.HelloMapActivity
import com.example.composedemos.mapinteractions.MapInteractionsActivity
import com.example.composedemos.markers.MarkersActivity
import com.example.composedemos.models.ModelsActivity
import com.example.composedemos.pathfollowing.PathFollowingActivity
import com.example.composedemos.pathstyling.PathStylingActivity
import com.example.composedemos.placeautocomplete.PlaceAutocompleteActivity
import com.example.composedemos.placedetails.PlaceDetailsActivity
import com.example.composedemos.placesearch.PlaceSearchActivity
import com.example.composedemos.polygons.PolygonsActivity
import com.example.composedemos.polylines.PolylinesActivity
import com.example.composedemos.popovers.PopoversActivity
import com.example.composedemos.roadmapmode.RoadmapModeActivity
import com.example.composedemos.routes.RoutesActivity

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
                text = "Compose Demos Catalog",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
            )
        }

        // ApiDemos Parity
        item { CategoryHeader("ApiDemos Parity") }
        item {
            SampleItem("Hello Map") {
                context.startActivity(Intent(context, HelloMapActivity::class.java))
            }
        }
        item {
            SampleItem("Polylines") {
                context.startActivity(Intent(context, PolylinesActivity::class.java))
            }
        }
        item {
            SampleItem("Map Interactions") {
                context.startActivity(Intent(context, MapInteractionsActivity::class.java))
            }
        }
        item {
            SampleItem("Popovers") {
                context.startActivity(Intent(context, PopoversActivity::class.java))
            }
        }
        item {
            SampleItem("Camera Controls") {
                context.startActivity(Intent(context, CameraControlsActivity::class.java))
            }
        }
        item {
            SampleItem("Polygons") {
                context.startActivity(Intent(context, PolygonsActivity::class.java))
            }
        }
        item {
            SampleItem("Models") {
                context.startActivity(Intent(context, ModelsActivity::class.java))
            }
        }
        item {
            SampleItem("Markers") {
                context.startActivity(Intent(context, MarkersActivity::class.java))
            }
        }

        // Additional Catalog Requirements
        item { CategoryHeader("Additional Catalog Requirements") }
        item {
            SampleItem("Camera Restrictions") {
                context.startActivity(Intent(context, CameraRestrictionsActivity::class.java))
            }
        }
        item {
            SampleItem("Flight Simulator") {
                context.startActivity(Intent(context, FlightSimulatorActivity::class.java))
            }
        }
        item {
            SampleItem("Routes API") {
                context.startActivity(Intent(context, RoutesActivity::class.java))
            }
        }
        item {
            SampleItem("Path Following") {
                context.startActivity(Intent(context, PathFollowingActivity::class.java))
            }
        }
        item {
            SampleItem("Path Styling") {
                context.startActivity(Intent(context, PathStylingActivity::class.java))
            }
        }
        item {
            SampleItem("Animating Models") {
                context.startActivity(Intent(context, AnimatingModelsActivity::class.java))
            }
        }
        item {
            SampleItem("Place Search") {
                context.startActivity(Intent(context, PlaceSearchActivity::class.java))
            }
        }
        item {
            SampleItem("Place Autocomplete") {
                context.startActivity(Intent(context, PlaceAutocompleteActivity::class.java))
            }
        }
        item {
            SampleItem("Place Details") {
                context.startActivity(Intent(context, PlaceDetailsActivity::class.java))
            }
        }
        item {
            SampleItem("Advanced Camera Animation") {
                context.startActivity(Intent(context, AdvancedCameraAnimationActivity::class.java))
            }
        }
        item {
            SampleItem("Data Visualization (Flood Fill)") {
                context.startActivity(Intent(context, DataVisualizationActivity::class.java))
            }
        }
        item {
            SampleItem("Cloud Map Styling") {
                context.startActivity(Intent(context, CloudStylingActivity::class.java))
            }
        }
        item {
            SampleItem("Roadmap Mode") {
                context.startActivity(Intent(context, RoadmapModeActivity::class.java))
            }
        }
        item {
            SampleItem("Field Of View") {
                context.startActivity(Intent(context, FieldOfViewActivity::class.java))
            }
        }
    }
}

@Composable
fun CategoryHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun SampleItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
