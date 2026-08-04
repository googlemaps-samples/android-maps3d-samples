/*
 * Copyright 2025 Google LLC
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

package com.example.maps3dkotlin.common

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.example.maps3dcommon.R
import com.google.android.material.appbar.MaterialToolbar

/**
 * Base activity for skeleton samples in Kotlin.
 * This activity displays a simple "Coming soon!" message and sets the toolbar title.
 */
abstract class BaseSkeletonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton)

        val toolbar: MaterialToolbar = findViewById(R.id.top_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(getTitleResId())
            setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Returns the string resource ID for the activity title.
     */
    @StringRes
    protected abstract fun getTitleResId(): Int
}
