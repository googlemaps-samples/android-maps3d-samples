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

package com.example.maps3d.common.showcase.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps3d.common.showcase.FrameworkType
import com.example.maps3d.common.showcase.ShowcaseRegistry

/**
 * Reusable UI and helper utilities for the cross-framework "Rosetta Stone" sample switcher.
 *
 * Allows developers to instantly jump between Jetpack Compose, Kotlin Views, and Java Views
 * implementations of the same sample.
 */
object CrossFrameworkSwitcher {

    /**
     * Launches the equivalent sample in the target framework.
     */
    fun switchSample(
        context: Context,
        sampleId: String,
        targetFramework: FrameworkType,
        finishCurrent: Boolean = false,
    ) {
        val sample = ShowcaseRegistry.getSampleById(sampleId)
        if (sample == null) {
            Toast.makeText(context, "Sample not found: $sampleId", Toast.LENGTH_SHORT).show()
            return
        }

        val className = sample.getActivityClassName(targetFramework)
        if (className == null) {
            Toast.makeText(
                context,
                "No ${targetFramework.displayName} implementation available for ${sample.title}",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        try {
            val targetClass = Class.forName(className)
            val intent = Intent(context, targetClass).apply {
                putExtra("EXTRA_SAMPLE_ID", sampleId)
            }
            context.startActivity(intent)
            if (finishCurrent && context is Activity) {
                context.finish()
            }
        } catch (e: ClassNotFoundException) {
            Toast.makeText(
                context,
                "Could not load activity: $className",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

/**
 * Composable pill button that switches to another framework.
 */
@Composable
fun FrameworkSwitchPill(
    sampleId: String,
    targetFramework: FrameworkType,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accentColor = Color(targetFramework.accentColorHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier.clickable {
            CrossFrameworkSwitcher.switchSample(context, sampleId, targetFramework)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch framework",
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${targetFramework.iconEmoji} ${targetFramework.badge}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}
