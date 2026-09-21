package com.example.physiosync.core.config

/**
 * Centralized configuration for the Seated Knee Extension exercise logic.
 * Contains configurable thresholds for pose detection confidence, keypoint smoothing,
 * state machine angle boundaries, tempo validation, and hysteresis.
 *
 * NO magic numbers scattered across the codebase.
 */
data class ExerciseConfig(
    val minKeypointConfidence: Float = 0.5f,
    val smoothingAlpha: Float = 0.3f,
    val waitingMaxAngle: Float = 100.0f,
    val extensionMinAngle: Float = 120.0f,
    val peakTargetAngle: Float = 160.0f,
    val minRomAngle: Float = 140.0f,
    val returnAngleThreshold: Float = 105.0f,
    val minTempoSeconds: Float = 1.0f,
    val maxTempoSeconds: Float = 6.0f,
    val hysteresisDegrees: Float = 5.0f
) {
    init {
        require(minKeypointConfidence in 0.0f..1.0f) { "Confidence threshold must be between 0.0 and 1.0" }
        require(smoothingAlpha in 0.0f..1.0f) { "Smoothing alpha must be between 0.0 and 1.0" }
        require(waitingMaxAngle < extensionMinAngle) { "waitingMaxAngle must be < extensionMinAngle" }
        require(extensionMinAngle < peakTargetAngle) { "extensionMinAngle must be < peakTargetAngle" }
        require(minRomAngle <= peakTargetAngle) { "minRomAngle must be <= peakTargetAngle" }
        require(minTempoSeconds > 0) { "minTempoSeconds must be positive" }
        require(maxTempoSeconds > minTempoSeconds) { "maxTempoSeconds must be > minTempoSeconds" }
    }
}
