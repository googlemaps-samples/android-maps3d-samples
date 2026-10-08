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

import com.google.android.gms.maps3d.model.Map3DMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * JVM Unit Tests for [Map3DSettingsController] and [Map3DSettings].
 */
class Map3DSettingsControllerTest {

    private lateinit var controller: Map3DSettingsController

    @Before
    fun setup() {
        controller = Map3DSettingsController()
    }

    @Test
    fun initialState_isConfiguredWithDefaults() {
        val state = controller.getSettings()
        assertEquals(Map3DMode.HYBRID, state.mapMode)
        assertEquals(Map3DColorScheme.FOLLOW_SYSTEM, state.colorScheme)
    }

    @Test
    fun customInitialState_isPreserved() {
        val custom = Map3DSettings(
            mapMode = Map3DMode.SATELLITE,
            colorScheme = Map3DColorScheme.DARK,
        )
        val customController = Map3DSettingsController(initialSettings = custom)
        assertEquals(Map3DMode.SATELLITE, customController.getSettings().mapMode)
        assertEquals(Map3DColorScheme.DARK, customController.getSettings().colorScheme)
    }

    @Test
    fun setMapMode_satellite_updatesStateAndNotifiesListener() {
        var notified = false
        var capturedSettings: Map3DSettings? = null
        controller.addListener { settings ->
            notified = true
            capturedSettings = settings
        }

        val updated = controller.setMapMode(Map3DMode.SATELLITE)

        assertTrue(notified)
        assertEquals(Map3DMode.SATELLITE, updated.mapMode)
        assertEquals(Map3DMode.SATELLITE, capturedSettings?.mapMode)
    }

    @Test
    fun setMapMode_roadmap_updatesState() {
        val updated = controller.setMapMode(Map3DMode.ROADMAP)
        assertEquals(Map3DMode.ROADMAP, updated.mapMode)
    }

    @Test
    fun setMapMode_unknownOrInvalid_doesNotChangeState() {
        var callCount = 0
        controller.addListener { callCount++ }

        val result = controller.setMapMode(999)

        assertEquals(Map3DMode.HYBRID, result.mapMode)
        assertEquals(0, callCount)
    }

    @Test
    fun setMapMode_sameValue_doesNotNotifyListener() {
        var callCount = 0
        controller.addListener { callCount++ }

        controller.setMapMode(Map3DMode.HYBRID) // Already HYBRID

        assertEquals(0, callCount)
    }

    @Test
    fun setColorScheme_dark_updatesStateAndNotifiesListener() {
        var notified = false
        controller.addListener { notified = true }

        val updated = controller.setColorScheme(Map3DColorScheme.DARK)

        assertTrue(notified)
        assertEquals(Map3DColorScheme.DARK, updated.colorScheme)
    }

    @Test
    fun setColorScheme_light_updatesState() {
        val updated = controller.setColorScheme(Map3DColorScheme.LIGHT)
        assertEquals(Map3DColorScheme.LIGHT, updated.colorScheme)
    }

    @Test
    fun setColorScheme_sameValue_doesNotNotify() {
        var callCount = 0
        controller.addListener { callCount++ }

        controller.setColorScheme(Map3DColorScheme.FOLLOW_SYSTEM) // Already FOLLOW_SYSTEM

        assertEquals(0, callCount)
    }

    @Test
    fun setSettings_updatesStateAndNotifiesListener() {
        var callCount = 0
        controller.addListener { callCount++ }

        val newSettings = Map3DSettings(
            mapMode = Map3DMode.SATELLITE,
            colorScheme = Map3DColorScheme.DARK,
        )
        val updated = controller.setSettings(newSettings)

        assertEquals(1, callCount)
        assertEquals(Map3DMode.SATELLITE, updated.mapMode)
        assertEquals(Map3DColorScheme.DARK, updated.colorScheme)
    }

    @Test
    fun removeListener_stopsReceivingUpdates() {
        var callCount = 0
        val listener: (Map3DSettings) -> Unit = { callCount++ }

        controller.addListener(listener)
        controller.setMapMode(Map3DMode.SATELLITE)
        assertEquals(1, callCount)

        controller.removeListener(listener)
        controller.setMapMode(Map3DMode.ROADMAP)
        assertEquals(1, callCount)
    }

    @Test
    fun initialState_uiControlsDefaultToEnabled() {
        val state = controller.getSettings()
        assertTrue(state.uiControls.allEnabled)
        assertTrue(state.uiControls.zoom)
        assertTrue(state.uiControls.tilt)
        assertTrue(state.uiControls.rotate)
        assertTrue(state.uiControls.compass)
        assertTrue(state.uiControls.panVertical)
        assertTrue(state.uiControls.panHorizontal)
    }

    @Test
    fun setAllUiControls_false_disablesAllAndNotifies() {
        var notified = false
        controller.addListener { notified = true }

        val updated = controller.setAllUiControls(false)

        assertTrue(notified)
        assertTrue(updated.uiControls.noneEnabled)
        assertFalse(updated.uiControls.zoom)
    }

    @Test
    fun setAllUiControls_true_enablesAll() {
        controller.setAllUiControls(false)
        val updated = controller.setAllUiControls(true)
        assertTrue(updated.uiControls.allEnabled)
    }

    @Test
    fun updateUiControl_togglesSingleControl() {
        var captured: Map3DSettings? = null
        controller.addListener { captured = it }

        val updated = controller.updateUiControl(zoom = false)

        assertFalse(updated.uiControls.zoom)
        assertTrue(updated.uiControls.tilt)
        assertFalse(captured?.uiControls?.zoom ?: true)
    }

    @Test
    fun setUiControls_sameState_doesNotNotify() {
        var callCount = 0
        controller.addListener { callCount++ }

        controller.setUiControls(Map3DUiControls()) // Already default

        assertEquals(0, callCount)
    }
}
