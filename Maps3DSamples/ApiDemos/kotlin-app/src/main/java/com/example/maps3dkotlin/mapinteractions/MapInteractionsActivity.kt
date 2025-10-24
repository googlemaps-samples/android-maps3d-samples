package com.example.maps3dkotlin.mapinteractions

import android.widget.Toast
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        super.onMap3DViewReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        CoroutineScope(Dispatchers.Main).launch {
            googleMap3D.setMap3DClickListener { location, placeId ->
                if (placeId != null) {
                    showToast("Clicked on place with ID: $placeId")
                } else {
                    showToast("Clicked on location: $location")
                }
            }
        }
    }

    fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@MapInteractionsActivity, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val BOULDER_LATITUDE = 40.029349
        private const val BOULDER_LONGITUDE = -105.300354
    }
}