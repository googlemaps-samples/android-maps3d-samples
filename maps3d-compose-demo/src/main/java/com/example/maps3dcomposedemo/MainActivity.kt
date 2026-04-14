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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
    var selectedSample by remember { mutableStateOf<String?>(null) }

    if (selectedSample == null) {
        LazyColumn(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            item {
                Text(
                    text = "Maps 3D Compose Samples",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item { SampleItem("Basic Map with Marker & Polyline") { selectedSample = "basic" } }
            item { 
                SampleItem("Hello Map") { 
                    context.startActivity(Intent(context, HelloMapActivity::class.java))
                } 
            }
            item { 
                SampleItem("Camera Controls") { 
                    context.startActivity(Intent(context, CameraControlsActivity::class.java))
                } 
            }
            item { 
                SampleItem("Map Interactions") { 
                    context.startActivity(Intent(context, MapInteractionsActivity::class.java))
                } 
            }
            item { 
                SampleItem("Markers") { 
                    context.startActivity(Intent(context, MarkersActivity::class.java))
                } 
            }
            item { 
                SampleItem("Models") { 
                    context.startActivity(Intent(context, ModelsActivity::class.java))
                } 
            }
            item { 
                SampleItem("Polygons") { 
                    context.startActivity(Intent(context, PolygonsActivity::class.java))
                } 
            }
            item { 
                SampleItem("Polylines") { 
                    context.startActivity(Intent(context, PolylinesActivity::class.java))
                } 
            }
            item { 
                SampleItem("Popovers") { 
                    context.startActivity(Intent(context, PopoversActivity::class.java))
                } 
            }
            item { 
                SampleItem("Map Options") { 
                    context.startActivity(Intent(context, MapOptionsActivity::class.java))
                } 
            }
            item { 
                SampleItem("Camera Animations") { 
                    context.startActivity(Intent(context, CameraAnimationsActivity::class.java))
                } 
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedSample) {
                "basic" -> BasicMapSample()
            }

            FloatingActionButton(
                onClick = { selectedSample = null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun SampleItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
