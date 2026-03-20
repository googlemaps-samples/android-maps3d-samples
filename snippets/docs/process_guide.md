# 🗺️ Adoption Guide: Replicating Snippets for New SDKs

This guide provides a step-by-step **Implementation Playbook** for porting this Standalone Snippet & Catalog architecture to a **New SDK module** (e.g., standard Maps Android, Places SDK).

---

## ⏱️ The Workflow Timeline

Building a scalable snippet index requires executing the following 4 distinct phases in order:

### **Phase 1: API Indexing & Audit Prep**
The foundation forces AI and analysts to know the absolute checklist index.
1.  **Decompile/Index Symbols**: Run `javap` or inspect online Markdown docs to list all Class methods.
2.  **Generate `api_manifest.json`**: Feed findings to an AI to create a continuous dictionary checklist.
3.  👉 *Refer to:* [**`tooling_blueprint.md`**](tooling_blueprint.md)

---

### **Phase 2: Architectural Setup (Annotations & Wrappers)**
Establish the runner harness structures to decouple snippet extracts from launcher maintenance.
1.  **Create Annotations**: Define `@SnippetGroup` and `@SnippetItem` for dynamic loaders.
2.  **Optional Decorators**: If managing persistent states (like Visual Maps), create a `TrackedMap3D` decorator style structure accumulate added items.
3.  **Create Runner detail**: Design a layout launcher activity using reflection to dynamic load `@SnippetGroup` loads list adapters list.
4.  👉 *Refer to:* [**`framework_overview.md`**](framework_overview.md)

---

### **Phase 3: Setup Automated Metrics (`catalog_api.py`)**
Before authoring boilerplate coordinates directly, configure the accounting audit tracker script.
1.  **Build Parser**: Write a Python script scanning `.java` and `.kt` source nodes finding sign matches.
2.  **Incremental checklist setups**: Generate **`CATALOG.md`** representing absolute counts vs deficit gaps below section checklists.
3.  👉 *Refer to:* [**`tooling_blueprint.md`**](tooling_blueprint.md)

---

### **Phase 4: Iterative snippet generation & authors**
Use consecutive prompt triggers sequential iterations AUTHORING coordinate loops matching checklist index deficits iteratively.
1.  Verify exact Java/kotlin parity matches.
2.  Guarantee UI-threaded state callback alerts inside lambda macros setup structures.
3.  👉 *Refer to:* [**`prompt_pipeline.md`**](prompt_pipeline.md)

---

## 💡 Top 3 Success Checklist
-   **Always run bytecode before parser flushes**: Force compiled triggers flawless before counting occurrences accurately.
-   **Maintain clean excerpt segments**: Guarantee extract blocks use absolute wrapper indices so juniors copy-paste extracts directly without crashing frameworks.
-   **Parity matching sets**: Keep variable nodes matching exactly back bounding coordinate triggers.
