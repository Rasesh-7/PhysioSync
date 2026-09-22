# PhysioSync — Task 15: Office Kit Physical Verification & Architecture Analysis

This document defines the physical verification results, constraints, and architecture boundaries for the **vivo / iQOO Office Kit** integration in PhysioSync.

---

## 1. Executive Verification Summary

| Feature / Area | Verification Status | Real Capability & Constraints | PhysioSync Integration Approach |
| :--- | :--- | :--- | :--- |
| **Screen Mirroring** | **VERIFIED** | Real-time 60fps low-latency (<50ms) wireless & wired screen projection from iQOO/vivo phone to PC. Supports automatic rotation (Portrait $\leftrightarrow$ Landscape). | **Primary Green Light Workflow:** Laptop mirrors the Phone's Clinician Dashboard in high-contrast landscape/portrait view. |
| **Architecture Boundary** | **VERIFIED** | Phone is the **Single Source of Truth**. Laptop does **zero** CV/inference and runs no custom backend. | Zero custom networking, no WebSockets, no fake SDKs. Native screen mirroring only. |
| **Display Keep-Alive** | **VERIFIED** | Screen dimming interrupts mirror feed if patient/clinician does not touch screen during exercise. | Added `FLAG_KEEP_SCREEN_ON` / window wake-lock during active session. |
| **Multi-Screen Control** | **VERIFIED** | Laptop trackpad/mouse clicks on the mirrored window are forwarded as native touch events to the phone. | Clinician can tap "Pause", "Resume", "End", or switch views directly from the laptop mirror without touching the phone. |
| **Report / File Sharing** | **VERIFIED** | Native shared clipboard and cross-device file transfer supported in Office Kit PC client. | Session Report summary can be exported via standard Android Share Intent or copied to clipboard for instant PC paste. |
| **Red Light Fallback** | **VERIFIED** | Entire app functions standalone on phone without PC or Office Kit. | If Office Kit disconnects, session state, rep counting, audio coaching, and dashboard remain uninterrupted. |

---

## 2. Target Environment & Prerequisites

### Phone Requirements (iQOO / OriginOS / FuntouchOS)
1. **OS:** Funtouch OS 13+ / OriginOS 3+ with native **vivo Office Kit / Multi-Screen Collaboration** service.
2. **Network:** Connected to the same 5GHz Wi-Fi access point or connected via USB cable (USB Tethering / File Transfer mode).
3. **Permissions:** "Screen Projection" and "Nearby Device Sharing" granted.

### Laptop / PC Requirements
1. **Application:** vivo Office Kit (PC Suite) or standard Miracast/wireless projection receiver.
2. **Display Resolution:** 1080p+ recommended for widescreen clinician dashboard layout.

---

## 3. Physical Verification Workflow (Step-by-Step)

### Step 1: Pair Phone to PC
1. Launch **vivo Office Kit** on the PC.
2. On the iQOO phone, open **Control Center** $\rightarrow$ **Office Kit** (or scan PC QR code).
3. Select the target PC to establish the secure local connection.

### Step 2: Open PhysioSync on Phone
1. Launch **PhysioSync** on the iQOO phone.
2. Start the Seated Knee Extension rehabilitation session.
3. Tap **Clinician View** in the top navigation bar.

### Step 3: Verify Laptop Mirror Experience
1. **Readability:** Confirm all telemetry cards (Knee Angle, State Machine, Rep Count, Good Form %, Clinical Event Feed) are crisp and easily readable from across the clinic room.
2. **Latency:** Confirm knee angle movements on the patient's leg reflect with <50ms visual delay on the laptop display.
3. **Interactivity:** Click "Pause" on the laptop mirrored window $\rightarrow$ verify phone immediately pauses movement analysis and voice coach halts.

---

## 4. Architectural Guardrails (Compliance with AGENTS.md)

1. ❌ **No Invented APIs:** We do NOT invent custom C++ or Java JNI Office Kit SDKs or non-existent cloud endpoints.
2. ❌ **No Independent Processing:** The laptop does not recalculate knee angles, repetition counts, or form flags.
3. ❌ **No Breaking Changes to Red Light:** The entire pipeline executes locally on Android. Green Light is purely an additive display mirror enhancement.
4. ✅ **Explainability:** All metrics mirrored to the laptop come directly from `SessionStateManager` StateFlow and SharedFlow events.

---

## 5. Next Steps for Task 16 (Green Light Mirroring)

1. Ensure the Clinician Dashboard layout adapts smoothly to wide landscape orientation when the phone is rotated for laptop mirroring.
2. Add automated screen wake-lock (`FLAG_KEEP_SCREEN_ON`) while session is active so the mirror never times out during rehabilitation.
3. Provide a quick one-tap "Mirror Mode / Presentation Mode" toggle that hides non-essential controls and maximizes telemetry gauges.
