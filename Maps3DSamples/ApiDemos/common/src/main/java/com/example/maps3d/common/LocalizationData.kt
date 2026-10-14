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

import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.LocaleOptions
import com.google.android.gms.maps3d.model.Map3DMode
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import java.util.Locale

private const val SYSTEM_DEFAULT_CODE = "default"

val DEFAULT_LOCALIZATION_CAMERA: Camera = camera {
    center = latLngAltitude {
        latitude = 37.8080
        longitude = -122.4177
        altitude = 0.0
    }
    tilt = 45.0
    heading = 340.0
    range = 18500.0
    roll = 0.0
}

/**
 * Curated language and region presets for the Maps Localization sample.
 */
enum class MapLocalePreset(
    val displayName: String,
    val nativeLabel: String,
    private val rawLanguage: String? = null,
    private val rawRegion: String? = null,
) {
    ENGLISH_US(
        displayName = "English (US)",
        nativeLabel = "en-US",
        rawLanguage = "en",
        rawRegion = "US",
    ),
    CHINESE_CHINA(
        displayName = "Chinese (China)",
        nativeLabel = "中文 (zh-CN)",
        rawLanguage = "zh",
        rawRegion = "CN",
    ),
    FRENCH_FRANCE(
        displayName = "French (France)",
        nativeLabel = "Français (fr-FR)",
        rawLanguage = "fr",
        rawRegion = "FR",
    ),
    JAPANESE_JAPAN(
        displayName = "Japanese (Japan)",
        nativeLabel = "日本語 (ja-JP)",
        rawLanguage = "ja",
        rawRegion = "JP",
    ),
    SPANISH_SPAIN(
        displayName = "Spanish (Spain)",
        nativeLabel = "Español (es-ES)",
        rawLanguage = "es",
        rawRegion = "ES",
    ),
    ARABIC_SAUDI_ARABIA(
        displayName = "Arabic (Saudi Arabia)",
        nativeLabel = "العربية (ar-SA)",
        rawLanguage = "ar",
        rawRegion = "SA",
    ),
    GERMAN_GERMANY(
        displayName = "German (Germany)",
        nativeLabel = "Deutsch (de-DE)",
        rawLanguage = "de",
        rawRegion = "DE",
    ),
    SYSTEM_DEFAULT(
        displayName = "System Default",
        nativeLabel = SYSTEM_DEFAULT_CODE,
    ),
    ;

    val language: String
        get() = rawLanguage ?: Locale.getDefault().language

    val region: String
        get() = rawRegion ?: Locale.getDefault().country
}

/**
 * Immutable UI state for the Maps Localization sample across Kotlin Views, Java Views, and Compose.
 *
 * @property selectedPreset Currently active [MapLocalePreset].
 * @property languageCode Active ISO-639 language code (`"en"`, `"zh"`, `"fr"`, etc.).
 * @property regionCode Active ISO-3166-1 alpha-2 region code (`"US"`, `"CN"`, `"FR"`, etc.).
 * @property mapMode Current [Map3DMode] (`HYBRID` or `ROADMAP` to display localized labels).
 * @property camera Target [Camera] position.
 */
data class LocalizationState(
    val selectedPreset: MapLocalePreset = MapLocalePreset.ENGLISH_US,
    val languageCode: String = selectedPreset.language,
    val regionCode: String = selectedPreset.region,
    @param:Map3DMode val mapMode: Int = Map3DMode.HYBRID,
    val camera: Camera = DEFAULT_LOCALIZATION_CAMERA,
) {
    /** Formatted locale badge text (e.g. `"zh-CN"` or `"default (en_US)"`). */
    val localeBadgeText: String
        get() = if (selectedPreset == MapLocalePreset.SYSTEM_DEFAULT) {
            "default (${Locale.getDefault()})"
        } else {
            "$languageCode-$regionCode"
        }

    /**
     * Builds a [LocaleOptions] instance matching the current [languageCode] and [regionCode]
     * for runtime locale updates via [com.google.android.gms.maps3d.GoogleMap3D.setLocale].
     */
    fun toLocaleOptions(): LocaleOptions = LocaleOptions().apply {
        language = languageCode
        region = regionCode
    }
}
