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

package com.example.maps3d.common.showcase

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user framework preferences and persistent navigation states for the Maps 3D Showcase.
 */
class ShowcasePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var preferredFramework: FrameworkType?
        get() {
            val id = prefs.getString(KEY_PREFERRED_FRAMEWORK, null) ?: return null
            return FrameworkType.fromId(id)
        }
        set(value) {
            prefs.edit().apply {
                if (value == null) {
                    remove(KEY_PREFERRED_FRAMEWORK)
                } else {
                    putString(KEY_PREFERRED_FRAMEWORK, value.id)
                }
                apply()
            }
        }

    var rememberChoice: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_CHOICE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_REMEMBER_CHOICE, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "maps3d_showcase_preferences"
        private const val KEY_PREFERRED_FRAMEWORK = "key_preferred_framework"
        private const val KEY_REMEMBER_CHOICE = "key_remember_choice"

        @Volatile
        private var instance: ShowcasePreferences? = null

        fun getInstance(context: Context): ShowcasePreferences =
            instance ?: synchronized(this) {
                instance ?: ShowcasePreferences(context.applicationContext).also { instance = it }
            }
    }
}
