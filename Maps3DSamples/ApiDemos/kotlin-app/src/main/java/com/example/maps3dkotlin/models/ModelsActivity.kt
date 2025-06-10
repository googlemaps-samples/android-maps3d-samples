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

package com.example.maps3dkotlin.models

import android.os.Bundle
import com.example.maps3dcommon.R
import com.example.maps3d.common.awaitCameraUpdate
import com.example.maps3d.common.toCameraUpdate
import com.example.maps3d.common.toValidCamera
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.vector3D
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Demonstrates the use of 3D models in a 3D map environment.
 *
 * This activity loads and displays a 3D plane model on a satellite map. It also
 * features camera animations to showcase the model from different angles.
 *
 * The activity extends [SampleBaseActivity] and overrides its properties to define
 * the initial camera position.
 *
 * The user can reset the view to the initial camera position using a button.
 */
class ModelsActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName
    override val initialCamera =
        camera {
            center = latLngAltitude {
                latitude = 47.133971
                longitude = 11.333161
                altitude = 2200.0
            }
            heading = 221.0
            tilt = 25.0
            range = 30_000.0
        }

    private var animationSequenceJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the reset view button to reset the camera to the initial position and restart the
        // animation sequence
        recenterButton = findViewById<MaterialButton>(R.id.reset_view_button).apply {
            setOnClickListener {
                val controller = googleMap3D ?: return@setOnClickListener

                animationSequenceJob?.cancel()

                animationSequenceJob = CoroutineScope(Dispatchers.Main).launch {
                    awaitCameraUpdate(
                        controller = controller,
                        cameraUpdate = flyToOptions {
                            endCamera = initialCamera
                            durationInMillis = 2_000
                        }.toCameraUpdate()
                    )
                    runAnimationSequence(controller)
                    animationSequenceJob = null
                }
            }
            visibility = android.view.View.VISIBLE
        }

        findViewById<MaterialButton>(R.id.stop_button).apply {
            setOnClickListener {
                animationSequenceJob?.cancel()
                animationSequenceJob = null
            }
            visibility = android.view.View.VISIBLE
        }
    }

    @OptIn(FlowPreview::class)
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        CoroutineScope(Dispatchers.Main).launch {
            cameraUpdates.sample(1_000.milliseconds).collect { camera ->
                snapshot(camera.toValidCamera())
            }
        }

        googleMap3D.addModel(
            modelOptions {
                id = "plane_model"
                position = latLngAltitude {
                    latitude = 47.133971
                    longitude = 11.333161
                    altitude = 2200.0
                }
                altitudeMode = AltitudeMode.ABSOLUTE
                orientation = orientation {
                    heading = 41.5
                    tilt = -90.0
                    roll = 0.0
                }
                url = PLANE_URL
                scale = vector3D {
                    x = PLANE_SCALE
                    y = PLANE_SCALE
                    z = PLANE_SCALE
                }
            }
        )

        animationSequenceJob = CoroutineScope(Dispatchers.Main).launch {
            runAnimationSequence(googleMap3D)
            animationSequenceJob = null
        }
    }

    private suspend fun runAnimationSequence(googleMap3D: GoogleMap3D) {
        delay(1_500.milliseconds)

        val camera = camera {
            center = latLngAltitude {
                latitude = 47.133971
                longitude = 11.333161
                altitude = 2200.0
            }
            heading = 221.4
            tilt = 75.0
            range = 700.0
        }

        // Fly to the plane model and wait until the animation is finished
        awaitCameraUpdate(
            controller = googleMap3D,
            cameraUpdate = flyToOptions {
                endCamera = camera
                durationInMillis = 3_500
            }.toCameraUpdate()
        )

        delay(500.milliseconds)

        awaitCameraUpdate(
            controller = googleMap3D,
            cameraUpdate = flyAroundOptions {
                center = camera
                durationInMillis = 3_500
                rounds = 0.5
            }.toCameraUpdate()
        )
    }

    companion object {
        const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
        const val PLANE_SCALE = 0.05
    }
}
