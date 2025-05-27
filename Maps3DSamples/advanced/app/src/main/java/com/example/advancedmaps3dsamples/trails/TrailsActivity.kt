// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.trails

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.advancedmaps3dsamples.common.ThreeDMap
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrailsActivity : ComponentActivity() {
    private val viewModel by viewModels<TrailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val viewState by viewModel.viewState.collectAsStateWithLifecycle()

            AdvancedMaps3DSamplesTheme(
                dynamicColor = false
            ) {
                when (val vs = viewState) {
                    ViewState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
                    is ViewState.TrailMap -> MapScreen(
                        options = vs.options,
                        onMap3dViewReady = { viewModel.setGoogleMap3D(it) },
                        onReleaseMap = { viewModel.releaseGoogleMap3D() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun MapScreen(
    options: Map3DOptions,
    onMap3dViewReady: (GoogleMap3D) -> Unit,
    onReleaseMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        ThreeDMap(
            modifier = Modifier.fillMaxSize(),
            options = options,
            onMap3dViewReady = onMap3dViewReady,
            onReleaseMap = onReleaseMap,
        )
    }
}

@Composable
fun LoadingScreen(modifier: Modifier) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(text = "Loading...")
        }
    }
}
