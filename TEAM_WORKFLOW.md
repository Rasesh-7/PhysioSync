# PhysioSync — Team GitHub Workflow

This file defines how the 3-person PhysioSync team works with GitHub and Antigravity.

## 1. Project Structure

PhysioSync is a phone-first AI-assisted physiotherapy prototype.

Core architecture:

Camera
→ Pose Estimation
→ Confidence Filtering
→ Smoothing
→ Knee Angle
→ State Machine
→ Rep Count
→ Form Classification
→ Session State / Events
→ Patient UI / TTS / Dashboard / Report

The phone is the source of truth. The laptop/Office Kit view must not independently calculate AI results.

The detailed implementation plan and `AGENTS.md` are the source of truth for product scope and task behavior.

## 2. Team Responsibilities

### Person 1 — Core AI + Integration

Owns:

- Task 0 — Foundation
- Task 1 — Camera
- Task 2 — Pose
- Task 3 — Skeleton
- Task 4 — Filtering / Smoothing
- Task 5 — Knee Angle
- Task 6 — State Machine
- Task 7 — Rep Counting
- Task 8 — Form Classification
- Task 9 — Session State / Events
- Task 14 — Red Light Integration
- Task 18 — Error Handling
- Task 19 — Performance
- Task 20 — End-to-End Testing
- Task 21 — Final Polish / Integration

Primary responsibility: make the core phone-side pipeline reliable and provide stable interfaces for the other two workstreams.

### Person 2 — Patient Experience

Owns:

- Task 10 — Patient UI
- Task 11 — TTS
- Task 12 — Session Report

Primary responsibility: consume shared session state/events and present the patient-facing experience.

Do NOT independently implement rep counting, angle calculation, or form classification.

### Person 3 — Clinician / Office Kit

Owns:

- Task 13 — Dashboard / Coach View
- Task 15 — Office Kit Verification
- Task 16 — Green Light Mirroring
- Task 17 — Optional Report Transfer

Primary responsibility: present the same phone session state in the clinician-facing dashboard and verify the actual Office Kit workflow.

Do NOT create a second AI/CV pipeline on the laptop.

## 3. Branch Strategy

The protected integration branch is:

    main

Use these long-lived feature branches:

    feature/core-ai
    feature/patient-experience
    feature/clinician-dashboard

Do not work directly on `main`.

Before starting work:

    git checkout main
    git pull origin main

Then switch to the appropriate feature branch.

## 4. GitHub Pull Request Workflow

Every completed piece of work should go through a Pull Request.

    feature branch
        ↓
    implement
        ↓
    test
        ↓
    commit
        ↓
    push
        ↓
    Pull Request → main
        ↓
    review
        ↓
    merge

Do not push directly to `main`.

Do not merge a PR without another teammate reviewing it.

The Core AI / Integration owner is the final integration owner for `main`.

## 5. Commit Rules

Keep commits focused and descriptive.

Good:

    Implement knee angle calculation
    Add patient session state UI
    Add coaching TTS events
    Add clinician dashboard metrics

Avoid:

    changes
    stuff
    final
    fixed everything

Do not mix unrelated features into one commit.

Before committing:

    git status
    git diff

Then:

    git add <relevant-files>
    git commit -m "<clear description>"

## 6. Pull Before Starting Work

Before beginning a new work session:

    git checkout main
    git pull origin main

Then update your feature branch from the latest main before starting a new task.

Do not blindly overwrite another teammate's changes.

If Git reports a conflict:
1. Stop.
2. Inspect the conflict.
3. Understand which change belongs to which subsystem.
4. Resolve deliberately.
5. Run relevant tests/build.
6. Only then commit the resolution.

If the conflict involves a shared architectural contract, coordinate with the Core AI / Integration owner before resolving it.

## 7. Shared Architecture Contract

Task 9 establishes the shared session state/event contract.

Downstream components should consume this contract rather than recreate upstream logic.

Important shared concepts include:

- Session started
- Session ended
- Rep completed
- Form flagged
- Coaching event
- Session paused
- Rep count
- Form status
- Relevant session metrics

Use the exact models/names defined by the implementation.

Do not casually rename or restructure shared models.

If a change to the contract is necessary:
1. Explain why.
2. Check all consumers.
3. Update affected tests.
4. Coordinate with the Core AI / Integration owner.
5. Merge only after the whole project still builds.

## 8. Avoid Cross-Team Interference

### Person 1 should primarily modify:
- Camera / pose / CV
- Exercise engine
- Session state/events
- Core integration
- Shared domain models

### Person 2 should primarily modify:
- Patient screens
- Patient interaction
- TTS
- Report presentation/generation

### Person 3 should primarily modify:
- Dashboard / Coach View
- Office Kit integration workflow
- Green Light functionality
- Optional report transfer

Avoid modifying another person's subsystem unless necessary. If another subsystem must change, communicate it before making the change.

## 9. Antigravity Rules

All three teammates use Antigravity.

Antigravity must follow the repository's `AGENTS.md`.

For each task:
1. Read `AGENTS.md`.
2. Read the relevant implementation-plan task.
3. Confirm the current branch.
4. Inspect the existing implementation before changing it.
5. Implement only the assigned task.
6. Run relevant tests/build checks.
7. Verify the task's exit criterion.
8. Report what changed and what was tested.
9. Stop before implementing unrelated later tasks.

Do not ask Antigravity to "build everything" on an active shared branch.

Use focused instructions such as:

    Implement Task 10 from the PhysioSync implementation plan.
    Follow AGENTS.md and TEAM_WORKFLOW.md.
    Do not implement Task 11 or later tasks.
    Do not modify the CV pipeline.
    Verify the Task 10 exit criterion before stopping.

## 10. Integration Points

### Integration Point A
Camera → Pose

Owned by Core AI.

### Integration Point B
Pose → Exercise Analysis

Owned by Core AI.

### Integration Point C
Exercise Analysis → Session Events

Owned by Core AI / Task 9.

### Integration Point D
Session Events → Patient UI / TTS / Report

Owned by Patient Experience.

### Integration Point E
Session State / Events → Dashboard

Owned by Clinician / Office Kit.

### Integration Point F
Existing Dashboard → Office Kit mirroring

Owned by Clinician / Office Kit.

## 11. What Must Never Happen

Do NOT:
- Push directly to `main`.
- Rewrite another teammate's work without coordination.
- Create a second independent rep-counting implementation.
- Create a second independent form-classification implementation.
- Make the laptop the source of truth.
- Add custom networking for Office Kit without explicit approval and feasibility verification.
- Invent an Office Kit SDK/API that has not been verified.
- Add cloud AI when the MVP requires on-device/local processing.
- Add extra exercises before the core knee-extension loop is stable.
- Commit API keys, secrets, `local.properties`, `.idea/`, or `.gradle/`.
- Change the Kotlin/Android architecture casually just to make one task easier.
- Merge code that has not been tested.

## 12. Files That Should Normally NOT Be Committed

Follow `.gitignore`.

Normally do not commit:

    local.properties
    .idea/
    .gradle/
    build/
    generated build artifacts
    API keys
    passwords
    secrets

If `.gitignore` needs to be changed, review the change carefully before committing.

## 13. Testing Before Pull Request

At minimum:
- Project compiles.
- Relevant unit/component tests pass.
- Existing functionality is not broken.
- Assigned task's exit criterion is satisfied.
- No accidental unrelated files are included.

For integration PRs, test the affected end-to-end flow where practical.

For device-specific functionality, test on the target Android/iQOO device when available.

## 14. Pull Request Template

Use this structure:

### What changed
- Brief description of implementation.

### Tasks completed
- Task number(s)

### Tests performed
- Build:
- Unit/component tests:
- Device test:
- Manual verification:

### Integration impact
- Shared models changed?
- Session events changed?
- UI/API contract changed?
- Other teammate affected?

### Known issues
- List only real known issues.

### Scope check
- No unrelated task implemented.
- No unnecessary architecture changes.
- No secrets committed.

## 15. Merge Order

Recommended integration order:

1. Core foundation and camera/pose pipeline
2. Exercise analysis
3. Session state/events
4. Patient UI/TTS/report
5. Dashboard
6. Red Light integration
7. Office Kit verification
8. Green Light integration
9. Error handling
10. Performance
11. End-to-end testing
12. Final polish

The exact task dependencies in `AGENTS.md` and the implementation plan take precedence.

## 16. When a Teammate Finishes

The teammate should:
1. Test the work.
2. Commit it.
3. Push the feature branch.
4. Open a Pull Request.
5. Tell the team what changed.
6. Identify any shared-contract changes.
7. Wait for review/merge.

After merge:

    git checkout main
    git pull origin main

Then update the feature branch before continuing.

## 17. If Two People Need the Same File

Do not immediately edit the same file independently.

First ask:
- Can the work be moved into separate files/classes?
- Can a stable interface be created?
- Can one person finish the shared base first?
- Can the work be sequenced instead of parallelized?

If both changes genuinely must touch the same file, coordinate before coding and keep the changes small.

## 18. Definition of Team Success

The repository is ready for the final demo when:
- Red Light phone-only flow works.
- Camera and pose tracking work reliably enough for the demo.
- Knee extension reps are detected.
- Form flags are explainable.
- Voice coaching works.
- Patient UI reflects the same session state.
- Clinician dashboard reflects the same session state.
- Office Kit Green Light workflow works as verified.
- Session report works.
- Error handling is present.
- Performance is acceptable on the target device.
- Complete flow has been tested end-to-end.
- No teammate's work is stranded on an unmerged branch.

The implementation plan remains the source of truth for what the product should contain. This file only defines how the team collaborates to build it.
