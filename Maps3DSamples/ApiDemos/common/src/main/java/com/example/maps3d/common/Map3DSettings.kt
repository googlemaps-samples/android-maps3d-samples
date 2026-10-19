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
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.UiControlSettings.ControlOrientation
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.Map3DControl
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.TiltControl
import com.google.android.gms.maps3d.model.ZoomControl

// [START maps_3d_quick_settings_model]
/**
 * Supported color schemes for 3D map and sample UI styling.
 */
enum class Map3DColorScheme(val value: Int) {
    FOLLOW_SYSTEM(2),
    LIGHT(0),
    DARK(1);

    companion object {
        fun fromValue(value: Int): Map3DColorScheme {
            return entries.firstOrNull { it.value == value } ?: FOLLOW_SYSTEM
        }
    }
}

// [START maps_3d_quick_settings_ui_controls_model]
/**
 * Visibility and placement state for built-in 3D camera UI controls.
 *
 * @property zoom Zoom in/out (+ / -) control.
 * @property tilt Pitch up/down control.
 * @property rotate Heading rotation (CW / CCW) control.
 * @property compass North orientation indicator & reset control.
 * @property panVertical North/South pan control.
 * @property panHorizontal East/West pan control.
 * @property controlsPosition Screen edge for main controls ([ControlPlacement.TRAILING] or [ControlPlacement.LEADING]); pan and compass controls are placed on the opposite edge.
 */
data class Map3DUiControls @JvmOverloads constructor(
    val zoom: Boolean = true,
    val tilt: Boolean = true,
    val rotate: Boolean = true,
    val compass: Boolean = true,
    val panVertical: Boolean = true,
    val panHorizontal: Boolean = true,
    @param:ControlPlacement @get:ControlPlacement
    val controlsPosition: Int = ControlPlacement.TRAILING,
) {
    val allEnabled: Boolean
        get() = zoom && tilt && rotate && compass && panVertical && panHorizontal

    val noneEnabled: Boolean
        get() = !zoom && !tilt && !rotate && !compass && !panVertical && !panHorizontal

    fun withAll(enabled: Boolean): Map3DUiControls = copy(
        zoom = enabled,
        tilt = enabled,
        rotate = enabled,
        compass = enabled,
        panVertical = enabled,
        panHorizontal = enabled,
    )

    /**
     * Calculates the top margin (in dp) required to place a floating action button directly
     * below the active top-anchored trailing camera controls.
     *
     * SDK control dimensions:
     * - Dual-button controls (Zoom, Tilt, Rotate, Pan): 104dp (two 48dp touch targets + 8dp spacing).
     * - Single-button control (Compass): 56dp (48dp touch target + padding).
     * - Inter-control and screen edge spacing: 16dp initial offset + 8dp margin per visible control.
     */
    fun calculateFabMarginDp(): Int {
        var totalHeightDp = 0
        if (controlsPosition == ControlPlacement.LEADING) {
            if (panVertical) totalHeightDp += 104
            if (panHorizontal) totalHeightDp += 104
            if (compass) totalHeightDp += 56
        } else {
            if (zoom) totalHeightDp += 104
            if (tilt) totalHeightDp += 104
            if (rotate) totalHeightDp += 104
        }
        return if (totalHeightDp > 0) 16 + totalHeightDp + 8 else 16
    }
}
// [END maps_3d_quick_settings_ui_controls_model]

/**
 * Immutable configuration snapshot for 3D map-level settings.
 *
 * Encapsulates global rendering options that apply to the 3D map surface itself
 * rather than individual scene objects (markers, polylines, or models).
 *
 * @property mapMode The 3D map rendering mode: [Map3DMode.HYBRID], [Map3DMode.SATELLITE], or [Map3DMode.ROADMAP].
 * @property colorScheme The map color scheme: [Map3DColorScheme.FOLLOW_SYSTEM], [Map3DColorScheme.LIGHT], or [Map3DColorScheme.DARK].
 * @property uiControls Visibility state for built-in 3D camera UI controls.
 */
data class Map3DSettings(
    @param:Map3DMode @get:Map3DMode val mapMode: Int = Map3DMode.HYBRID,
    val colorScheme: Map3DColorScheme = Map3DColorScheme.FOLLOW_SYSTEM,
    val uiControls: Map3DUiControls = Map3DUiControls(),
) {
    /**
     * Applies this configuration to a live [GoogleMap3D] instance and updates app day/night theme.
     */
    fun applyTo(googleMap3D: GoogleMap3D) {
        googleMap3D.setMapMode(mapMode)

        // Apply dark / light mode via AppCompatDelegate
        val nightMode = when (colorScheme) {
            Map3DColorScheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Map3DColorScheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Map3DColorScheme.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        // Forward to SDK delegate if available at runtime
        try {
            val method = googleMap3D.javaClass.getMethod(
                "setMapColorScheme",
                Int::class.javaPrimitiveType,
            )
            method.invoke(googleMap3D, colorScheme.value)
        } catch (_: NoSuchMethodException) {
            // Unexported in current SDK RC client stub; handled by AppCompatDelegate
        } catch (e: Exception) {
            Log.w("Map3DSettings", "Could not apply colorScheme directly to GoogleMap3D", e)
        }

        // Apply built-in UI controls configuration
        try {
            googleMap3D.getUiControlSettings()?.let { settings ->
                val mainControls = mutableListOf<Map3DControl>()
                if (uiControls.zoom) mainControls.add(ZoomControl(ControlOrientation.VERTICAL))
                if (uiControls.tilt) mainControls.add(TiltControl(ControlOrientation.VERTICAL))
                if (uiControls.rotate) mainControls.add(RotateControl(ControlOrientation.VERTICAL))

                val panControls = mutableListOf<Map3DControl>()
                if (uiControls.panVertical) panControls.add(PanVerticalControl(ControlOrientation.VERTICAL))
                if (uiControls.panHorizontal) panControls.add(PanHorizontalControl(ControlOrientation.VERTICAL))
                if (uiControls.compass) panControls.add(CompassControl())

                val mainPosition = if (uiControls.controlsPosition == ControlPlacement.LEADING) {
                    ControlPlacement.LEADING
                } else {
                    ControlPlacement.TRAILING
                }
                val panPosition = if (mainPosition == ControlPlacement.TRAILING) {
                    ControlPlacement.LEADING
                } else {
                    ControlPlacement.TRAILING
                }

                settings.setUiControlsPlacement(mainControls, mainPosition)
                settings.setUiControlsPlacement(panControls, panPosition)
            }
        } catch (e: Throwable) {
            Log.e("Map3DSettings", "Could not apply UI controls placement to GoogleMap3D", e)
        }
    }

    companion object {
        /**
         * Finds the first [Map3DView] inside [root] without using reflection.
         */
        @JvmStatic
        fun findMap3DView(root: View?): Map3DView? {
            if (root == null) return null
            if (root is Map3DView) return root
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) {
                    val found = findMap3DView(root.getChildAt(i))
                    if (found != null) return found
                }
            }
            return null
        }

        private data class ControlMarginState(
            val baseTopMargin: Int,
            val appliedTopMargin: Int,
        )

        /**
         * Resolves the status bar height (in pixels) for the given [view].
         */
        @SuppressLint("InternalInsetResource", "DiscouragedApi")
        @JvmStatic
        @JvmOverloads
        fun resolveStatusBarHeightPx(view: View, explicitStatusBarTop: Int? = null): Int {
            if (explicitStatusBarTop != null && explicitStatusBarTop >= 0) {
                return explicitStatusBarTop
            }
            val rootInsets = ViewCompat.getRootWindowInsets(view)
            if (rootInsets != null) {
                val barType = WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.displayCutout()
                val topInset = maxOf(
                    rootInsets.getInsets(barType).top,
                    rootInsets.getInsetsIgnoringVisibility(barType).top,
                )
                if (topInset > 0) {
                    return topInset
                }
            }
            val resId = view.resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resId > 0) view.resources.getDimensionPixelSize(resId) else 0
        }

        /**
         * Computes how many vertical pixels of the status bar overlap the top of [view].
         *
         * Returns `> 0` for full-screen maps that extend behind the status bar, and `0` when
         * the map is positioned below a non-overlay toolbar (`top_bar`).
         */
        @JvmStatic
        @JvmOverloads
        fun resolveStatusBarOverlapPx(view: View, explicitStatusBarTop: Int? = null): Int {
            val parentView = view.parent as? ViewGroup
            val topBar = parentView?.findViewById<View?>(R.id.top_bar)
            val appBarLayout = parentView?.findViewById<View?>(R.id.app_bar_layout)
            if (topBar != null && appBarLayout == null) {
                if (view.isLaidOut && topBar.isLaidOut && topBar.isVisible) {
                    val viewLoc = IntArray(2)
                    val barLoc = IntArray(2)
                    view.getLocationInWindow(viewLoc)
                    topBar.getLocationInWindow(barLoc)
                    val barBottom = barLoc[1] + topBar.height
                    return (barBottom - viewLoc[1]).coerceAtLeast(0)
                }
                return 0
            }
            val statusBarTop = resolveStatusBarHeightPx(view, explicitStatusBarTop)
            if (statusBarTop <= 0) return 0

            return if (view.isLaidOut) {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                val viewTopInWindow = location[1]
                (statusBarTop - viewTopInWindow).coerceAtLeast(0)
            } else {
                statusBarTop
            }
        }

        /**
         * Offsets top-anchored SDK UI control containers (`ControlPlacement.LEADING` and
         * `ControlPlacement.TRAILING`) inside [mapView] so the status bar does not overlap them
         * when [mapView] is rendered full-screen.
         *
         * Workaround: [Map3DView] does not yet respect window insets or expose a padding API for
         * its internal UI control overlays on edge-to-edge screens.
         */
        @JvmStatic
        @JvmOverloads
        fun applyStatusBarInsetsToMapView(
            mapView: ViewGroup,
            explicitStatusBarTop: Int? = null,
        ) {
            // Workaround: Until Map3DView natively handles window insets or exposes map padding,
            // offset its top-aligned UI control containers when the map draws under the status bar.
            if (explicitStatusBarTop != null) {
                mapView.setTag(
                    R.id.map3d_status_bar_top_tag,
                    explicitStatusBarTop.coerceAtLeast(0),
                )
            }
            val effectiveExplicitTop =
                explicitStatusBarTop ?: (mapView.getTag(R.id.map3d_status_bar_top_tag) as? Int)
            val statusBarOverlap = resolveStatusBarOverlapPx(mapView, effectiveExplicitTop)
            ensureRootLayoutListener(mapView)
            offsetTopControlContainers(mapView, statusBarOverlap, mapView)
        }

        private fun ensureRootLayoutListener(rootMapView: ViewGroup) {
            if (rootMapView.getTag(R.id.map3d_insets_listener_tag) == true) return
            rootMapView.setTag(R.id.map3d_insets_listener_tag, true)

            rootMapView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val cachedTop = rootMapView.getTag(R.id.map3d_status_bar_top_tag) as? Int
                val overlap = resolveStatusBarOverlapPx(rootMapView, cachedTop)
                offsetTopControlContainers(rootMapView, overlap, rootMapView)
            }
        }

        private fun ensureContainerLayoutListener(container: LinearLayout, rootMapView: ViewGroup) {
            if (container.getTag(R.id.map3d_insets_listener_tag) == true) return
            container.setTag(R.id.map3d_insets_listener_tag, true)

            container.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val cachedTop = rootMapView.getTag(R.id.map3d_status_bar_top_tag) as? Int
                val overlap = resolveStatusBarOverlapPx(rootMapView, cachedTop)
                applyMarginToControlContainer(container, overlap)
            }
        }

        private fun applyMarginToControlContainer(
            container: LinearLayout,
            statusBarOverlap: Int,
        ) {
            val lp = container.layoutParams as? ViewGroup.MarginLayoutParams ?: return
            val state = container.getTag(R.id.map3d_base_top_margin_tag) as? ControlMarginState
            val baseTopMargin = if (state == null || lp.topMargin != state.appliedTopMargin) {
                lp.topMargin
            } else {
                state.baseTopMargin
            }
            val targetTopMargin = baseTopMargin + statusBarOverlap.coerceAtLeast(0)
            container.setTag(
                R.id.map3d_base_top_margin_tag,
                ControlMarginState(baseTopMargin, targetTopMargin),
            )
            if (lp.topMargin != targetTopMargin) {
                lp.topMargin = targetTopMargin
                container.layoutParams = lp
                container.post {
                    if (container.isAttachedToWindow) {
                        container.requestLayout()
                    }
                }
            }
        }

        private fun offsetTopControlContainers(
            group: ViewGroup,
            statusBarOverlap: Int,
            rootMapView: ViewGroup,
        ) {
            for (i in 0 until group.childCount) {
                val child = group.getChildAt(i) ?: continue
                if (child is LinearLayout) {
                    val lp = child.layoutParams as? ViewGroup.MarginLayoutParams
                    val isTopAligned = when (lp) {
                        is FrameLayout.LayoutParams -> {
                            val vGravity = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                            vGravity != Gravity.BOTTOM && vGravity != Gravity.CENTER_VERTICAL
                        }
                        null -> false
                        else -> !child.isLaidOut || child.top <= group.height / 2
                    }
                    if (lp != null && isTopAligned) {
                        ensureContainerLayoutListener(child, rootMapView)
                        applyMarginToControlContainer(child, statusBarOverlap)
                        // Do not recurse into individual control views inside a control container
                        continue
                    }
                }
                if (child is ViewGroup) {
                    offsetTopControlContainers(child, statusBarOverlap, rootMapView)
                }
            }
        }
    }
}
// [END maps_3d_quick_settings_model]
