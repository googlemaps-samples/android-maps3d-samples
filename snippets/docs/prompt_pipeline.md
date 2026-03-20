# 💬 The Prompt Pipeline: Building a Snippet Framework

Use this **4-Stage Prompt Pipeline** to instruct an AI Coding Assistant to build a standalone snippet and cataloging module for a **New SDK**. 

Copy and paste these prompts **one at a time**, waiting for the Assistant to complete and verify each stage before proceeding.

---

### 🟢 **Stage 1: API Audit & Indexing**
> **Goal**: Feed the SDK specification list and generate the Ground Truth JSON dictionary representing all classes/methods.

```text
You are an expert Tooling Developer. We need to create an API Index map for a new SDK module.

I will provide you with a list of the public classes and methods (either compiled symbols or documentation). 

Your Task:
1. Create an `api_manifest.json` dictionary.
2. The keys must be the Class names (e.g., "Model", "Polyline").
3. The values must be a list of non-getter/setter methods (e.g., "setClickListener", "setExtruded").

[INSERT SDK REFERENCE OR COMPILED SYMBOLS HERE]
```

---

### 🔵 **Stage 2: Interface & Decorator Scaffolding**
> **Goal**: Design the Annotations and the Clean-Slate Wrapper (`TrackedMap3D`) to decouple creating snippets from manual session cleanup.

```text
You are an expert Android Architect. We have an index of SDK methods. Now we need to build the snippet execution harness.

Your Task:
1. Create two Annotations (`@SnippetGroup`, `@SnippetItem`) to allow reflection crawling of categorized snippet classes dynamically.
2. Create a Decorator Wrapper/Delegate (e.g., `TrackedMap3D` or `TrackedPlacesClient`) wrapping the native SDK handle.
3. This wrapper MUST track added items (Markers, Models, Anchors) in an internal list so the Runner Application can execute `wrapper.clear()` automatically between view switches.

Output the code for the Annotations and the Decorator Delegate in both Kotlin and Java.
```

---

### 🟡 **Stage 3: Catalog Script Generation**
> **Goal**: Create the Python script (`catalog_api.py`) that reads `.java`/`.kt` and maps occurrences against `api_manifest.json`.

```text
You are a Static Analyzer Engineer. We need a script to track which SDK APIs are covered by our documentation.

Your Task:
1. Create a Python script `catalog_api.py`.
2. It must read `api_manifest.json` as the ground truth checklist list.
3. It must scan `.java` and `.kt` source files inside the snippets module directories, looking for Class.Method calls or extracted tags.
4. Output a `CATALOG.md` file listing a table of API occurrences and accurate line locations.
5. Create a bottom section "Missing API Coverage" for methods with 0 occurrences.

Write the core parser algorithm robustly dealing with lambda variables extensions and safe-calls.
```

---

### 🔴 **Stage 4: Code Generation & Parity Loops**
> **Goal**: Iteratively write the actual snippet extracts using the approved catalog and decorator.

```text
You are a Principal DevRel Developer. We have the Annotations, target Manifest, and Cataloging script setup.

Your Task:
1. Scan the `CATALOG.md` Missing APIs list.
2. Pick a category topic (e.g., "Camera Controls", "Shapes").
3. Generate Snippet classes (e.g., `CameraSnippets.kt` and `CameraSnippets.java`) filling this missing deficit gap.
4. Ensure exact step-by-step logic parity between Kotlin and Java.
5. Use the Decorator wrapper created earlier to add elements, and wrap async layout callbacks to dispatch Toast alerts on the UI thread securely.

Let's start by generating the snippets for [INSERT CATEGORY].
```
