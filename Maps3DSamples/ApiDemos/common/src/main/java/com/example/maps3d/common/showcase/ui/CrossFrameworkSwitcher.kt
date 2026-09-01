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
import android.content.res.ColorStateList
import android.graphics.Color as AndroidColor
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.R as AppCompatR
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.maps3d.common.showcase.FrameworkType
import com.example.maps3d.common.showcase.ShowcaseRegistry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.R as MaterialR

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
    @JvmStatic
    @JvmOverloads
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

    /**
     * Configures the [MaterialToolbar] in standard Views (Kotlin/Java) with a back navigation arrow
     * and a prominent next-framework cycler button.
     */
    @JvmStatic
    @JvmOverloads
    fun setupToolbarSwitcher(
        activity: Activity,
        toolbar: MaterialToolbar,
        sampleId: String? = null,
    ) {
        val targetSampleId = sampleId
            ?: activity.intent?.getStringExtra("EXTRA_SAMPLE_ID")
            ?: ShowcaseRegistry.findSampleForActivity(activity.javaClass.name)?.id
            ?: return

        val sample = ShowcaseRegistry.getSampleById(targetSampleId) ?: return
        val currentFramework = ShowcaseRegistry.detectFramework(activity.javaClass.name)
        val nextFramework = ShowcaseRegistry.getNextAvailableFramework(sample, currentFramework) ?: return

        // Set navigation icon (Back arrow) to finish activity
        toolbar.navigationIcon = androidx.appcompat.content.res.AppCompatResources.getDrawable(activity, AppCompatR.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { activity.finish() }

        // Remove any previous switcher button if any
        toolbar.findViewWithTag<View>("FRAMEWORK_SWITCHER_TAG")?.let {
            toolbar.removeView(it)
        }

        val button = MaterialButton(
            activity,
            null,
            MaterialR.attr.materialButtonOutlinedStyle,
        ).apply {
            tag = "FRAMEWORK_SWITCHER_TAG"
            text = "${nextFramework.iconEmoji} ${nextFramework.badge}"
            textSize = 12f
            setPadding(24, 0, 24, 0)
            setTextColor(AndroidColor.WHITE)
            strokeColor = ColorStateList.valueOf(AndroidColor.WHITE)
            strokeWidth = 2
            cornerRadius = 32
            layoutParams = androidx.appcompat.widget.Toolbar.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL,
            ).apply {
                marginEnd = (16 * activity.resources.displayMetrics.density).toInt()
            }
            setOnClickListener {
                switchSample(activity, sample.id, nextFramework, finishCurrent = true)
            }
        }
        toolbar.addView(button)
    }
}

/**
 * Composable pill button that switches to another framework.
 */
@Composable
fun FrameworkSwitchPill(
    sampleId: String,
    targetFramework: FrameworkType,
    finishCurrent: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val accentColor = Color(targetFramework.accentColorHex)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = modifier.clickable {
            CrossFrameworkSwitcher.switchSample(context, sampleId, targetFramework, finishCurrent = finishCurrent)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch to ${targetFramework.displayName}",
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${targetFramework.iconEmoji} ${targetFramework.badge}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}

@Composable
fun SampleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    sampleId: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val detectedSample = remember(sampleId) {
        sampleId?.let { ShowcaseRegistry.getSampleById(it) }
            ?: (context as? Activity)?.let { act ->
                act.intent?.getStringExtra("EXTRA_SAMPLE_ID")?.let { ShowcaseRegistry.getSampleById(it) }
                    ?: ShowcaseRegistry.findSampleForActivity(act.javaClass.name)
            }
    }

    val currentFramework = FrameworkType.COMPOSE
    val nextFramework = remember(detectedSample) {
        detectedSample?.let { ShowcaseRegistry.getNextAvailableFramework(it, currentFramework) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                IconButton(
                    onClick = {
                        onBackClick?.invoke() ?: (context as? Activity)?.finish()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
                if (detectedSample != null && nextFramework != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    FrameworkSwitchPill(
                        sampleId = detectedSample.id,
                        targetFramework = nextFramework,
                        finishCurrent = true,
                    )
                }
            }
        }
    }
}
