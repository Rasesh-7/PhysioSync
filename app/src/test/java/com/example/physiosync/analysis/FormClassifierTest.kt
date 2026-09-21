package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.FormFlag
import org.junit.Assert.assertEquals
import org.junit.Test

class FormClassifierTest {

    private val config = ExerciseConfig(
        minKeypointConfidence = 0.5f,
        minRomAngle = 140.0f,
        minTempoSeconds = 1.0f,
        maxTempoSeconds = 6.0f
    )

    @Test
    fun `classifies proper movement as GOOD`() {
        val result = FormClassifier.classifyRepetition(
            peakAngle = 165f,
            cycleDurationSeconds = 2.5f,
            averageConfidence = 0.9f,
            config = config
        )

        assertEquals(FormFlag.GOOD, result.formFlag)
        assertEquals("Proper form executed.", result.reason)
    }

    @Test
    fun `classifies incomplete extension as REDUCED_ROM`() {
        val result = FormClassifier.classifyRepetition(
            peakAngle = 135f, // Below 140 minRomAngle
            cycleDurationSeconds = 2.5f,
            averageConfidence = 0.9f,
            config = config
        )

        assertEquals(FormFlag.REDUCED_ROM, result.formFlag)
    }

    @Test
    fun `classifies fast movement as IRREGULAR_TEMPO`() {
        val result = FormClassifier.classifyRepetition(
            peakAngle = 165f,
            cycleDurationSeconds = 0.5f, // Below 1.0s minTempoSeconds
            averageConfidence = 0.9f,
            config = config
        )

        assertEquals(FormFlag.IRREGULAR_TEMPO, result.formFlag)
    }

    @Test
    fun `classifies slow movement as IRREGULAR_TEMPO`() {
        val result = FormClassifier.classifyRepetition(
            peakAngle = 165f,
            cycleDurationSeconds = 8.0f, // Above 6.0s maxTempoSeconds
            averageConfidence = 0.9f,
            config = config
        )

        assertEquals(FormFlag.IRREGULAR_TEMPO, result.formFlag)
    }

    @Test
    fun `classifies low tracking confidence as LOW_CONFIDENCE`() {
        val result = FormClassifier.classifyRepetition(
            peakAngle = 165f,
            cycleDurationSeconds = 2.5f,
            averageConfidence = 0.3f, // Below 0.5 minConfidence
            config = config
        )

        assertEquals(FormFlag.LOW_CONFIDENCE, result.formFlag)
    }
}
