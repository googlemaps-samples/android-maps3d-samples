# 🛠️ Tooling Blueprint: API Indexing & Cataloging

To prevent manual checklists from going stale, the framework automates snippet coverage tracking. This is accomplished using a **static code analyzer** mapping compiled SDK endpoints against extracted code blocks.

---

## 📋 1. The API Manifest (`api_manifest.json`)

Before cataloging snippets, the analyzer needs a "Ground Truth" dictionary of all class methods inside the SDK module suite.

### **How to generate it:**
If source code is NOT available, run `javap` or `nm` over the compiled `.jar` or `.aar` extracts:

```bash
# Decompile classes into method lists:
for class in $(find . -name "*.class"); do
    javap -p $class >> raw_symbols.txt
done
```

### **The Dictionary Structure**
The static analyzer reads a list of objects describing continuous function pointers mapping safely:

```json
{
  "Model": [
    "setClickListener",
    "getScale",
    "getPosition"
  ],
  "Polyline": [
    "setClickListener",
    "getWidth"
  ]
}
```

---

## 🐍 2. The Catalog Script (`catalog_api.py`)

The python parser processes `.java` and `.kt` source files iteratively index extracts.

### **Algorithm Blueprint: How `catalog_api.py` Works**

To guarantee accuracy without building fragile regex engines for syntax analyzers, the parser works directly with **compiled bytecode** and source Line Tables:

1.  **Extract Region Boundaries (`extract_region_tags`)**:
    *   Scans `.java` and `.kt` source nodes to find `// [START maps_...]` and `// [END maps_...]`.
    *   Caches the 1-based start line and end line coordinate bounds for every `Tag`.

2.  **Decompile and Analyze Bytecode (`parse_javap_output`)**:
    *   The script runs **`javap -c -l`** over de-compiled snippets classfiles.
    *   `-c` spits out the disassembled instruction stream (e.g. `invokevirtual Method setPosition`).
    *   `-l` extracts the **`LineNumberTable`**: mapping bytecode instruction offsets back to absolute source lines.

3.  **Map Executed Instruction Offsets**:
    *   When an `invokevirtual` matches an endpoint listed in `api_manifest.json`, the script records the instruction **offset pointer**.
    *   It falls back backwards through the `LineNumberTable` sorted offsets to find the closest source line row that correlates with that offset accurately.

4.  **Correlate Tag Range Overlays & Generate Outputs**:
    *   If the exact execution line falls inside a previously cached `[START]`/`[END]` Region Tag bound, it binds that tag’s row frame to the coordinate.
    *   Dumps separately into **`CATALOG.md`** (Index) and **`COVERAGE.md`** (Matrix matrices).

### **Example `CATALOG.md` Layout**
| Snippets Feature | Kotlin Location | Java Location |
| :--- | :--- | :--- |
| **1. Fly To** | `CameraControlSnippets.kt#L62-L81` | `CameraControlSnippets.java#L44-L64` |

---

## 🏃 3. Continuous Updates

To maintain documentation accuracy, bind execution of the Python parser to a convenient script setup that compiles building layout:

```bash
#!/bin/bash
# run_catalog.sh
./gradlew :snippets:compileDebugKotlin
python3 scripts/catalog_api.py
```
This forces developers to keep methods building flawlessly before sync layouts are flushed backwards.
