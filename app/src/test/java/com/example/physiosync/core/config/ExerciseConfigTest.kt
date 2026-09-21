package com.example.physiosync.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseConfigTest {

    @Test
    fun `default configuration has valid thresholds`() {
        val config = ExerciseConfig()
        assertTrue(config.minKeypointConfidence in 0.0f..1.0f)
        assertTrue(config.smoothingAlpha in 0.0f..1.0f)
        assertTrue(config.waitingMaxAngle < config.extensionMinAngle)
        assertTrue(config.extensionMinAngle < config.peakTargetAngle)
        assertTrue(config.minRomAngle <= config.peakTargetAngle)
        assertTrue(config.minTempoSeconds < config.maxTempoSeconds)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid confidence throws exception`() {
        ExerciseConfig(minKeypointConfidence = 1.5f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid angle order throws exception`() {
        ExerciseConfig(waitingMaxAngle = 150.0f, extensionMinAngle = 120.0f)
    }
}
