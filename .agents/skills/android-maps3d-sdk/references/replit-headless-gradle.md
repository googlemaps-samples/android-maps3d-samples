# Replit & Headless Container Gradle Configuration

> **LOAD THIS FILE ONLY WHEN `$REPL_ID` IS SET, RUNNING IN A HEADLESS CLOUD CONTAINER (Replit, Codespaces, IDX), OR WHEN GRADLE FAILS DUE TO MISSING `ANDROID_HOME` OR OOM (`Exit code 137`).**

## 1. Auto-Detect `$REPL_ID` & Use Ephemeral Low-Memory CLI Flags

Default Android Gradle builds spawn both a `GradleDaemon` (`2GB+`) and a separate `KotlinCompileDaemon` (`1.5GB+`), crashing 2GB–4GB Linux containers with `Exit code 137`.

**Do NOT modify tracked `gradle.properties`** to cap RAM — pass ephemeral memory flags directly on the command line so `git status` stays clean:

```bash
# Day-1 setup verification (Manifest + Resources + Kotlin)
./gradlew processDebugMainManifest processDebugResources compileDebugKotlin \
  -Dorg.gradle.jvmargs="-Xmx1536m -XX:MaxMetaspaceSize=512m" \
  -Dkotlin.daemon.jvm.options="-Xmx1024m" \
  -Dorg.gradle.daemon=false \
  -Dorg.gradle.workers.max=2 \
  --console=plain -q

# Day-2 Kotlin-only edit verification
./gradlew compileDebugKotlin \
  -Dorg.gradle.jvmargs="-Xmx1536m -XX:MaxMetaspaceSize=512m" \
  -Dkotlin.daemon.jvm.options="-Xmx1024m" \
  -Dorg.gradle.daemon=false \
  -Dorg.gradle.workers.max=2 \
  --console=plain -q
```

---

## 2. Headless `ANDROID_HOME` & `secrets.properties` Setup

If Gradle fails with `SDK location not found`:
1. Locate the installed Android SDK directory (`$ANDROID_HOME`, `$ANDROID_SDK_ROOT`, `/opt/android-sdk`, or `~/Android/Sdk`).
2. Write `sdk.dir=<discovered_path>` into git-ignored `local.properties` at the project root.
3. Ensure `local.defaults.properties` (and `secrets.properties` if absent) exists at the **project root** so `secrets-gradle-plugin` resolves `${MAPS3D_API_KEY}` during headless compilation:
   ```properties
   MAPS3D_API_KEY=DEFAULT_API_KEY
   ```
