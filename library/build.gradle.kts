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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.spotless)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                "ktlint_standard_max-line-length" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

android {
    namespace = "com.example.maps3dsamples.library"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.play.services.maps)
    implementation(libs.startup.runtime)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core)
}

abstract class GenerateArtifactIdTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val version: Property<String>

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        val packageName = "com.example.maps3dsamples.library.utils.meta"
        val packagePath = packageName.replace('.', '/')
        val outputFile = File(dir, "$packagePath/ArtifactId.kt")
        outputFile.parentFile.mkdirs()
        val attributionId = "gmp_git_androidmaps3dsamples_v${version.get()}"
        outputFile.writeText(
            """
            package $packageName

            /**
             * Automatically generated object containing the library's attribution ID.
             * This is used to track library usage for analytics.
             */
            public object AttributionId {
                public const val VALUE: String = "$attributionId"
            }
            """.trimIndent(),
        )
    }
}

val generateArtifactIdFile = tasks.register<GenerateArtifactIdTask>("generateArtifactIdFile") {
    outputDir.set(layout.buildDirectory.dir("generated/source/artifactId"))
    version.set(project.version.toString())
}

androidComponents {
    onVariants { variant ->
        variant.sources.java?.addGeneratedSourceDirectory(
            generateArtifactIdFile,
            GenerateArtifactIdTask::outputDir,
        )
    }
}
