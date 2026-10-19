plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
}

tasks.register<Exec>("installAndLaunch") {
    description = "Installs and launches the demo app."
    group = "install"
    dependsOn("app:installDebug")
    commandLine("adb", "shell", "am", "start", "-n", "com.example.alohaexplorer/.MainActivity")
}