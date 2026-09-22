package com.example.physiosync.core.model

/**
 * Single source of truth event model emitted during a physiotherapy session.
 * Downstream UI, Voice/TTS, Clinician Dashboard, and Reports consume these events.
 */
sealed interface SessionEvent {
    data class SessionStarted(val timestampMs: Long = System.currentTimeMillis()) : SessionEvent

    data class RepCompleted(
        val repIndex: Int,
        val peakAngle: Float,
        val formFlag: FormFlag,
        val durationSeconds: Float,
        val timestampMs: Long = System.currentTimeMillis()
    ) : SessionEvent

    data class FormFlagged(
        val formFlag: FormFlag,
        val reason: String,
        val timestampMs: Long = System.currentTimeMillis()
    ) : SessionEvent

    data class CoachingEvent(
        val message: String,
        val timestampMs: Long = System.currentTimeMillis()
    ) : SessionEvent

    data object SessionPaused : SessionEvent
    data object SessionResumed : SessionEvent

    data class TargetGoalReached(val targetReps: Int) : SessionEvent

    data class SessionEnded(
        val totalReps: Int,
        val goodReps: Int,
        val flaggedReps: Int,
        val timestampMs: Long = System.currentTimeMillis()
    ) : SessionEvent
}
