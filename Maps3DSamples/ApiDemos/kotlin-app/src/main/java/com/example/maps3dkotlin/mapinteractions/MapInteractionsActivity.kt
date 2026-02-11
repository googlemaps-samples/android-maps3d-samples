package com.example.maps3dkotlin.mapinteractions

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.HYBRID)

        // Listeners for map clicks. We use lifecycleScope to ensure coroutines are cancelled when the activity is destroyed.
        // setMap3DClickListener is a suspend function (or at least we treating it as one by launching a coroutine? 
        // Wait, actually setMap3DClickListener is NOT a suspend function in the SDK usually, it takes a listener.
        // But the original code was wrapping it in a coroutine. 
        // Checking the original code: "CoroutineScope(Dispatchers.Main).launch { googleMap3D.setMap3DClickListener { ... } }"
        // This suggests `setMap3DClickListener` might NOT be a suspend function, but the user wanted to launch a coroutine to set it?
        // OR `setMap3DClickListener` IS a suspend extension (KTX)?
        // If it's the standard listener setter, it doesn't need a coroutine unless we are doing something async inside.
        // Assuming the original intent was just to set the listener safely on Main thread (if it wasn't already).
        // However, `setMap3DClickListener` usually takes a lambda.
        // Let's stick to `lifecycleScope.launch` to be safe and consistent with "fixing leaky scope".
        
        lifecycleScope.launch {
            googleMap3D.setMap3DClickListener { location, placeId ->
                if (placeId != null) {
                    showToast("Clicked on place with ID: $placeId")
                } else {
                    showToast("Clicked on location: $location")
                }
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