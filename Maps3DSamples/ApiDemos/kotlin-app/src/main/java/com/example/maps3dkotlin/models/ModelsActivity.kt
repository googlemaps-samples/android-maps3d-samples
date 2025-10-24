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
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.awaitCameraUpdate
import com.example.maps3d.common.toCameraUpdate
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * This activity provides a demonstration of how to incorporate 3D models into a map environment.
 * It loads a 3D airplane model and displays it on a satellite map, complemented by a sequence
 * of camera animations that showcase the model from various perspectives.
 *
 * The user can interact with the scene by resetting the camera to its initial position, which
 * also restarts the animation sequence, or by stopping the animation altogether.
 */
class ModelsActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName

    // The initial camera position is defined declaratively, providing a clear and concise
    // starting point for the map's view.
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

    // A Job to keep track of the animation sequence coroutine. This allows us to cancel
    // the animation if the user interacts with the UI.
    private var animationSequenceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    /**
     * Sets up the user interface elements and their click listeners.
     */
    private fun setupUI() {
        // The reset button allows the user to return to the initial camera position and
        // restart the animation sequence.
        findViewById<MaterialButton>(R.id.reset_view_button).apply {
            setOnClickListener {
                googleMap3D?.let { restartAnimation(it) }
            }
            visibility = View.VISIBLE
        }

        // The stop button allows the user to cancel the animation sequence.
        findViewById<MaterialButton>(R.id.stop_button).apply {
            setOnClickListener {
                animationSequenceJob?.cancel()
                animationSequenceJob = null
            }
            visibility = View.VISIBLE
        }
    }

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)

        // A coroutine is launched to collect camera updates and generate snapshots.
        // This is done in a lifecycle-aware manner to prevent memory leaks.
        lifecycleScope.launch {
            cameraUpdates.sample(1.seconds).collect { camera ->
                snapshot(camera.toValidCamera())
            }
        }

        lifecycleScope.launch {
            delay(10.milliseconds)

            // The 3D model is added to the map.
            addPlaneModel(googleMap3D)

            // The animation sequence is started.
            startAnimationSequence(googleMap3D)
        }
    }

    /**
     * Adds the 3D airplane model to the map.
     *
     * @param googleMap3D The map object to which the model will be added.
     */
    private fun addPlaneModel(googleMap3D: GoogleMap3D) {
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
        ).also { model ->
            model.setClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        this@ModelsActivity,
                        "Model clicked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Starts the camera animation sequence.
     *
     * @param googleMap3D The map object on which the animation will be run.
     */
    private fun startAnimationSequence(googleMap3D: GoogleMap3D) {
        animationSequenceJob = lifecycleScope.launch {
            runAnimationSequence(googleMap3D)
            animationSequenceJob = null
        }
    }

    /**
     * Restarts the camera animation sequence.
     *
     * @param googleMap3D The map object on which the animation will be run.
     */
    private fun restartAnimation(googleMap3D: GoogleMap3D) {
        animationSequenceJob?.cancel()
        animationSequenceJob = lifecycleScope.launch {
            awaitCameraUpdate(
                controller = googleMap3D,
                cameraUpdate = flyToOptions {
                    endCamera = initialCamera
                    durationInMillis = 2.seconds.inWholeMilliseconds
                }.toCameraUpdate()
            )
            runAnimationSequence(googleMap3D)
            animationSequenceJob = null
        }
    }

    /**
     * Runs the camera animation sequence, which consists of flying to the model and then
     * flying around it.
     *
     * @param googleMap3D The map object on which the animation will be run.
     */
    private suspend fun runAnimationSequence(googleMap3D: GoogleMap3D) {
        delay(1500.milliseconds)

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

        // Fly to the plane model.
        awaitCameraUpdate(
            controller = googleMap3D,
            cameraUpdate = flyToOptions {
                endCamera = camera
                durationInMillis = 3500.milliseconds.inWholeMilliseconds
            }.toCameraUpdate()
        )

        delay(500.milliseconds)

        // Fly around the plane model.
        awaitCameraUpdate(
            controller = googleMap3D,
            cameraUpdate = flyAroundOptions {
                center = camera
                durationInMillis = 3500.milliseconds.inWholeMilliseconds
                rounds = 0.5
            }.toCameraUpdate()
        )
    }

    companion object {
        private const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
        private const val PLANE_SCALE = 0.05
    }
}