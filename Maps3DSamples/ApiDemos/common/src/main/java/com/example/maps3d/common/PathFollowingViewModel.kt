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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.maps3d.model.LatLngAltitude
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing reactive UI state for 3D Path Following samples across
 * Kotlin Views, Java Views, and Jetpack Compose.
 *
 * Delegates all calculation, physics, and interpolation to [PathPlaybackController].
 */
class PathFollowingViewModel(
    initialRoute: List<LatLngAltitude> = PathData.URBAN_PATH
) : ViewModel() {

    private val controller = PathPlaybackController(initialRoute)
    private val _uiState = MutableStateFlow(controller.getState())

    /**
     * Observable reactive state for Kotlin Coroutine Flow collectors and Compose.
     */
    val uiState: StateFlow<PathPlaybackState> = _uiState.asStateFlow()

    /**
     * Observable LiveData bridge for Java View consumers.
     */
    val liveData by lazy { _uiState.asLiveData() }

    val currentState: PathPlaybackState
        get() = _uiState.value

    fun advance(deltaTimeSeconds: Double) {
        _uiState.value = controller.advance(deltaTimeSeconds)
    }

    fun seekToRatio(ratio: Float) {
        _uiState.value = controller.seekToRatio(ratio)
    }

    fun seekToDistance(distanceMeters: Double) {
        _uiState.value = controller.seekToDistance(distanceMeters)
    }

    fun setScrubbing(isScrubbing: Boolean) {
        _uiState.value = controller.setScrubbing(isScrubbing)
    }

    fun setPlaying(isPlaying: Boolean) {
        _uiState.value = controller.setPlaying(isPlaying)
    }

    fun togglePlayPause() {
        _uiState.value = controller.togglePlayPause()
    }

    fun setRoute(newRoute: List<LatLngAltitude>, applyDefaults: Boolean = true) {
        _uiState.value = controller.setRoute(newRoute, applyDefaults)
    }

    fun setAltitudeMode(mode: Int) {
        _uiState.value = controller.setAltitudeMode(mode)
    }

    fun setDrawsOccludedSegments(drawsOccluded: Boolean) {
        _uiState.value = controller.setDrawsOccludedSegments(drawsOccluded)
    }

    fun setPathAltitudeOffset(offset: Double) {
        _uiState.value = controller.setPathAltitudeOffset(offset)
    }

    fun setCameraRange(range: Double) {
        _uiState.value = controller.setCameraRange(range)
    }

    fun setGroundAltitude(altitude: Double) {
        _uiState.value = controller.setGroundAltitude(altitude)
    }

    fun setHeadingOffset(offset: Double) {
        _uiState.value = controller.setHeadingOffset(offset)
    }

    fun setCameraTilt(tilt: Double) {
        _uiState.value = controller.setCameraTilt(tilt)
    }

    fun setFollowSpeed(speedMps: Double) {
        _uiState.value = controller.setFollowSpeed(speedMps)
    }

    fun reset() {
        _uiState.value = controller.reset()
    }
}
