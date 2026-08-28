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

import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude

/**
 * Represents the 3D spatial transformation (position, orientation, scale) of an entity in the scene.
 */
data class EntityPose(
    val position: LatLngAltitude,
    val heading: Double, // degrees [0, 360)
    val pitch: Double = -90.0,
    val roll: Double = 0.0,
    val scale: Double = 0.08
)

/**
 * Execution mode for "1. SDK Simple flyTo" demonstrating CPU performance trade-offs.
 */
enum class SimpleFlyToMode(val label: String, val description: String) {
    MIDPOINT_JUMP(
        "Midpoint Reposition (Low CPU)",
        "Schedules a single discrete action at t = T/2 to move the plane to destination."
    ),
    SYNCHRONIZED_FLIGHT(
        "Synchronized Animation (High CPU)",
        "Animates the plane per-frame along trajectory for the full duration of flyTo."
    )
}

/**
 * State of animation lifecycle in the world model.
 */
enum class AnimationExecutionState {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

/**
 * Sealed class representing camera animation commands emitted from the controller/ViewModel to the UI.
 */
sealed interface CameraAnimationCommand {
    data class NativeFlyTo(
        val targetCamera: Camera,
        val durationMs: Long
    ) : CameraAnimationCommand

    data class SetCameraDirect(
        val camera: Camera
    ) : CameraAnimationCommand

    data object StopCameraAnimation : CameraAnimationCommand
}

/**
 * Immutable snapshot of the entire 3D simulation scene at a specific point in time.
 */
data class WorldState(
    val entities: Map<String, EntityPose> = emptyMap(),
    val camera: Camera,
    val executionState: AnimationExecutionState = AnimationExecutionState.IDLE,
    val selectedApproach: AnimationApproach = AnimationApproach.SIMPLE_FLY_TO,
    val simpleFlyToMode: SimpleFlyToMode = SimpleFlyToMode.SYNCHRONIZED_FLIGHT,
    val currentStepIndex: Int = 0,
    val totalSteps: Int = TourData.SAN_FRANCISCO_TOUR.size,
    val stepTitle: String = "",
    val stepDescription: String = "",
    val statusText: String = "Press Play to start the aerial tour.",
    val elapsedTimeMs: Long = 0L,
    val totalDurationMs: Long = 5000L,
    val progressRatio: Float = 0.0f,
    val pendingCameraCommand: CameraAnimationCommand? = null
) {
    val isPlaying: Boolean
        get() = executionState == AnimationExecutionState.RUNNING

    val isFinished: Boolean
        get() = executionState == AnimationExecutionState.FINISHED

    fun getEntityPose(id: String): EntityPose? = entities[id]
}
