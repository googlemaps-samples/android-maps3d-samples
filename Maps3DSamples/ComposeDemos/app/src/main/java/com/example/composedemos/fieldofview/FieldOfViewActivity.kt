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

package com.example.composedemos.fieldofview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps3d.common.showcase.ui.SampleTopBar
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D
import com.google.maps.android.compose3d.utils.toValidCamera
import kotlin.math.abs
import kotlin.math.tan

class FieldOfViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FieldOfViewScreen()
                }
            }
        }
    }

    companion object {
        val SF_FINANCIAL_DISTRICT = LatLng(37.7952, -122.4028)
    }
}

@Composable
fun FieldOfViewScreen() {
    var fovValue by remember { mutableFloatStateOf(45.0f) }

    val initialCamera = remember {
        camera {
            center = latLngAltitude {
                latitude = FieldOfViewActivity.SF_FINANCIAL_DISTRICT.latitude
                longitude = FieldOfViewActivity.SF_FINANCIAL_DISTRICT.longitude
                altitude = 150.0
            }
            heading = 45.0
            tilt = 65.0
            range = 800.0
        }.toValidCamera()
    }

    var cameraState by remember { mutableStateOf(initialCamera) }
    var mapInstance by remember { mutableStateOf<com.google.android.gms.maps3d.GoogleMap3D?>(null) }

    fun updateCameraFov(newFov: Float) {
        fovValue = newFov

        val activeMap = mapInstance
        val liveCam = activeMap?.getCamera()?.toValidCamera()

        val refCam: Camera = if (liveCam != null && (abs(liveCam.center.latitude) > 0.001 || abs(liveCam.center.longitude) > 0.001)) {
            liveCam
        } else {
            cameraState
        }

        val baseFovRad = Math.toRadians(45.0 / 2.0)
        val targetFovRad = Math.toRadians(newFov.toDouble() / 2.0)
        val targetRange = (800.0 * tan(baseFovRad) / tan(targetFovRad)).coerceIn(150.0, 3000.0)

        val updatedCam = camera {
            center = refCam.center
            heading = refCam.heading
            tilt = refCam.tilt
            roll = refCam.roll
            range = targetRange
        }.toValidCamera()

        cameraState = updatedCam
        activeMap?.setCamera(updatedCam)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap3D(
            camera = cameraState,
            mapMode = Map3DMode.SATELLITE,
            onMapReady = { mapInstance = it },
            modifier = Modifier.fillMaxSize(),
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "Field of View: ${fovValue.toInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = fovValue,
                    onValueChange = { updateCameraFov(it) },
                    valueRange = 15.0f..120.0f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "FOV Presets:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val presets = listOf(
                        20.0f to "20° Tele",
                        45.0f to "45° Standard",
                        90.0f to "90° Wide",
                        120.0f to "120° Ultra",
                    )

                    presets.forEach { (presetFov, label) ->
                        val isSelected = abs(fovValue - presetFov) < 1.0f

                        if (isSelected) {
                            Button(
                                onClick = { updateCameraFov(presetFov) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { updateCameraFov(presetFov) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
