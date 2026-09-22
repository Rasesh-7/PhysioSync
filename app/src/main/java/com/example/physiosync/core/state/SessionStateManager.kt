package com.example.physiosync.core.state

import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.RepetitionDetail
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.model.SessionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Single Source of Truth Session Manager for PhysioSync.
 * Owns the central `SessionState` StateFlow and emits `SessionEvent` SharedFlow.
 * All downstream UI/voice/dashboard layers read state from here.
 */
class SessionStateManager {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun startSession() {
        val now = System.currentTimeMillis()
        _state.update {
            SessionState(
                isSessionActive = true,
                sessionStartTimeMs = now,
                currentState = ExerciseState.WAITING,
                currentForm = FormFlag.GOOD
            )
        }
        _events.tryEmit(SessionEvent.SessionStarted(timestampMs = now))
    }

    fun pauseSession() {
        if (!_state.value.isSessionActive || _state.value.isPaused) return
        _state.update { it.copy(isPaused = true) }
        _events.tryEmit(SessionEvent.SessionPaused)
    }

    fun resumeSession() {
        if (!_state.value.isSessionActive || !_state.value.isPaused) return
        _state.update { it.copy(isPaused = false) }
        _events.tryEmit(SessionEvent.SessionResumed)
    }

    fun updateTargetReps(target: Int) {
        if (target > 0) {
            _state.update { it.copy(targetReps = target) }
        }
    }

    fun updateAngleAndState(angle: Float, exerciseState: ExerciseState, formFlag: FormFlag = FormFlag.GOOD) {
        if (!_state.value.isSessionActive || _state.value.isPaused) return
        _state.update {
            it.copy(
                currentKneeAngle = angle,
                currentState = exerciseState,
                currentForm = formFlag
            )
        }
    }

    fun recordRepetition(peakAngle: Float, formFlag: FormFlag, durationSeconds: Float) {
        if (!_state.value.isSessionActive || _state.value.isPaused) return

        val currentCount = _state.value.repCount + 1
        val isGood = formFlag == FormFlag.GOOD
        val newGoodCount = if (isGood) _state.value.goodRepCount + 1 else _state.value.goodRepCount
        val newFlaggedCount = if (!isGood) _state.value.flaggedRepCount + 1 else _state.value.flaggedRepCount

        val repDetail = RepetitionDetail(
            repIndex = currentCount,
            peakAngle = peakAngle,
            formFlag = formFlag,
            durationSeconds = durationSeconds
        )

        val updatedReps = _state.value.completedReps + repDetail

        _state.update {
            it.copy(
                repCount = currentCount,
                goodRepCount = newGoodCount,
                flaggedRepCount = newFlaggedCount,
                currentForm = formFlag,
                completedReps = updatedReps
            )
        }

        _events.tryEmit(
            SessionEvent.RepCompleted(
                repIndex = currentCount,
                peakAngle = peakAngle,
                formFlag = formFlag,
                durationSeconds = durationSeconds
            )
        )

        if (!isGood) {
            _events.tryEmit(
                SessionEvent.FormFlagged(
                    formFlag = formFlag,
                    reason = formFlag.description
                )
            )
        }

        if (currentCount >= _state.value.targetReps) {
            _events.tryEmit(SessionEvent.TargetGoalReached(targetReps = _state.value.targetReps))
        }
    }

    fun emitCoachingMessage(message: String) {
        _state.update { it.copy(latestCoachingMessage = message) }
        _events.tryEmit(SessionEvent.CoachingEvent(message = message))
    }

    fun endSession() {
        if (!_state.value.isSessionActive) return
        val finalState = _state.value
        val durationMs = if (finalState.sessionStartTimeMs > 0) System.currentTimeMillis() - finalState.sessionStartTimeMs else 0L

        _state.update {
            it.copy(
                isSessionActive = false,
                isCompleted = true,
                sessionDurationMs = durationMs
            )
        }

        _events.tryEmit(
            SessionEvent.SessionEnded(
                totalReps = finalState.repCount,
                goodReps = finalState.goodRepCount,
                flaggedReps = finalState.flaggedRepCount
            )
        )
    }

    fun resetSession() {
        _state.value = SessionState()
    }
}
