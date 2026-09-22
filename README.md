# 🏥 PhysioSync — AI-Assisted Rehabilitation Prototype

> **iQOO × Reskilll Hackathon Prototype**  
> *Phone-First, On-Device AI Physiotherapy Rehabilitation & Form Coaching*

---

## 🌟 Overview

**PhysioSync** is a phone-first, on-device AI rehabilitation monitoring and voice coaching application designed for knee rehabilitation (**Seated Knee Extension**). Powered by on-device computer vision and deterministic biomechanical analysis, PhysioSync captures, filters, and analyzes patient movement in real time—delivering instant voice coaching, posture feedback, and session reports without requiring any external cloud AI dependencies.

---

## 🚀 Key Features

### 🤳 1. Intelligent Dual-Camera Tracking
- **Default Selfie Camera Mode:** Opens into the front selfie camera so patients can sit facing their phone screen during exercises.
- **Smart Hardware Fallback:** Automatically detects camera availability and falls back to the rear camera on devices without front-facing hardware.
- **Live Camera Switcher:** Tap the camera icon (`📷`) anytime to switch between front and rear camera views.
- **Mirror-Corrected Skeleton:** Real-time canvas overlay that flips coordinates dynamically for natural mirror-view feedback.

### 📐 2. Real-Time On-Device AI Pose & Angle Analysis
- **On-Device Machine Learning:** Employs MLKit Pose Detection running 100% locally on the device (zero cloud latency, full privacy).
- **OneEuroFilter Smoothing:** Filters raw keypoint coordinates to eliminate visual jitter and sensor noise.
- **Dynamic Knee Angle Calculation:** Calculates 2D vectors across **Hip $\rightarrow$ Knee $\rightarrow$ Ankle** keypoints to measure leg extension angle in real time.

### 🔄 3. Deterministic 4-Phase Exercise State Machine
- Movement cycle tracking using a deterministic state machine:
  $$\text{WAITING} \longrightarrow \text{EXTENDING} \longrightarrow \text{PEAK} \longrightarrow \text{RETURNING} \longrightarrow \text{WAITING}$$
- **Pause-Gated Repetition Counter:** Prevents false-positive counts when the session is paused or tracking confidence is low.
- **Explainable Form Classification:** Classifies every repetition into `GOOD`, `REDUCED_ROM`, `IRREGULAR_TEMPO`, or `LOW_CONFIDENCE`.

### 🗣️ 4. Event-Driven AI Voice Coaching & Sound Effects
- **Hands-Free TTS Assistant (`VoiceCoachManager`):** Listens to central session events and speaks timely coaching tips (e.g. *"Good repetition"*, *"Extend your knee further"*, *"Maintain steady tempo"*).
- **Debounced Audio Output:** 2.5-second debounce window ensures coaching speech never floods or stutters during fast movements.
- **Crisp Sound Effects (`SoundEffectsManager`):** Instant audio chimes for valid reps, posture warnings, and goal completion.

### 🎯 5. Target Rep Goal Selector & Fanfare
- **Interactive Goal Chips:** Easily select rep goals (**5**, **10**, **15**, **20** reps) directly from the top header.
- **Celebratory Fanfare:** Plays a distinct multi-note audio fanfare and triggers a special voice announcement (*"Goal achieved! Target repetitions completed"*) upon finishing the set.

### 📊 6. Patient-Centric Posture & Progress Report
- **Posture Quality Score:** High-level posture score percentage (e.g. `80% Excellent Form`).
- **Actionable Posture Advice:** Plain-English improvement tips (e.g., *"Aim to extend your leg fully straight at the top of each kick"*).
- **Repetition Breakdown:** Itemized breakdown of every repetition with posture recommendations instead of complex clinical jargon.

---

## 🛠️ Architecture & Data Pipeline

PhysioSync enforces a **Single Source of Truth** pattern via `SessionStateManager`. All UI layers, voice coaching, sound effects, and reports consume state and events from this central pipeline:

```
    [ Phone CameraX Feed ]
              │
              ▼
   [ MLKit Pose Detector ]
              │
              ▼
    [ OneEuroKeypointFilter ]
              │
              ▼
  [ JointAngleCalculator (2D) ]
              │
              ▼
   [ ExerciseStateMachine ]
              │
              ▼
     [ RepetitionCounter ]
              │
              ▼
     [ FormClassifier ]
              │
              ▼
   ┌──────────────────────────────────────────────┐
   │         SessionStateManager (SSOT)           │
   │   • StateFlow<SessionState>                  │
   │   • SharedFlow<SessionEvent>                 │
   └──────────────────────┬───────────────────────┘
                          │
         ┌────────────────┼────────────────┐
         ▼                ▼                ▼
   [ Patient UI ]  [ Voice Coach ]  [ Sound Effects ]
   (Camera+Overlay)   (Android TTS)   (Audio Chimes)
         │
         ▼
  [ Posture Report ]
```

---

## 💻 Tech Stack

- **Language:** Kotlin 1.9+
- **UI Framework:** Jetpack Compose (Material3)
- **Camera:** CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Pose AI:** Google MLKit Pose Detection (Accurate model)
- **Concurrency:** Kotlin Coroutines & StateFlow / SharedFlow
- **Text-To-Speech:** Native Android `TextToSpeech`
- **Audio Effects:** Android `ToneGenerator` & `AudioManager`

---

## 🚦 Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1+) or newer
- **JDK:** Java 17
- **Device / Emulator:** Android 8.0 (API level 26) or higher with camera access

### Installation & Build

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Rasesh-7/PhysioSync.git
   cd PhysioSync
   ```

2. **Open in Android Studio:**
   - Open Android Studio and select **Open an Existing Project**.
   - Navigate to the `PhysioSync` folder.

3. **Build & Run:**
   - Connect an Android device via USB debugging (or use an emulator with camera enabled).
   - Press **Run 'app'** (`Shift + F10`).

---

## 🏆 Hackathon Context

PhysioSync is built specifically for the **iQOO × Reskilll Hackathon**. It showcases high-performance on-device AI capabilities on modern smartphone hardware, demonstrating a reliable end-to-end digital physiotherapy assistant.
