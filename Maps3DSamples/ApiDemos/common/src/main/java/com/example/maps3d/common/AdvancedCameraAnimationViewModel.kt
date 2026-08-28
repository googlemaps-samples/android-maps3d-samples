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

package com.example.maps3d.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared Architecture ViewModel for 3D Advanced Camera Animation across Kotlin, Java, and Compose.
 *
 * Bridges presentation layers with [WorldController] and the reactive [WorldState] pipeline.
 */
class AdvancedCameraAnimationViewModel(
    flightPath: List<LatLng> = TourData.AIRPLANE_FLIGHT_PATH,
    keyframes: List<CameraKeyframe> = TourData.SAN_FRANCISCO_TOUR
) : ViewModel() {

    private val controller = WorldController(flightPath, keyframes)

    private val _worldState = MutableStateFlow(controller.getState())
    val worldState: StateFlow<WorldState> = _worldState.asStateFlow()

    // Backward compatibility alias for UI consumers
    val uiState: StateFlow<WorldState> = _worldState.asStateFlow()

    val liveData: LiveData<WorldState> = _worldState.asLiveData()

    val currentState: WorldState
        get() = _worldState.value

    fun setApproach(approach: AnimationApproach) {
        _worldState.value = controller.setApproach(approach)
    }

    fun setSimpleFlyToMode(mode: SimpleFlyToMode) {
        _worldState.value = controller.setSimpleFlyToMode(mode)
    }

    fun play() {
        _worldState.value = controller.play()
    }

    fun pause() {
        _worldState.value = controller.pause()
    }

    fun setPlaying(isPlaying: Boolean) {
        if (isPlaying) play() else pause()
    }

    fun togglePlayPause() {
        _worldState.value = controller.togglePlayPause()
    }

    fun resetTour() {
        _worldState.value = controller.reset()
    }

    fun onNativeCameraAnimationFinished() {
        _worldState.value = controller.onNativeCameraAnimationFinished()
    }

    fun tick(deltaTimeSeconds: Double) {
        _worldState.value = controller.tick(deltaTimeSeconds)
    }

    fun updateAirplanePose(pose: EntityPose) {
        _worldState.value = controller.updateAirplanePose(pose)
    }

    fun setKeyframeStep(index: Int) {
        _worldState.value = controller.setKeyframeStep(index)
    }


}
