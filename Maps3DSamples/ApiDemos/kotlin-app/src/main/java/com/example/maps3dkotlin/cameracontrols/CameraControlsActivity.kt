package com.example.maps3dkotlin.cameracontrols

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.WindowCompat
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.google.android.gms.maps3d.model.Polygon
import com.example.maps3dkotlin.cameracontrols.DataModel.nycCameraRestriction
import com.example.maps3dkotlin.cameracontrols.DataModel.nycPolygonOptions
import com.example.maps3dkotlin.cameracontrols.DataModel.EMPIRE_STATE_BUILDING_LATITUDE
import com.example.maps3dkotlin.cameracontrols.DataModel.EMPIRE_STATE_BUILDING_LONGITUDE
import com.example.maps3dkotlin.common.copy
import com.example.maps3dkotlin.common.toCompassDirection
import com.example.maps3dkotlin.common.toValidCamera
import com.example.maps3dkotlin.common.wrapIn
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.material.slider.Slider

class CameraControlsActivity : Activity(), OnMap3DViewReadyCallback {
    private lateinit var map3DView: Map3DView
    private var googleMap3D: GoogleMap3D? = null
    private lateinit var cameraStateText: TextView
    private lateinit var rollSlider: Slider
    private lateinit var rollSliderLabel: TextView

    private var restrictionCubeFaces: MutableList<Polygon> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_camera_controls)

        map3DView = findViewById(R.id.map3dView)
        map3DView.onCreate(savedInstanceState)
        map3DView.getMap3DViewAsync(this)

        cameraStateText = findViewById(R.id.camera_state_text)

        rollSlider = findViewById(R.id.roll_slider)
        rollSliderLabel = findViewById(R.id.roll_slider_label)

        updateRollSliderLabel(rollSlider.value)

        findViewById<MaterialButton>(R.id.fly_around).setOnClickListener {
            flyAroundCurrentCenter()
        }

        findViewById<MaterialButton>(R.id.fly_to).setOnClickListener {
            flyToEmpireStateBuilding()
        }

        findViewById<MaterialButton>(R.id.toggle_restriction).setOnClickListener { view ->
            val button = view as MaterialButton

            if (button.isChecked) {
                button.text = getText(R.string.camera_remove_restriction)
                googleMap3D?.setCameraRestriction(nycCameraRestriction)
            } else {
                button.text = getText(R.string.camera_activate_restriction)
                googleMap3D?.setCameraRestriction(null)
            }
        }

        findViewById<MaterialButton>(R.id.show_restriction).setOnClickListener { view ->
            val button = view as MaterialButton

            if (button.isChecked) {
                button.text = getText(R.string.camera_hide_restriction)

                // Clear any existing cube face polygons
                restrictionCubeFaces.forEach { it.remove() }
                restrictionCubeFaces.clear()

                // Create and add each face of the cube
                restrictionCubeFaces.addAll(
                    nycPolygonOptions.mapNotNull { polygonOptions ->
                        googleMap3D?.addPolygon(polygonOptions)
                    }
                )
            } else {
                button.text = getText(R.string.camera_show_restriction)
                restrictionCubeFaces.forEach { it.remove() }
                restrictionCubeFaces.clear()
            }
        }

        // Set listener for RadioGroup
        findViewById<RadioGroup>(R.id.map_mode_radio_group).setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.map_mode_hybrid -> googleMap3D?.setMapMode(Map3DMode.HYBRID)
                R.id.map_mode_satellite -> googleMap3D?.setMapMode(Map3DMode.SATELLITE)
            }
        }

        rollSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateRollSliderLabel(value)
                updateMapRoll(value.toDouble())
            }
        }

        // Set listener for Reset Roll Button
        findViewById<MaterialButton>(R.id.reset_roll_button).setOnClickListener {
            val resetValue = 0.0f
            rollSlider.value = resetValue
            updateRollSliderLabel(resetValue)
            updateMapRoll(resetValue.toDouble())
        }
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

    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }

    private fun updateRollSliderLabel(value: Float) {
        rollSliderLabel.text = getString(R.string.camera_roll_label_dynamic, value)
    }

    private fun updateMapRoll(newRoll: Double) {
        googleMap3D?.getCamera()?.let { currentCamera ->
            googleMap3D?.setCamera(
                currentCamera.toValidCamera().copy(roll = newRoll)
            )
        }
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
        this.googleMap3D = googleMap3D

        // Set the initial camera position
        googleMap3D.getCamera()?.let { camera ->
            updateCameraPosition(camera)
        }

        // Track camera position changes
        googleMap3D.setCameraChangedListener { cameraPosition ->
            updateCameraPosition(cameraPosition)
        }

        googleMap3D.setOnMapSteadyListener { isSteady ->
            if (isSteady) {
                googleMap3D.setOnMapSteadyListener(null)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000.milliseconds)
                    flyToEmpireStateBuilding()
                }
            }
        }
    }

    /**
     * Initiates a camera flight animation to the Empire State Building.
     *
     * This function uses the [GoogleMap3D.flyCameraTo] method to move the camera's viewpoint
     * smoothly to the Empire State Building's location. The camera is configured
     * with specific latitude, longitude, altitude, heading, tilt, and range values
     * to provide an engaging view of the landmark.
     *
     * The flight duration is set to 2 seconds.
     *
     * This function assumes that the [googleMap3D] object is initialized and ready for use.
     */
    private fun flyToEmpireStateBuilding() {
        googleMap3D?.flyCameraTo(
            flyToOptions {
                endCamera = camera {
                    center = latLngAltitude {
                        latitude = EMPIRE_STATE_BUILDING_LATITUDE
                        longitude = EMPIRE_STATE_BUILDING_LONGITUDE
                        altitude = 212.0
                    }
                    heading = 34.0
                    tilt = 67.0
                    range = 750.0
                    roll = 0.0
                }
                durationInMillis = 2_000
            }
        )
    }

    /**
     * Initiates a camera fly-around animation around the current camera's center.
     *
     * This function retrieves the current camera position from the map and then
     * starts a fly-around animation using the [GoogleMap3D.flyCameraAround] method. The animation
     * will orbit around the current center point, with the specified duration and number of rounds.
     *
     * If the map or the current camera position is not available, this function
     * will do nothing.
     *
     * The fly-around animation parameters are:
     * - `center`: The current camera position (latitude, longitude, altitude).
     * - `durationInMillis`: The duration of the fly-around animation in milliseconds (5000 ms).
     * - `rounds`: The number of times the camera will orbit around the center (1.0 round).
     */
    private fun flyAroundCurrentCenter() {
        val camera = googleMap3D?.getCamera()?.toValidCamera() ?: return

        googleMap3D?.flyCameraAround(
            flyAroundOptions {
                center = camera
                durationInMillis = 5_000
                rounds = 1.0
            }
        )
    }

    /**
     * Updates the camera state text view with the current camera's position information.
     *
     * This function takes a [Camera] object and extracts its center coordinates (latitude,
     * longitude, altitude), heading, tilt, and range. It then formats these values into a
     * string that is displayed in the `cameraStateText` TextView.
     *
     * It also appends the compass direction based on the camera's heading.
     *
     * The update to the TextView is performed on the main thread using a coroutine.
     *
     * @param camera The [Camera] object representing the current camera position.
     */
    private fun updateCameraPosition(camera: Camera) {
        val nbsp = "\u00A0"

        val cameraStateString = buildString {
            // Latitude
            append(getString(R.string.cam_lat_label, camera.center.latitude))
            append(", ")

            // Longitude
            append(getString(R.string.cam_lng_label, camera.center.longitude))
            append(", ")

            // Altitude
            append(getString(R.string.cam_alt_label, camera.center.altitude))
            append(",\n") // Newline after altitude

            // Heading
            append(getString(R.string.cam_hdg_label, camera.heading ?: 0.0))
            val compassString = (camera.heading ?: 0.0).toCompassDirection() // Assuming this utility exists
            append("$nbsp($compassString)")
            append(", ")

            // Tilt
            append(getString(R.string.cam_tlt_label, camera.tilt ?: 0.0))
            append(", ")

            // Range
            append(getString(R.string.cam_rng_label, camera.range ?: 0.0))
        }

        val resetValue = (camera.roll ?: 0.0).toFloat().wrapIn(-180f .. 180f)

        CoroutineScope(Dispatchers.Main).launch {
            cameraStateText.text = cameraStateString
            rollSlider.value = resetValue
            updateRollSliderLabel(resetValue)
        }
    }

    override fun onError(error: Exception) {
        Log.e("HelloMapActivity", "Error loading map", error)
        super.onError(error)
    }
}
