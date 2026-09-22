package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.FormFlag

/**
 * Result of form classification for a repetition.
 */
data class FormClassificationResult(
    val formFlag: FormFlag,
    val reason: String,
    val coachingTip: String
)

/**
 * Deterministic form classifier for Seated Knee Extension exercise.
 * Evaluates tracking confidence, Range of Motion (ROM), and movement tempo against ExerciseConfig.
 */
object FormClassifier {

    fun classifyRepetition(
        peakAngle: Float,
        cycleDurationSeconds: Float,
        averageConfidence: Float,
        config: ExerciseConfig = ExerciseConfig()
    ): FormClassificationResult {
        val formFlag = when {
            averageConfidence < config.minKeypointConfidence -> FormFlag.LOW_CONFIDENCE
            peakAngle < config.minRomAngle -> FormFlag.REDUCED_ROM
            cycleDurationSeconds < config.minTempoSeconds || cycleDurationSeconds > config.maxTempoSeconds -> FormFlag.IRREGULAR_TEMPO
            else -> FormFlag.GOOD
        }

        return FormClassificationResult(
            formFlag = formFlag,
            reason = formFlag.description,
            coachingTip = formFlag.coachingTip
        )
    }
}
