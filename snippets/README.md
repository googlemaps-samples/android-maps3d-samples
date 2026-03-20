# Maps 3D Android Snippets

A collection of standalone, extracting-ready code snippets demonstrating standard integration endpoints for the **Google Maps 3D SDK for Android**.

## 📂 Code Layout

The snippets are available in both **Kotlin** and **Java** module sets accurately:
- **Kotlin**: [`snippets/kotlin-app`](kotlin-app/src/main/java/com/example/snippets/kotlin/snippets)
- **Java**: [`snippets/java-app`](java-app/src/main/java/com/example/snippets/java/snippets)

### **Thematic Topics Coverages**
- **`MapInitSnippets`**: Lifecycle initialization listeners and reading steady state intervals.
- **`CameraControlSnippets`**: Orchestrating animations with explicit coordination.
- **`PolygonSnippets` / `PolylineSnippets`**: Managing mesh geometry vector objects overlays.
- **`PopoverSnippets` / `MarkerSnippets`**: Static labels and dynamic 2D frames over 3D coordinates anchor bounds.

---

## 🔬 Architectural Design: The `TrackedMap3D` Decorator

To maintain clean, lightweight extracted code extracts free from lifecycle clearing boilerplate setups, these snippets wrap `GoogleMap3D` in a **Decorator delegate** called **`TrackedMap3D`**:

```kotlin
class TrackedMap3D(val delegate: GoogleMap3D, private val items: MutableList<Any>) {
    fun addMarker(...) {
        val marker = delegate.addMarker(...)
        if (marker != null) items.add(marker) // Track reference for clearing
        return marker
    }
}
```

### **Why use a Delegate wrapper?**
When the layout detail launcher switches between snippets to preview live inside sample frame interfaces, `SnippetRegistry` clears tracked coordinates references accumulated from previous snippets cleanly to guarantee a fully interactive clean-slate coordinate frame every snapshot launch cycle iteratively.

---

## 📜 Complete API Index Catalog
For a detailed breakdown of EXACTLY which native class functions or parameters are covered inside these extracted instruction extracts, refer to the fully indexed checklist summary:

👉 **[`CATALOG.md`](CATALOG.md)**
