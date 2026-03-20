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

### **Algorithm Blueprint**
1.  **Extract Tag Buffers**:
    *   Find full comments containing `[START maps_...]` and `[END maps_...]`.
    *   Isolate the raw text coordinate segments accurately.
2.  **Signature Parsing**:
    *   Read snippet files looking for calls (e.g., `model.setClickListener`).
    *   Increment an **Occurrence Dictionary map** (`{"Model.setClickListener": 1}`).
3.  **Generate `CATALOG.md`**:
    *   Create Markdown Tables listing absolute counts.
    *   If a method has **`0` counts**, listed securely below inside the "Missing API Coverage" deficits layout index to highlight gaps.

### **Example Layout**
| API Endpoint | Occurrences | Snippets Locations |
| :--- | :--- | :--- |
| `Model.setClickListener` | 1 | `ModelSnippets.kt:74` |
| `Polyline.setClickListener` | 2 | `PolylineSnippets.kt:50`, `PolylineSnippets.java:45` |

---

## 🏃 3. Continuous Updates

To maintain documentation accuracy, bind the parser execution to a convenient script that compiles the snippets before generating accurate Bytecode indexing:

```bash
#!/bin/bash
# run_catalog.sh
./gradlew :snippets:compileDebugKotlin
python3 scripts/catalog_api.py
```
This forces developers to keep methods building flawlessly before sync layouts are flushed backwards.
