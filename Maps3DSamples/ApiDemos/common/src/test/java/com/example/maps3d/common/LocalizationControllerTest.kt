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
import com.google.common.truth.Truth.assertThat
import java.util.Locale
import org.junit.Before
import org.junit.Test

/**
 * Tier 2 unit tests for [LocalizationController] and [MapLocalePreset].
 */
class LocalizationControllerTest {

    private lateinit var controller: LocalizationController

    @Before
    fun setUp() {
        controller = LocalizationController()
    }

    @Test
    fun `getState defaults to English US in HYBRID mode`() {
        val state = controller.getState()

        assertThat(state.selectedPreset).isEqualTo(MapLocalePreset.ENGLISH_US)
        assertThat(state.languageCode).isEqualTo("en")
        assertThat(state.regionCode).isEqualTo("US")
        assertThat(state.localeBadgeText).isEqualTo("en-US")
        assertThat(state.mapMode).isEqualTo(Map3DMode.HYBRID)
    }

    @Test
    fun `selectPreset updates language and region options`() {
        val updated = controller.selectPreset(MapLocalePreset.CHINESE_CHINA)

        assertThat(updated.selectedPreset).isEqualTo(MapLocalePreset.CHINESE_CHINA)
        assertThat(updated.languageCode).isEqualTo("zh")
        assertThat(updated.regionCode).isEqualTo("CN")
        assertThat(updated.localeBadgeText).isEqualTo("zh-CN")

        val localeOptions = updated.toLocaleOptions()
        assertThat(localeOptions.language).isEqualTo("zh")
        assertThat(localeOptions.region).isEqualTo("CN")
    }

    @Test
    fun `selectPreset SYSTEM_DEFAULT resolves default LocaleOptions`() {
        controller.selectPreset(MapLocalePreset.FRENCH_FRANCE)

        val resetState = controller.selectPreset(MapLocalePreset.SYSTEM_DEFAULT)
        val expectedDefaultLocale = Locale.getDefault()

        assertThat(resetState.selectedPreset).isEqualTo(MapLocalePreset.SYSTEM_DEFAULT)
        assertThat(resetState.languageCode).isEqualTo(expectedDefaultLocale.language)
        assertThat(resetState.regionCode).isEqualTo(expectedDefaultLocale.country)
        assertThat(resetState.localeBadgeText).startsWith("default (")

        val localeOptions = resetState.toLocaleOptions()
        assertThat(localeOptions.language).isEqualTo(expectedDefaultLocale.language)
        assertThat(localeOptions.region).isEqualTo(expectedDefaultLocale.country)
    }

    @Test
    fun `setMapMode updates mapMode cleanly`() {
        val updatedState = controller.setMapMode(Map3DMode.ROADMAP)
        assertThat(updatedState.mapMode).isEqualTo(Map3DMode.ROADMAP)
    }
}
