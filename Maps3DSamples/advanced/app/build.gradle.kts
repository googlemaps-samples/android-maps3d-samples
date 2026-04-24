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

import java.util.Properties
import org.gradle.api.GradleException

// Check for secrets.properties file and valid API key before proceeding with build tasks.
val secretsFile = rootProject.file("secrets.properties")
val isCI = System.getenv("CI")?.toBoolean() ?: false

if (!isCI) {
    val requestedTasks = gradle.startParameter.taskNames
    if (requestedTasks.isEmpty() && !secretsFile.exists()) {
        // It's likely an IDE sync if no tasks are specified, so just issue a warning.
        println("Warning: secrets.properties not found. Gradle sync may succeed, but building/running the app will fail.")
    } else if (requestedTasks.isNotEmpty()) {
        val buildTaskKeywords = listOf("build", "install", "assemble")
        val isBuildTask = requestedTasks.any { task ->
            buildTaskKeywords.any { keyword ->
                task.contains(keyword, ignoreCase = true)
            }
        }

        val testTaskKeywords = listOf("test", "report", "lint")
        val isTestTask = requestedTasks.any { task ->
            testTaskKeywords.any { keyword ->
                task.contains(keyword, ignoreCase = true)
            }
        }

        val isDebugTask = requestedTasks.any { task ->
            task.contains("Debug", ignoreCase = true) || task.contains("installAndLaunch", ignoreCase = true)
        }

        if (isBuildTask && !isTestTask && isDebugTask) {
            val defaultsFile = rootProject.file("local.defaults.properties")
            val requiredKeysMessage = if (defaultsFile.exists()) {
                defaultsFile.readText()
            } else {
                "MAPS3D_API_KEY=<YOUR_API_KEY>"
            }

            if (!secretsFile.exists()) {
                throw GradleException("secrets.properties file not found. Please create a 'secrets.properties' file in the root project directory with the following content:\n\n$requiredKeysMessage")
            }

            val secrets = Properties()
            secretsFile.inputStream().use { secrets.load(it) }
            val apiKey = secrets.getProperty("MAPS3D_API_KEY")

            if (apiKey.isNullOrBlank() || !apiKey.matches(Regex("^AIza[a-zA-Z0-9_-]{35}$"))) {
                throw GradleException("Invalid or missing MAPS3D_API_KEY in secrets.properties. Please provide a valid Google Maps API key (starts with 'AIza').")
            }
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    lint {
        sarifOutput = layout.buildDirectory.file("reports/lint-results.sarif").get().asFile
    }
    namespace = "com.example.advancedmaps3dsamples"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.advancedmaps3dsamples"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.7.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS3D_API_KEY"] = "DEFAULT_API_KEY"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.datetime)
    implementation(libs.dagger)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)

    implementation(libs.play.services.base)
    implementation(libs.play.services.maps3d)

    testImplementation(libs.google.truth)

    // Google Maps Utils for the polyline decoder
    implementation(libs.maps.utils.ktx)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.material.icons.extended)
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

tasks.register<Exec>("installAndLaunch") {
    description = "Installs and launches the demo app."
    group = "install"
    dependsOn("installDebug")
    commandLine("adb", "shell", "am", "start", "-n", "com.example.advancedmaps3dsamples/.MainActivity")
}

tasks.register("prepareKotlinBuildScriptModel"){} 