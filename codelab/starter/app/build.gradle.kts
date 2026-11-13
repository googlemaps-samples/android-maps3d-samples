plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example.alohaexplorer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.alohaexplorer"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Core AndroidX Activity and edge-to-edge window support
    implementation(libs.androidx.activity)
    // Jetpack Compose integration for ComponentActivity (setContent)
    implementation(libs.androidx.activity.compose)
    // AppCompat support for traditional Android Views (AppCompatActivity)
    implementation(libs.androidx.appcompat)
    // ConstraintLayout for positioning the 3D map and bottom control bar
    implementation(libs.androidx.constraintlayout)
    // Kotlin extensions for core Android framework APIs
    implementation(libs.androidx.core.ktx)
    // Lifecycle-aware coroutine scopes (lifecycleScope) and DefaultLifecycleObserver
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Material Design 3 components for Jetpack Compose
    implementation(libs.androidx.material3)
    // Core Jetpack Compose UI primitives and AndroidView interop
    implementation(libs.androidx.ui)
    // Jetpack Compose graphics and color utilities
    implementation(libs.androidx.ui.graphics)
    // Android Studio preview support for @Composable functions
    implementation(libs.androidx.ui.tooling.preview)
    // Material Design components for Android Views
    implementation(libs.material)
    // Compose Bill of Materials (BOM) to align Compose library versions
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI inspection and tooling in debug builds
    debugImplementation(libs.androidx.ui.tooling)

    // JUnit 4 framework for local JVM unit tests
    testImplementation(libs.junit)

    // AndroidX JUnit runner extensions for instrumented tests
    androidTestImplementation(libs.androidx.junit)
    // Espresso UI testing framework for instrumented tests
    androidTestImplementation(libs.androidx.espresso.core)

    // TODO: Prerequisites - Add the Maps 3D SDK dependency
    // Google Play Services Maps 3D SDK for 3D map rendering, camera animations, markers, polygons, and glTF models
    // implementation(libs.play.services.maps3d)
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}