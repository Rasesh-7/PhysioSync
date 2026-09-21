package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager

/**
 * Result returned after processing a frame through the repetition counting pipeline.
 */
data class RepetitionProcessResult(
    val angleResult: JointAngleResult?,
    val transition: StateTransitionResult
)

/**
 * Event-driven repetition counter that processes pose frames, drives state machine transitions,
 * and records valid completed repetitions into the single-source-of-truth SessionStateManager.
 */
class RepetitionCounter(
    private val defaultConfig: ExerciseConfig = ExerciseConfig(),
    val stateMachine: ExerciseStateMachine = ExerciseStateMachine(defaultConfig)
) {
    /**
     * Processes a single pose frame and updates session state & rep counts atomically.
     */
    fun processFrame(
        poseFrame: PoseFrame?,
        config: ExerciseConfig = defaultConfig,
        sessionStateManager: SessionStateManager
    ): RepetitionProcessResult {
        val angleResult = KneeAngleCalculator.calculateKneeAngle(poseFrame, config)
        val transition = stateMachine.update(angleResult?.angleDegrees, config)

        if (angleResult != null) {
            sessionStateManager.updateAngleAndState(
                angle = angleResult.angleDegrees,
                exerciseState = transition.currentState
            )
        }

        if (transition.isCycleCompleted) {
            val formFlag = if (transition.peakAngle >= config.minRomAngle) {
                FormFlag.GOOD
            } else {
                FormFlag.REDUCED_ROM
            }

            sessionStateManager.recordRepetition(
                peakAngle = transition.peakAngle,
                formFlag = formFlag,
                durationSeconds = transition.cycleDurationSeconds
            )
        }

        return RepetitionProcessResult(
            angleResult = angleResult,
            transition = transition
        )
    }

    /**
     * Resets internal state machine counter.
     */
    fun reset() {
        stateMachine.reset()
    }
}
