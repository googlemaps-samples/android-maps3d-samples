import subprocess
import os
import sys
import re

def run_command(cmd, cwd=None):
    print(f"Running: {cmd}")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True, cwd=cwd)
    if result.returncode != 0:
        print(f"Command failed with exit code {result.returncode}")
        print(result.stderr)
        return False, result.stdout
    return True, result.stdout

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 automation_script.py <TestClassName>")
        sys.exit(1)
        
    test_class = sys.argv[1]
    
    # Workspace root
    workspace_root = "/Users/dkhawk/AndroidStudioProjects/github-maps-code/android-maps3d-samples/feat-fillout-missing-samples"
    
    # 1. Install and Run Test
    # We use am instrument to keep the app installed so we can pull files with run-as.
    print("Installing app...")
    success, _ = run_command("./gradlew :Maps3DSamples:ComposeDemos:app:installDebug", cwd=workspace_root)
    if not success: sys.exit(1)
    
    print("Installing test app...")
    success, _ = run_command("./gradlew :Maps3DSamples:ComposeDemos:app:installDebugAndroidTest", cwd=workspace_root)
    if not success: sys.exit(1)
    
    print("Running test...")
    cmd = f"adb shell am instrument -w -e class com.example.composedemos.{test_class} com.example.composedemos.test/androidx.test.runner.AndroidJUnitRunner"
    success, output = run_command(cmd, cwd=workspace_root)
    if not success:
        print("Test failed.")
        sys.exit(1)
        
    print("Test passed. Pulling screenshot...")
    
    # 2. Pull Screenshot
    filename_mapping = {
        "HelloMapVisualTest": "hello_map_screenshot.png",
        "PolylinesVisualTest": "polylines_screenshot.png",
        "MapInteractionsVisualTest": "map_interactions_screenshot.png",
        "PopoversVisualTest": "popovers_screenshot.png",
        "CameraControlsVisualTest": "camera_controls_screenshot.png",
    }
    
    filename = filename_mapping.get(test_class, f"{test_class.lower()}_screenshot.png")
    local_path = filename
    
    # Use run-as to read the file from the app's data directory and pipe it to a local file
    cat_cmd = f"adb shell run-as com.example.composedemos cat files/{filename}"
    print(f"Running: {cat_cmd}")
    result = subprocess.run(cat_cmd, shell=True, capture_output=True, text=False)
    if result.returncode != 0:
        print(f"Failed to read screenshot via run-as. Error: {result.stderr.decode('utf-8')}")
        sys.exit(1)
        
    with open(local_path, "wb") as f:
        f.write(result.stdout)
    print(f"Pulled screenshot to {local_path}")
        
    # 3. Scale Image using sips (macOS built-in)
    # Get dimensions
    dim_cmd = f"sips -g pixelWidth -g pixelHeight {local_path}"
    success, dim_output = run_command(dim_cmd)
    if not success:
        print("Failed to get image dimensions.")
        sys.exit(1)
        
    # Parse width and height
    try:
        width = int(re.search(r"pixelWidth: (\d+)", dim_output).group(1))
        height = int(re.search(r"pixelHeight: (\d+)", dim_output).group(1))
    except AttributeError:
        print(f"Failed to parse dimensions from output: {dim_output}")
        sys.exit(1)
        
    new_width = int(width * 0.5)
    new_height = int(height * 0.5)
    
    print(f"Scaling from {width}x{height} to {new_width}x{new_height}")
    scale_cmd = f"sips -z {new_height} {new_width} {local_path}"
    success, _ = run_command(scale_cmd)
    if not success:
        print("Failed to scale image.")
        sys.exit(1)
        
    # 4. Move to Source
    # Target directory: Maps3DSamples/ComposeDemos/app/src/main/assets/screenshots/
    target_dir = f"{workspace_root}/Maps3DSamples/ComposeDemos/app/src/main/assets/screenshots"
    os.makedirs(target_dir, exist_ok=True)
    
    target_path = f"{target_dir}/{local_path}"
    os.rename(local_path, target_path)
    print(f"Screenshot saved to {target_path}")
    
    # 5. Update Catalog
    catalog_path = f"{workspace_root}/Maps3DSamples/ComposeDemos/app/README.md"
    
    # Mapping from test class to Feature Name in table
    mapping = {
        "HelloMapVisualTest": "Basic Map",
        "PolylinesVisualTest": "Polylines",
        "MapInteractionsVisualTest": "Map Interactions",
        "PopoversVisualTest": "Popovers",
        "CameraControlsVisualTest": "Camera Controls",
    }
    
    feature_name = mapping.get(test_class)
    if feature_name:
        with open(catalog_path, "r") as f:
            catalog_content = f.read()
            
        # Mapping to get activity file path
        activity_mapping = {
            "Basic Map": "hellomap/HelloMapActivity.kt",
            "Polylines": "polylines/PolylinesActivity.kt",
            "Map Interactions": "mapinteractions/MapInteractionsActivity.kt",
            "Popovers": "popovers/PopoversActivity.kt",
            "Camera Controls": "cameracontrols/CameraControlsActivity.kt",
        }
        activity_path = activity_mapping.get(feature_name)
        
        image_link = f'<img src="src/main/assets/screenshots/{local_path}" alt="Screenshot" width="121"/>'
        
        # Find the line for this feature and replace it
        lines = catalog_content.split("\n")
        updated = False
        for i, line in enumerate(lines):
            if f"| **{feature_name}** |" in line:
                # Get the activity file basename
                file_basename = activity_path.split("/")[-1]
                # Construct new line
                lines[i] = f"| **{feature_name}** | ✅ Done | [{file_basename}](src/main/java/com/example/composedemos/{activity_path}) | {image_link} |"
                updated = True
                break
                
        if updated:
            catalog_content = "\n".join(lines)
            with open(catalog_path, "w") as f:
                f.write(catalog_content)
            print(f"Updated Compose Catalog README.md for {feature_name}")
        else:
            print(f"Feature {feature_name} not found in catalog.")
    else:
        print(f"No mapping found for {test_class} in catalog.")

if __name__ == "__main__":
    main()
