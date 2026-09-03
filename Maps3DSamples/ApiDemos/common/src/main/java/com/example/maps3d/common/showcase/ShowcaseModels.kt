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

/**
 * Enumerates the supported development frameworks for Google Maps 3D on Android.
 *
 * Each framework represents a distinct pedagogical pillar with full feature parity.
 *
 * @property id Unique programmatic identifier.
 * @property displayName Human-readable name for UI titles and headers.
 * @property badge Short badge label displayed on sample cards.
 * @property description Brief summary of the architecture and target audience.
 * @property iconEmoji Distinctive emoji/glyph icon representing the framework.
 * @property accentColorHex Hex color value for theme accents.
 */
enum class FrameworkType(
    val id: String,
    val displayName: String,
    val badge: String,
    val description: String,
    val iconEmoji: String,
    val accentColorHex: Long,
) {
    COMPOSE(
        id = "compose",
        displayName = "Jetpack Compose",
        badge = "Compose",
        description = "Modern declarative 3D UI with GoogleMap3D composable, state flows & coroutines.",
        iconEmoji = "⚛️",
        accentColorHex = 0xFF4285F4,
    ),
    KOTLIN_VIEWS(
        id = "kotlin",
        displayName = "Kotlin Views",
        badge = "Kotlin",
        description = "Traditional Android XML Views with ViewModels, Kotlin DSLs & lifecycle scopes.",
        iconEmoji = "💜",
        accentColorHex = 0xFF7F52FF,
    ),
    JAVA_VIEWS(
        id = "java",
        displayName = "Java Views",
        badge = "Java",
        description = "Enterprise Android XML Views with standard Java builders and event listeners.",
        iconEmoji = "☕",
        accentColorHex = 0xFFE76F51,
    );

    companion object {
        fun fromId(id: String?): FrameworkType =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: COMPOSE
    }
}

/**
 * Pedagogical difficulty tiers for organizing samples progressively.
 *
 * @property displayName User-facing tier header title.
 * @property badge Emoji icon indicating the tier.
 * @property order Sort order from beginner to advanced.
 */
enum class SampleTier(
    val displayName: String,
    val badge: String,
    val order: Int,
) {
    BEGINNER("Beginner", "🟢", 1),
    INTERMEDIATE("Intermediate", "🟡", 2),
    ADVANCED("Advanced", "🔴", 3);
}

/**
 * Metadata definition for a sample across frameworks.
 *
 * @property id Canonical feature identifier (e.g. "path_following").
 * @property title User-facing title.
 * @property subtitle Short summary of what is demonstrated.
 * @property tier Pedagogical difficulty classification.
 * @property tags Keyword tags for search filtering.
 * @property iconEmoji Distinctive visual emoji for the sample.
 * @property composeActivity Full class name of Compose Activity implementation.
 * @property kotlinActivity Full class name of Kotlin Views Activity implementation.
 * @property javaActivity Full class name of Java Views Activity implementation.
 */
data class ShowcaseSample(
    val id: String,
    val title: String,
    val subtitle: String,
    val tier: SampleTier,
    val tags: List<String>,
    val iconEmoji: String,
    val composeActivity: String? = null,
    val kotlinActivity: String? = null,
    val javaActivity: String? = null,
) {
    fun getActivityClassName(framework: FrameworkType): String? = when (framework) {
        FrameworkType.COMPOSE -> composeActivity
        FrameworkType.KOTLIN_VIEWS -> kotlinActivity
        FrameworkType.JAVA_VIEWS -> javaActivity
    }

    fun isAvailable(framework: FrameworkType): Boolean =
        getActivityClassName(framework) != null
}
