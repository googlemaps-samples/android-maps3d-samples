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

package com.example.advancedmaps3dsamples

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.advancedmaps3dsamples.ainavigator.AiNavigatorActivity
import com.example.advancedmaps3dsamples.scenarios.ScenariosActivity
import com.example.advancedmaps3dsamples.ui.theme.AdvancedMaps3DSamplesTheme
import dagger.hilt.android.AndroidEntryPoint

data class MapSample(@StringRes val label: Int, val clazz: Class<*>)

private val samples =
    listOf(
        MapSample(R.string.map_sample_scenarios, ScenariosActivity::class.java),
        MapSample(R.string.map_sample_ai_navigator, AiNavigatorActivity::class.java),
    )

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvancedMaps3DSamplesTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.map_sample_title)) }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .fillMaxSize() // Make the column take up the whole screen
                                .padding(16.dp), // Add some overall padding to the column
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Add vertical spacing between items
                    ) {
                        for (sample in samples) {
                            Sample(
                                sample = sample,
                                onClick = { launchActivity(sample.clazz) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

    }

    private fun launchActivity(clazz: Class<*>) {
        try {
            startActivity(Intent(this, clazz))
        } catch (e: ClassNotFoundException) {
            Log.e("SamplesMenu", "Activity not found: $clazz", e)
            Toast.makeText(this, "Activity not found: $clazz", Toast.LENGTH_SHORT).show()
        }
    }
}


@Composable
fun Sample(sample: MapSample, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(modifier = modifier, onClick = onClick) { Text(text = stringResource(sample.label)) }
}
