package com.example.snippets.kotlin.utils

import com.google.android.gms.maps3d.GoogleMap3D
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.example.snippets.kotlin.TrackedMap3D

/**
 * **Coroutine Helper**: Awaiting Camera Animation.
 * Pauses execution until the camera stops moving (flyTo or flyAround).
 * 
 * @param action The block that triggers the animation, executed AFTER the listener is attached.
 */
suspend fun TrackedMap3D.awaitAnimation(action: () -> Unit) {
    this.delegate.awaitAnimation(action)
}

suspend fun GoogleMap3D.awaitAnimation(action: () -> Unit) = suspendCancellableCoroutine { continuation ->
    setCameraAnimationEndListener {
        setCameraAnimationEndListener(null) // Cleanup
        if (continuation.isActive) {
            continuation.resume(Unit)
        }
    }

    continuation.invokeOnCancellation {
        setCameraAnimationEndListener(null)
    }

    // Trigger animation after attaching listener
    action()
}

/**
 * **Coroutine Helper**: Awaiting Map Steady State.
 * In a 3D environment, "Steady" means the map has finished loading all visible tiles
 * and the camera has stopped moving.
 */
suspend fun GoogleMap3D.awaitCameraSteady() = suspendCancellableCoroutine { continuation ->
    setOnMapSteadyListener { isSteady ->
        if (isSteady) {
            setOnMapSteadyListener(null) // Cleanup
            if (continuation.isActive) {
                continuation.resume(Unit) // Resume the suspended coroutine
            }
        }
    }

    // Safety: If the coroutine is cancelled, remove the listener.
    continuation.invokeOnCancellation {
        setOnMapSteadyListener(null)
    }
}
