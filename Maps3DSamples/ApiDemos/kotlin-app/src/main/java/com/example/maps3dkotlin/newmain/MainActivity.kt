package com.example.maps3dkotlin.newmain

import android.app.Activity
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnCameraAnimationEndListener
import com.google.android.gms.maps3d.OnCameraChangedListener
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.OnMapReadyListener
import com.google.android.gms.maps3d.OnMapSteadyListener
import com.google.android.gms.maps3d.Popover
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.Glyph
import com.google.android.gms.maps3d.model.ImageView
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.PinConfiguration
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.cameraRestriction
import com.google.android.gms.maps3d.model.flyAroundOptions
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.latLngBounds
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.popoverOptions
import com.google.android.gms.maps3d.model.popoverShadow
import com.google.android.gms.maps3d.model.popoverStyle
import com.google.android.gms.maps3d.model.vector3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.logging.Logger

/** Main activity of the GMS 3D Maps SDK demo app. */
class MainActivity : AppCompatActivity(), OnMap3DViewReadyCallback {

  private lateinit var view: Map3DView
  private lateinit var flyToButton: Button
  private lateinit var flyAroundButton: Button
  private lateinit var updateCameraButton: Button
  private lateinit var toggleRestrictionButton: ToggleButton
  private lateinit var stateButton: Button
  private var stateIndex = 0
  private lateinit var popover: Popover
  private lateinit var currentCamera: Camera
  private var popoverToggleCount = 0

  private val cameraRestriction = cameraRestriction {
    minAltitude = 0.0
    maxAltitude = 10000000.0
    minHeading = 0.0
    maxHeading = 360.0
    minTilt = 0.0
    maxTilt = 90.0
    bounds = latLngBounds {
      northEastLat = 47.638
      northEastLng = -122.145
      southWestLat = 47.585
      southWestLng = -122.238
    }
  }

  private val modelOptions = modelOptions {
    position = BALLOON_COORDINATES
    url = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/balloon-pin-BlXF32yD.glb"
    altitudeMode = AltitudeMode.ABSOLUTE
    scale = vector3D {
      x = 10.0
      y = 10.0
      z = 10.0
    }
    orientation = orientation {
      heading = 0.0
      tilt = 0.0
      roll = 0.0
    }
  }

  private var markerOptionsInAvdaDeBrasil = markerOptions {
    position = AVDA_DE_BRASIL_COORDINATES
    zIndex = 1
    isExtruded = true
    label = "Avda De Brasil Park, Spain"
    isDrawnWhenOccluded = true
  }

  private var markerOptionsInElViso = markerOptions {
    position = EL_VISO_COORDINATES
    zIndex = 1
    isExtruded = true
    label = "El Viso, Spain"
    isDrawnWhenOccluded = true
  }

  private val markerOptionsInIrishRover = markerOptions {
    position = IRISH_ROVER_COORDINATES
    zIndex = 1
    isExtruded = true
    label = "Irish Rover, Spain"
    isDrawnWhenOccluded = true
  }

  private var markerOptionsInMadrid = markerOptions {
    position = SANTIAGO_BERNABEU_COORDINATES
    zIndex = 1
    isExtruded = true
    label = "Madrid, Spain"
    isDrawnWhenOccluded = true
  }

  private var markerOptionsInSanRafael = markerOptions {
    position = SAN_RAFAEL_HOSPITAL_COORDINATES
    zIndex = 1
    isExtruded = true
    label = "San Rafael, Spain"
    isDrawnWhenOccluded = true
  }

  private lateinit var balloonModel: Model
  private lateinit var markerInAvdaDeBrasil: Marker
  private lateinit var markerInElViso: Marker
  private lateinit var markerInIrishRover: Marker
  private lateinit var markerInMadrid: Marker
  private lateinit var markerInSanRafael: Marker

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Start measurement
    setContentView(R.layout.main)

    view = findViewById<Map3DView>(R.id.map3dView)
    // view.onCreate(savedInstanceState) // Moved to background coroutine
    flyToButton = findViewById<Button>(R.id.flyToButton)
    flyAroundButton = findViewById<Button>(R.id.flyAroundButton)
    updateCameraButton = findViewById<Button>(R.id.updateCameraButton)
    toggleRestrictionButton = findViewById<ToggleButton>(R.id.toggleCameraRestrictionButton)

    val glyphImage = Glyph.fromColor(Color.YELLOW)
    glyphImage.setImage(ImageView(R.drawable.ook))
    val glyphText = Glyph.fromColor(Color.YELLOW)
    glyphText.setText("ABCDEFGHIJKLMNOPQ")
    markerOptionsInAvdaDeBrasil.setStyle(
      PinConfiguration.builder().setScale(8f).setGlyph(glyphImage).build()
    )
    markerOptionsInElViso.setStyle(PinConfiguration.builder().setGlyph(glyphText).build())
    markerOptionsInIrishRover.setStyle(
      PinConfiguration.builder().setScale(0.5f).setGlyph(Glyph.fromColor(Color.YELLOW)).build()
    )
    markerOptionsInMadrid.setStyle(ImageView(R.drawable.ook))
    markerOptionsInSanRafael.setStyle(
      PinConfiguration.builder()
        .setBackgroundColor(Color.BLUE)
        .setBorderColor(Color.GREEN)
        .setGlyph(glyphImage)
        .build()
    )
    Log.d(TAG, "onCreate")
    
    // Experiment: Initialize Map3DView on IO thread to avoid Main thread freeze during SDK loading.
    lifecycleScope.launch(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing Map3DView on IO thread")
            view.onCreate(savedInstanceState)
            view.getMap3DViewAsync(this@MainActivity)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Map3DView on IO", e)
        }
    }
  }

  override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
    googleMap3D.setMap3DClickListener { location: LatLngAltitude, placeId: String? ->
      Log.d(
        TAG,
        "onMap3DClick: ${location.latitude}, ${location.longitude}, ${location.altitude}, ${placeId}",
      )
    }

    // LINT.IfChange()
    currentCamera = camera {
      center = latLngAltitude {
        latitude = 37.841157
        longitude = -122.551679
        altitude = 10000.0
      }
      heading = 330.0
      tilt = 75.0
      roll = 0.0
      range = 2000.0
    }
    // LINT.ThenChange(//depot/google3/java/com/google/android/gmscore/integ/testapps/maps3d/basic/res/layout/main.xml)

    flyToButton.setOnClickListener {
      Log.d(TAG, "flyToButton clicked")
      val flyToSF = flyToOptions {
        endCamera = camera {
          center = SF_COORDINATES
          heading = 0.0
          tilt = 0.0
          roll = 0.0
          range = 0.0
        }
        durationInMillis = 10000
      }
      googleMap3D.stopCameraAnimation()
      googleMap3D.flyCameraTo(flyToSF)
    }
    flyAroundButton.setOnClickListener {
      Log.d(TAG, "flyAroundButton clicked")
      val flyAroundMadrid = flyAroundOptions {
        center = camera {
          center = SANTIAGO_BERNABEU_COORDINATES
          heading = 0.0
          tilt = 0.0
          roll = 0.0
          range = 0.0
        }
        durationInMillis = 10000
        rounds = 1.0
      }
      googleMap3D.stopCameraAnimation()
      googleMap3D.flyCameraAround(flyAroundMadrid)
    }
    updateCameraButton.setOnClickListener {
      Log.d(TAG, "updateCameraButton clicked")
      googleMap3D.stopCameraAnimation()
      googleMap3D.setCamera(
        camera =
          camera {
            center = MIAMI_COORDINATES
            heading = 0.0
            tilt = 0.0
            roll = 0.0
            range = 0.0
          }
      )
    }

    googleMap3D.setCameraAnimationEndListener {
      Logger.getLogger(TAG).fine("onCameraAnimationEnd ")
    }

    googleMap3D.setCameraChangedListener { camera ->
      this@MainActivity.currentCamera = camera
      Logger.getLogger(TAG)
        .fine("onCameraChanged: ${camera.center.latitude}, ${camera.center.longitude}")
    }

    googleMap3D.setOnMapReadyListener { sceneReadiness ->
      Logger.getLogger(TAG).fine("onMapReady sceneReadiness: ${sceneReadiness}")
      // Use lifecycleScope to run heavy 3D object creation on a background thread.
      lifecycleScope.launch(Dispatchers.Default) {
        if (!::balloonModel.isInitialized) {
          balloonModel = googleMap3D.addModel(modelOptions)
          balloonModel.setClickListener { Log.d(TAG, "Map3DModel onModelClick") }
        }
        if (!::markerInAvdaDeBrasil.isInitialized) {
          googleMap3D.addMarker(markerOptionsInAvdaDeBrasil)?.let {
            markerInAvdaDeBrasil = it
            it.setClickListener {
              Log.d(TAG, "Map3DMarker onMarkerClick markerInAvdaDeBrasil")
            }
          }
        }
        if (!::markerInElViso.isInitialized) {
          googleMap3D.addMarker(markerOptionsInElViso)?.let {
            markerInElViso = it
            it.setClickListener {
              Log.d(TAG, "Map3DMarker onMarkerClick markerInElViso")
            }
          }
        }
        if (!::markerInIrishRover.isInitialized) {
          googleMap3D.addMarker(markerOptionsInIrishRover)?.let {
            markerInIrishRover = it
            it.setClickListener {
              Log.d(TAG, "Map3DMarker onMarkerClick markerInIrishRover")
            }
          }
        }
        if (!::markerInMadrid.isInitialized) {
          googleMap3D.addMarker(markerOptionsInMadrid)?.let {
            markerInMadrid = it
            it.setClickListener {
              Log.d(TAG, "Map3DMarker onMarkerClick markerInMadrid")
            }
          }
        }
        if (!::markerInSanRafael.isInitialized) {
          googleMap3D.addMarker(markerOptionsInSanRafael)?.let {
            markerInSanRafael = it
            it.setClickListener {
              Log.d(TAG, "Map3DMarker onMarkerClick markerInSanRafael")
            }
          }
        }
        if (!::popover.isInitialized) {
          setupPopover(googleMap3D)
        }
      }
    }

    googleMap3D.setOnMapSteadyListener(
      object : OnMapSteadyListener {
        override fun onMapSteadyChange(isSceneSteady: Boolean) {
          Logger.getLogger(TAG).fine("onMapSteadyChange isSceneSteady: ${isSceneSteady}")
        }
      }
    )

    toggleRestrictionButton.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked) {
        googleMap3D.setCameraRestriction(null)
      } else {
        googleMap3D.setCameraRestriction(cameraRestriction)
      }
    }

    stateButton = findViewById<Button>(R.id.toggleCameraTiltHeadingRollRangeButton)
    stateButton.text = "Tilt 45"
    stateButton.setOnClickListener {
      stateIndex = (stateIndex + 1) % 5
      when (stateIndex) {
        0 -> {
          stateButton.text = "Tilt 45"
          currentCamera.tilt = 45.0
          googleMap3D.setCamera(currentCamera)
        }
        1 -> {
          stateButton.text = "Heading 180"
          currentCamera.heading = 180.0
          googleMap3D.setCamera(currentCamera)
        }
        2 -> {
          stateButton.text = "Roll 45"
          currentCamera.roll = 45.0
          googleMap3D.setCamera(currentCamera)
        }
        3 -> {
          stateButton.text = "Range 500"
          currentCamera.range = 500.0
          googleMap3D.setCamera(currentCamera)
        }
        4 -> {
          stateButton.text = "Range 5000"
          currentCamera.range = 5000.0
          googleMap3D.setCamera(currentCamera)
        }
      }
    }

    Log.d(TAG, "onMap3DViewReady")
  }

  override fun onError(error: Exception) {
    Log.d(TAG, "onError: $error")
  }

  override fun onDestroy() {
    super.onDestroy()
    view.onDestroy()
  }

  override fun onPause() {
    super.onPause()
    view.onPause()
  }

  override fun onResume() {
    super.onResume()
    view.onResume()
  }

  suspend fun setupPopover(googleMap3D: GoogleMap3D) {
    val center = latLngAltitude {
      latitude = 37.819852
      longitude = -122.478549
      altitude = 150.0
    }
    val markerInGoldenGate =
      googleMap3D.addMarker(
        markerOptions {
          position = center
          zIndex = 1
          isExtruded = true
          label = "Golden Gate Bridge"
          isDrawnWhenOccluded = true
        }
      )!!

    // View creation must happen on the Main thread.
    val infoView = withContext(Dispatchers.Main) {
        createGoldenGateInfoView()
    }

    val popoverOptions = popoverOptions {
      content = infoView
      positionAnchor = markerInGoldenGate
      autoPanEnabled = true
      autoCloseEnabled = true
      altitudeMode = AltitudeMode.ABSOLUTE
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
                runOnUiThread { popover.remove() }
                Log.d(TAG, "Popover removed")
                popoverToggleCount = 0
            } else {
                Log.d(TAG, "Popover toggled")
                runOnUiThread { popover.toggle() }
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

  private companion object {
    private const val TAG = "MainActivity"
    private const val BASE_METRIC_NAME = "SystemHealthMaps3DFetch"

    val BALLOON_COORDINATES = latLngAltitude {
      latitude = 37.7749
      longitude = -122.4194
      altitude = 500.0
    }
    val SF_COORDINATES = latLngAltitude {
      latitude = 37.7749
      longitude = -122.4194
      altitude = 5000.0
    }
    val MIAMI_COORDINATES = latLngAltitude {
      latitude = 25.7616
      longitude = -80.1917
      altitude = 5000.0
    }

    val AVDA_DE_BRASIL_COORDINATES = latLngAltitude {
      latitude = 40.4563082
      longitude = -3.6992609
      altitude = 5000.0
    }

    val EL_VISO_COORDINATES = latLngAltitude {
      latitude = 40.4447979
      longitude = -3.6948896
      altitude = 5000.0
    }

    val IRISH_ROVER_COORDINATES = latLngAltitude {
      latitude = 40.4544804
      longitude = -3.697733
      altitude = 5000.0
    }

    val SAN_RAFAEL_HOSPITAL_COORDINATES = latLngAltitude {
      latitude = 40.4515547
      longitude = -3.6920172
      altitude = 5000.0
    }

    val SANTIAGO_BERNABEU_COORDINATES = latLngAltitude {
      latitude = 40.45306
      longitude = -3.68835
      altitude = 5000.0
    }
  }
}
