# Secrets Enforcement (Gradle)

To prevent accidental exposure of API keys, add this Gradle task to your project's root `build.gradle.kts` or module-level `build.gradle.kts`. It checks if `secrets.properties` is tracked by Git and fails the build if it is.

## Kotlin DSL (`build.gradle.kts`)

```kotlin
tasks.register("checkSecretsExposure") {
    doLast {
        // 1. Check if secrets.properties is tracked by Git
        val secretsFile = file("secrets.properties")
        if (secretsFile.exists()) {
            val process = ProcessBuilder("git", "ls-files", "--error-unmatch", "secrets.properties")
                .directory(project.rootDir)
                .start()
            val exitCode = process.waitFor()
            
            // If git ls-files finds the file, it returns 0
            if (exitCode == 0) {
                throw GradleException(
                    "SECURITY ALERT: 'secrets.properties' is tracked by Git! " +
                    "Remove it from version control immediately using 'git rm --cached secrets.properties'."
                )
            }
        }

        // 2. Check if local.defaults.properties contains a real-looking key
        val defaultsFile = file("local.defaults.properties")
        if (defaultsFile.exists()) {
            val props = java.util.Properties()
            defaultsFile.inputStream().use { props.load(it) }
            
            props.forEach { key, value ->
                val valueStr = value.toString()
                // Simple heuristic: if it's long and doesn't look like a placeholder
                if (valueStr.length > 20 && !valueStr.contains("YOUR_API_KEY") && !valueStr.contains("PLACEHOLDER")) {
                     throw GradleException(
                         "SECURITY ALERT: Potential real API key found in 'local.defaults.properties' for key '$key'. " +
                         "Use placeholders in this file and put real keys in 'secrets.properties'."
                     )
                }
            }
        }
    }
}

// Run this check before building
tasks.named("preBuild") {
    dependsOn("checkSecretsExposure")
}
```

## Groovy DSL (`build.gradle`)

```groovy
task checkSecretsExposure {
    doLast {
        // 1. Check if secrets.properties is tracked by Git
        def secretsFile = file("secrets.properties")
        if (secretsFile.exists()) {
            def process = new ProcessBuilder("git", "ls-files", "--error-unmatch", "secrets.properties")
                .directory(project.rootDir)
                .start()
            def exitCode = process.waitFor()
            
            if (exitCode == 0) {
                throw new GradleException(
                    "SECURITY ALERT: 'secrets.properties' is tracked by Git! " +
                    "Remove it from version control immediately using 'git rm --cached secrets.properties'."
                )
            }
        }

        // 2. Check if local.defaults.properties contains a real-looking key
        def defaultsFile = file("local.defaults.properties")
        if (defaultsFile.exists()) {
            def props = new Properties()
            defaultsFile.withInputStream { props.load(it) }
            
            props.each { key, value ->
                def valueStr = value.toString()
                if (valueStr.length() > 20 && !valueStr.contains("YOUR_API_KEY") && !valueStr.contains("PLACEHOLDER")) {
                     throw new GradleException(
                         "SECURITY ALERT: Potential real API key found in 'local.defaults.properties' for key '$key'. " +
                         "Use placeholders in this file and put real keys in 'secrets.properties'."
                     )
                }
            }
        }
    }
}

preBuild.dependsOn checkSecretsExposure
```
