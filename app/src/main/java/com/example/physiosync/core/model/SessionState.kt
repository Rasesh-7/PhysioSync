package com.example.physiosync.core.model

/**
 * Recorded detail for a single completed repetition.
 */
data class RepetitionDetail(
    val repIndex: Int,
    val peakAngle: Float,
    val formFlag: FormFlag,
    val durationSeconds: Float,
    val timestampMs: Long = System.currentTimeMillis()
)

/**
 * Immutable single source of truth representation of the rehabilitation session state.
 * Consumed directly by Patient UI, Clinician Dashboard, Voice TTS, and Report modules.
 */
data class SessionState(
    val isSessionActive: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val repCount: Int = 0,
    val goodRepCount: Int = 0,
    val flaggedRepCount: Int = 0,
    val currentKneeAngle: Float = 0.0f,
    val currentState: ExerciseState = ExerciseState.WAITING,
    val currentForm: FormFlag = FormFlag.GOOD,
    val latestCoachingMessage: String? = null,
    val completedReps: List<RepetitionDetail> = emptyList(),
    val sessionStartTimeMs: Long = 0L,
    val sessionDurationMs: Long = 0L
)
