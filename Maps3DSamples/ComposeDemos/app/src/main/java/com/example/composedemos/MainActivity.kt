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

package com.example.composedemos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.maps3d.common.showcase.FrameworkType
import com.example.maps3d.common.showcase.ShowcasePreferences
import com.example.maps3d.common.showcase.ui.CrossFrameworkSwitcher
import com.example.maps3d.common.showcase.ui.FrontDoorScreen
import com.example.maps3d.common.showcase.ui.UnifiedCatalogScreen

/**
 * The main "Front Door" launcher activity for the Google Maps 3D Showcase.
 *
 * Integrates framework selection, persistent user preferences, real-time search,
 * and pedagogical tier navigation.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ShowcaseApp()
                }
            }
        }
    }
}

@Composable
fun ShowcaseApp() {
    val context = LocalContext.current
    val prefs = remember { ShowcasePreferences.getInstance(context) }

    // Read stored framework preference
    var selectedFramework by remember { mutableStateOf(prefs.preferredFramework) }
    var showFrontDoor by remember { mutableStateOf(selectedFramework == null || !prefs.rememberChoice) }

    if (showFrontDoor || selectedFramework == null) {
        FrontDoorScreen(
            initialRememberChoice = prefs.rememberChoice,
            onFrameworkSelected = { framework, rememberChoice ->
                prefs.rememberChoice = rememberChoice
                if (rememberChoice) {
                    prefs.preferredFramework = framework
                }
                selectedFramework = framework
                showFrontDoor = false
            },
        )
    } else {
        UnifiedCatalogScreen(
            currentFramework = selectedFramework!!,
            onFrameworkChanged = { newFramework ->
                selectedFramework = newFramework
                if (prefs.rememberChoice) {
                    prefs.preferredFramework = newFramework
                }
            },
            onResetToFrontDoor = {
                prefs.preferredFramework = null
                showFrontDoor = true
            },
            onSampleClick = { sample ->
                CrossFrameworkSwitcher.switchSample(
                    context = context,
                    sampleId = sample.id,
                    targetFramework = selectedFramework!!,
                )
            },
        )
    }
}
