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

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.maps.android.SphericalUtil

/**
 * Pure domain state machine and flight simulation engine managing the 3D World.
 *
 * Decoupled from Android UI, Views, and GoogleMap3D rendering handles.
 */
class WorldController(
    val flightPath: List<LatLng> = TourData.AIRPLANE_FLIGHT_PATH,
    val keyframes: List<CameraKeyframe> = TourData.SAN_FRANCISCO_TOUR,
    val planeEntityId: String = TourData.AIRPLANE_MODEL_ID
) {

    private val startLoc: LatLng = flightPath.firstOrNull() ?: LatLng(37.8199, -122.4783)
    private val endLoc: LatLng = flightPath.lastOrNull() ?: LatLng(37.8024, -122.4058)
    private val initialHeading: Double = if (flightPath.size >= 2) {
        TrajectoryFlightAnimator.normalizeHeading(SphericalUtil.computeHeading(flightPath[0], flightPath[1]))
    } else {
        105.0
    }

    private val initialPlanePose = EntityPose(
        position = LatLngAltitude(startLoc.latitude, startLoc.longitude, 250.0),
        heading = TrajectoryFlightAnimator.normalizeHeading(initialHeading + 180.0),
        pitch = -90.0,
        roll = 0.0,
        scale = 0.08
    )

    private val finalPlanePose = EntityPose(
        position = LatLngAltitude(endLoc.latitude, endLoc.longitude, 250.0),
        heading = TrajectoryFlightAnimator.normalizeHeading(initialHeading + 180.0),
        pitch = -90.0,
        roll = 0.0,
        scale = 0.08
    )

    private var simpleFlyToMode: SimpleFlyToMode = SimpleFlyToMode.SYNCHRONIZED_FLIGHT
    private var selectedApproach: AnimationApproach = AnimationApproach.SIMPLE_FLY_TO
    private var executionState: AnimationExecutionState = AnimationExecutionState.IDLE
    private var currentStepIndex: Int = 0
    private var elapsedTimeMs: Long = 0L
    private val totalFlyToDurationMs: Long = 5000L

    // Active Entity Animators
    private var planeTrajectoryAnimator: TrajectoryFlightAnimator = TrajectoryFlightAnimator(flightPath)
    private var planeMidpointAnimator: MidpointJumpAnimator = MidpointJumpAnimator(initialPlanePose, finalPlanePose)
    private var orbitAnimator: ContinuousOrbitAnimator = ContinuousOrbitAnimator(startLoc, initialHeading)

    private var state: WorldState

    init {
        state = buildInitialState()
    }

    private fun buildInitialState(): WorldState {
        val initialCam = if (selectedApproach == AnimationApproach.KEYFRAME_TOUR) {
            TourData.OVERVIEW_CAMERA
        } else {
            camera {
                center = latLngAltitude {
                    latitude = startLoc.latitude
                    longitude = startLoc.longitude
                    altitude = 250.0
                }
                heading = initialHeading
                tilt = 65.0
                range = 600.0
            }
        }

        val firstStep = keyframes.firstOrNull()
        return WorldState(
            entities = mapOf(planeEntityId to initialPlanePose),
            camera = initialCam,
            executionState = AnimationExecutionState.IDLE,
            selectedApproach = selectedApproach,
            simpleFlyToMode = simpleFlyToMode,
            currentStepIndex = 0,
            totalSteps = keyframes.size,
            stepTitle = firstStep?.stepTitle ?: "",
            stepDescription = firstStep?.stepDescription ?: "",
            statusText = "Press Play to start the aerial tour.",
            elapsedTimeMs = 0L,
            totalDurationMs = totalFlyToDurationMs,
            progressRatio = 0f,
            pendingCameraCommand = null
        )
    }

    fun getState(): WorldState = state

    fun setApproach(approach: AnimationApproach): WorldState {
        selectedApproach = approach
        executionState = AnimationExecutionState.IDLE
        elapsedTimeMs = 0L
        currentStepIndex = 0
        val firstStep = keyframes.firstOrNull()
        state = buildInitialState().copy(
            selectedApproach = approach,
            stepTitle = if (approach == AnimationApproach.KEYFRAME_TOUR) (firstStep?.stepTitle ?: "") else "",
            stepDescription = if (approach == AnimationApproach.KEYFRAME_TOUR) (firstStep?.stepDescription ?: "") else "",
            statusText = when (approach) {
                AnimationApproach.SIMPLE_FLY_TO -> "1. Native SDK flyTo with ${simpleFlyToMode.label}"
                AnimationApproach.KEYFRAME_TOUR -> firstStep?.stepTitle ?: "Declarative Keyframe Tour"
                AnimationApproach.DISPATCHER_FRAME_LOOP -> "3. High-rate 400 m/s flight frame loop"
                AnimationApproach.ORBIT_360_SPIN -> "4. 360-degree continuous orbital camera spin"
            }
        )
        return state
    }

    fun setSimpleFlyToMode(mode: SimpleFlyToMode): WorldState {
        simpleFlyToMode = mode
        state = state.copy(
            simpleFlyToMode = mode,
            statusText = "Selected: ${mode.label}"
        )
        return state
    }

    fun play(): WorldState {
        executionState = AnimationExecutionState.RUNNING

        val command = when (selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> {
                val targetCam = camera {
                    center = latLngAltitude {
                        latitude = endLoc.latitude
                        longitude = endLoc.longitude
                        altitude = 250.0
                    }
                    heading = 285.0 // Look back West-Northwest toward Golden Gate Bridge to see the plane fly in
                    tilt = 65.0
                    range = 600.0
                }
                CameraAnimationCommand.NativeFlyTo(targetCam, totalFlyToDurationMs)
            }
            else -> null
        }

        state = state.copy(
            executionState = AnimationExecutionState.RUNNING,
            pendingCameraCommand = command,
            statusText = when (selectedApproach) {
                AnimationApproach.SIMPLE_FLY_TO -> "Flying to Coit Tower (${simpleFlyToMode.label})"
                AnimationApproach.KEYFRAME_TOUR -> "Running keyframe tour: Step ${currentStepIndex + 1} of ${keyframes.size}"
                AnimationApproach.DISPATCHER_FRAME_LOOP -> "Flying at 400 m/s along flight path"
                AnimationApproach.ORBIT_360_SPIN -> "Continuous 360° orbital spin active"
            }
        )
        return state
    }

    fun pause(): WorldState {
        executionState = AnimationExecutionState.PAUSED
        state = state.copy(
            executionState = AnimationExecutionState.PAUSED,
            pendingCameraCommand = CameraAnimationCommand.StopCameraAnimation,
            statusText = "Tour paused."
        )
        return state
    }

    fun togglePlayPause(): WorldState {
        return if (executionState == AnimationExecutionState.RUNNING) pause() else play()
    }

    fun reset(): WorldState {
        executionState = AnimationExecutionState.IDLE
        elapsedTimeMs = 0L
        currentStepIndex = 0
        planeTrajectoryAnimator.reset()
        planeMidpointAnimator.reset()
        orbitAnimator.reset(initialHeading)
        state = buildInitialState().copy(
            selectedApproach = selectedApproach,
            simpleFlyToMode = simpleFlyToMode,
            pendingCameraCommand = CameraAnimationCommand.SetCameraDirect(buildInitialState().camera),
            statusText = "Tour reset. Press Play to begin."
        )
        return state
    }

    fun onNativeCameraAnimationFinished(): WorldState {
        executionState = AnimationExecutionState.FINISHED
        state = state.copy(
            executionState = AnimationExecutionState.FINISHED,
            progressRatio = 1.0f,
            statusText = "Flight complete: Arrived at Coit Tower.",
            entities = mapOf(planeEntityId to finalPlanePose)
        )
        return state
    }

    /**
     * Advances the world model by [deltaTimeSeconds].
     * Synchronizes plane entity pose and camera position atomically.
     */
    fun tick(deltaTimeSeconds: Double): WorldState {
        if (executionState != AnimationExecutionState.RUNNING) return state

        val deltaMs = (deltaTimeSeconds * 1000.0).toLong()
        elapsedTimeMs += deltaMs

        when (selectedApproach) {
            AnimationApproach.SIMPLE_FLY_TO -> {
                val planePose = if (simpleFlyToMode == SimpleFlyToMode.MIDPOINT_JUMP) {
                    planeMidpointAnimator.update(elapsedTimeMs, totalFlyToDurationMs)
                } else {
                    planeTrajectoryAnimator.update(elapsedTimeMs, totalFlyToDurationMs)
                }

                val ratio = (elapsedTimeMs.toFloat() / totalFlyToDurationMs).coerceIn(0f, 1f)
                val isDone = elapsedTimeMs >= totalFlyToDurationMs

                state = state.copy(
                    entities = mapOf(planeEntityId to planePose),
                    elapsedTimeMs = elapsedTimeMs,
                    progressRatio = ratio,
                    executionState = if (isDone) AnimationExecutionState.FINISHED else AnimationExecutionState.RUNNING,
                    statusText = if (isDone) "Flight complete: Arrived at Coit Tower." else "Flying to Coit Tower: ${(ratio * 100).toInt()}%"
                )
            }

            AnimationApproach.DISPATCHER_FRAME_LOOP -> {
                val totalDist = planeTrajectoryAnimator.totalDistance
                val speedMps = 400.0
                val totalDurationMs = if (speedMps > 0) ((totalDist / speedMps) * 1000.0).toLong() else 5000L
                val planePose = planeTrajectoryAnimator.update(elapsedTimeMs, totalDurationMs)
                val ratio = (elapsedTimeMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                val isDone = elapsedTimeMs >= totalDurationMs

                val cam = camera {
                    center = latLngAltitude {
                        latitude = planePose.position.latitude
                        longitude = planePose.position.longitude
                        altitude = 250.0
                    }
                    heading = TrajectoryFlightAnimator.normalizeHeading(planePose.heading + 180.0)
                    tilt = 65.0
                    range = 600.0
                }

                state = state.copy(
                    entities = mapOf(planeEntityId to planePose),
                    camera = cam,
                    elapsedTimeMs = elapsedTimeMs,
                    progressRatio = ratio,
                    executionState = if (isDone) AnimationExecutionState.FINISHED else AnimationExecutionState.RUNNING,
                    statusText = if (isDone) "Flight complete: Arrived at Coit Tower." else "Flying at 400 m/s: ${(ratio * 100).toInt()}%"
                )
            }

            AnimationApproach.ORBIT_360_SPIN -> {
                val newHeading = orbitAnimator.tick(deltaTimeSeconds)
                val cam = camera {
                    center = latLngAltitude {
                        latitude = startLoc.latitude
                        longitude = startLoc.longitude
                        altitude = 250.0
                    }
                    heading = newHeading
                    tilt = 65.0
                    range = 600.0
                }

                state = state.copy(
                    camera = cam,
                    entities = mapOf(planeEntityId to initialPlanePose),
                    statusText = "360° Orbit Spin: ${newHeading.toInt()}°"
                )
            }

            AnimationApproach.KEYFRAME_TOUR -> {
                // Keyframe tour advances via explicit step commands
            }
        }

        return state
    }

    fun updateAirplanePose(pose: EntityPose): WorldState {
        state = state.copy(
            entities = mapOf(planeEntityId to pose)
        )
        return state
    }

    fun setKeyframeStep(index: Int): WorldState {
        if (index !in keyframes.indices) return state
        currentStepIndex = index
        val step = keyframes[index]
        state = state.copy(
            currentStepIndex = index,
            stepTitle = step.stepTitle,
            stepDescription = step.stepDescription,
            statusText = "${step.stepTitle}: ${step.stepDescription}"
        )
        return state
    }


}
