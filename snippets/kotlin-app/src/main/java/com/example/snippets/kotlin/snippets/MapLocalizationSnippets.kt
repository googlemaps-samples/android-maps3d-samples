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

package com.example.snippets.kotlin.snippets

import com.example.snippets.kotlin.TrackedMap3D
import com.example.snippets.kotlin.annotations.SnippetGroup
import com.example.snippets.kotlin.annotations.SnippetItem
import com.google.android.gms.maps3d.model.LocaleOptions
import java.util.Locale

@SnippetGroup(
    title = "Map Localization",
    description = "Snippets demonstrating 3D map language and region localization " +
        "using LocaleOptions and GoogleMap3D.setLocale.",
)
class MapLocalizationSnippets(private val map: TrackedMap3D) {
    /**
     * Localizes 3D map labels to Simplified Chinese (`zh`) and China (`CN`) region.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "1. Chinese (zh-CN)",
        description = "Applies language='zh' and region='CN' via LocaleOptions and setLocale.",
    )
    fun setChineseLocalization() {
        // [START maps_android_3d_localization_init_kt]
        val localeOptions = LocaleOptions().apply {
            language = "zh"
            region = "CN"
        }
        map.setLocale(localeOptions)
        // [END maps_android_3d_localization_init_kt]
    }

    /**
     * Localizes 3D map labels to French (`fr`) and France (`FR`) region.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "2. French (fr-FR)",
        description = "Applies language='fr' and region='FR' via LocaleOptions and setLocale.",
    )
    fun setFrenchLocalization() {
        // [START maps_android_3d_localization_runtime_kt]
        val localeOptions = LocaleOptions().apply {
            language = "fr"
            region = "FR"
        }
        map.setLocale(localeOptions)
        // [END maps_android_3d_localization_runtime_kt]
    }

    /**
     * Localizes 3D map labels to Japanese (`ja`) and Japan (`JP`) region.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "3. Japanese (ja-JP)",
        description = "Applies language='ja' and region='JP' via LocaleOptions and setLocale.",
    )
    fun setJapaneseLocalization() {
        // [START maps_android_3d_localization_japanese_kt]
        val localeOptions = LocaleOptions().apply {
            language = "ja"
            region = "JP"
        }
        map.setLocale(localeOptions)
        // [END maps_android_3d_localization_japanese_kt]
    }

    /**
     * Resets the map language and region back to the Android device's system default locale.
     */
    @Suppress("unused")
    @SnippetItem(
        title = "4. Reset System Default",
        description = "Resets language and region to the device's system default locale.",
    )
    fun resetToSystemDefaultLocale() {
        // [START maps_android_3d_localization_default_kt]
        val defaultLocale = Locale.getDefault()
        val defaultLocaleOptions = LocaleOptions().apply {
            language = defaultLocale.language
            region = defaultLocale.country
        }
        map.setLocale(defaultLocaleOptions)
        // [END maps_android_3d_localization_default_kt]
    }
}
