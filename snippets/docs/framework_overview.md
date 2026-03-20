# 📖 Framework Overview: Snippet Architecture

This guide details the **Decoupled Architecture** used to maintain lightweight, standalone code snippets that can be dynamically loaded into a runner application without manual manifest linking boilerplates.

---

## 🧩 1. Dynamic Loading via Annotations

To avoid hardcoded lists inside launching activities (e.g. `when (snippet) { ... }`), the framework uses simple **Runtime Annotations**. 

### **The Anatomy**
1.  **`@SnippetGroup`**: Applied at the class level to categorize a series of snippets (e.g. "Polygons", "Camera").
2.  **`@SnippetItem`**: Applied at the function level to denote an individual runnable code extract.

### **Example (Kotlin)**
```kotlin
@SnippetGroup(
    title = "Models",
    description = "Snippets demonstrating 3D Model integration."
)
class ModelSnippets(private val context: Context, private val map: TrackedMap3D) {

    @SnippetItem(
        title = "1. Basic Model",
        description = "Places a GlTF model onto coordinates."
    )
    fun addBasicModel() { ... }
}
```

### **Why use Annotations?**
-   **Reflection Loading**: The runner application uses reflection to crawl the package index for classes carrying coordinate loads.
-   **No UI Boilerplate**: Adding a new snippet function immediately populates the UI checklist dynamically without modifying an adapter.

---

## 🛡️ 2. Optional: The Session State Cleaner (Decorator)

For **Visual Map SDKs** or Canvas overlay rendering engines, added elements (Markers, Shapes) may persist in-memory until explicitly `.remove()` is executed. 

Whenever snippets add nodes that pollute standard coordinate viewframes iteratively, it is helpful to wrap the handler in an optional **Decorator Delegate**:

### **Visual Case Study Design**
```java
public class TrackedMap3D {
    private final GoogleMap3D delegate;
    private final List<Object> sessionItems = new ArrayList<>();

    public TrackedMap3D(GoogleMap3D delegate) { this.delegate = delegate; }

    public Marker addMarker(MarkerOptions options) {
        Marker marker = delegate.addMarker(options);
        if (marker != null) sessionItems.add(marker); // Track reference
        return marker;
    }

    // Runner App cleans up easily:
    public void clear() { for (Object item : sessionItems) { ... } }
}
```

### **Applicability**
*   **Visual APIs (Maps, 3D Renderers)**: Highly useful for keeping snippets lightweight and free from `.remove()` boilerplates.
*   **Pure Data APIs (Places, Geocoding)**: Usually unnecessary since responses are simple asynchronous models directly fetched layout buffers.

