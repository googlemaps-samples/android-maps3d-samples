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

package com.example.maps3dkotlin.mapinteractions

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.launch
import com.example.maps3dcommon.R

class MapInteractionsActivity : SampleBaseActivity() {
    override val TAG = this::class.java.simpleName
    override val initialCamera = camera {
        center = latLngAltitude {
            latitude = BOULDER_LATITUDE
            longitude = BOULDER_LONGITUDE
            altitude = 1833.9
        }
        heading = 326.0
        tilt = 75.0
        range = 3757.0
    }

    private lateinit var clickedInfoText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_map_interactions)

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        clickedInfoText = findViewById(R.id.clicked_info_text)
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        // Listeners for map clicks. We use lifecycleScope to ensure coroutines are cancelled when the activity is destroyed.
        lifecycleScope.launch {
            googleMap3D.setMap3DClickListener { location, placeId ->
                val message = if (placeId != null) {
                    "Clicked Place ID: $placeId"
                } else {
                    "Clicked Location: ${location.latitude}, ${location.longitude}"
                }
                clickedInfoText.text = message
                clickedInfoText.contentDescription = message
                showToast(message)
            }
        }
    }

    private fun showToast(message: String) {
        lifecycleScope.launch {
            Toast.makeText(this@MapInteractionsActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val BOULDER_LATITUDE = 40.029349
        private const val BOULDER_LONGITUDE = -105.300354
    }
}