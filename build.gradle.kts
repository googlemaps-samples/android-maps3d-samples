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

plugins {
    alias(libs.plugins.spotless) apply false
}

// Evaluate if we are in a CI environment
val isCI = System.getenv("CI")?.toBoolean() ?: false

// Share the isCI flag with all subprojects via Gradle's extra properties
extra["isCI"] = isCI

if (!isCI) {
    val secretsFile = file("secrets.properties")
    val requestedTasks = gradle.startParameter.taskNames

    if (requestedTasks.isEmpty() && !secretsFile.exists()) {
        println("Warning: secrets.properties not found. Gradle sync may succeed, but building/running the app will fail.")
    } else if (requestedTasks.isNotEmpty()) {
        // List of application / demo modules that require API keys to run
        val appModules = setOf(
            "ApiDemos:java-app",
            "ApiDemos:kotlin-app",
            "ComposeDemos:app",
            "advanced:app",
            "maps3d-compose-demo",
            "snippets:java-app",
            "snippets:kotlin-app"
        )

        // Check if any requested task builds or installs an application APK
        val isAppBuildTask = requestedTasks.any { name ->
            val n = name.lowercase()
            val isBuildOrInstall = n.contains("build") || n.contains("assemble") || n.contains("install") || n.contains("bundle")
            val isRootTask = !name.contains(":") // Root assemble/install builds all sample apps
            val isAppModuleTask = appModules.any { mod -> name.contains(mod, ignoreCase = true) }
            isBuildOrInstall && (isRootTask || isAppModuleTask)
        }

        val isTestTask = requestedTasks.any { name ->
            val n = name.lowercase()
            n.contains("test") || n.contains("lint")
        }

        if (isAppBuildTask && !isTestTask) {
            val defaultsFile = file("local.defaults.properties")
            val requiredKeysMessage = if (defaultsFile.exists()) {
                defaultsFile.readText()
            } else {
                "MAPS3D_API_KEY=<YOUR_API_KEY>\nMAPS_API_KEY=<YOUR_API_KEY>\nPLACES_API_KEY=<YOUR_API_KEY>"
            }

            if (!secretsFile.exists()) {
                throw GradleException("secrets.properties file not found. Please create a 'secrets.properties' file (or symlink to /usr/local/google/home/dkhawk/git/gmp-github/secrets.properties) in the root project directory with valid Google API keys:\n\n$requiredKeysMessage")
            }

            val secrets = Properties()
            secretsFile.inputStream().use { secrets.load(it) }
            val maps3dApiKey = secrets.getProperty("MAPS3D_API_KEY")
            val mapsApiKey = secrets.getProperty("MAPS_API_KEY") ?: maps3dApiKey
            val placesApiKey = secrets.getProperty("PLACES_API_KEY")

            fun isValidKey(key: String?): Boolean {
                return !key.isNullOrBlank() &&
                    !key.startsWith("DEFAULT_") &&
                    !key.startsWith("<YOUR_") &&
                    key.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))
            }

            if (!isValidKey(maps3dApiKey)) {
                throw GradleException("Invalid or missing MAPS3D_API_KEY in secrets.properties ('$maps3dApiKey'). Please provide a valid Google Maps API key starting with 'AIza'.")
            }
            if (!isValidKey(mapsApiKey)) {
                throw GradleException("Invalid or missing MAPS_API_KEY in secrets.properties ('$mapsApiKey'). Please provide a valid Google Maps API key starting with 'AIza'.")
            }
            if (!isValidKey(placesApiKey)) {
                throw GradleException("Invalid or missing PLACES_API_KEY in secrets.properties ('$placesApiKey'). Please provide a valid Google Places API key starting with 'AIza'.")
            }
        }
    }
}
