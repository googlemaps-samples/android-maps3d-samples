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

import java.util.Properties

// Evaluate if we are in a CI environment
val isCI = System.getenv("CI")?.toBoolean() ?: false

// Share the isCI flag with all subprojects via Gradle's extra properties
extra["isCI"] = isCI

if (!isCI) {
    val secretsFile = file("secrets.properties")
    val requestedTasks = gradle.startParameter.taskNames

    if (requestedTasks.isEmpty() && !secretsFile.exists()) {
        // It's likely an IDE sync if no tasks are specified, so just issue a warning.
        println("Warning: secrets.properties not found. Gradle sync may succeed, but building/running the app will fail.")
    } else if (requestedTasks.isNotEmpty()) {
        val buildTaskKeywords = setOf("build", "install", "assemble")
        val testTaskKeywords = setOf("test", "report", "lint")

        val isBuildTask = requestedTasks.any { name ->
            buildTaskKeywords.any { kw -> name.contains(kw, ignoreCase = true) }
        }
        val isTestTask = requestedTasks.any { name ->
            testTaskKeywords.any { kw -> name.contains(kw, ignoreCase = true) }
        }
        val isDebugTask = requestedTasks.any { task ->
            task.contains("Debug", ignoreCase = true) || task.contains("installAndLaunch", ignoreCase = true)
        }

        if (isBuildTask && !isTestTask && isDebugTask) {
            val defaultsFile = file("local.defaults.properties")
            val requiredKeysMessage = if (defaultsFile.exists()) {
                defaultsFile.readText()
            } else {
                "MAPS3D_API_KEY=<YOUR_API_KEY>\nPLACES_API_KEY=<YOUR_API_KEY>"
            }

            if (!secretsFile.exists()) {
                throw GradleException("secrets.properties file not found. Please create a 'secrets.properties' file in the root project directory with the following content:\n\n$requiredKeysMessage")
            }

            val secrets = Properties()
            secretsFile.inputStream().use { secrets.load(it) }
            val mapsApiKey = secrets.getProperty("MAPS3D_API_KEY")
            val placesApiKey = secrets.getProperty("PLACES_API_KEY")

            if (mapsApiKey.isNullOrBlank() || !mapsApiKey.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))) {
                throw GradleException("Invalid or missing MAPS3D_API_KEY in secrets.properties. Please provide a valid Google Maps API key (starts with 'AIza').")
            }

            if (placesApiKey.isNullOrBlank() || !placesApiKey.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))) {
                throw GradleException("Invalid or missing PLACES_API_KEY in secrets.properties. Please provide a valid Google Places API key (starts with 'AIza').")
            }
        }
    }
}
