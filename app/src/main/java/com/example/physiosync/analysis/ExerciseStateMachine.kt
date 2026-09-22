package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.ExerciseState

/**
 * Result emitted on every state machine evaluation.
 */
data class StateTransitionResult(
    val previousState: ExerciseState,
    val currentState: ExerciseState,
    val isCycleCompleted: Boolean = false,
    val peakAngle: Float = 0f,
    val cycleDurationSeconds: Float = 0f
)

/**
 * Deterministic state machine managing Seated Knee Extension exercise states.
 * Sequence: WAITING -> EXTENDING -> PEAK -> RETURNING -> WAITING (Cycle Complete)
 */
class ExerciseStateMachine(
    private val defaultConfig: ExerciseConfig = ExerciseConfig()
) {
    var currentState: ExerciseState = ExerciseState.WAITING
        private set

    var peakAngle: Float = 0f
        private set

    var cycleStartTimestampMs: Long = 0L
        private set

    /**
     * Evaluates current knee angle and drives deterministic state transitions.
     */
    fun update(angle: Float?, config: ExerciseConfig = defaultConfig): StateTransitionResult {
        val prevState = currentState

        if (angle == null) {
            return StateTransitionResult(
                previousState = prevState,
                currentState = currentState,
                isCycleCompleted = false,
                peakAngle = peakAngle
            )
        }

        val now = System.currentTimeMillis()
        var isCycleCompleted = false
        var durationSeconds = 0f

        // Minimum peak angle required to recognize an intentional extension movement
        val minPeakThreshold = (config.extensionMinAngle + 10f).coerceAtMost(config.minRomAngle)

        when (currentState) {
            ExerciseState.WAITING -> {
                // Transition to EXTENDING when knee starts extending beyond threshold
                if (angle >= config.extensionMinAngle) {
                    currentState = ExerciseState.EXTENDING
                    peakAngle = angle
                    cycleStartTimestampMs = now
                }
            }

            ExerciseState.EXTENDING -> {
                // 1. Aborted extension: returned back to resting position before reaching meaningful extension
                if (angle <= config.waitingMaxAngle) {
                    currentState = ExerciseState.WAITING
                    peakAngle = 0f
                }
                // 2. Reached full extension target
                else if (angle >= config.peakTargetAngle) {
                    if (angle > peakAngle) peakAngle = angle
                    currentState = ExerciseState.PEAK
                }
                // 3. Reached local peak and started returning down (must have reached at least minPeakThreshold)
                else if (angle < (peakAngle - config.hysteresisDegrees) && peakAngle >= minPeakThreshold) {
                    currentState = ExerciseState.PEAK
                }
                // 4. Still extending upwards
                else if (angle > peakAngle) {
                    peakAngle = angle
                }
            }

            ExerciseState.PEAK -> {
                if (angle > peakAngle) {
                    peakAngle = angle
                }

                // Transition to RETURNING when knee angle decreases past hysteresis threshold
                if (angle <= (peakAngle - config.hysteresisDegrees)) {
                    currentState = ExerciseState.RETURNING
                }
            }

            ExerciseState.RETURNING -> {
                // Transition back to WAITING when knee returns to resting position (Cycle Complete!)
                if (angle <= config.returnAngleThreshold) {
                    currentState = ExerciseState.WAITING
                    isCycleCompleted = true
                    durationSeconds = if (cycleStartTimestampMs > 0) {
                        (now - cycleStartTimestampMs) / 1000f
                    } else 0f
                }
            }
        }

        val result = StateTransitionResult(
            previousState = prevState,
            currentState = currentState,
            isCycleCompleted = isCycleCompleted,
            peakAngle = peakAngle,
            cycleDurationSeconds = durationSeconds
        )

        if (isCycleCompleted) {
            // Reset for next repetition cycle
            peakAngle = 0f
            cycleStartTimestampMs = 0L
        }

        return result
    }

    /**
     * Resets state machine to WAITING.
     */
    fun reset() {
        currentState = ExerciseState.WAITING
        peakAngle = 0f
        cycleStartTimestampMs = 0L
    }
}
