# **Architectural Frameworks for Advanced Agentic Skill Development: A Technical Synthesis**

The rapid maturation of Large Language Model (LLM) ecosystems has necessitated a transition from static Software Development Kits (SDKs) to dynamic, agent-centric capabilities known as Agentic Skills. This architectural shift represents a fundamental move from code libraries designed for human invocation to portable, reusable packages of instructions, scripts, and resources specifically optimized for autonomous systems1. For organizations possessing an extensive body of sample code but struggling with lackluster agent performance, the transformation involves more than simple prompt engineering; it requires the systematic encapsulation of domain expertise into the AgentSkills.io open standard, utilizing progressive disclosure to manage the inherent constraints of the LLM context window3.

## **The Taxonomy of Agentic Skills versus Traditional SDKs**

The primary limitation of traditional SDK-based approaches for agents is threefold: token inefficiency, exponential growth in decision complexity, and a lack of modular reusability3. When a developer provides an agent with thousands of lines of raw API samples, the agent often suffers from "context dilution," where the core task instructions compete for attention with low-level implementation details4. Agentic skills resolve this by functioning as "onboarding guides" or "static cheat-sheets" that transform a general-purpose model into a specialized agent equipped with procedural knowledge and explicit applicability conditions7.

The formal definition of a skill extends beyond the simple prompt. It is a four-tuple formalization consisting of a textual descriptor (![][image1]), an intra-skill policy (![][image2]), a resource set (![][image3]), and a set of applicability conditions (![][image4])7. This formalization allows the skill to persist across sessions and carry executable policies that are significantly more robust than one-time, session-scoped plans10.

| Concept | Unit of Reuse | Internal Logic | Context Impact |
| :---- | :---- | :---- | :---- |
| **SDK Tool** | Atomic Function | Fixed Code | High (if all loaded) |
| **Prompt Template** | Static Text | No decision logic | High (monolithic) |
| **Agent Skill** | Procedural Module | ReAct/Heuristics | Low (progressive) |
| **Workflow** | Deterministic Graph | Rigid Sequence | Moderate |

1

## **The Three-Tier Progressive Disclosure Architecture**

The defining characteristic of an "advanced" skill is its adherence to the principle of progressive disclosure. This architectural pattern originates from user interface design but, when applied to agents, ensures that the system loads only the necessary information at each phase of the task lifecycle5. Advanced SDKs, such as the Microsoft Agent Framework and Google ADK, implement this through three distinct levels of information loading1.

### **Level 1: Metadata and Discovery (The L1 Layer)**

At the start of any agentic session, only the metadata—specifically the name and description—is injected into the system prompt. This "menu" allows the agent to identify available capabilities without incurring a massive token penalty5. A skill with 100 installed capabilities might only consume 1,000 baseline tokens if structured correctly, representing a 90% reduction in context overhead compared to monolithic prompts12.

### **Level 2: Instructions and Workflow (The L2 Layer)**

The full SKILL.md body is loaded only when the agent explicitly activates a skill based on user intent. This file acts as the "brain," containing the high-level procedural instructions that guide the agent through multi-step tasks like deployment, code review, or architecture planning5. The specification recommends keeping this layer under 500 lines and 5,000 tokens to ensure the agent remains focused on the primary workflow4.

### **Level 3: Resources and Deterministic Scripts (The L3 Layer)**

Detailed reference materials, such as API specifications, schemas, and templates, are stored in a references/ or assets/ directory. These are loaded on demand only when the L2 instructions require them1. Furthermore, executable scripts (Python, Bash, or PowerShell) reside in a scripts/ directory to handle fragile, repetitive, or algorithmic tasks that the LLM would otherwise struggle to perform reliably15.

## **Engineering Semantic Assets from Existing Code Samples**

A common pitfall in skill development is providing the agent with raw code samples and expecting it to infer the underlying patterns. To create a "productive" skill, these samples must be transformed into structured assets and parameterized templates20.

### **Few-Shot Template Parameterization**

Few-shot prompting is a well-established best practice that significantly improves output consistency6. To utilize an existing body of sample code effectively, developers should extract 3 to 5 high-quality examples that cover different slices of the API usage space24. These should not be raw files but rather "semantic templates" where variable sections are marked for the agent to populate25.

Mathematically, the probability of a correct agent response ![][image5] is positively correlated with the relevance and quality of the provided few-shot examples ![][image6]:

![][image7]  
where ![][image8] is the user query and ![][image9] is the weight of the example based on its structural similarity to the task7.

### **Asset vs. Reference Directory Organization**

The distinction between assets/ and references/ is critical for accurate execution. Files that the agent is meant to read and build upon (e.g., boilerplate code, schemas) should be placed in references/17. Conversely, static files used "as-is" in the final output (e.g., images, company logos, fonts) belong in the assets/ directory17.

| Directory | Purpose | Loaded into Context? | Interaction Type |
| :---- | :---- | :---- | :---- |
| references/ | API specs, Schemas | Yes, on-demand | Read and Interpret |
| assets/ | Templates, Logos | No | Direct Output Injection |
| scripts/ | Automation, CLI tools | No (Output only) | Execution |
| examples/ | Test data, Use cases | Yes, if requested | Pattern Matching |

15

## **Intent Mapping and Description Optimization**

The description field in the YAML frontmatter of the SKILL.md file carries the entire burden of triggering the skill during implicit invocation29. If the description fails to accurately map user intent to the skill's capabilities, the agent will never reach for the instructions, regardless of their quality29.

### **The Imperative Phrase Principle**

Advanced skill descriptions should use imperative phrasing—"Use this skill when..."—rather than descriptive phrasing—"This skill does..."29. This framing treats the description as an instruction for the agent to act29. Developers should also include "negative triggers" to prevent false positives, clearly stating scenarios where the skill should *not* be used15.

### **The Trigger Evaluation Loop**

To improve accuracy, developers should implement an optimization loop for their descriptions. This involves splitting a set of user queries into a "train set" (60%) and a "validation set" (40%)29. The train set queries guide the refinement of the description, while the validation set determines if the changes generalize across diverse phrasing and casual language29.

| Strategy | Description Attribute | Impact on Accuracy |
| :---- | :---- | :---- |
| **Intent-Based** | Focuses on user's end goal | High |
| **Domain-Specific** | Mentions specific file types or APIs | Moderate |
| **Exclusionary** | Lists "do not use" cases | High (reduces false triggers) |
| **Technical** | Describes internal mechanics | Low |

15

## **Procedural Logic and Deterministic Execution Patterns**

A "lackluster" skill often provides vague guidance that leads to hallucinations. A productive, advanced skill employs strict procedural patterns such as ReAct (Reasoning \+ Acting) and Plan-Validate-Execute4.

### **The ReAct Pattern in Skills**

The ReAct pattern addresses the issue of models hallucinating tool actions. By forcing the agent to interleave thoughts (analyzing the state), actions (calling a tool or script), and observations (evaluating the result), the skill ensures the agent remains grounded in real-world data8.

### **The Plan-Validate-Execute Cycle**

For complex or destructive operations—such as database migrations or large-scale code refactors—the SKILL.md should enforce a "Plan-Validate-Execute" pattern4. The agent first creates an implementation plan (often as a JSON or Markdown artifact), validates it against a "source of truth" using a bundled validation script, and only executes the changes after the plan is verified4. This pattern is machine-verifiable and allows for reversible planning, where the agent can iterate on the plan without modifying the original files16.

## **Advanced State Management and Persistence**

Maintaining the state of an agentic workflow across multiple turns is a primary differentiator for advanced systems. Standard LLM calls are stateless, but advanced agent frameworks introduce "shared memory" through state schemas and checkpointers34.

### **Stateful Orchestration with LangGraph**

LangGraph represents a shift toward stateful, event-driven orchestration by modeling workflows as a StateGraph34. In this architecture, the state is a first-class citizen, typically defined as a Pydantic model that accumulates conversation history and task-specific memory34. Reducer functions are used to manage how nodes update the state, enabling "append-only" logs that prevent overwriting critical information34.

### **Checkpointing and "Time Travel"**

Production-grade skills implement state persistence using backends like SQLite or Postgres37. This allows for:

* **Thread Recovery**: Resuming a session after an interruption33.  
* **Time Travel**: Reverting to a previous agent state to debug or retry from a different branch31.  
* **Approval Gates**: Pausing execution until a human provides validation, particularly for high-stakes financial or legal tasks32.

## **Evaluation and Lifecycle Management of Agentic Skills**

A skill's productivity is measured by its success rate, latency, and reliability. Tools like SkillGrade provide a framework for automated evaluation, allowing developers to catch regressions before deployment15.

### **The SkillGrade Rubric**

SkillGrade automates the testing of agent discovery and execution. It generates tasks based on the skill's eval.yaml and uses "grader" models to assess performance based on a rubric15. Effective grading should focus on outcomes (e.g., "Is the security vulnerability patched?") rather than procedural steps40.

| Metric | Measurement Method | Target Baseline |
| :---- | :---- | :---- |
| **Discovery Rate** | Trigger success on validation set | \> 0.85 |
| **Success Rate** | Task completion (Outcome-based) | \> 0.80 |
| **Token Efficiency** | Token usage per successful task | Lower is better |
| **Latency** | End-to-end processing time | Dependent on model |

30

### **Logic Validation and "Ruthless" QA**

Before releasing a skill, developers should subject it to a "ruthless" QA audit. This involves feeding the full SKILL.md to an LLM and asking it to hunt for vulnerabilities, unsupported configurations, or implicit assumptions about the environment15. This "Logic Validation" phase uncovers "Execution Blockers" where the agent might be forced to guess due to ambiguous instructions15.

## **Security and Governance in Skill Deployment**

As agent skills become more autonomous, security teams must implement strict dependency governance and sandboxing1. Advanced skills should follow the principle of "least privilege," requesting only the minimum toolset required for their specific domain43.

### **Sandboxing and Input Filtering**

Executable scripts bundled with skills must run in isolated environments (e.g., containers, seccomp, or firejail) to prevent malicious filesystem or network access1. Furthermore, "Output Filtering" patterns should be used to scrub Personally Identifiable Information (PII) or harmful content before it is shared with the user39.

### **The Role of Meta-Skills and Human Oversight**

While the generation of skills can be automated using "Meta-Skills" (skills designed to create other skills), human oversight remains paramount1. Organizations are encouraged to treat SKILL.md files like code dependencies, requiring peer reviews and version control for every change12.

## **Synthesis: The Roadmap to a Productive Agent Skill**

The transition from a "lackluster" attempt to a "productive" skill is a multi-stage process of refinement. It begins with the extraction of real expertise from hands-on tasks and existing project artifacts4. By organizing this expertise into the three-level progressive disclosure architecture, developers ensure that deep domain knowledge is available on-demand without degrading the agent's performance5.

The integration of semantic assets—such as parameterized templates derived from the user's extensive code samples—provides the agent with the "analogical reasoning" capabilities needed to excel at complex SDK implementations21. When combined with deterministic scripts for algorithmic verification and robust state management for multi-turn persistence, the resulting agent skill becomes a reliable, enterprise-ready capability that moves beyond basic assistance toward true autonomous agency8. The ultimate utility of these systems is found not just in the intelligence of the underlying model, but in the robustness of the structural and procedural guardrails provided by the well-designed skill package18.

#### **Works cited**

1. Agent Skills | Microsoft Learn, [https://learn.microsoft.com/en-us/agent-framework/agents/skills](https://learn.microsoft.com/en-us/agent-framework/agents/skills)  
2. Give Your Agents Domain Expertise with Agent Skills in Microsoft Agent Framework, [https://devblogs.microsoft.com/agent-framework/give-your-agents-domain-expertise-with-agent-skills-in-microsoft-agent-framework/](https://devblogs.microsoft.com/agent-framework/give-your-agents-domain-expertise-with-agent-skills-in-microsoft-agent-framework/)  
3. aws-samples/sample-strands-agents-agentskills: Agent Skills implementation for Strands Agents SDK \- GitHub, [https://github.com/aws-samples/sample-strands-agents-agentskills](https://github.com/aws-samples/sample-strands-agents-agentskills)  
4. [https://agentskills.io/skill-creation/best-practices](https://agentskills.io/skill-creation/best-practices)  
5. The SKILL.md Pattern: How to Write AI Agent Skills That Actually Work | by Bibek Poudel, [https://bibek-poudel.medium.com/the-skill-md-pattern-how-to-write-ai-agent-skills-that-actually-work-72a3169dd7ee](https://bibek-poudel.medium.com/the-skill-md-pattern-how-to-write-ai-agent-skills-that-actually-work-72a3169dd7ee)  
6. Effective context engineering for AI agents \- Anthropic, [https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents)  
7. Agent Skill Framework: Perspectives on the Potential of Small Language Models in Industrial Environments \- arXiv, [https://arxiv.org/html/2602.16653v1](https://arxiv.org/html/2602.16653v1)  
8. Agent Skills: The Missing Layer That Makes AI Agents Enterprise Ready \- DEV Community, [https://dev.to/sreeni5018/agent-skills-the-missing-layer-that-makes-ai-agents-enterprise-ready-3gc](https://dev.to/sreeni5018/agent-skills-the-missing-layer-that-makes-ai-agents-enterprise-ready-3gc)  
9. deepagentsjs/examples/skills/skill-creator/SKILL.md at main \- GitHub, [https://github.com/langchain-ai/deepagentsjs/blob/main/examples/skills/skill-creator/SKILL.md](https://github.com/langchain-ai/deepagentsjs/blob/main/examples/skills/skill-creator/SKILL.md)  
10. SoK: Agentic Skills — Beyond Tool Use in LLM Agents \- arXiv, [https://arxiv.org/html/2602.20867v1](https://arxiv.org/html/2602.20867v1)  
11. Choose a design pattern for your agentic AI system | Cloud Architecture Center, [https://docs.cloud.google.com/architecture/choose-design-pattern-agentic-ai-system](https://docs.cloud.google.com/architecture/choose-design-pattern-agentic-ai-system)  
12. Developer's Guide to Building ADK Agents with Skills, [https://developers.googleblog.com/en/developers-guide-to-building-adk-agents-with-skills/](https://developers.googleblog.com/en/developers-guide-to-building-adk-agents-with-skills/)  
13. Progressive Disclosure: the technique that helps control context (and tokens) in AI agents, [https://medium.com/@martia\_es/progressive-disclosure-the-technique-that-helps-control-context-and-tokens-in-ai-agents-8d6108b09289](https://medium.com/@martia_es/progressive-disclosure-the-technique-that-helps-control-context-and-tokens-in-ai-agents-8d6108b09289)  
14. Introducing Agent Plugins for AWS | AWS Developer Tools Blog, [https://aws.amazon.com/blogs/developer/introducing-agent-plugins-for-aws/](https://aws.amazon.com/blogs/developer/introducing-agent-plugins-for-aws/)  
15. GitHub \- mgechev/skills-best-practices: Write professional-grade skills for agents, validate them using LLMs, and maintain a lean context window., [https://github.com/mgechev/skills-best-practices](https://github.com/mgechev/skills-best-practices)  
16. Skill authoring best practices \- Claude API Docs, [https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/best-practices)  
17. skill-creator \- anthropics/financial-services-plugins \- GitHub, [https://github.com/anthropics/financial-services-plugins/blob/main/financial-analysis/skills/skill-creator/SKILL.md](https://github.com/anthropics/financial-services-plugins/blob/main/financial-analysis/skills/skill-creator/SKILL.md)  
18. GitHub \- gohypergiant/agent-skills: A collection of skills for AI coding agents. Skills are packaged instructions and scripts that extend agent capabilities., [https://github.com/gohypergiant/agent-skills](https://github.com/gohypergiant/agent-skills)  
19. agent-skills.instructions.md \- github/awesome-copilot, [https://github.com/github/awesome-copilot/blob/main/instructions/agent-skills.instructions.md](https://github.com/github/awesome-copilot/blob/main/instructions/agent-skills.instructions.md)  
20. Include few-shot examples | Generative AI on Vertex AI \- Google Cloud Documentation, [https://docs.cloud.google.com/vertex-ai/generative-ai/docs/learn/prompts/few-shot-examples](https://docs.cloud.google.com/vertex-ai/generative-ai/docs/learn/prompts/few-shot-examples)  
21. Achieving 5x Agentic Coding Performance with Few-Shot Prompting, [https://towardsdatascience.com/5x-agentic-coding-performance-with-few-shot-prompting/](https://towardsdatascience.com/5x-agentic-coding-performance-with-few-shot-prompting/)  
22. Daily Papers \- Hugging Face, [https://huggingface.co/papers?q=experience%20memory](https://huggingface.co/papers?q=experience+memory)  
23. Few-Shot Learning for LLMs: Examples and Implementation Guide \- Tetrate, [https://tetrate.io/learn/ai/few-shot-learning-llms](https://tetrate.io/learn/ai/few-shot-learning-llms)  
24. Few-Shot Prompting for Agentic Systems: Teaching by Example \- Comet, [https://www.comet.com/site/blog/few-shot-prompting/](https://www.comet.com/site/blog/few-shot-prompting/)  
25. Few-Shot Prompting \- Portkey Docs, [https://docs.portkey.ai/docs/guides/use-cases/few-shot-prompting](https://docs.portkey.ai/docs/guides/use-cases/few-shot-prompting)  
26. LangChain Prompt Templates: Complete Guide with Examples \- Latenode Blog, [https://latenode.com/blog/ai-frameworks-technical-infrastructure/langchain-setup-tools-agents-memory/langchain-prompt-templates-complete-guide-with-examples](https://latenode.com/blog/ai-frameworks-technical-infrastructure/langchain-setup-tools-agents-memory/langchain-prompt-templates-complete-guide-with-examples)  
27. FloorplanQA: A Benchmark for Spatial Reasoning in LLMs using Structured Representations, [https://arxiv.org/html/2507.07644v2](https://arxiv.org/html/2507.07644v2)  
28. Provide examples (few-shot prompting) \- Amazon Nova \- AWS Documentation, [https://docs.aws.amazon.com/nova/latest/userguide/prompting-examples.html](https://docs.aws.amazon.com/nova/latest/userguide/prompting-examples.html)  
29. Optimizing skill descriptions \- Agent Skills, [https://agentskills.io/skill-creation/optimizing-descriptions](https://agentskills.io/skill-creation/optimizing-descriptions)  
30. Building AI with Chatbot Intent Recognition: Guide \- IrisAgent, [https://irisagent.com/blog/building-chatbots-with-intent-detection-guide/](https://irisagent.com/blog/building-chatbots-with-intent-detection-guide/)  
31. Design Patterns for Agentic AI and Multi-Agent Systems \- AppsTek Corp, [https://appstekcorp.com/blog/design-patterns-for-agentic-ai-and-multi-agent-systems/](https://appstekcorp.com/blog/design-patterns-for-agentic-ai-and-multi-agent-systems/)  
32. 7 Must-Know Agentic AI Design Patterns \- MachineLearningMastery.com, [https://machinelearningmastery.com/7-must-know-agentic-ai-design-patterns/](https://machinelearningmastery.com/7-must-know-agentic-ai-design-patterns/)  
33. Single-responsibility agents and multi-agent workflows in AI-powered development tools, [https://www.epam.com/insights/ai/blogs/single-responsibility-agents-and-multi-agent-workflows](https://www.epam.com/insights/ai/blogs/single-responsibility-agents-and-multi-agent-workflows)  
34. The Architecture of Agents: Planning, Action, and State Management in Large Language Models | by Tejaswi kashyap \- GoPenAI, [https://blog.gopenai.com/the-architecture-of-agents-planning-action-and-state-management-in-large-language-models-e00b340fcf09](https://blog.gopenai.com/the-architecture-of-agents-planning-action-and-state-management-in-large-language-models-e00b340fcf09)  
35. LangGraph State Management Patterns | Claude Code Skill \- MCP Market, [https://mcpmarket.com/tools/skills/langgraph-state-management](https://mcpmarket.com/tools/skills/langgraph-state-management)  
36. The Best Open Source Frameworks For Building AI Agents in 2026 \- Firecrawl, [https://www.firecrawl.dev/blog/best-open-source-agent-frameworks](https://www.firecrawl.dev/blog/best-open-source-agent-frameworks)  
37. LangGraph Checkpoint Patterns \- Claude Code Skill \- MCP Market, [https://mcpmarket.com/tools/skills/langgraph-state-persistence](https://mcpmarket.com/tools/skills/langgraph-state-persistence)  
38. Claude Code Guide: Features and Best Practices, [https://www.elliotjreed.com/ai/claude-code-guide-and-tips](https://www.elliotjreed.com/ai/claude-code-guide-and-tips)  
39. claude-skills/engineering/agent-designer/SKILL.md at main \- GitHub, [https://github.com/alirezarezvani/claude-skills/blob/main/engineering/agent-designer/SKILL.md](https://github.com/alirezarezvani/claude-skills/blob/main/engineering/agent-designer/SKILL.md)  
40. GitHub \- mgechev/skillgrade: "Unit tests" for your agent skills, [https://github.com/mgechev/skillgrade](https://github.com/mgechev/skillgrade)  
41. Skill Design for LLM Agents by Minko Gechev \- GitNation, [https://gitnation.com/contents/skill-design-for-llm-agents](https://gitnation.com/contents/skill-design-for-llm-agents)  
42. What is agentic coding? How it works and use cases | Google Cloud, [https://cloud.google.com/discover/what-is-agentic-coding](https://cloud.google.com/discover/what-is-agentic-coding)  
43. Top 10 Claude Code Skills Every Builder Should Know in 2026 \- Composio, [https://composio.dev/content/top-claude-skills](https://composio.dev/content/top-claude-skills)  
44. CodeBuddy Code Best Practices, [https://www.codebuddy.ai/docs/cli/best-practices](https://www.codebuddy.ai/docs/cli/best-practices)  
45. 3 Principles for Designing Agent Skills \- Block Engineering Blog, [https://engineering.block.xyz/blog/3-principles-for-designing-agent-skills](https://engineering.block.xyz/blog/3-principles-for-designing-agent-skills)  
46. The Few Shot Prompting Guide \- PromptHub, [https://www.prompthub.us/blog/the-few-shot-prompting-guide](https://www.prompthub.us/blog/the-few-shot-prompting-guide)

[image1]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAYCAYAAADDLGwtAAAA3UlEQVR4XuWQz+rBURDFZyGy8+eXYiErSwsbdiTFQvECtvIQdmKjpCjqV17C3oJkxyvIE9hYUOKMubem8QLKqU/fe885zdy+RF+tEIhZU6sELuAJ9ib7EE/i4sgGVlWSYsMGVgPwABEbsPKg6M47cFDZW2mwBSvQc987GOtSEpzBQnlDkvc1lUdLcANx5fVJ3hf1RsIZvFZrA47aqJGs4AleYZINE+VRmaRYV17FeS1QAF02g+AK2q70R7KSixkwAzmXUQecwBz8k0zkv7AGU1/y4ndl1T0AUur+g3oBR8kohfRh6YEAAAAASUVORK5CYII=>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAwAAAAXCAYAAAA/ZK6/AAAAnElEQVR4XmNgGAWDEcgBsTIQqwCxKhSD2CAsgKSOQQeIzwLxfzx4EUyxBFRxKhDbA/E1KB0BxLuB2BqIrYCYH6YhCIi1oWxFID4CZQcAcT+UjRUwAfEdBojpILCegYAGfyD+AsRsUP4BID4Il8UC9gPxJiT+AiD+CMSMSGJwAPL4HyAORhIrAuKvQMyLJIYChND47EAsjCY2CogCACLOG0H+y5foAAAAAElFTkSuQmCC>

[image3]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAYCAYAAADDLGwtAAAAv0lEQVR4Xu3QMQtBYRTG8VOUlMHCQFa+gOxsBptB2ZltssmgGOzMfAGDfABlM8ssVsXM/3buvR1vdzCweepXPOcZ3q7IP79MEg2U3YNNFwcMsMUc8bcF6eGMjP8/jydq4YJkcUfTdGnR4dR0MsEDCdNVRYcj08lR9E02Q9FhPSiCt/SDgqRwxd500hYdzkw3xgVF08kCNyyxwg5rlOzIywkb/3cBOXML45Xu+yLTEh1W3IObjuinibmHqHw0+m5eF+ghYle6+xAAAAAASUVORK5CYII=>

[image4]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAsAAAAYCAYAAAAs7gcTAAAA40lEQVR4XmNgGBJAC4i9gVgIXQIGeIG4G4jvAvF/KH4KxMzIikDABIgvA3EeECsDsQAQv4ZiFiR1DJZA/BlKwwA3EH9igNgEB/xAfB+I25EFgSAbiL8CsSKyYC8Qf2CAmIQM0oHYHFmAlQHipkXIgrhAIAPEx+HoEthAFwNEsRm6BDZAkuJ1DBDF+ugS2MB2BojiQnQJKABFDB+M08cAUbwDLo0AnkD8DIjlYAIuQPwPiH8DcQoQM0FxEBBfA2JNmEIYaATiXwwQG74wQBLOBiAWQVaEDASBOAaI3YGYE01uZAAA0qgoog3dXjUAAAAASUVORK5CYII=>

[image5]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAC0AAAAYCAYAAABurXSEAAACzElEQVR4Xu2WWahNYRiGP0PGjBnKdGSWErkh4ohLElIKCYkLColEhhsiY3FDHDciN4hQSiEp4UJKmc4xDxfmTJne17f++s571j6ntn0ulKeedvv9/rX22v//rX8ts//8u3TRoAiawI4a1hfj4DENi6A5vAIHaiGPkfAufAZfZJ8P4D1YCS/BWeYzofSCz2E/LYBm8DZ8an5ejuN5q+B9eBxOT4MzJpqP6SR5QY7AX3BEyPjDi7J8e8gT1+A2DYWF5sevyr43gO3giizfkuWJM/CQZAXhP3wLG2kBPITfYY+QjYUvYZuQ5XHA/OKGacF8BT7A1iHrD7/AQSHLpZv5iU9qATSEH+FP2DPk7OPd4Xsh2GZvzM8TYbt9gz+s+mSQ83CXZDWYYX7Ry7VgPqOsHQwZL+ArXBKyPLqaH3tCC2CmeW2PFsBeeEtDZZ/lL2EZvAofw84hTyszIWR5pMlYGjLeJ+vM22IHbBFqidTv7bUQ4e7B5V8DV8LV5jcDe7zC/MaJjDY/6QDJlTQZl+Gp7PMdvAGHhnHKFPPjCvZ1mrXrcFImZ7DcfFbymG3ei3nbYIT9/Nqq9/MC82N1u4sMNr+mMVpIpN5aL3ltLIPvNRTSZGg/8yHC/Jzkke7mY8ZrIbHffEC55LUxx/yYDloI8IHEMfyDkSFZflHySGq/vlpIVMLPsKkWaoFPLp50uBYCFeZj9OZenOV8IhZirtXSftzIeYILWqiDPubHsbXyaGy+47CFdH/eYH7s4ez7fKvZ3xvhI8n+PKrvmN8knOVP5j8yNQ6qgyqreR+wX2+aP+l4TvoErg1jemcZV5iP+LOwZaiTo/C0ZCWBT0NuY8XQ1vwm4wxzVSJcGW7BkyUvCWXm7wijtPCXsF34rpP3DlQStprv79q3xcIVeAWnaaGUcGnZk/O0UCQ74SYN64NWVpof4ivuZivdqv2nKH4DKFqdUwU3xRoAAAAASUVORK5CYII=>

[image6]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAZCAYAAADuWXTMAAABDklEQVR4XmNgGAUuQHyLSBwG1QMHjEDMBcQ7gfg/ELsCMTsQs0HFJYA4B4j/ALE/VA8GeAnEn4GYFV0CCs4BsQG6IAhoMUBs3Y4mbo7E3g3Egkh8OMhigGguQxITAeJdSPwqJDYKWMUA0RwIxEpAbAnEB4G4HlkRLvAKiL8D8WEgPgHEjxkghtkjK8IGtBkgCrcgiXED8RsGSKiDAAsDxBsYgCLNoDgEaS5BEgPF/XokfjYQJyPx4WANA0SzCboEFIDi/TwDJMGgAJANIOd9AmJmNDkYaADiPnRBEDBmwJ44QEAKiBcD8T8gVkGWAEXBXSD+xgDRDIqmh0B8H4gfMECiDqQJlJ43QrSMgiEGAOT9PA8uk8BIAAAAAElFTkSuQmCC>

[image7]: <data:image/png;base64,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>

[image8]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA8AAAAZCAYAAADuWXTMAAABQUlEQVR4Xt3SvStHURzH8W8iydMoi0HJH4DV02Kw+AcUZfW4YMEkUkjKLqvB06D8A2RQyobJw6BYJPL0/jrn6HuP+zuLiU+9hvv5nvu7957fEfmXKUUrulEbzQqmEds4xDLWcIN91Jh1PzKJO/RHfTnOcImKaPaVJbyiIx74dOED0/Ggxw9mot5GX1nXHNuyGle4F/d6qbyJW/edMXG/uGDLnOhG6roLWx74stOWOQkP2QtFCZ7FvU5VKAskPGQ0FL+6WU/Ru7hDkEolXnCNMjs4xxOKbBlFT5k+tS/qZV3cQHdTM44tDPvrET+f9deZ6KHX/24T7Rjy/Zy4Y6p7kntjSAtO8YAdTOAIJ2j2a/SzMt9rU4wm9Io7rvNYNfMp1JnrZAbxiEXsYiM7TqdN3EapWzRkx+noZwxgBfXR7K/mE2L0RXl7kBOOAAAAAElFTkSuQmCC>

[image9]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAXCAYAAAALHW+jAAABGElEQVR4XmNgGAWjYHABYyDWRhcEAh4ktgIDdjUYoBmIJwDxbSDOQhL3A+KfQMwP5T8F4tcIaezABIg3QtnPgXgRktxyBoghMLASiL8CMSOSGAZIAGJDIDYA4v9QPgyALFiGxDcH4rNIfBBIYUD4AAVcA+LdSPxUBogF6khiZUDshcTHCZiB+B8QtyGJTQTi7wwI74HoPUDMDuVrAHE3EFtA+RhgLxCvgLJZgfgMA8QSZahYDAPEeyDABMRVQBzJgNCDARyA+DwDJMxAXk8E4oNAfIwBElGgVABzrSADxKI1QJwMFcMJZIGYC4kvBsTySHwYEAbi90DMhy5BLgCl16VA7MQAiX2KQTgDJF0WoEtQArjRBUYgAABNXypdprVnXwAAAABJRU5ErkJggg==>