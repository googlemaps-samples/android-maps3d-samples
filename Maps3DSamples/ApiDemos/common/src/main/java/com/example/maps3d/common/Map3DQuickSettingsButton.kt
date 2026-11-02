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

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import com.example.maps3dcommon.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.material.floatingactionbutton.FloatingActionButton

// [START maps_3d_quick_settings_button]
/**
 * A self-contained settings button overlay for 3D map activities.
 *
 * Can be positioned anywhere in a layout (e.g., top-end or bottom-end of the map).
 * Clicking the button opens a modal [Map3DQuickSettingsDialog] allowing the user
 * to switch map modes (Satellite, Hybrid, Roadmap), color schemes (System, Light, Dark),
 * and camera UI controls.
 *
 * Defaults to [Map3DSettingsController.shared] so user choices stay consistent across
 * all samples during an app session.
 */
class Map3DQuickSettingsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle,
) : FloatingActionButton(context, attrs, defStyleAttr) {

    fun interface OnSettingsChangedListener {
        fun onSettingsChanged(settings: Map3DSettings)
    }

    private val controllerListener: (Map3DSettings) -> Unit = { updatedSettings ->
        val map = attachedMap ?: mapProvider?.invoke()
        map?.let { updatedSettings.applyTo(it) }
        updateTopMarginForUiControls(updatedSettings.uiControls)
        requestSiblingMapViewInsets()
        onSettingsChangedListener?.onSettingsChanged(updatedSettings)
    }

    /**
     * Controller backing this button's settings state.
     * Defaults to the application-scoped [Map3DSettingsController.shared] instance.
     */
    var settingsController: Map3DSettingsController = Map3DSettingsController.shared
        set(value) {
            if (field !== value) {
                field.removeListener(controllerListener)
                field = value
                if (isAttachedToWindow) {
                    field.addListener(controllerListener)
                }
                updateTopMarginForUiControls(field.getSettings().uiControls)
            }
        }

    /**
     * The hosting sample's default [Map3DMode], restored if the user taps "Reset to Defaults".
     */
    @Map3DMode
    var sampleDefaultMapMode: Int = Map3DMode.HYBRID

    /**
     * Additional top inset (in pixels) to add when the map and button are laid out edge-to-edge
     * behind the status bar.
     */
    var statusBarTopOffsetPx: Int = 0
        set(value) {
            if (field != value) {
                field = value
                updateTopMarginForUiControls()
            }
        }

    private var attachedMap: GoogleMap3D? = null
    private var mapProvider: (() -> GoogleMap3D?)? = null
    private var activeDialog: AlertDialog? = null
    private var onSettingsChangedListener: OnSettingsChangedListener? = null
    private var lastAppliedUiControls: Map3DUiControls? = null

    init {
        setImageResource(R.drawable.settings_24px)
        contentDescription = context.getString(R.string.map3d_settings_button_description)
        size = SIZE_MINI

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            updateTopMarginForUiControls()
            requestSiblingMapViewInsets()
            insets
        }

        setOnClickListener {
            showDialog()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        settingsController.addListener(controllerListener)
        updateTopMarginForUiControls()
        requestSiblingMapViewInsets()
        ViewCompat.requestApplyInsets(this)
        post {
            updateTopMarginForUiControls()
        }
    }

    override fun onDetachedFromWindow() {
        activeDialog?.dismiss()
        activeDialog = null
        settingsController.removeListener(controllerListener)
        super.onDetachedFromWindow()
    }

    private fun findSiblingMapView(): ViewGroup? {
        val parentGroup = parent as? ViewGroup ?: return null
        return parentGroup.findViewById(R.id.map3dView)
    }

    private fun requestSiblingMapViewInsets() {
        val mapView = findSiblingMapView() ?: return
        ViewCompat.requestApplyInsets(mapView)
    }

    private fun resolveEffectiveStatusBarOffsetPx(): Int {
        if (statusBarTopOffsetPx > 0) return statusBarTopOffsetPx
        val parentGroup = parent as? ViewGroup ?: return 0
        val mapView = findSiblingMapView()
        return Map3DSettings.resolveStatusBarOverlapPx(mapView ?: parentGroup)
    }

    /**
     * Re-applies the top margin for the most recently configured [Map3DUiControls] (or the
     * active [settingsController] controls if none have been explicitly set yet).
     */
    fun updateTopMarginForUiControls() {
        val controls = lastAppliedUiControls ?: getSettings().uiControls
        applyTopMargin(controls)
    }

    /**
     * Adjusts this button's top margin so it sits directly below the active trailing camera UI
     * controls ([Map3DUiControls.zoom], [Map3DUiControls.tilt], [Map3DUiControls.rotate]).
     */
    fun updateTopMarginForUiControls(uiControls: Map3DUiControls) {
        lastAppliedUiControls = uiControls
        applyTopMargin(uiControls)
    }

    private fun applyTopMargin(uiControls: Map3DUiControls) {
        val lp = layoutParams as? ViewGroup.MarginLayoutParams ?: return
        val density = resources.displayMetrics.density
        val baseMarginDp = uiControls.calculateFabMarginDp()
        val effectiveStatusBarOffset = resolveEffectiveStatusBarOffsetPx()
        val targetTopMargin = effectiveStatusBarOffset + (baseMarginDp * density).toInt()
        if (lp.topMargin != targetTopMargin) {
            lp.topMargin = targetTopMargin
            layoutParams = lp
        }
    }

    /**
     * Attaches a [GoogleMap3D] instance.
     *
     * Settings changes will automatically be applied to this map instance.
     */
    fun attachMap(googleMap3D: GoogleMap3D?) {
        this.attachedMap = googleMap3D
        val currentSettings = settingsController.getSettings()
        updateTopMarginForUiControls()
        if (googleMap3D != null) {
            currentSettings.applyTo(googleMap3D)
            requestSiblingMapViewInsets()
            // Re-apply after a short delay to ensure native engine layout pass preserves controls
            postDelayed({
                if (attachedMap == googleMap3D) {
                    val latest = settingsController.getSettings()
                    latest.applyTo(googleMap3D)
                    updateTopMarginForUiControls()
                    requestSiblingMapViewInsets()
                    onSettingsChangedListener?.onSettingsChanged(latest)
                }
            }, 350L)
        }
    }

    /**
     * Sets a provider lambda that resolves the active [GoogleMap3D] instance on demand.
     */
    fun setMapProvider(provider: () -> GoogleMap3D?) {
        this.mapProvider = provider
    }

    /** Returns the current [Map3DSettings] snapshot. */
    fun getSettings(): Map3DSettings = settingsController.getSettings()

    /** Programmatically sets new [Map3DSettings]. */
    fun setSettings(settings: Map3DSettings) {
        settingsController.setSettings(settings)
    }

    /** Sets a callback invoked whenever the user updates settings in the dialog. */
    fun setOnSettingsChangedListener(listener: OnSettingsChangedListener?) {
        this.onSettingsChangedListener = listener
    }

    /** Displays the [Map3DQuickSettingsDialog] modal. */
    fun showDialog() {
        val map = attachedMap ?: mapProvider?.invoke()
        activeDialog?.dismiss()
        activeDialog = Map3DQuickSettingsDialog.show(
            context = context,
            controller = settingsController,
            googleMap3D = map,
            sampleDefaultMapMode = sampleDefaultMapMode,
            onDismiss = { activeDialog = null },
        )
    }
}
// [END maps_3d_quick_settings_button]
