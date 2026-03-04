import os
import re
import subprocess
import glob
from collections import defaultdict

# Setup directories relative to the script location
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SNIPPETS_DIR = os.path.dirname(SCRIPT_DIR)
import json

def extract_region_tags(snippets_dir):
    region_tags = defaultdict(list)
    
    for root, _, filenames in os.walk(snippets_dir):
        if 'build' in root or '.gradle' in root or 'scripts' in root: continue
        for name in filenames:
            if name.endswith(('.kt', '.java', '.xml')):
                filepath = os.path.join(root, name)
                with open(filepath, 'r', encoding='utf-8') as f:
                    try:
                        lines = f.readlines()
                    except UnicodeDecodeError:
                        continue
                        
                active_tags = {}
                for i, line in enumerate(lines):
                    line_num = i + 1
                    start_match = re.search(r'\[START\s+([a-zA-Z0-9_]+)\]', line)
                    if start_match:
                        active_tags[start_match.group(1)] = line_num
                    end_match = re.search(r'\[END\s+([a-zA-Z0-9_]+)\]', line)
                    if end_match:
                        tag = end_match.group(1)
                        if tag in active_tags:
                            start_line = active_tags.pop(tag)
                            region_tags[filepath].append({
                                'tag': tag,
                                'start': start_line,
                                'end': line_num
                            })
    return region_tags

def parse_javap_output(snippets_dir):
    class_files = []
    for ext in ['kotlin-classes', 'compileDebugJavaWithJavac', 'javac']:
        path = os.path.join(snippets_dir, f"**/build/**/{ext}/**/*.class")
        class_files.extend(glob.glob(path, recursive=True))

    usages = defaultdict(list)
    
    for cfile in class_files:
        if 'R.class' in cfile or 'R$' in cfile or 'BuildConfig.class' in cfile: continue
        
        result = subprocess.run(['javap', '-c', '-l', '-p', '-constants', cfile], capture_output=True, text=True)
        out = result.stdout
        
        lines = out.split('\n')
        source_file = None
        method_calls = []
        current_line_table = {}
        
        for line in lines:
            line = line.strip()
            if line.startswith("Compiled from "):
                source_file_match = re.search(r'"([^"]+)"', line)
                if source_file_match:
                    source_file = source_file_match.group(1)
            
            if "Code:" in line:
                current_line_table = {}
                method_calls = []
            
            match_invoke = re.search(r'^(\d+):\s+invoke.*\s+//\s*(?:Field|Method|InterfaceMethod)\s+com/google/android/gms/maps3d/([^:]+)\.([^:("]+)', line)
            if match_invoke:
                offset = int(match_invoke.group(1))
                class_path = match_invoke.group(2)
                method_name = match_invoke.group(3)
                
                class_parts = class_path.split('/')[-1].split('$')
                # If there's an outer class and inner class, join them, else just class_name
                # e.g. "PinConfiguration$Builder" -> "PinConfiguration.Builder"
                if len(class_parts) > 1 and class_parts[-1] != "1":
                    class_name = f"{class_parts[0]}.{class_parts[-1]}"
                else:
                    class_name = class_parts[0]
                    
                if method_name == "<init>":
                    method_name = class_parts[-1]
                method_calls.append((offset, class_name, method_name))
                
            match_line = re.search(r'line\s+(\d+):\s+(\d+)', line)
            if match_line and "LineNumberTable:" in out:
                line_num = int(match_line.group(1))
                offset = int(match_line.group(2))
                current_line_table[offset] = line_num
                
            if not line and method_calls and current_line_table:
                sorted_offsets = sorted(current_line_table.keys())
                for (off, cname, mname) in method_calls:
                    matched_line = None
                    for o in reversed(sorted_offsets):
                        if off >= o:
                            matched_line = current_line_table[o]
                            break
                    if matched_line and source_file:
                        usages[f"{cname}.{mname}"].append({"file": source_file, "line": matched_line})
                method_calls = []
                current_line_table = {}

    return usages

def format_catalog_section(section_title, api_surface, usages, file_map, region_tags, language_ext):
    lines = [f"## {section_title}\n"]
    missing_apis = []
    
    for class_name, methods in sorted(api_surface.items()):
        lines.append(f"### `{class_name}`\n")
        methods = sorted(list(set(methods)))
        
        has_elements = False
        for method in methods:
            key = f"{class_name}.{method}"
            use_list = usages.get(key, [])
            
            filtered_uses = []
            for u in use_list:
                rel_path = file_map.get(u['file'], u['file'])
                if rel_path.endswith(language_ext):
                    filtered_uses.append(u)
                    
            if not filtered_uses:
                if not (method.startswith("get") or method.startswith("set")):
                    missing_apis.append(key)
                lines.append(f"- `{method}`: ❌ No coverage\n")
            else:
                formatted_uses = []
                for u in filtered_uses:
                    basename = u['file']
                    line_num = u['line']
                    rel_path = file_map.get(basename, basename)
                    
                    active_tag = "No Tag"
                    full_path_for_tag = None
                    for fp in region_tags.keys():
                        if basename in fp:
                            full_path_for_tag = fp
                            break
                    
                    if full_path_for_tag and full_path_for_tag in region_tags:
                        for region in region_tags[full_path_for_tag]:
                            if region['start'] <= line_num <= region['end']:
                                active_tag = region['tag']
                                break
                    
                    formatted_uses.append(f"[{rel_path}:{line_num}]({rel_path}#L{line_num}) (Tag: `{active_tag}`)")
                
                formatted_uses = sorted(list(set(formatted_uses)))
                lines.append(f"- `{method}`:\n")
                for fu in formatted_uses:
                    lines.append(f"  - {fu}\n")
            has_elements = True
            
        if not has_elements:
             lines.append(f"_No public methods exposing documentation snippets detected._\n")
        lines.append("\n")
        
    lines.append(f"### Missing Extracted API Coverage ({section_title})\n")
    lines.append("The following non-getter/setter APIs currently have `0` occurrences within this section:\n\n")
    if not missing_apis:
        lines.append("🎉 All non-getter/setter APIs are fully covered!\n\n")
    else:
        for missing in sorted(missing_apis):
            lines.append(f"- `{missing}`\n")
        lines.append("\n")
        
    return lines, missing_apis

def main():
    print(f"Cataloging Maps3D API snippets...\nSCRIPT_DIR: {SCRIPT_DIR}\nSNIPPETS_DIR: {SNIPPETS_DIR}")
    
    print("Compiling Snippets to ensure fresh bytecode...")
    res = subprocess.run(["./gradlew", "assembleDebug"], cwd=SNIPPETS_DIR, capture_output=True, text=True)
    if res.returncode != 0:
         print(f"Warning: Build failed. javap results may be incomplete. {res.stderr}")

    manifest_path = os.path.join(SCRIPT_DIR, "api_manifest.json")
    if not os.path.exists(manifest_path):
        print(f"Error: API manifest resource not found at {manifest_path}")
        return
        
    with open(manifest_path, "r", encoding="utf-8") as f:
        api_surface = json.load(f)
        
    print(f"Loaded {sum(len(v) for v in api_surface.values())} API endpoints from {manifest_path}.")
    
    usages = parse_javap_output(SNIPPETS_DIR)
    region_tags = extract_region_tags(SNIPPETS_DIR)
    
    file_map = {}
    for filepath in region_tags.keys():
        basename = os.path.basename(filepath)
        rel_path = os.path.relpath(filepath, SNIPPETS_DIR)
        file_map[basename] = rel_path
        
    catalog_lines = ["# Maps3D API Snippets Catalog\n\n"]
    catalog_lines.append("This document was auto-generated by `scripts/catalog_api.py`. It serves as a cross-reference matrix ensuring that every critical feature in the 3D Maps SDK is actively demonstrated inside a snippet boundary (`// [START ...]`).\n\n")
    
    # Generate Kotlin Section
    kt_lines, kt_missing = format_catalog_section("Kotlin Snippets", api_surface, usages, file_map, region_tags, ".kt")
    catalog_lines.extend(kt_lines)
    
    # Generate Java Section
    java_lines, java_missing = format_catalog_section("Java Snippets", api_surface, usages, file_map, region_tags, ".java")
    catalog_lines.extend(java_lines)
        
    catalog_path = os.path.join(SNIPPETS_DIR, "CATALOG.md")
    with open(catalog_path, 'w', encoding='utf-8') as f:
        f.writelines(catalog_lines)
        
    print(f"Catalog successfully generated at {catalog_path}.")
    print(f"Identified {len(kt_missing)} missing APIs in Kotlin, and {len(java_missing)} in Java.")

if __name__ == '__main__':
    main()
