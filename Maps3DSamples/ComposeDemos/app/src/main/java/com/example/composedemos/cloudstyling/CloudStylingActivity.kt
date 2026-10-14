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

package com.example.composedemos.cloudstyling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.example.maps3d.common.Map3DQuickSettingsWidget
import com.example.maps3d.common.rememberMap3DSessionSettings
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.compose3d.GoogleMap3D

class CloudStylingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CloudStylingScreen()
                }
            }
        }
    }

    companion object {
        val SF_LOCATION = LatLng(37.7915, -122.4010)
        const val CUSTOM_MAP_ID = "9a35234a36da44d2c47bf626"
    }
}

@Composable
fun CloudStylingScreen() {
    val sessionSettings by rememberMap3DSessionSettings(defaultMapMode = Map3DMode.ROADMAP)
    val currentLocale = Locale.current
    var selectedMapMode by remember { mutableIntStateOf(Map3DMode.ROADMAP) }

    val initialLocation = CloudStylingActivity.SF_LOCATION
    val currentCameraState = remember {
        camera {
            center = latLngAltitude {
                latitude = initialLocation.latitude
                longitude = initialLocation.longitude
                altitude = 250.0
            }
            heading = 45.0
            tilt = 65.0
            range = 800.0
        }.toValidCamera()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen 3D Map View configured with custom Cloud Map ID
        GoogleMap3D(
            camera = currentCameraState,
            mapMode = selectedMapMode,
            options = Map3DInitConfig.create(
                centerLat = initialLocation.latitude,
                centerLng = initialLocation.longitude,
                centerAlt = 250.0,
                heading = 45.0,
                tilt = 65.0,
                roll = 0.0,
                range = 800.0,
                minAltitude = 0.0,
                maxAltitude = 1000000.0,
                minHeading = 0.0,
                maxHeading = 360.0,
                minTilt = 0.0,
                maxTilt = 90.0,
                bounds = null,
                mapMode = Map3DMode.ROADMAP,
                mapId = CloudStylingActivity.CUSTOM_MAP_ID,
                language = currentLocale.language,
                region = currentLocale.region,
            ),
            modifier = Modifier.fillMaxSize(),
        )

        // Top AppBar matching activity_cloud_styling.xml (#80BB86FC)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x80BB86FC))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_title_cloud_styling),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }

        // Bottom Map Mode Selection Card Overlay matching activity_cloud_styling.xml
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.map_mode),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val modes = listOf(
                        Map3DMode.ROADMAP to stringResource(R.string.roadmap),
                        Map3DMode.HYBRID to stringResource(R.string.hybrid),
                        Map3DMode.SATELLITE to stringResource(R.string.satellite),
                    )

                    modes.forEach { (modeValue, modeTitle) ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .selectable(
                                    selected = (selectedMapMode == modeValue),
                                    onClick = {
                                        selectedMapMode = modeValue
                                    },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = (selectedMapMode == modeValue),
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = modeTitle,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
