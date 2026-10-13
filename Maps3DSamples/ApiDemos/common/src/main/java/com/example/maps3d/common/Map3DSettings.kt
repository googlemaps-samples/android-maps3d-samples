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
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.UiControlSettings.ControlPlacement
import com.google.android.gms.maps3d.model.CompassControl
import com.google.android.gms.maps3d.model.Map3DControl
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.PanHorizontalControl
import com.google.android.gms.maps3d.model.PanVerticalControl
import com.google.android.gms.maps3d.model.RotateControl
import com.google.android.gms.maps3d.model.Spacer
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
 * Visibility state for built-in 3D camera UI controls.
 *
 * @property zoom Zoom in/out (+ / -) control on trailing edge.
 * @property tilt Pitch up/down control on trailing edge.
 * @property rotate Heading rotation (CW / CCW) control on trailing edge.
 * @property compass North orientation indicator & reset control on trailing edge.
 * @property panVertical North/South pan control on leading edge.
 * @property panHorizontal East/West pan control on leading edge.
 */
data class Map3DUiControls(
    val zoom: Boolean = true,
    val tilt: Boolean = true,
    val rotate: Boolean = true,
    val compass: Boolean = true,
    val panVertical: Boolean = true,
    val panHorizontal: Boolean = true,
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
     * below the active top-anchored trailing camera controls ([zoom], [tilt], [rotate]).
     */
    fun calculateFabMarginDp(): Int {
        var count = 0
        if (zoom) count++
        if (tilt) count++
        if (rotate) count++
        return if (count > 0) 16 + count * 104 + 8 else 16
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
                val trailingControls = mutableListOf<Map3DControl>()
                if (uiControls.zoom) trailingControls.add(ZoomControl(null))
                if (uiControls.tilt) trailingControls.add(TiltControl(null))
                if (uiControls.rotate) trailingControls.add(RotateControl(null))
                if (uiControls.compass) {
                    if (trailingControls.isNotEmpty()) {
                        trailingControls.add(Spacer())
                    }
                    trailingControls.add(CompassControl())
                }
                settings.setUiControlsPlacement(trailingControls, ControlPlacement.TRAILING)

                val leadingControls = mutableListOf<Map3DControl>()
                if (uiControls.panVertical) leadingControls.add(PanVerticalControl(null))
                if (uiControls.panHorizontal) leadingControls.add(PanHorizontalControl(null))
                settings.setUiControlsPlacement(leadingControls, ControlPlacement.LEADING)
            }
        } catch (e: Throwable) {
            Log.e("Map3DSettings", "Could not apply UI controls placement to GoogleMap3D", e)
        }

        // Keep top-anchored UI controls below the status bar on full-screen maps
        resolveMapView(googleMap3D)?.let { mapView ->
            applyStatusBarInsetsToMapView(mapView)
            mapView.post { applyStatusBarInsetsToMapView(mapView) }
            mapView.postDelayed({ applyStatusBarInsetsToMapView(mapView) }, 100L)
        }
    }

    companion object {
        /**
         * Resolves the underlying [ViewGroup] (`Map3DView`) associated with a [GoogleMap3D] instance.
         */
        @JvmStatic
        fun resolveMapView(googleMap3D: GoogleMap3D): ViewGroup? {
            return try {
                val field = googleMap3D.javaClass.declaredFields.firstOrNull {
                    ViewGroup::class.java.isAssignableFrom(it.type)
                } ?: return null
                field.isAccessible = true
                field.get(googleMap3D) as? ViewGroup
            } catch (_: Throwable) {
                null
            }
        }

        /**
         * Resolves the status bar height (in pixels) for the given [view].
         */
        @SuppressLint("InternalInsetResource", "DiscouragedApi")
        @JvmStatic
        @JvmOverloads
        fun resolveStatusBarHeightPx(view: View, explicitStatusBarTop: Int? = null): Int {
            if (explicitStatusBarTop != null && explicitStatusBarTop > 0) {
                return explicitStatusBarTop
            }
            val rootInsets = ViewCompat.getRootWindowInsets(view)
            if (rootInsets != null) {
                val barType = WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.displayCutout()
                val visibleTop = rootInsets.getInsets(barType).top
                if (visibleTop > 0) {
                    return visibleTop
                }
                if (!rootInsets.isVisible(WindowInsetsCompat.Type.statusBars())) {
                    return rootInsets.getInsets(WindowInsetsCompat.Type.displayCutout()).top
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
                if (view.isLaidOut && topBar.isLaidOut && topBar.visibility == View.VISIBLE) {
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
         */
        @JvmStatic
        @JvmOverloads
        fun applyStatusBarInsetsToMapView(
            mapView: ViewGroup,
            explicitStatusBarTop: Int? = null,
        ) {
            val statusBarOverlap = resolveStatusBarOverlapPx(mapView, explicitStatusBarTop)
            val baseMarginPx = (16 * mapView.resources.displayMetrics.density).toInt()
            offsetTopControlContainers(mapView, statusBarOverlap, baseMarginPx)
            ensureMapViewInsetListeners(mapView)
        }

        private fun ensureMapViewInsetListeners(mapView: ViewGroup) {
            if (mapView.getTag(R.id.map3dView) == true) return
            mapView.setTag(R.id.map3dView, true)

            mapView.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                val group = v as? ViewGroup ?: return@addOnLayoutChangeListener
                val overlap = resolveStatusBarOverlapPx(group)
                val baseMarginPx = (16 * group.resources.displayMetrics.density).toInt()
                offsetTopControlContainers(group, overlap, baseMarginPx)
            }

            mapView.setOnHierarchyChangeListener(
                object : ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: View?, child: View?) {
                        mapView.post { applyStatusBarInsetsToMapView(mapView) }
                    }

                    override fun onChildViewRemoved(parent: View?, child: View?) {}
                },
            )
        }

        private fun offsetTopControlContainers(
            group: ViewGroup,
            statusBarOverlap: Int,
            baseMarginPx: Int,
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
                        val effectiveBase = if (child.paddingTop > 0) 0 else baseMarginPx
                        val targetTopMargin = if (statusBarOverlap > 0) {
                            statusBarOverlap + effectiveBase
                        } else {
                            effectiveBase
                        }
                        if (lp.topMargin != targetTopMargin) {
                            lp.topMargin = targetTopMargin
                            child.layoutParams = lp
                        }
                        // Do not recurse into individual control views inside a control container
                        continue
                    }
                }
                if (child is ViewGroup) {
                    offsetTopControlContainers(child, statusBarOverlap, baseMarginPx)
                }
            }
        }
    }
}
// [END maps_3d_quick_settings_model]
