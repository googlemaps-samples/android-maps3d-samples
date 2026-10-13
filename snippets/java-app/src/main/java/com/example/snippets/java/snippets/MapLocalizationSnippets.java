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

package com.example.snippets.java.snippets;

import com.example.snippets.java.TrackedMap3D;
import com.example.snippets.java.annotations.SnippetGroup;
import com.example.snippets.java.annotations.SnippetItem;
import com.google.android.gms.maps3d.model.LocaleOptions;
import java.util.Locale;

@SnippetGroup(
        title = "Map Localization",
        description =
                "Snippets demonstrating 3D map language and region localization using LocaleOptions and GoogleMap3D.setLocale.")
public class MapLocalizationSnippets {

    private final TrackedMap3D map;

    public MapLocalizationSnippets(TrackedMap3D map) {
        this.map = map;
    }

    /** Localizes 3D map labels to Simplified Chinese (`zh`) and China (`CN`) region. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "1. Chinese (zh-CN)",
            description = "Applies language='zh' and region='CN' via LocaleOptions and setLocale.")
    public void setChineseLocalization() {
        // [START maps_android_3d_localization_init_java]
        LocaleOptions localeOptions = new LocaleOptions();
        localeOptions.setLanguage("zh");
        localeOptions.setRegion("CN");
        map.setLocale(localeOptions);
        // [END maps_android_3d_localization_init_java]
    }

    /** Localizes 3D map labels to French (`fr`) and France (`FR`) region. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "2. French (fr-FR)",
            description = "Applies language='fr' and region='FR' via LocaleOptions and setLocale.")
    public void setFrenchLocalization() {
        // [START maps_android_3d_localization_runtime_java]
        LocaleOptions localeOptions = new LocaleOptions();
        localeOptions.setLanguage("fr");
        localeOptions.setRegion("FR");
        map.setLocale(localeOptions);
        // [END maps_android_3d_localization_runtime_java]
    }

    /** Localizes 3D map labels to Japanese (`ja`) and Japan (`JP`) region. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "3. Japanese (ja-JP)",
            description = "Applies language='ja' and region='JP' via LocaleOptions and setLocale.")
    public void setJapaneseLocalization() {
        // [START maps_android_3d_localization_japanese_java]
        LocaleOptions localeOptions = new LocaleOptions();
        localeOptions.setLanguage("ja");
        localeOptions.setRegion("JP");
        map.setLocale(localeOptions);
        // [END maps_android_3d_localization_japanese_java]
    }

    /** Resets the map language and region back to the Android device's system default locale. */
    @SuppressWarnings("unused")
    @SnippetItem(
            title = "4. Reset System Default",
            description = "Resets language and region to the device's system default locale.")
    public void resetToSystemDefaultLocale() {
        // [START maps_android_3d_localization_default_java]
        Locale defaultLocale = Locale.getDefault();
        LocaleOptions defaultLocaleOptions = new LocaleOptions();
        defaultLocaleOptions.setLanguage(defaultLocale.getLanguage());
        defaultLocaleOptions.setRegion(defaultLocale.getCountry());
        map.setLocale(defaultLocaleOptions);
        // [END maps_android_3d_localization_default_java]
    }
}
