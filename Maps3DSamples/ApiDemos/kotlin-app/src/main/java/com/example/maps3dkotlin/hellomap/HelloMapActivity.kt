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
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.example.maps3dcommon.R


/**
 * `HelloMapActivity` is an Android activity that demonstrates the usage of the `Map3DView`.  This
 * is close the minimal activity.  It inflates the `activity_hello_map.xml` layout file and
 * demonstrates how to initialize the `Map3DView` and get a `GoogleMap3D` reference.
 */
class HelloMapActivity : Activity(), OnMap3DViewReadyCallback {
    private val TAG = this::class.java.simpleName
    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_map)

        val rootView = findViewById<View>(R.id.map_container)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            view.updatePadding(
                top = view.paddingTop + statusBarInsets.top,
                bottom = view.paddingBottom + navigationBarInsets.bottom
            )

            WindowInsetsCompat.CONSUMED
        }

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)
    }

    /**
     * Called when the [Map3DView] is ready to be used.
     *
     * This callback is triggered when the [GoogleMap3D] object is initialized and ready
     * for interaction. You can use this method to set up map features, add markers,
     * set the camera position, etc.
     *
     * @param googleMap3D The [GoogleMap3D] object representing the 3D map.
     */
    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        // Interact with the googleMap3D object here
        this.googleMap3D = googleMap3D
    }

    override fun onError(error: Exception) {
        Log.e(TAG, "Error loading map", error)
        super.onError(error)
    }

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
