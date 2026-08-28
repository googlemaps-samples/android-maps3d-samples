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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * Custom Touch Gesture Handler for 3D Path Following demo.
 *
 * Features:
 * 1. Vertical sweep up/down (1 finger): Smoothly adjusts camera tilt [0°, 85°].
 * 2. Horizontal sweep left/right (1 finger): Smoothly adjusts camera rotation / heading offset.
 * 3. Pinch gesture (2 fingers): Damped camera distance/zoom adjustment.
 * 4. Tiered Long-Press & Hold:
 *    - Hold >= 500ms: 2x Speed Boost
 *    - Hold >= 2000ms: 5x Warp Speed Boost
 * 5. Double-Tap & Hold (YouTube-style Shuttle):
 *    - Double-tap & hold Right side: +5x Fast-Forward along route
 *    - Double-tap & hold Left side: -5x Rewind backwards along route
 *    - Quick double-tap: Skips ahead/back by 10% of the route.
 */
class PathTouchHandler(
    context: Context,
    private val viewModel: PathFollowingViewModel
) : View.OnTouchListener {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout().toLong()
    private val initialBoostTimeout = ViewConfiguration.getLongPressTimeout().toLong() // ~500ms
    private val warpBoostTimeout = 2000L // 2.0s
    private val handler = Handler(Looper.getMainLooper())

    private val headingSensitivity = 0.08
    private val tiltSensitivity = 0.06
    private val zoomDamping = 0.65

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private var isScaling = false

    // Double-tap & hold shuttle state
    private var lastTapTime = 0L
    private var lastTapX = 0f
    private var isDoubleTapHold = false
    private var wasPlayingBeforeShuttle = false

    private val initialBoostRunnable = Runnable {
        if (!isDragging && !isScaling && !isDoubleTapHold) {
            viewModel.setSpeedBoostMultiplier(2.0)
        }
    }

    private val warpBoostRunnable = Runnable {
        if (!isDragging && !isScaling && !isDoubleTapHold) {
            viewModel.setSpeedBoostMultiplier(5.0)
        }
    }

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                isDragging = false
                cancelBoosts()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val rawFactor = detector.scaleFactor.toDouble()
                if (rawFactor > 0.5 && rawFactor < 2.0) {
                    val dampedFactor = 1.0 + (rawFactor - 1.0) * zoomDamping
                    viewModel.adjustRange(dampedFactor)
                }
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
            }
        }
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val now = System.currentTimeMillis()
                val isDoubleTapCandidate = (now - lastTapTime < doubleTapTimeout) &&
                        (abs(event.x - lastTapX) < touchSlop * 4)

                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                isDragging = false
                isScaling = false

                if (isDoubleTapCandidate) {
                    isDoubleTapHold = true
                    wasPlayingBeforeShuttle = viewModel.currentState.isPlaying

                    val isRightSide = event.x > v.width / 2f
                    val multiplier = if (isRightSide) 5.0 else -5.0
                    viewModel.setPlaying(true)
                    viewModel.setSpeedBoostMultiplier(multiplier)
                } else {
                    isDoubleTapHold = false
                    lastTapTime = now
                    lastTapX = event.x

                    handler.postDelayed(initialBoostRunnable, initialBoostTimeout)
                    handler.postDelayed(warpBoostRunnable, warpBoostTimeout)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                cancelBoosts()
                isDragging = false
            }

            MotionEvent.ACTION_POINTER_UP -> {
                cancelBoosts()
                val remainingIndex = if (event.actionIndex == 0) 1 else 0
                if (remainingIndex < event.pointerCount) {
                    lastX = event.getX(remainingIndex)
                    lastY = event.getY(remainingIndex)
                    downX = lastX
                    downY = lastY
                }
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && !isScaling && !scaleDetector.isInProgress) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY

                    val totalMove = abs(event.x - downX) + abs(event.y - downY)
                    if (!isDragging && totalMove > touchSlop) {
                        isDragging = true
                        cancelBoosts()
                    }

                    if (isDragging) {
                        if (abs(dx) > 0.1f) {
                            viewModel.adjustHeading(dx * headingSensitivity)
                        }
                        if (abs(dy) > 0.1f) {
                            viewModel.adjustTilt(-dy * tiltSensitivity)
                        }
                    }

                    lastX = event.x
                    lastY = event.y
                } else if (event.pointerCount > 1) {
                    lastX = event.x
                    lastY = event.y
                }
            }

            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                if (isDoubleTapHold) {
                    val holdDuration = now - lastTapTime
                    if (holdDuration < 300L) {
                        // Quick double-tap: Skip +/- 10%
                        val isRightSide = event.x > v.width / 2f
                        viewModel.skipRatio(if (isRightSide) 0.10f else -0.10f)
                    }
                    // Revert playing state if it wasn't playing before
                    if (!wasPlayingBeforeShuttle) {
                        viewModel.setPlaying(false)
                    }
                    isDoubleTapHold = false
                }
                cancelBoosts()
                isDragging = false
                isScaling = false
            }

            MotionEvent.ACTION_CANCEL -> {
                cancelBoosts()
                if (isDoubleTapHold && !wasPlayingBeforeShuttle) {
                    viewModel.setPlaying(false)
                }
                isDoubleTapHold = false
                isDragging = false
                isScaling = false
            }
        }

        return true
    }

    private fun cancelBoosts() {
        handler.removeCallbacks(initialBoostRunnable)
        handler.removeCallbacks(warpBoostRunnable)
        viewModel.setSpeedBoostMultiplier(1.0)
    }
}
