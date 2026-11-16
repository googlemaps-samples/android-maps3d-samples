# Copyright 2026 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import json
import os
import sys
import urllib.error
import urllib.request

SKILL_DIR = ".agents/skills/android-maps3d-sdk"
DEFAULT_SKILL_FILE = os.path.join(SKILL_DIR, "SKILL.md")
INDEX_FILE = os.path.join(SKILL_DIR, "references/index.json")
MAX_SKILL_LINES = 250
REQUIRED_FRONTMATTER_KEYS = [
    "name: android-maps3d-sdk",
    "maven_artifact:",
    "min_sdk:",
    "min_kotlin:",
    "supported_compile_sdk:",
]


def validate_skill_structure(skill_dir=SKILL_DIR):
    """Validates SKILL.md line budget, YAML metadata, policy guardrails, and reference paths."""
    errors = []
    skill_md = os.path.join(skill_dir, "SKILL.md")
    index_path = os.path.join(skill_dir, "references", "index.json")
    lines = []

    if not os.path.exists(skill_md):
        errors.append(f"Missing router file: {skill_md}")
    else:
        with open(skill_md, "r", encoding="utf-8") as f:
            lines = f.readlines()
        content = "".join(lines)
        if len(lines) >= MAX_SKILL_LINES:
            errors.append(
                f"{skill_md} has {len(lines)} lines (must be < {MAX_SKILL_LINES} lines)."
            )
        if not lines or lines[0].strip() != "---":
            errors.append(f"{skill_md} is missing YAML frontmatter at line 1.")
        for key in REQUIRED_FRONTMATTER_KEYS:
            if key not in content:
                errors.append(f"{skill_md} is missing required YAML frontmatter entry '{key}'.")
        if "com.google.android.geo.maps3d.API_KEY" not in content:
            errors.append(f"{skill_md} must specify com.google.android.geo.maps3d.API_KEY.")
        forbidden_patterns = [
            (".last_checked", "hidden timestamp state files trigger time-bomb/side-effect heuristics"),
            ("npx -y", "auto-executing unpinned npx commands violates skill security policy"),
            ("skills@latest", "unpinned @latest package execution violates supply-chain policy"),
            ("raw.githubusercontent.com", "unsolicited remote markdown/manifest fetches violate hermetic skill policy"),
        ]
        for pattern, reason in forbidden_patterns:
            if pattern in content:
                errors.append(f"{skill_md} contains forbidden pattern '{pattern}' ({reason}).")

    if not os.path.exists(index_path):
        errors.append(f"Missing reference index file: {index_path}")
    else:
        try:
            with open(index_path, "r", encoding="utf-8") as f:
                index_data = json.load(f)
            refs = index_data.get("references", [])
            if len(refs) != 8:
                errors.append(f"Expected 8 modular references in {index_path}, found {len(refs)}.")
            indexed_paths = set()
            for ref in refs:
                ref_rel = ref.get("path", "")
                ref_full = os.path.normpath(os.path.join(skill_dir, ref_rel))
                indexed_paths.add(ref_full)
                if not os.path.exists(ref_full):
                    errors.append(f"Referenced file does not exist: {ref_full}")
            refs_dir = os.path.join(skill_dir, "references")
            for entry in os.listdir(refs_dir):
                if entry == "index.json":
                    continue
                full_entry = os.path.normpath(os.path.join(refs_dir, entry))
                if full_entry not in indexed_paths:
                    errors.append(f"Unindexed file found in references/: {full_entry}")
        except Exception as e:
            errors.append(f"Invalid JSON in {index_path}: {e}")

    assets_dir = os.path.join(skill_dir, "assets")
    if os.path.exists(assets_dir):
        errors.append(f"Legacy assets directory should not exist: {assets_dir}")

    if errors:
        for err in errors:
            print(f"VALIDATION ERROR: {err}", file=sys.stderr)
        return False

    print(
        f"Skill validation passed: {skill_md} ({len(lines)} lines), YAML metadata, security guardrails, and all 8 modular references verified."
    )
    return True


def get_gemini_response(api_key, prompt):
    model = (os.getenv("GEMINI_MODEL") or "").strip() or "gemini-2.5-flash"
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}"
    headers = {"Content-Type": "application/json"}
    data = {"contents": [{"parts": [{"text": prompt}]}]}

    req = urllib.request.Request(
        url, data=json.dumps(data).encode("utf-8"), headers=headers
    )
    try:
        with urllib.request.urlopen(req) as response:
            res_data = json.loads(response.read().decode("utf-8"))
            return res_data["candidates"][0]["content"]["parts"][0]["text"]
    except urllib.error.HTTPError as e:
        print(f"Gemini API Error ({e.code}): {e.reason}", file=sys.stderr)
        try:
            error_body = e.read().decode("utf-8")
            print(f"Error details: {error_body}", file=sys.stderr)
        except Exception:
            pass
        return None
    except Exception as e:
        print(f"Error calling Gemini API: {e}", file=sys.stderr)
        return None


def main():
    parser = argparse.ArgumentParser(
        description="Validate or update the android-maps3d-sdk skill."
    )
    parser.add_argument(
        "--validate",
        action="store_true",
        help="Validate SKILL.md line count, JSON manifests, and modular reference files.",
    )
    args = parser.parse_args()

    if args.validate:
        sys.exit(0 if validate_skill_structure() else 1)

    api_key = os.getenv("GEMINI_API_KEY")
    diff_file = os.getenv("DIFF_FILE", "release_diff.patch")
    skill_file = os.getenv("SKILL_FILE", DEFAULT_SKILL_FILE)

    if not api_key:
        print("GEMINI_API_KEY not found", file=sys.stderr)
        sys.exit(1)

    if not os.path.exists(diff_file):
        print(f"Diff file {diff_file} not found.", file=sys.stderr)
        sys.exit(1)

    if not os.path.exists(skill_file):
        print(f"Skill file {skill_file} not found.", file=sys.stderr)
        sys.exit(1)

    with open(diff_file, "r", encoding="utf-8") as f:
        diff_content = f.read()

    with open(skill_file, "r", encoding="utf-8") as f:
        skill_content = f.read()

    prompt = f"""
You are an expert technical writer and Android developer.
Your task is to update the Gemini CLI skill router (`SKILL.md`) for the Google Maps 3D Android SDK (`com.google.android.gms:play-services-maps3d`) based on the latest release diff.

Here is the current `SKILL.md` file:
```markdown
{skill_content}
```

Here is the git diff representing the changes introduced in this release:
```diff
{diff_content}
```

CRITICAL REQUIREMENTS:
- Preserve the modular Step 1-4 hermetic router structure of `SKILL.md` and keep its total length strictly under 250 lines.
- Do NOT add automatic remote update fetches, `.last_checked` writes, or `npx -y` commands.
- Keep the YAML frontmatter intact at the top of the file (between `---`).
- Keep `com.google.android.geo.maps3d.API_KEY` and the 15-row 2D-to-3D translation table pointing to the 8 files in `references/`.
- Return ONLY the raw updated markdown content without wrapping it in ```markdown...``` code blocks.
"""

    print("Requesting update from Gemini...", file=sys.stderr)
    response_text = get_gemini_response(api_key, prompt)
    if response_text:
        if response_text.startswith("```markdown"):
            response_text = response_text.replace("```markdown\n", "", 1)
            if response_text.endswith("```"):
                response_text = response_text[:-3]
        elif response_text.startswith("```"):
            response_text = response_text.replace("```\n", "", 1)
            if response_text.endswith("```"):
                response_text = response_text[:-3]

        response_text = response_text.strip() + "\n"

        with open(skill_file, "w", encoding="utf-8") as f:
            f.write(response_text)
        print(f"Successfully updated {skill_file}", file=sys.stderr)
        if not validate_skill_structure():
            sys.exit(1)
    else:
        sys.exit(1)


if __name__ == "__main__":
    main()
