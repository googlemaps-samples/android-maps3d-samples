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

package com.example.maps3dkotlin.sampleactivity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.maps3d.common.DEFAULT_CAMERA
import com.example.maps3d.common.Map3DQuickSettingsButton
import com.example.maps3d.common.Map3DSettings
import com.example.maps3d.common.Map3DSettingsController
import com.example.maps3d.common.Map3DUiControls
import com.google.android.gms.maps3d.model.Map3DMode
import com.example.maps3d.common.toCameraString
import com.example.maps3d.common.toValidCamera
import com.example.maps3d.common.toHeading
import com.example.maps3d.common.toTilt
import com.example.maps3d.common.toRange
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnCameraChangedListener
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Base activity for sample map activities.
 *
 * This abstract class provides common functionality and setup for activities that display
 * a 3D map using the Google Maps 3D API. It handles lifecycle events for the [Map3DView],
 * manages the [GoogleMap3D] instance, and includes utility buttons for taking snapshots
 * and recentering the map.
 *
 * Subclasses must implement:
 * - [initialCamera]: The initial camera position and orientation for the map.
 * - [TAG]: A unique tag for logging purposes.
 *
 * Subclasses can optionally override the following methods (with `@CallSuper` annotation):
 * - [onCreate] : to extend the base behavior.
 * - [onResume]: to extend the base behavior.
 * - [onPause]: to extend the base behavior.
 * - [onDestroy]: to extend the base behavior.
 * - [onSaveInstanceState]: to extend the base behavior.
 * - [onMap3DViewReady]: to extend the base behavior.
 * - [onError]: to extend the base behavior.
 *
 * The activity layout includes a [Map3DView], a snapshot button, and a recenter button.
 */
abstract class SampleBaseActivity : AppCompatActivity(), OnMap3DViewReadyCallback {
    protected lateinit var map3DView: Map3DView
    protected var googleMap3D: GoogleMap3D? = null

    protected lateinit var snapshotButton: MaterialButton
    protected lateinit var recenterButton: MaterialButton

    abstract val initialCamera: Camera
    abstract val TAG: String

    @get:LayoutRes
    protected open val layoutResId: Int = R.layout.activity_common_map

    // Private mutable state to hold the current camera position
    // This is updated by the listener when it's active.
    private val _currentCamera = MutableStateFlow(DEFAULT_CAMERA)

    // Public Flow that manages the listener lifecycle
    val cameraUpdates: Flow<Camera> = callbackFlow {
        val cameraChangedListener = OnCameraChangedListener { cameraPosition ->
            // Send the new camera position to the flow's channel
            trySend(cameraPosition)
            // Also update the private state
            _currentCamera.value = cameraPosition
        }

        // Get the current map instance (ensure it's not null before setting listener)
        val map = googleMap3D
        if (map != null) {
            // Set the listener when the flow is collected
            Log.d(TAG, "Attaching CameraChangeListener")
            map.setCameraChangedListener(cameraChangedListener)

            // Ensure the initial camera position is emitted when the flow is collected
            // This handles cases where the map is ready before the flow is collected
            map.getCamera()?.let { initial ->
                trySend(initial)
                _currentCamera.value = initial // Also update private state on collection
            }
        } else {
            // Handle case where map is not ready when flow is collected
            // This might require a different approach, e.g., collecting after onMap3DViewReady
            // For simplicity in this implementation, we assume map is ready or will be ready
            // when the flow is collected, or the initial state is sufficient until then.
            // A more robust solution might involve combining flows or waiting for the map.
            Log.w(TAG, "googleMap3D is null when callbackFlow is collected.")
            // Emit the default or initial camera if map is null? Or rely on onMap3DViewReady?
            trySend(DEFAULT_CAMERA) // Emit initial default value
        }


        // The awaitClose block runs when the collector is cancelled
        awaitClose {
            // Remove the listener when the flow collection stops
            Log.d(TAG, "Detaching CameraChangeListener")
            googleMap3D?.setCameraChangedListener(null)
        }
    }

    private var lastStatusBarTop: Int = 0

    private fun applyStatusBarInsetsToMapControls(
        statusBarTop: Int,
        uiControls: Map3DUiControls? = null,
    ) {
        if (statusBarTop > 0) {
            lastStatusBarTop = statusBarTop
        }
        if (::map3DView.isInitialized) {
            Map3DSettings.applyStatusBarInsetsToMapView(map3DView, lastStatusBarTop)
        }
        // Keep the Quick Settings FAB directly below the active trailing controls
        findViewById<Map3DQuickSettingsButton>(R.id.quick_settings_button)?.let { button ->
            val currentUi = uiControls ?: button.getSettings().uiControls
            button.statusBarTopOffsetPx = lastStatusBarTop
            button.updateTopMarginForUiControls(currentUi)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        bindWindowInsetsAndMapHierarchy()
    }

    private fun bindWindowInsetsAndMapHierarchy() {
        val rootView = findViewById<View?>(R.id.map_container)
        val hasOverlayAppBar = findViewById<View?>(R.id.app_bar_layout) != null
        if (!hasOverlayAppBar) {
            lastStatusBarTop = 0
            findViewById<Map3DView?>(R.id.map3dView)?.let { currentMapView ->
                Map3DSettings.applyStatusBarInsetsToMapView(currentMapView, 0)
            }
            findViewById<Map3DQuickSettingsButton?>(R.id.quick_settings_button)?.let { button ->
                button.statusBarTopOffsetPx = 0
                button.updateTopMarginForUiControls()
            }
            return
        }

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
                val statusBarInsets = insets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout(),
                )
                val navInsets = insets.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.navigationBars(),
                )

                findViewById<View>(R.id.app_bar_layout)?.updatePadding(top = statusBarInsets.top)
                applyStatusBarInsetsToMapControls(statusBarInsets.top)

                findViewById<View>(R.id.control_scroll_view)?.let { scrollView ->
                    val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
                    val margin16dp = (16 * scrollView.resources.displayMetrics.density).toInt()
                    layoutParams.bottomMargin = navInsets.bottom + margin16dp
                    scrollView.layoutParams = layoutParams
                }

                WindowInsetsCompat.CONSUMED
            }
        }

        findViewById<Map3DView?>(R.id.map3dView)?.let { currentMapView ->
            Map3DSettings.applyStatusBarInsetsToMapView(currentMapView, lastStatusBarTop)
        }
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableImmersiveMode()

        setContentView(layoutResId)
        val topBar = findViewById<MaterialToolbar>(R.id.top_bar)

        topBar?.title = title

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        findViewById<MaterialButton>(R.id.snapshot_button)?.let { button ->
            snapshotButton = button.apply {
                setOnClickListener {
                    snapshot()
                }
                visibility = View.VISIBLE
            }
        }

        findViewById<MaterialButton>(R.id.reset_view_button)?.let { button ->
            recenterButton = button.apply {
                setOnClickListener {
                    googleMap3D?.flyCameraTo(
                        flyToOptions {
                            endCamera = initialCamera
                            durationInMillis = 2_000
                        },
                    )
                }
                visibility = View.VISIBLE
            }
        }
    }

    /**
     * Enables sticky immersive mode so the 3D map canvas occupies the entire display,
     * hiding status and navigation bars while allowing transient reveal on edge swipes.
     */
    protected fun enableImmersiveMode() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
        map3DView.onResume()
        googleMap3D?.let { map ->
            val currentSettings = Map3DSettingsController.shared.settings.value
            currentSettings.applyTo(map)
            applyStatusBarInsetsToMapControls(lastStatusBarTop, currentSettings.uiControls)
            onQuickSettingsChanged(currentSettings)
        }
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        map3DView.onPause()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        map3DView.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    @CallSuper
    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }

    /**
     * Default Map Mode for this sample when the user has not explicitly overridden the mode in the
     * current app session.
     */
    @Map3DMode
    protected open val defaultMapMode: Int = Map3DMode.HYBRID

    /**
     * Hook invoked whenever Quick Settings change so subclasses with inline controls can stay in
     * sync.
     */
    protected open fun onQuickSettingsChanged(settings: Map3DSettings) {}

    /**
     * Called when the [Map3DView] is initialized and the map scene is ready.
     *
     * This method is called from within [GoogleMap3D.setOnMapReadyListener] to ensure that
     * the map scene is fully loaded and ready for 3D content additions (markers, models, etc.).
     *
     * Subclasses should override this method to add their content.
     *
     * @param googleMap3D The [GoogleMap3D] instance that is ready to be used.
     */
    private var isMapInitialized = false

    @CallSuper
    protected open fun onMapReady(googleMap3D: GoogleMap3D) {
        if (isMapInitialized) return
        isMapInitialized = true
        Log.d(TAG, "onMapReady called (guaranteed once)")

        googleMap3D.setCamera(initialCamera)
        Map3DSettingsController.shared.onSampleEntered(defaultMapMode)
        val initialSettings = Map3DSettingsController.shared.settings.value
        initialSettings.applyTo(googleMap3D)

        findViewById<Map3DQuickSettingsButton?>(R.id.quick_settings_button)?.apply {
            settingsController = Map3DSettingsController.shared
            sampleDefaultMapMode = defaultMapMode
            attachMap(googleMap3D)
            setOnSettingsChangedListener { updatedSettings ->
                applyStatusBarInsetsToMapControls(lastStatusBarTop, updatedSettings.uiControls)
                onQuickSettingsChanged(updatedSettings)
            }
        }
        applyStatusBarInsetsToMapControls(lastStatusBarTop, initialSettings.uiControls)
        onQuickSettingsChanged(initialSettings)

        // Workaround: The Maps 3D SDK onMapReady callback fires when the map object
        // is instantiated, but the internal native rendering pipeline and layout pass may briefly
        // override control hierarchies. A short delay ensures
        // the native map viewport has fully stabilized before ensuring UI controls are present.
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isDestroyed && !isFinishing) {
                this.googleMap3D?.let { map ->
                    val currentSettings = Map3DSettingsController.shared.settings.value
                    currentSettings.applyTo(map)
                    applyStatusBarInsetsToMapControls(lastStatusBarTop, currentSettings.uiControls)
                    onQuickSettingsChanged(currentSettings)
                }
            }
        }, 350L)
    }

    @CallSuper
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D
        Log.d(TAG, "onMap3DViewReady called")
        Log.d(TAG, "Setting initial camera: " + initialCamera.toCameraString())
        // Wire up the standardized listener
        googleMap3D.setOnMapReadyListener { sceneReadiness->
            Log.w(TAG, "on map ready listener $sceneReadiness")
            googleMap3D.setOnMapReadyListener(null)
            onMapReady(googleMap3D)
        }

        // Mark map view as steady when rendering stabilizes for automated visual tests
        googleMap3D.setOnMapSteadyListener { isSceneSteady ->
            if (isSceneSteady) {
                map3DView.contentDescription = "MapSteady"
            }
        }

        // Ensure onMapReady triggers even on delayed or reused fragments.
        lifecycleScope.launch {
            delay(2000)
            onMapReady(googleMap3D)
        }
    }

    @CallSuper
    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
    }

    /**
     * Captures a snapshot of the current camera position and logs it.
     *
     * This function retrieves the current camera from the [googleMap3D] instance,
     * converts it to a string representation using [toCameraString], and then logs
     * the string to the console using [Log.d]. This is useful for debugging
     * and understanding the current state of the map's camera.
     *
     * The output may be pasted as code to create a camera object with the same location and
     * orientation.
     */
    protected fun snapshot() {
        googleMap3D?.getCamera()?.let { camera ->
            snapshot(camera.toValidCamera())
        }
    }

    protected fun snapshot(camera: Camera) {
        Log.d(TAG, camera.toCameraString())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val map = googleMap3D ?: return super.onKeyDown(keyCode, event)
        val currentCamera = map.getCamera() ?: return super.onKeyDown(keyCode, event)

        var dTilt = 0.0
        var dHeading = 0.0
        var dRange = 0.0

        val incrementAmount = 5.0
        val rangeIncrementAmount = 200.0
        var handled = true

        when (keyCode) {
            KeyEvent.KEYCODE_W -> dTilt = -incrementAmount
            KeyEvent.KEYCODE_S -> dTilt = incrementAmount
            KeyEvent.KEYCODE_A -> dHeading = -incrementAmount
            KeyEvent.KEYCODE_D -> dHeading = incrementAmount
            KeyEvent.KEYCODE_Z -> dRange = -rangeIncrementAmount
            KeyEvent.KEYCODE_X -> dRange = rangeIncrementAmount
            else -> handled = false
        }

        if (handled) {
            val newCamera = camera {
                center = currentCamera.center
                heading = ((currentCamera.heading ?: 0.0) + dHeading).toHeading()
                tilt = ((currentCamera.tilt ?: 0.0) + dTilt).toTilt()
                roll = currentCamera.roll
                range = ((currentCamera.range ?: 0.0) + dRange).toRange()
            }
            map.setCamera(newCamera)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
