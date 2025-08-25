package com.example.maps3dkotlin.cameracontrols

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.flyToOptions
import com.google.android.gms.maps3d.model.latLngAltitude
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class CameraControlsActivityTest : OnMap3DViewReadyCallback {
    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<CameraControlsActivity>
    private var googleMap: GoogleMap3D? = null
    private var mapReadyLatch = CountDownLatch(1)

    override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
        googleMap = googleMap3D
        mapReadyLatch.countDown()
    }

    override fun onError(error: Exception) {
        mapReadyLatch.countDown()
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        scenario = ActivityScenario.launch(CameraControlsActivity::class.java)

        // Wait for the map to be ready
        scenario.onActivity { activity ->
            val mapFragment = activity.findViewById<Map3DView>(R.id.map3dView)
            mapFragment.getMap3DViewAsync(this)
        }
        mapReadyLatch.await(5, TimeUnit.SECONDS)

        // Let the initial animation finish
        CoroutineScope(Dispatchers.Main).launch {
            delay(2.seconds)
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testMapIsDisplayed() {
        onView(withId(R.id.map3dView)).check(matches(isDisplayed()))
    }

    @Test
    fun testFlyToEmpireStateBuilding() {
        // The activity automatically flies to the Empire State Building on start.
        // We'll wait for the map to be steady to confirm this initial animation has finished.
        var steadyLatch = CountDownLatch(1)
        scenario.onActivity {
            googleMap?.setOnMapSteadyListener { isSteady ->
                if (isSteady) {
                    steadyLatch.countDown()
                }
            }
        }
        // The initial animation can take a while
        steadyLatch.await(10, TimeUnit.SECONDS)

        // Now, let's fly somewhere else and then back to test the button.
        // First, fly to a different location.
        val differentCamera = camera {
            center = latLngAltitude {
                latitude = 40.7484
                longitude = -73.9857
                altitude = 100.0
            }
        }
        steadyLatch = CountDownLatch(1)
        scenario.onActivity {
            googleMap?.flyCameraTo(flyToOptions { endCamera = differentCamera })
        }
        steadyLatch.await(5, TimeUnit.SECONDS)


        // Now, click the "Fly to Empire State Building" button.
        steadyLatch = CountDownLatch(1)
        onView(withId(R.id.fly_to)).perform(click())

        // Wait for the animation to complete by waiting for the map to be steady.
        steadyLatch.await(5, TimeUnit.SECONDS)

        val cameraPosition = googleMap?.getCamera()
        assertNotNull(cameraPosition)
        cameraPosition?.let {
            assertEquals(DataModel.EMPIRE_STATE_BUILDING_LATITUDE, it.center.latitude, 0.01)
            assertEquals(DataModel.EMPIRE_STATE_BUILDING_LONGITUDE, it.center.longitude, 0.01)
        }
    }

    @Test
    fun testFlyAround() {
        // Click the "Fly Around" button.
        onView(withId(R.id.fly_around)).perform(click())

        // Wait for the initial animation to start.
        Thread.sleep(3000)

        // Now, wait for the animation to finish
        googleMap?.setCameraAnimationEndListener {
            Log.w("CameraControlsActivityTest", "Camera animation end callback called")
        }

        val cameraPosition = googleMap?.getCamera()
        assertNotNull(cameraPosition)
        cameraPosition?.let {
            assertEquals(40.7, it.center.latitude, 0.1)
            assertEquals(-74.0, it.center.longitude, 0.1)
        }
    }
}