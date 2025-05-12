package com.example.maps3dkotlin.sampleactivity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.annotation.CallSuper
import androidx.core.view.WindowCompat
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.common.DEFAULT_CAMERA
import com.example.maps3dkotlin.common.toCameraString
import com.example.maps3dkotlin.common.toValidCamera
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnCameraChangedListener
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Camera
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
abstract class SampleBaseActivity : Activity(), OnMap3DViewReadyCallback {
    protected lateinit var map3DView: Map3DView
    protected var googleMap3D: GoogleMap3D? = null

    protected lateinit var snapshotButton: MaterialButton
    protected lateinit var recenterButton: MaterialButton

    abstract val initialCamera: Camera
    abstract val TAG: String

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

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_common_map)

        findViewById<MaterialToolbar>(R.id.top_bar).title = title

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        snapshotButton = findViewById<MaterialButton>(R.id.snapshot_button).apply {
            setOnClickListener {
                snapshot()
            }
            visibility = android.view.View.VISIBLE
        }

        recenterButton = findViewById<MaterialButton>(R.id.reset_view_button).apply {
            setOnClickListener {
                googleMap3D?.flyCameraTo(
                    flyToOptions {
                        endCamera = initialCamera
                        durationInMillis = 2_000
                    }
                )
            }
            visibility = android.view.View.VISIBLE
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        map3DView.onResume()
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
     * Called when the [Map3DView] is ready to be used.
     *
     * This method is called after the [Map3DView] has been initialized and is ready to receive
     * commands. It sets the internal [googleMap3D] reference and sets the initial camera
     * position and orientation.
     *
     * @param googleMap3D The [GoogleMap3D] instance that is ready to be used.
     */
    @CallSuper
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        this.googleMap3D = googleMap3D

        googleMap3D.setCamera(initialCamera)
    }

    @CallSuper
    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
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
}
