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

def generate_snippet_index(snippets_dir, file_map):
    groups = defaultdict(lambda: {
        'description': '',
        'items': defaultdict(lambda: {
            'description': '',
            'kotlin': None,
            'java': None
        })
    })

    for root, _, filenames in os.walk(snippets_dir):
        if 'build' in root or '.gradle' in root or 'scripts' in root: continue
        for name in filenames:
            if name.endswith(('.kt', '.java')):
                filepath = os.path.join(root, name)
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()

                # Find Group
                group_match = re.search(r'@SnippetGroup\((.*?)\)', content, re.DOTALL)
                if not group_match: continue

                group_content = group_match.group(1)
                group_title_match = re.search(r'title\s*=\s*"([^"]+)"', group_content)
                group_desc_match = re.search(r'description\s*=\s*"([^"]+)"', group_content)

                if not group_title_match: continue

                group_title = group_title_match.group(1)
                group_desc = group_desc_match.group(1) if group_desc_match else ""

                if not groups[group_title]['description']:
                     groups[group_title]['description'] = group_desc

                is_kotlin = name.endswith('.kt')
                # Find Items
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if '@SnippetItem' in line:
                        # Grab context (usually next few lines have the full annotation)
                        context = "\n".join(lines[i:i+10])
                        item_match = re.search(r'@SnippetItem\((.*?)\)', context, re.DOTALL)
                        if item_match:
                            item_content = item_match.group(1)
                            item_title_match = re.search(r'title\s*=\s*"([^"]+)"', item_content)
                            item_desc_match = re.search(r'description\s*=\s*"([^"]+)"', item_content)

                            if item_title_match:
                                item_title = item_title_match.group(1)
                                item_desc = item_desc_match.group(1) if item_desc_match else ""
                                
                                # Read back or ahead for closest Region Tag
                                tag = "No Tag"
                                min_dist = 999
                                start_j = max(0, i - 15)
                                end_j = min(len(lines), i + 15)
                                for j in range(start_j, end_j):
                                     start_match = re.search(r'\[START\s+([a-zA-Z0-9_]+)\]', lines[j])
                                     if start_match:
                                          current_tag = start_match.group(1)
                                          dist = abs(j - i)
                                          if dist < min_dist:
                                              min_dist = dist
                                              tag = current_tag
                                          
                                rel_path = file_map.get(name, os.path.relpath(filepath, snippets_dir))
                                link = f"[{rel_path}:{i+1}]({rel_path}#L{i+1})"

                                current_item = groups[group_title]['items'][item_title]
                                if not current_item['description']:
                                    current_item['description'] = item_desc
                                
                                if is_kotlin:
                                    current_item['kotlin'] = {'link': link, 'tag': tag}
                                else:
                                    current_item['java'] = {'link': link, 'tag': tag}

    # Format Markdown
    lines = ["## 📑 Snippet Concepts Index\n\n"]
    lines.append("This section maps high-level concepts (groups) to specific demonstration files and lines in both languages.\n\n")

    for title, group in sorted(groups.items()):
        lines.append(f"### {title}\n")
        
        if group['description']:
            lines.append(f"> {group['description']}\n\n")
        
        # Sort items: try numerical sort if prefixed with numbers (e.g. "1. Basic")
        sorted_items = sorted(group['items'].items(), key=lambda x: (re.search(r'^(\d+)', x[0]).group(1) if re.search(r'^(\d+)', x[0]) else x[0]))
        
        for item_title, item in sorted_items:
             lines.append(f"- **{item_title}**:\n")
             if item['description']:
                  lines.append(f"  - *Description*: {item['description']}\n")
             
             if item['kotlin']:
                  lines.append(f"  - **Kotlin**\n")
                  lines.append(f"    - {item['kotlin']['link']}\n")
                  lines.append(f"    - Tag: `{item['kotlin']['tag']}`\n")
             if item['java']:
                  lines.append(f"  - **Java**\n")
                  lines.append(f"    - {item['java']['link']}\n")
                  lines.append(f"    - Tag: `{item['java']['tag']}`\n")
                  
        lines.append("\n")

    return lines

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
        
    catalog_lines = ["# 🗺️ Maps3D API Snippets Catalog\n\n"]
    catalog_lines.append("This document serves as a comprehensive developer reference and mapping matrix for the 3D Maps SDK features.\n\n")
    
    # Generate Snippet Index (Concepts to Code)
    snippet_index_lines = generate_snippet_index(SNIPPETS_DIR, file_map)
    catalog_lines.extend(snippet_index_lines)
    catalog_lines.append("\n---\n\n")
    catalog_lines.append("## 📊 Maps3D API Coverage Matrix\n\n")
    catalog_lines.append("This matrix ensures that every critical feature in the 3D Maps SDK is actively demonstrated inside a snippet boundary (`// [START ...]`).\n\n")
    
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
