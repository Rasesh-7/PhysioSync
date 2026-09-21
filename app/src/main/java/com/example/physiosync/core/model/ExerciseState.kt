package com.example.physiosync.core.model

/**
 * Deterministic state machine states for the Seated Knee Extension exercise.
 * Cycle: WAITING -> EXTENDING -> PEAK -> RETURNING -> WAITING
 */
enum class ExerciseState {
    WAITING,
    EXTENDING,
    PEAK,
    RETURNING
}
