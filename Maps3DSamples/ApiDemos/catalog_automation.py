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
    if len(sys.argv) < 3:
        print("Usage: python3 catalog_automation.py <java|kotlin> <TestClassName>")
        sys.exit(1)
        
    app_type = sys.argv[1]
    test_class = sys.argv[2]
    
    if app_type not in ["java", "kotlin"]:
        print("Invalid app type. Use 'java' or 'kotlin'.")
        sys.exit(1)
        
    # Workspace root relative to this script
    script_dir = os.path.dirname(os.path.abspath(__file__))
    workspace_root = os.path.abspath(os.path.join(script_dir, "../.."))
    
    package_mapping = {
        "java": "com.example.maps3djava",
        "kotlin": "com.example.maps3dkotlin"
    }
    package = package_mapping[app_type]
    
    module_mapping = {
        "java": ":Maps3DSamples:ApiDemos:java-app",
        "kotlin": ":Maps3DSamples:ApiDemos:kotlin-app"
    }
    module = module_mapping[app_type]
    
    # 1. Install and Run Test
    print(f"Installing {app_type} app...")
    success, _ = run_command(f"./gradlew {module}:installDebug", cwd=workspace_root)
    if not success: sys.exit(1)
    
    print(f"Installing {app_type} test app...")
    success, _ = run_command(f"./gradlew {module}:installDebugAndroidTest", cwd=workspace_root)
    if not success: sys.exit(1)
    
    print("Running test...")
    cmd = f"adb shell am instrument -w -e class {package}.{test_class} {package}.test/androidx.test.runner.AndroidJUnitRunner"
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
        "PolygonsVisualTest": "polygons_screenshot.png",
        "ModelsVisualTest": "models_screenshot.png",
        "MarkersVisualTest": "markers_screenshot.png",
        "RoutesVisualTest": "routes_screenshot.png",
    }
    
    filename = filename_mapping.get(test_class, f"{test_class.lower()}_screenshot.png")
    local_path = filename
    
    # Use run-as to read the file from the app's data directory and pipe it to a local file
    cat_cmd = f"adb shell run-as {package} cat files/{filename}"
    print(f"Running: {cat_cmd}")
    result = subprocess.run(cat_cmd, shell=True, capture_output=True, text=False)
    if result.returncode != 0:
        print(f"Failed to read screenshot via run-as. Error: {result.stderr.decode('utf-8')}")
        sys.exit(1)
        
    with open(local_path, "wb") as f:
        f.write(result.stdout)
    print(f"Pulled screenshot to {local_path}")
        
    # 3. Scale Image using sips (macOS built-in)
    dim_cmd = f"sips -g pixelWidth -g pixelHeight {local_path}"
    success, dim_output = run_command(dim_cmd)
    if not success:
        print("Failed to get image dimensions.")
        sys.exit(1)
        
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
    app_dir_mapping = {
        "java": "java-app",
        "kotlin": "kotlin-app"
    }
    app_dir = app_dir_mapping[app_type]
    target_dir = f"{workspace_root}/Maps3DSamples/ApiDemos/{app_dir}/screenshots"
    os.makedirs(target_dir, exist_ok=True)
    
    target_path = f"{target_dir}/{local_path}"
    os.rename(local_path, target_path)
    print(f"Screenshot saved to {target_path}")
    
    # 5. Update Catalog
    catalog_path = f"{workspace_root}/Maps3DSamples/ApiDemos/{app_dir}/README.md"
    
    mapping = {
        "HelloMapVisualTest": "Basic Map",
        "PolylinesVisualTest": "Polylines",
        "MapInteractionsVisualTest": "Map Interactions",
        "PopoversVisualTest": "Popovers",
        "CameraControlsVisualTest": "Camera Controls",
        "PolygonsVisualTest": "Polygons",
        "ModelsVisualTest": "Models",
        "MarkersVisualTest": "Markers",
        "RoutesVisualTest": "Routes API",
    }
    
    feature_name = mapping.get(test_class)
    if feature_name:
        if not os.path.exists(catalog_path):
            print(f"Catalog file not found: {catalog_path}")
            sys.exit(1)
            
        with open(catalog_path, "r") as f:
            catalog_content = f.read()
            
        image_link = f'<img src="screenshots/{local_path}" alt="Screenshot" width="121"/>'
        
        lines = catalog_content.split("\n")
        updated = False
        for i, line in enumerate(lines):
            if f"| **{feature_name}** |" in line:
                parts = line.split("|")
                if len(parts) >= 5:
                    parts[4] = f" {image_link} "
                    lines[i] = "|".join(parts)
                    updated = True
                    break
                
        if updated:
            catalog_content = "\n".join(lines)
            with open(catalog_path, "w") as f:
                f.write(catalog_content)
            print(f"Updated Catalog README.md for {feature_name}")
        else:
            print(f"Feature {feature_name} not found in catalog.")
    else:
        print(f"No mapping found for {test_class} in catalog.")

if __name__ == "__main__":
    main()
