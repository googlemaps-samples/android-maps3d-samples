# Future Work for Android Maps 3D SDK Skill

This document tracks planned enhancements and evaluation strategies to further level up the skill.

## 1. Adaptive Fidelity Patterns
To make the skill respect hardware constraints (battery, thermal), we plan to add an "Adaptive Fidelity" pattern.
*   **Proposal**: Provide a `DefaultLifecycleObserver` that listens for `ACTION_POWER_SAVE_MODE_CHANGED` and throttles the camera `range` or disables heavy animations when battery saver is active.
*   **Status**: Conceptualized.

## 2. Behavioral & Visual Evaluation Strategies
To accurately evaluate an agent using this skill (moving beyond static text assertions), we plan to implement the following eval strategies:

### A. Logcat State-Machine Verification
*   **Concept**: Assert on the sequence of events emitted by the agent's code (e.g., using `CameraLogger`).
*   **Example**: Verify that `CameraAnimationEnd` happens before `MapSteady` (if applicable) and before assets are loaded.

### B. Automated Visual Testing
*   **Concept**: Use emulator screenshots or video recordings to verify that 3D elements are rendered correctly and centered.

### C. Synthesized Test Assertion
*   **Concept**: Require the agent to generate a passing instrumentation test (using the Espresso or `ComposeTestRule` snippets in the catalogs) as part of the evaluation success criteria.

### D. Walkthrough Artifact Grading
*   **Concept**: Grade the agent on its ability to produce a `walkthrough.md` with an embedded video demonstrating the working feature.
