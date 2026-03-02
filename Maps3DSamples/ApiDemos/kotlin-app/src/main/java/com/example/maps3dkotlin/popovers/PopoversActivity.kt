package com.example.maps3dkotlin.popovers

import android.graphics.Color
import android.graphics.Point
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import com.example.maps3dkotlin.sampleactivity.SampleBaseActivity
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle

class PopoversActivity : SampleBaseActivity() {
    override val TAG = "PopoversActivity"

    // San Francisco
    private val CONTENT_LAT = 37.820642
    private val CONTENT_LNG = -122.478227
    private val CONTENT_ALT = 0.0

    private var popover: Popover? = null
    private var popoverToggleCount = 0

    override val initialCamera = camera {
        center = latLngAltitude {
            latitude = CONTENT_LAT
            longitude = CONTENT_LNG
            altitude = CONTENT_ALT
        }
        heading = 0.0
        tilt = 45.0
        range = 4075.0
    }

    override fun onMapReady(googleMap3D: GoogleMap3D) {
        super.onMapReady(googleMap3D)
        googleMap3D.setMapMode(Map3DMode.SATELLITE)
        setupPopover(googleMap3D)
    }

    fun setupPopover(googleMap3D: GoogleMap3D) {
        val center = latLngAltitude {
            latitude = 37.819852
            longitude = -122.478549
            altitude = 0.0
        }

        val markerInGoldenGate =
            googleMap3D.addMarker(
                markerOptions {
                    position = center
                    label = "Golden Gate Bridge"
                    zIndex = 1
                    isExtruded = true
                    isDrawnWhenOccluded = true
                    collisionBehavior = CollisionBehavior.REQUIRED
                    altitudeMode = AltitudeMode.RELATIVE_TO_MESH
                }
            )

        if (markerInGoldenGate == null) {
            Log.e(TAG, "Failed to create marker")
            return
        } else {
            Log.w(TAG, "Marker created")
        }

        val popoverOptions = popoverOptions {
            content = createGoldenGateInfoView()
            positionAnchor = markerInGoldenGate
            autoPanEnabled = true
            autoCloseEnabled = true

            anchorOffset = Point(0, 0)
            popoverStyle = popoverStyle {
                padding = 20.0f
                backgroundColor = Color.WHITE
                borderRadius = 8.0f
                shadow = popoverShadow {
                    color = Color.argb(77, 0, 0, 0) // 0.3 * 255 = 76.5 ~= 77
                    offsetX = 2.0f
                    offsetY = 4.0f
                    radius = 4.0f
                }
            }
        }
        popover = googleMap3D.addPopover(popoverOptions)
        markerInGoldenGate.let {
            it.setClickListener {
                Log.d(TAG, "Marker clicked")
                if (popoverToggleCount > 5) {
                    runOnUiThread { popover?.remove() }
                    Log.d(TAG, "Popover removed")
                    popoverToggleCount = 0
                } else {
                    Log.d(TAG, "Popover toggled")
                    runOnUiThread { popover?.toggle() }
                    popoverToggleCount++
                }
            }
        }

        Log.d(TAG, "Popover created")
    }

    private fun createGoldenGateInfoView(): View {
        val context = this
        val layout =
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams =
                    LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }

        val titleView =
            TextView(context).apply {
                text = "The Golden Gate Bridge"
                textSize = 18f
                setTextColor(Color.BLACK)
            }
        layout.addView(titleView)

        val headlineView =
            TextView(context).apply {
                text = "San Francisco, CA"
                textSize = 14f
                setTextColor(Color.DKGRAY)
            }
        layout.addView(headlineView)

        val contentView =
            TextView(context).apply {
                text =
                    "The Golden Gate Bridge is a suspension bridge\n" +
                            " spanning the one-mile-wide strait connecting\n" +
                            " San Francisco Bay and the Pacific Ocean.\n" +
                            " The bridge was completed in 1937."
                textSize = 12f
                setTextColor(Color.GRAY)
            }
        layout.addView(contentView)

        return layout
    }

}
