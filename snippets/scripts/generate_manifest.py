import os
import re
import json
from collections import defaultdict

# Setup directories relative to the script location
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SNIPPETS_DIR = os.path.dirname(SCRIPT_DIR)
MAPS3D_REF_DIR = os.path.join(SNIPPETS_DIR, "..", "maps3d-ref")

# Functions to omit explicitly
LIFECYCLE_METHODS = {
    "onCreate", "onDestroy", "onStart", "onStop", 
    "onResume", "onPause", "onLowMemory", "onSaveInstanceState",
    "onAttachedToWindow", "onDetachedFromWindow", "onError"
}

def find_source_files(directory):
    files = []
    for root, _, filenames in os.walk(directory):
        for name in filenames:
            if name.endswith('.kt') or name.endswith('.java'):
                files.append(os.path.join(root, name))
    return files

def extract_api_surface(ref_dir):
    """
    Parses maps3d-ref Kotlin and Java files to identify public Classes, Functions, and Properties.
    Skips items annotated with @Hide, 'internal', or 'interface' declarations.
    """
    api_surface = defaultdict(list)
    src_files = find_source_files(ref_dir)
    
    for src_file in src_files:
        with open(src_file, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            
        current_class = None
        in_interface = False
        skip_current_element = False
        
        for line in lines:
            stripped = line.strip()
            
            # Reset skip flag if we find an actual class or function declaration
            is_declaration = bool(re.search(r'\b(class|fun|val|var|interface|enum|public)\b', stripped))
            
            if "@Hide" in stripped or "internal " in stripped or "private " in stripped or "protected " in stripped:
                skip_current_element = True
                if not is_declaration:
                    continue
            
            # Skip entire interfaces as requested
            if "interface " in stripped and "class " not in stripped:
                in_interface = True
                continue
            if in_interface and stripped == "}":
                in_interface = False
                continue
            if in_interface:
                continue
                
            class_match = re.search(r'(?:public\s+)?(?:final\s+)?(?:abstract\s+)?(?:open\s+)?(?:sealed\s+)?(?:data\s+)?class\s+(\w+)', stripped)
            if not class_match:
                class_match = re.search(r'(?:public\s+)?(?:static\s+)?(?:final\s+)?(?:public\s+)?enum\s+(\w+)', stripped)
                
            if class_match and not skip_current_element:
                parsed_class = class_match.group(1)
                basename = os.path.splitext(os.path.basename(src_file))[0]
                if parsed_class != basename:
                    current_class = f"{basename}.{parsed_class}"
                else:
                    current_class = parsed_class
            elif class_match and skip_current_element:
                current_class = None 
                
            if is_declaration and skip_current_element:
                skip_current_element = False
                continue
            
            # Skip anonymous overrides (e.g. object: IOnPlaceClickListener { override fun ... })
            if "override " in stripped or "@Override" in stripped:
                continue

            if current_class and not skip_current_element:
                # 1. Kotlin Functions
                fun_match = re.search(r'(?:public\s+)?fun\s+(?:<[^>]+>\s+)?(\w+)', stripped)
                if fun_match:
                    api_surface[current_class].append(fun_match.group(1))
                
                # 2. Kotlin Properties
                prop_match = re.search(r'(?:public\s+)?(?:val|var)\s+(\w+)', stripped)
                if prop_match:
                    prop_name = prop_match.group(1)
                    api_surface[current_class].append(prop_name)
                    capitalized = prop_name[0].upper() + prop_name[1:]
                    api_surface[current_class].append("get" + capitalized)
                    if "var " in stripped:
                        api_surface[current_class].append("set" + capitalized)
                        
                # 3. Java Methods
                # Match: public void setPosition(...) or public static float clamp(...)
                java_method_match = re.search(r'public\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?[\w\.<>\[\]]+\s+(\w+)\s*\(', stripped)
                if java_method_match:
                    method_name = java_method_match.group(1)
                    if method_name != current_class: # Exclude Java constructors
                        api_surface[current_class].append(method_name)
                        
            if skip_current_element and not stripped.startswith("@") and not stripped.startswith("//") and stripped != "":
                 skip_current_element = False
                 
    # Post-processing Cleanup
    api_surface.pop('for', None)
    
    if "Map3DView" in api_surface:
        api_surface["Map3DView"] = [m for m in api_surface["Map3DView"] if m not in LIFECYCLE_METHODS]
                 
    return api_surface

def main():
    print(f"Extracting API surface from maps3d-ref at: {MAPS3D_REF_DIR}")
    
    if not os.path.exists(MAPS3D_REF_DIR):
        print(f"Error: {MAPS3D_REF_DIR} does not exist.")
        print("This script must be run from an environment where the 'maps3d-ref' source is available.")
        return
        
    api_surface = extract_api_surface(MAPS3D_REF_DIR)
    
    # Deduplicate methods
    for clazz in api_surface:
        api_surface[clazz] = sorted(list(set(api_surface[clazz])))
    
    manifest_path = os.path.join(SCRIPT_DIR, "api_manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(api_surface, f, indent=2)
        
    total_endpoints = sum(len(v) for v in api_surface.values())
    print(f"Successfully extracted {total_endpoints} API endpoints across {len(api_surface)} classes.")
    print(f"Saved independent API resource to: {manifest_path}")

if __name__ == '__main__':
    main()
