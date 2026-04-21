# Google Maps 3D SDK Documentation & Resources

This file guides you to external and internal resources for the Google Maps 3D SDK. Because this SDK is new, these resources are your primary source of truth.

## External Documentation
*   **Official Reference**: [Google Maps Platform 3D SDK for Android](https://developers.google.com/maps/documentation/android-3d-sdk)
*   **API Reference**: Refer to the Javadoc/KDoc links provided on the developer site for specific class signatures.

## Resources & Samples

Depending on whether you are working inside the `android-maps3d-samples` repository or in a standalone project, use the appropriate source below.

### 1. Local Workspace (If working inside the sample repo)
If you are working directly in the `android-maps3d-samples` repository, use these local directories:
*   **Snippets**: `./snippets/`
*   **Sample Apps**: `./Maps3DSamples/` (Contains advanced usage and Compose integration examples).


### 2. GitHub Repository (If working in a separate project)
If you are working in a new or separate project, you can read the reference implementations directly from GitHub using your tools (e.g., `read_url_content`):
*   **Main Repository**: `https://github.com/googlemaps-samples/android-maps3d-samples`
*   **Kotlin Snippets**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/snippets/kotlin-app`
*   **Java Snippets**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/snippets/java-app`
*   **Advanced Samples**: `https://github.com/googlemaps-samples/android-maps3d-samples/tree/main/Maps3DSamples` (Excellent reference for Jetpack Compose integration).



## Search Strategies for Agents
When asked to implement a feature, do not assume the API. Use your search tools to find examples in the local workspace or repository first.

### Example Searches:
*   To find how to add a Marker: Search for `"Marker"` in `snippets/` or `Maps3DSamples/`.
*   To find camera animation examples: Search for `"animateCamera"` or `"FlyTo"`.
*   To find polyline usage: Search for `"Polyline"`.

