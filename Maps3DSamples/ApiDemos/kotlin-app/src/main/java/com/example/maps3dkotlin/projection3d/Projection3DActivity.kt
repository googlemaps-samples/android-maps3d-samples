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

package com.example.maps3dkotlin.projection3d

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.maps3d.common.Projection3D
import com.example.maps3d.common.toValidCamera
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Demonstrates 3D-to-2D Screen Projection using [Projection3D] in Android Views.
 *
 * Key Concepts Demonstrated:
 * 1. Perspective Screen Coordinate Transformation:
 *    - Transforms 3D landmark coordinates ([LatLngAltitude]) to 2D screen viewport pixels using
 *      [Projection3D.toScreenCoordinate] based on real-time camera parameters.
 *
 * 2. Anchoring Native Android Views over 3D World Geometry:
 *    - Dynamically translates a native 2D [CardView] overlay across $(X, Y)$ screen space to stay
 *      tightly anchored above the landmark as the camera pans, tilts, zooms, or orbits.
 *
 * 3. Frustum Clipping & Depth Detection:
 *    - Automatically hides or displays the floating callout based on whether the 3D coordinate is
 *      inside the visible camera view frustum.
 *
 * 4. Interactive Map Tap Re-Projection:
 *    - Tapping anywhere on the 3D map surface moves the focus target to the tapped point.
 */
class Projection3DActivity : SampleBaseActivity() {

    override val TAG = "Projection3DActivity"

    override val initialCamera: Camera
        get() = camera {
            center = latLngAltitude {
                latitude = SF_LANDMARKS[0].location.latitude
                longitude = SF_LANDMARKS[0].location.longitude
                altitude = 150.0
            }
            heading = 45.0
            tilt = 65.0
            roll = 0.0
            range = 800.0
        }.toValidCamera()

    // --- UI Elements ---

    private var floatingCallout: View? = null
    private var calloutTitle: TextView? = null
    private var calloutCoords: TextView? = null
    private var calloutAltitude: TextView? = null

    private var controlsCard: CardView? = null
    private var cardHeader: View? = null
    private var cardContent: View? = null
    private var btnCollapse: MaterialButton? = null

    private var tvTarget: TextView? = null
    private var tvScreenCoords: TextView? = null
    private var tvStatus: TextView? = null

    // --- State ---

    private var activeLandmarkName: String = SF_LANDMARKS[0].name
    private var activeLocation: LatLngAltitude = SF_LANDMARKS[0].location
    private var isCollapsed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide default scroll view from base skeleton
        findViewById<View>(R.id.control_scroll_view)?.visibility = View.GONE

        // Inflate Projection 3D overlay panel into map container
        findViewById<ViewGroup>(R.id.map_container)?.let { container ->
            layoutInflater.inflate(R.layout.control_panel_projection_3d, container, true)
        }

        findViewById<MaterialToolbar>(R.id.top_bar)?.apply {
            setTitle(R.string.feature_title_projection_3d)
            setNavigationOnClickListener { finish() }
        }

        initViews()
    }

    private fun initViews() {
        floatingCallout = findViewById(R.id.floating_callout)
        calloutTitle = findViewById(R.id.callout_title)
        calloutCoords = findViewById(R.id.callout_coords)
        calloutAltitude = findViewById(R.id.callout_altitude)

        controlsCard = findViewById(R.id.control_panel)
        cardHeader = findViewById(R.id.card_header)
        cardContent = findViewById(R.id.card_content)
        btnCollapse = findViewById(R.id.btn_collapse)

        tvTarget = findViewById(R.id.tv_projection_target)
        tvScreenCoords = findViewById(R.id.tv_projection_screen_coords)
        tvStatus = findViewById(R.id.tv_projection_status)

        btnCollapse?.setOnClickListener {
            toggleControls()
        }

        cardHeader?.setOnClickListener {
            toggleControls()
        }

        findViewById<Button>(R.id.btn_landmark_transamerica)?.setOnClickListener {
            selectLandmark(SF_LANDMARKS[0])
        }

        findViewById<Button>(R.id.btn_landmark_coit)?.setOnClickListener {
            selectLandmark(SF_LANDMARKS[1])
        }

        findViewById<Button>(R.id.btn_landmark_ferry)?.setOnClickListener {
            selectLandmark(SF_LANDMARKS[2])
        }
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)

        // Enable surface tap to re-project arbitrary coordinates
        googleMap3D.setMap3DClickListener { location, _ ->
            activeLandmarkName = "Custom Ground Pin"
            activeLocation = location
            googleMap3D.getCamera()?.let { cam ->
                updateProjection(cam)
            }
        }

        // Collect camera updates from base activity flow
        lifecycleScope.launch {
            cameraUpdates.collectLatest { liveCamera ->
                updateProjection(liveCamera)
            }
        }
    }

    private fun selectLandmark(landmark: LandmarkData) {
        activeLandmarkName = landmark.name
        activeLocation = landmark.location

        googleMap3D?.let { map ->
            val curr = map.getCamera()?.toValidCamera() ?: initialCamera
            map.flyCameraTo(
                flyToOptions {
                    endCamera = camera {
                        center = landmark.location
                        heading = curr.heading
                        tilt = curr.tilt
                        range = 750.0
                    }
                    durationInMillis = 1500
                }
            )
        }
    }

    private fun updateProjection(currentCamera: Camera) {
        val width = map3DView.width
        val height = map3DView.height
        if (width <= 0 || height <= 0) return

        val projection = Projection3D(
            camera = currentCamera,
            viewportWidth = width,
            viewportHeight = height,
            fovYDegrees = Projection3D.DEFAULT_FOV_Y_DEGREES
        )

        val screenCoord = projection.toScreenCoordinate(activeLocation)

        runOnUiThread {
            tvTarget?.text = getString(R.string.projection_target_format, activeLandmarkName)

            if (screenCoord.isVisible && !screenCoord.x.isNaN() && !screenCoord.y.isNaN()) {
                tvScreenCoords?.text = getString(
                    R.string.projection_screen_coords_format,
                    screenCoord.x.toInt(),
                    screenCoord.y.toInt()
                )
                tvStatus?.text = getString(R.string.projection_visible)
                tvStatus?.setTextColor(0xFF2E7D32.toInt())

                floatingCallout?.visibility = View.VISIBLE
                calloutTitle?.text = activeLandmarkName
                calloutCoords?.text = getString(
                    R.string.projection_coords_simple_format,
                    screenCoord.x.toInt(),
                    screenCoord.y.toInt()
                )
                calloutAltitude?.text = getString(
                    R.string.projection_depth_format,
                    screenCoord.depth.toInt(),
                    activeLocation.altitude.toInt()
                )

                // Anchor callout horizontally centered and pinned directly above the target coordinate
                val calloutW = floatingCallout?.width ?: 0
                val calloutH = floatingCallout?.height ?: 0
                floatingCallout?.translationX = screenCoord.x - (calloutW / 2f)
                floatingCallout?.translationY = screenCoord.y - calloutH
            } else {
                tvScreenCoords?.text = getString(R.string.projection_out_of_view)
                tvStatus?.text = getString(R.string.projection_culled)
                tvStatus?.setTextColor(0xFFC62828.toInt())
                floatingCallout?.visibility = View.GONE
            }
        }
    }

    private fun toggleControls() {
        isCollapsed = !isCollapsed
        if (isCollapsed) {
            cardContent?.visibility = View.GONE
            btnCollapse?.setIconResource(R.drawable.expand_less_24px)
        } else {
            cardContent?.visibility = View.VISIBLE
            btnCollapse?.setIconResource(R.drawable.expand_more_24px)
        }
    }

    private data class LandmarkData(
        val name: String,
        val location: LatLngAltitude
    )

    companion object {
        private val SF_LANDMARKS = listOf(
            LandmarkData("Transamerica Pyramid", LatLngAltitude(37.7952, -122.4028, 260.0)),
            LandmarkData("Coit Tower", LatLngAltitude(37.8024, -122.4058, 110.0)),
            LandmarkData("Ferry Building", LatLngAltitude(37.7955, -122.3937, 75.0))
        )
    }
}
