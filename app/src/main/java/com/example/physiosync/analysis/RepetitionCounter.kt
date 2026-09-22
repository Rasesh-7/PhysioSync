package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager

/**
 * Result returned after processing a frame through the repetition counting pipeline.
 */
data class RepetitionProcessResult(
    val angleResult: JointAngleResult?,
    val transition: StateTransitionResult,
    val formResult: FormClassificationResult? = null
)

/**
 * Event-driven repetition counter that processes pose frames, drives state machine transitions,
 * classifies repetition form, and records valid completed repetitions into SessionStateManager.
 */
class RepetitionCounter(
    private val defaultConfig: ExerciseConfig = ExerciseConfig(),
    val stateMachine: ExerciseStateMachine = ExerciseStateMachine(defaultConfig)
) {
    /**
     * Processes a single pose frame and updates session state & rep counts atomically.
     * Guaranteed to skip state transitions and rep counting when session is paused or inactive.
     */
    fun processFrame(
        poseFrame: PoseFrame?,
        config: ExerciseConfig = defaultConfig,
        sessionStateManager: SessionStateManager
    ): RepetitionProcessResult {
        val sessionState = sessionStateManager.state.value

        // STRICT GATING: Do not process frames, advance state machine, or count reps when paused or inactive!
        if (!sessionState.isSessionActive || sessionState.isPaused || sessionState.isCompleted) {
            return RepetitionProcessResult(
                angleResult = null,
                transition = StateTransitionResult(
                    previousState = stateMachine.currentState,
                    currentState = stateMachine.currentState,
                    isCycleCompleted = false,
                    peakAngle = stateMachine.peakAngle
                ),
                formResult = null
            )
        }

        val angleResult = KneeAngleCalculator.calculateKneeAngle(poseFrame, config)
        val transition = stateMachine.update(angleResult?.angleDegrees, config)

        if (angleResult != null) {
            sessionStateManager.updateAngleAndState(
                angle = angleResult.angleDegrees,
                exerciseState = transition.currentState
            )
        }

        var formResult: FormClassificationResult? = null

        if (transition.isCycleCompleted) {
            val avgConfidence = angleResult?.averageConfidence ?: 1.0f
            formResult = FormClassifier.classifyRepetition(
                peakAngle = transition.peakAngle,
                cycleDurationSeconds = transition.cycleDurationSeconds,
                averageConfidence = avgConfidence,
                config = config
            )

            sessionStateManager.recordRepetition(
                peakAngle = transition.peakAngle,
                formFlag = formResult.formFlag,
                durationSeconds = transition.cycleDurationSeconds
            )
        }

        return RepetitionProcessResult(
            angleResult = angleResult,
            transition = transition,
            formResult = formResult
        )
    }

    /**
     * Resets internal state machine counter.
     */
    fun reset() {
        stateMachine.reset()
    }
}
