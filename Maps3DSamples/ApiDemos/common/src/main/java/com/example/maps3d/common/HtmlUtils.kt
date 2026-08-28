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
import android.text.Spanned
import androidx.annotation.RawRes
import androidx.core.text.HtmlCompat

/**
 * Shared utility for loading and parsing raw HTML resources across Kotlin, Java, and Compose.
 */
object HtmlUtils {

    /**
     * Reads an HTML file from [res/raw] and parses it into an Android [Spanned] instance.
     */
    @JvmStatic
    fun loadRawHtml(context: Context, @RawRes resId: Int): Spanned {
        val htmlText = context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    /**
     * Reads an HTML file from [res/raw] as a raw string.
     */
    @JvmStatic
    fun loadRawHtmlString(context: Context, @RawRes resId: Int): String {
        return context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    }
}
