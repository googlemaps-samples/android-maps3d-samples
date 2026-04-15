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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt

class CameraChangedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CameraChangedScreen()
                }
            }
        }
    }
}

@Composable
fun CameraChangedScreen() {
    // Camera centered on San Francisco
    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = 37.7749
                longitude = -122.4194
                altitude = 0.0
            }
            heading = 0.0
            tilt = 45.0
            range = 2000.0
            roll = 0.0
        }
    }

    // Use a flow to hold the camera state, as requested
    val cameraFlow = remember { MutableStateFlow(initialCamera) }
    val currentCamera by cameraFlow.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = initialCamera,
            mapMode = Map3DMode.HYBRID,
            modifier = Modifier.fillMaxSize(),
            onCameraChanged = { camera ->
                cameraFlow.value = camera
            }
        )

        val heading = currentCamera.heading?.toFloat() ?: 0f
        val tilt = currentCamera.tilt?.toFloat() ?: 0f
        val range = currentCamera.range?.toFloat() ?: 0f

        // Overlay the Whiskey Compass at the top
        WhiskeyCompass(
            heading = heading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 48.dp), // Padding for edge-to-edge only at top
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        )

        // Overlay Tilt Scale on the right
        TiltScale(
            tilt = tilt,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        // Overlay Range Scale at the bottom
        RangeScale(
            range = range,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
fun TiltScale(tilt: Float, modifier: Modifier = Modifier) {
    // User requested: 90 = straight down, 0 = horizontal
    // SDK: 0 = straight down, 90 = horizontal
    // So displayedTilt = 90 - sdkTilt
    val displayedTilt = 90f - tilt
    val textMeasurer = rememberTextMeasurer()
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .height(300.dp)
            .width(80.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerLineY = size.height / 2f
            val centerLineX = size.width / 2f
            
            // Pixels per degree
            val pixelsPerDegree = 5f
            
            // Draw a fixed center line (pointer)
            drawLine(
                color = Color.Red,
                start = Offset(0f, centerLineY),
                end = Offset(size.width, centerLineY),
                strokeWidth = 2f
            )

            // Draw scrolling ticks
            translate(top = centerLineY + (displayedTilt * pixelsPerDegree)) {
                for (i in 0..90 step 5) {
                    val yPos = -i * pixelsPerDegree
                    
                    val isMajor = i % 15 == 0
                    val tickLength = if (isMajor) 15f else 8f
                    
                    drawLine(
                        color = onPrimaryColor,
                        start = Offset(centerLineX - tickLength, yPos),
                        end = Offset(centerLineX + tickLength, yPos),
                        strokeWidth = if (isMajor) 2f else 1f
                    )
                    
                    if (isMajor) {
                        val measuredText = textMeasurer.measure(i.toString(), style = TextStyle(color = onPrimaryColor, fontSize = 12.sp))
                        drawText(
                            textLayoutResult = measuredText,
                            topLeft = Offset(centerLineX + 20f, yPos - measuredText.size.height / 2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RangeScale(range: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        Text(
            text = "Range: ${range.roundToInt()} m",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
