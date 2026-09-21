# PhysioSync — Antigravity Agent Instructions

## 1. Project mission

Build **PhysioSync**, a phone-first AI-assisted physiotherapy rehabilitation prototype for the iQOO × Reskilll hackathon.

Core MVP:
- One exercise only: **seated knee extension**.
- Phone camera captures the patient.
- On-device pose estimation detects body keypoints.
- Hip/knee/ankle keypoints are filtered and smoothed.
- Knee angle drives a deterministic exercise state machine.
- State transitions produce repetition counts.
- Form is classified using centralized configurable thresholds.
- The phone gives real-time voice coaching.
- The same session state powers the patient UI, clinician dashboard, and session report.
- Office Kit is used primarily for **screen mirroring** of the clinician dashboard from phone to laptop.
- The phone is always the source of truth.

The prototype must demonstrate a complete, reliable loop rather than many partially implemented features.

---

## 2. How you must work

The user will not repeatedly restate the entire project context.

Treat this file as the persistent implementation contract.

### Task execution rule

Work on **exactly one numbered task at a time**.

The canonical sequence is:

0. Foundation
1. Camera
2. Pose detection
3. Skeleton overlay
4. Filtering and smoothing
5. Knee-angle calculation
6. Exercise state machine
7. Repetition counting
8. Form classification
9. Session state and events
10. Patient UI
11. Voice/TTS coaching
12. Session report
13. Clinician dashboard
14. Red Light integration
15. Office Kit verification
16. Green Light mirroring
17. Optional Office Kit report transfer
18. Error handling
19. Performance optimization
20. End-to-end testing
21. Final polish

Do NOT automatically implement later tasks.

When a task is requested, implement only that task and its prerequisites.

After implementation:
1. Run appropriate tests/checks.
2. Verify the task's exit criterion.
3. Report what was completed, what was tested, and any blockers.
4. Stop and wait for the next task instruction.

If the user says "continue", "next", or equivalent, proceed to the next unfinished task in this sequence.

If a task depends on an earlier task that has not passed its exit criterion, stop and explain the dependency instead of building around the broken layer.

---

## 3. Existing-plan authority

The project implementation plan supplied by the user is the source of truth for task scope and sequencing.

Do not silently redesign the product.

Do not expand scope because a feature seems useful.

If the implementation plan and an implementation convenience conflict, preserve the product architecture and ask before making a material architectural change.

---

## 4. Architecture

Use this conceptual pipeline:

Camera
→ Pose model
→ Keypoint confidence filtering
→ Keypoint smoothing
→ Knee-angle calculation
→ Exercise state machine
→ Rep counting
→ Form classification
→ Session state/events
→ Patient UI / TTS / Dashboard / Report

The phone performs the entire rehabilitation-analysis pipeline.

### Single source of truth

The phone owns:
- pose data
- filtered keypoints
- knee angle
- exercise state
- rep count
- form classification
- coaching events
- session state
- report data

Patient UI, TTS, dashboard, and report consume the same session state/events.

Do not independently recalculate exercise logic in UI layers.

---

## 5. AI/CV rules

The MVP is **pose-based**.

Do not add accelerometer sensor fusion, audio analysis, cloud AI, or additional ML models unless the user explicitly asks for a future extension.

Use deterministic exercise logic around the pose output.

### Knee extension

Primary keypoints:
- hip
- knee
- ankle

The camera should preferably view the patient from the side.

Calculate the knee angle from the three points.

Use a centralized configuration for thresholds such as:
- confidence threshold
- smoothing parameters
- extension/return angle thresholds
- target range
- tempo limits
- debounce/hysteresis values

Do not scatter magic numbers across the codebase.

The exact threshold values must be configurable and easy to tune during testing.

### Exercise states

Use the planned state-machine concept:

WAITING
→ EXTENDING
→ PEAK
→ RETURNING
→ WAITING

A repetition is counted only when the complete valid movement cycle occurs.

Avoid counting based on a single frame.

Low-confidence pose data must not silently produce a valid repetition.

---

## 6. Form classification

The MVP should support deterministic flags such as:
- GOOD
- REDUCED_ROM
- IRREGULAR_TEMPO
- LOW_CONFIDENCE

Keep classification explainable.

A flagged repetition must have a concrete reason that can be displayed to the user/clinician.

Do not claim medical diagnosis or clinical-grade accuracy.

PhysioSync is an AI-assisted rehabilitation monitoring/coaching prototype, not a replacement for a physiotherapist.

---

## 7. Session/event architecture

Use a centralized session state/event model.

Important event concepts include:
- session_started
- rep_completed
- form_flagged
- coaching_event
- session_paused
- session_ended

All downstream consumers should react to the same event/state information.

Do not create separate hidden counters for:
- patient UI
- dashboard
- report
- TTS

One source of truth only.

---

## 8. Patient UI

The live patient screen should make the core loop obvious:

- camera preview
- skeleton overlay
- current knee angle
- repetition count
- current form/status
- coaching feedback
- pause/end controls

The UI must remain usable while the camera/pose pipeline is running.

The patient view is not the clinician dashboard.

---

## 9. Voice coaching

Voice coaching must be:
- short
- understandable
- event-driven
- non-spammy

Examples:
- "Good repetition."
- "Extend your knee further."
- "Slow down your movement."
- "Please reposition."

Do not trigger repeated speech every frame.

Use meaningful coaching events or debounced conditions.

---

## 10. Clinician dashboard

The dashboard is part of the same phone application.

It should expose session information clearly enough to be mirrored to a laptop.

It can display:
- exercise name
- current/total reps
- good/flagged reps
- current knee angle
- current form status
- recent coaching/form events
- session progress

The laptop does not calculate anything.

---

## 11. Office Kit rules

Office Kit must NEVER be treated as a custom networking protocol.

Do not invent:
- Office Kit SDKs
- undocumented APIs
- WebSocket channels
- custom streaming protocols
- WebRTC replacements
- laptop-side AI processing

### Current MVP Office Kit usage

The primary Green Light workflow is:

Phone PhysioSync Dashboard
→ verified Office Kit screen mirroring
→ laptop display

The laptop is a passive mirror.

### Task 15 is verification first

Before implementing Office Kit integration, verify what is actually available on the target hardware/environment:
- pairing/discovery
- screen mirroring
- remote control
- file transfer
- clipboard
- permissions/setup
- limitations

Do not assume an API exists.

### Task 16

After verification, implement only the confirmed screen-mirroring workflow.

### Task 17

Report transfer is optional and must only use a verified Office Kit capability.

The core app must continue working if Office Kit is unavailable.

### Future Office Kit extensions

Potential future capabilities may include:
- report transfer
- clinician interaction
- remote session controls
- phone ↔ clinician workstation workflow

These are NOT part of the current MVP unless explicitly requested.

For any future Office Kit feature:
1. Verify the actual supported capability first.
2. Document limitations.
3. Only then design implementation.
4. Never substitute an invented Office Kit API.

---

## 12. Red Light / Green Light architecture

### Red Light

Must work phone-only.

Required:
- camera
- on-device pose
- skeleton
- filtering/smoothing
- knee angle
- state machine
- rep count
- form classification
- session state
- patient UI
- TTS
- dashboard
- report

No laptop dependency.

### Green Light

Adds verified Office Kit integration.

The core rehabilitation pipeline does not change.

Office Kit must not become a dependency for:
- pose inference
- rep counting
- form classification
- TTS
- report generation

If Office Kit fails, Red Light functionality must remain usable.

---

## 13. Error handling

Plan explicit states for:
- camera permission denied
- camera unavailable
- no person detected
- wrong body position
- poor lighting
- low pose confidence
- unstable tracking
- pause/resume
- session completion
- Office Kit unavailable/disconnected

Do not silently continue with invalid pose data.

---

## 14. Performance

The prototype should be evaluated on the actual target Android/iQOO device.

Pay attention to:
- camera FPS
- pose inference latency
- UI responsiveness
- memory
- CPU/GPU usage
- thermal behavior
- TTS timing

Do not invent accuracy or performance percentages.

Measure them when possible.

---

## 15. Testing requirements

Every task must have appropriate validation.

Prefer:
- unit tests for angle/state/form logic
- component tests for session state
- integration tests for camera → pose → analysis
- device testing for camera and performance
- manual verification for Office Kit
- end-to-end testing for the complete demo

For movement logic, test at minimum:
- valid repetition
- partial/reduced ROM
- irregular movement
- low-confidence frames
- false-positive prevention
- multiple repetitions
- pause/resume

---

## 16. Code quality rules

Prefer clear, maintainable code over clever code.

Keep responsibilities separated:
- camera capture
- pose inference
- keypoint processing
- exercise analysis
- session state
- presentation
- voice
- report
- Office Kit integration boundary

Do not duplicate business logic.

Do not hardcode values that should be configuration.

Do not create unnecessary abstractions before they are justified.

Do not add dependencies without a clear reason.

Do not add backend/auth/database/persistence for the single-session MVP unless explicitly requested.

---

## 17. Scope guardrails

### Must build
- one seated knee-extension exercise
- on-device pose
- skeleton overlay
- deterministic rep counting
- deterministic form classification
- voice coaching
- session summary/report
- clinician dashboard
- Red Light phone-only flow
- Green Light Office Kit mirroring

### Optional
- Office Kit report transfer
- additional polish
- auto pause/resume improvements

### Future/stretch only
- accelerometer sensor fusion
- audio/voice strain analysis
- multiple exercises
- exercise history
- cloud/backend
- accounts/authentication
- full clinician portal
- remote clinician controls

### Do not build unless explicitly requested
- custom WebSocket/WebRTC networking
- laptop application
- cloud dependency for core inference
- undocumented Office Kit API
- medical diagnosis
- clinical accuracy claims

---

## 18. How to interpret user instructions

If the user says:

### "Start"
Start Task 0.

### "Continue"
Implement the next unfinished task.

### "Next task"
Implement exactly the next task in sequence.

### "Implement Task N"
Implement only Task N, respecting its prerequisites.

### "Fix it"
Inspect the current implementation, reproduce the issue, fix the smallest appropriate layer, test it, and do not jump ahead to future tasks.

### "Review"
Review the current implementation against this document and identify concrete deviations, bugs, missing tests, or architectural violations. Do not implement unrelated future features.

### "Test"
Run the tests/checks relevant to the current task and report the result.

### "What's next?"
Identify the next unfinished task and its prerequisites, but do not implement it unless asked to continue.

---

## 19. Important anti-drift rules

Never:
- implement future tasks automatically
- replace the planned architecture without permission
- build a second source of truth
- invent Office Kit APIs
- add a backend because local implementation is inconvenient
- add multiple exercises because one exercise seems too simple
- optimize prematurely before functionality works
- hide failures with mock data
- claim a feature works without testing it
- claim medical/clinical accuracy without evidence
- make the demo depend on an optional feature

If a dependency is unavailable, report the blocker and propose the smallest compatible alternative rather than silently changing architecture.

---

## 20. Definition of MVP completion

The MVP is complete only when all of the following are demonstrated:

1. Patient starts a seated knee-extension session.
2. Camera captures the movement.
3. On-device pose tracking works.
4. Skeleton is visible.
5. Knee angle is calculated.
6. Valid repetitions are counted.
7. Imperfect/reduced-ROM movement can be flagged.
8. Voice coaching works without frame-by-frame spam.
9. Patient session state updates correctly.
10. Clinician Dashboard reflects the same session state.
11. Session report is generated.
12. Red Light works without Office Kit.
13. Office Kit has been physically verified.
14. Green Light can mirror the Dashboard to the laptop through the verified Office Kit workflow.
15. The complete flow survives repeated end-to-end testing.

Only after this should substantial future functionality be considered.

---

## 21. Working style

When reporting progress, use:

### Completed
- ...

### Tested
- ...

### Exit criterion
- PASS / NOT YET

### Issues
- ...

### Next task
- ...

Keep reports concise and implementation-focused.

Do not ask the user to repeat the project requirements contained in this file.
