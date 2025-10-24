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

package com.example.maps3dkotlin.hellomap

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

/**
 * `HelloMapActivity` serves as a foundational example for integrating `Map3DView` into an
 * Android application. It demonstrates the essential steps: inflating the layout containing the
 * map, initializing the `Map3DView`, and obtaining a reference to the `GoogleMap3D` object
 * to interact with the map programmatically.
 */
class HelloMapActivity : Activity(), OnMap3DViewReadyCallback {
    // Using a companion object for the TAG is a common Kotlin pattern. It's static, final,
    // and accessible via the class name, making it a good practice for logging.
    companion object {
        private val TAG = HelloMapActivity::class.java.simpleName
    }

    // The `lateinit` modifier is used here for map3DView because it will be initialized
    // in the onCreate method. This avoids making it nullable.
    private lateinit var map3DView: Map3DView

    // The googleMap3D object is nullable as it's only assigned when the map is ready.
    // This is a safer approach than using a nullable property with a non-null assertion.
    private var googleMap3D: GoogleMap3D? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_map)

        // The root view is identified once and then used for setting up window insets.
        // This is a clean way to handle the view hierarchy.
        val rootView = findViewById<View>(R.id.map_container)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            // We retrieve the insets for the status and navigation bars to adjust the padding.
            // This ensures that the map content is not obscured by the system UI.
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // The padding of the view is updated to account for the system bars.
            // This is a more robust way to handle window insets than manual calculations.
            view.updatePadding(
                top = view.paddingTop + statusBarInsets.top,
                bottom = view.paddingBottom + navigationBarInsets.bottom
            )

            // We consume the insets to prevent further processing by other views.
            WindowInsetsCompat.CONSUMED
        }

        // The Map3DView is initialized and its lifecycle is managed by forwarding the
        // activity's lifecycle events. This is a required step for the map to function correctly.
        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    /**
     * This callback is invoked when the `Map3DView` has been initialized and is ready for use.
     * At this point, the `GoogleMap3D` object is available, allowing for interaction with the
     * map's properties and features, such as setting the camera position or adding markers.
     *
     * @param googleMap3D The `GoogleMap3D` object that is now ready.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        // Once the map is ready, we can store a reference to the GoogleMap3D object.
        // This allows us to interact with the map later on, for example, in response to user input.
        this.googleMap3D = googleMap3D
    }

    /**
     * Handles any errors that occur during the initialization of the map. This is a critical
     * part of the lifecycle, as it allows the application to gracefully handle situations
     * where the map could not be loaded, for instance, due to a network issue or an invalid
     * API key.
     *
     * @param error The exception that occurred.
     */
    override fun onError(error: Exception) {
        // It's important to log errors to aid in debugging. In a production application,
        // you might also want to display a user-friendly message.
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
    }

    // The following methods are lifecycle callbacks that must be forwarded to the Map3DView.
    // This is a requirement of the Maps SDK to ensure that the map's state is correctly
    // managed throughout the activity's lifecycle. For example, onResume is where the map
    // will start rendering, and onPause is where it will stop.

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        super.onPause()
        map3DView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map3DView.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
}