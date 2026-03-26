package com.example.placesuikit3d.common

import com.example.placesuikit3d.utils.CameraUpdate
import com.example.placesuikit3d.utils.toHeading
import com.example.placesuikit3d.utils.toRange
import com.example.placesuikit3d.utils.toRoll
import com.example.placesuikit3d.utils.toTilt
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Map3dViewModelTest {

    private class TestMap3dViewModel : Map3dViewModel() {
        override val TAG = "TestMap3dViewModel"

        fun getLastEmittedCameraUpdate(): CameraUpdate? {
            val field = Map3dViewModel::class.java.getDeclaredField("_pendingCameraUpdate")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val flow = field.get(this) as MutableSharedFlow<CameraUpdate?>
            return flow.replayCache.lastOrNull()
        }
    }

    @Test
    fun `setCameraTilt emits Move update with updated tilt`() {
        val viewModel = TestMap3dViewModel()
        val initialCamera = viewModel.currentCamera.value
        val newTilt = 45.0

        viewModel.setCameraTilt(newTilt)

        val update = viewModel.getLastEmittedCameraUpdate()
        assertThat(update).isInstanceOf(CameraUpdate.Move::class.java)

        val moveUpdate = update as CameraUpdate.Move
        assertThat(moveUpdate.camera.tilt).isEqualTo(newTilt.toTilt())
        // Ensure other properties remain unchanged
        assertThat(moveUpdate.camera.heading).isEqualTo(initialCamera.heading)
        assertThat(moveUpdate.camera.range).isEqualTo(initialCamera.range)
        assertThat(moveUpdate.camera.roll).isEqualTo(initialCamera.roll)
    }

    @Test
    fun `setCameraHeading emits Move update with updated heading`() {
        val viewModel = TestMap3dViewModel()
        val initialCamera = viewModel.currentCamera.value
        val newHeading = 180.0

        viewModel.setCameraHeading(newHeading)

        val update = viewModel.getLastEmittedCameraUpdate()
        assertThat(update).isInstanceOf(CameraUpdate.Move::class.java)

        val moveUpdate = update as CameraUpdate.Move
        assertThat(moveUpdate.camera.heading).isEqualTo(newHeading.toHeading())
        assertThat(moveUpdate.camera.tilt).isEqualTo(initialCamera.tilt)
    }

    @Test
    fun `setCameraRange emits Move update with updated range`() {
        val viewModel = TestMap3dViewModel()
        val initialCamera = viewModel.currentCamera.value
        val newRange = 2500.0

        viewModel.setCameraRange(newRange)

        val update = viewModel.getLastEmittedCameraUpdate()
        assertThat(update).isInstanceOf(CameraUpdate.Move::class.java)

        val moveUpdate = update as CameraUpdate.Move
        assertThat(moveUpdate.camera.range).isEqualTo(newRange.toRange())
        assertThat(moveUpdate.camera.tilt).isEqualTo(initialCamera.tilt)
    }

    @Test
    fun `setCameraRoll emits Move update with updated roll`() {
        val viewModel = TestMap3dViewModel()
        val initialCamera = viewModel.currentCamera.value
        val newRoll = 90.0

        viewModel.setCameraRoll(newRoll)

        val update = viewModel.getLastEmittedCameraUpdate()
        assertThat(update).isInstanceOf(CameraUpdate.Move::class.java)

        val moveUpdate = update as CameraUpdate.Move
        assertThat(moveUpdate.camera.roll).isEqualTo(newRoll.toRoll())
        assertThat(moveUpdate.camera.tilt).isEqualTo(initialCamera.tilt)
    }
}
