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
import android.widget.ScrollView

/**
 * A [ScrollView] that constrains its maximum measured height so that
 * collapsible control panels never cover the entire display across varied screen sizes,
 * orientations, and multi-window configurations.
 *
 * Supports specifying `android:maxHeight` in XML or defaults to 480dp.
 */
class MaxHeightScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ScrollView(context, attrs, defStyleAttr) {

    private var maxHeightPx: Int = -1

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.maxHeight))
            try {
                maxHeightPx = typedArray.getDimensionPixelSize(0, -1)
            } finally {
                typedArray.recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxAllowed = if (maxHeightPx > 0) {
            maxHeightPx
        } else {
            (DEFAULT_MAX_HEIGHT_DP * context.resources.displayMetrics.density).toInt()
        }

        val originalSize = MeasureSpec.getSize(heightMeasureSpec)
        val originalMode = MeasureSpec.getMode(heightMeasureSpec)

        val targetHeight = if (originalMode != MeasureSpec.UNSPECIFIED && originalSize > 0) {
            minOf(maxAllowed, originalSize)
        } else {
            maxAllowed
        }

        val constrainedHeightSpec = MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, constrainedHeightSpec)
    }

    companion object {
        private const val DEFAULT_MAX_HEIGHT_DP = 480
    }
}
