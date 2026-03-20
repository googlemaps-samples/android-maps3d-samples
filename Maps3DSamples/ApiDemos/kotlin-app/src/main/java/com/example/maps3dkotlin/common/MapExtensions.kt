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

package com.example.maps3dkotlin.common

import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.FlyAroundOptions
import com.google.android.gms.maps3d.model.FlyToOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * Initiates a FlyTo camera animation and suspends until it finishes.
 * If the coroutine is cancelled, the animation is stopped and listeners cleaned up.
 */
suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyToOptions) {
    suspendCancellableCoroutine<Unit> { cont ->
        // 1. Set the listener to resume the coroutine when the animation finishes
        this.setCameraAnimationEndListener {
            this.setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }

        // 2. Handle cancellation (e.g., if the user leaves the screen or stops the tour)
        cont.invokeOnCancellation {
            this.stopCameraAnimation()
            this.setCameraAnimationEndListener(null)
        }

        // 3. Start the animation
        this.flyCameraTo(options)
    }
}

/**
 * Initiates a FlyAround camera animation and suspends until it finishes.
 */
suspend fun GoogleMap3D.awaitCameraAnimation(options: FlyAroundOptions) {
    suspendCancellableCoroutine<Unit> { cont ->
        this.setCameraAnimationEndListener {
            this.setCameraAnimationEndListener(null)
            if (cont.isActive) cont.resume(Unit)
        }

        cont.invokeOnCancellation {
            this.stopCameraAnimation()
            this.setCameraAnimationEndListener(null)
        }

        this.flyCameraAround(options)
    }
}

/**
 * Suspends until the map reports it is "steady" (finished rendering 3D tiles),
 * up to a maximum duration.
 * 
 * @param timeout The maximum amount of time to wait.
 * @return True if the map became steady before the timeout, false if it timed out.
 */
suspend fun GoogleMap3D.awaitMapSteady(timeout: Duration): Boolean {
    // withTimeoutOrNull returns null if the timeout is reached before the block finishes
    val result = withTimeoutOrNull(timeout) {
        suspendCancellableCoroutine<Unit> { cont ->
            this@awaitMapSteady.setOnMapSteadyListener { isSteady ->
                if (isSteady) {
                    this@awaitMapSteady.setOnMapSteadyListener(null)
                    if (cont.isActive) cont.resume(Unit)
                }
            }

            cont.invokeOnCancellation {
                this@awaitMapSteady.setOnMapSteadyListener(null)
            }
        }
    }
    
    // If result is not null, it means we successfully resumed from the steady state!
    return result != null
}
