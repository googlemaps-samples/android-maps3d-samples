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

package com.example.maps3dkotlin.sampleactivity

import android.content.Context
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.example.maps3d.common.Map3DSettings
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode

/**
 * Unit tests verifying that [Map3DSettings.applyStatusBarInsetsToMapView] shifts top-anchored
 * SDK UI control containers below the status bar without moving bottom-anchored overlays.
 */
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class Map3DStatusBarInsetsTest {

    @Test
    fun applyStatusBarInsetsToMapView_offsetsTopLeadingAndTrailingContainers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mapView = FrameLayout(context)
        val baseMarginPx = (16 * context.resources.displayMetrics.density).toInt()
        val statusBarPx = 96

        val trailingControls = LinearLayout(context)
        val trailingLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END,
        ).apply { topMargin = baseMarginPx }
        mapView.addView(trailingControls, trailingLp)

        val leadingControls = LinearLayout(context)
        val leadingLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START,
        ).apply { topMargin = baseMarginPx }
        mapView.addView(leadingControls, leadingLp)

        val bottomControls = LinearLayout(context)
        val bottomLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.START,
        ).apply { topMargin = baseMarginPx }
        mapView.addView(bottomControls, bottomLp)

        Map3DSettings.applyStatusBarInsetsToMapView(mapView, explicitStatusBarTop = statusBarPx)

        assertThat((trailingControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(statusBarPx + baseMarginPx)
        assertThat((leadingControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(statusBarPx + baseMarginPx)
        assertThat((bottomControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(baseMarginPx)
    }

    @Test
    fun applyStatusBarInsetsToMapView_reappliesInsetsWhenChildControlContainerAddedLater() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mapView = FrameLayout(context)
        val baseMarginPx = (16 * context.resources.displayMetrics.density).toInt()
        val statusBarPx = 84

        Map3DSettings.applyStatusBarInsetsToMapView(mapView, explicitStatusBarTop = statusBarPx)

        val delayedTrailingControls = LinearLayout(context)
        val trailingLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END,
        ).apply { topMargin = baseMarginPx }
        mapView.addView(delayedTrailingControls, trailingLp)

        // Simulate explicit re-application (as triggered by onChildViewAdded / onMapReady)
        Map3DSettings.applyStatusBarInsetsToMapView(mapView, explicitStatusBarTop = statusBarPx)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat((delayedTrailingControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(statusBarPx + baseMarginPx)
    }

    @Test
    fun applyStatusBarInsetsToMapView_recoversWhenNestedDelegateResetsTopMargin() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val mapView = FrameLayout(context)
        val delegateFrame = FrameLayout(context)
        mapView.addView(
            delegateFrame,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        val baseMarginPx = (16 * context.resources.displayMetrics.density).toInt()
        val statusBarPx = 90

        val nestedTrailingControls = LinearLayout(context)
        val trailingLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END,
        ).apply { topMargin = baseMarginPx }
        delegateFrame.addView(nestedTrailingControls, trailingLp)

        Map3DSettings.applyStatusBarInsetsToMapView(mapView, explicitStatusBarTop = statusBarPx)
        assertThat((nestedTrailingControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(statusBarPx + baseMarginPx)

        // Simulate SDK setUiControlsPlacement resetting topMargin on the nested container
        val resetLp = (nestedTrailingControls.layoutParams as FrameLayout.LayoutParams).apply {
            topMargin = baseMarginPx
        }
        nestedTrailingControls.layoutParams = resetLp
        nestedTrailingControls.layout(0, 0, 100, 300)
        shadowOf(Looper.getMainLooper()).idle()

        assertThat((nestedTrailingControls.layoutParams as FrameLayout.LayoutParams).topMargin)
            .isEqualTo(statusBarPx + baseMarginPx)
    }
}
