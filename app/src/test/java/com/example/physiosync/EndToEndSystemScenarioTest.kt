package com.example.physiosync

import com.example.physiosync.analysis.RepetitionCounter
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager
import com.example.physiosync.pose.KeypointFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Task 20 — End-to-End System & Scenario Test Suite.
 * Validates all required rehabilitation movement scenarios:
 * 1. Valid repetition
 * 2. Reduced ROM repetition
 * 3. Irregular tempo repetition
 * 4. Low confidence frame protection
 * 5. False positive prevention
 * 6. Multiple sequential repetitions
 * 7. Pause / resume lifecycle gating
 */
class EndToEndSystemScenarioTest {

    private lateinit var config: ExerciseConfig
    private lateinit var keypointFilter: KeypointFilter
    private lateinit var repetitionCounter: RepetitionCounter
    private lateinit var sessionStateManager: SessionStateManager

    @Before
    fun setUp() {
        config = ExerciseConfig(
            minKeypointConfidence = 0.5f,
            smoothingAlpha = 1.0f,
            minTempoSeconds = 0.0f
        )
        keypointFilter = KeypointFilter(config)
        repetitionCounter = RepetitionCounter(config)
        sessionStateManager = SessionStateManager()
        sessionStateManager.startSession()
    }

    private fun createPoseFrame(
        timestampMs: Long,
        hipX: Float, hipY: Float,
        kneeX: Float, kneeY: Float,
        ankleX: Float, ankleY: Float,
        confidence: Float = 0.95f
    ): PoseFrame {
        val landmarks = mapOf(
            LandmarkType.LEFT_HIP to BodyLandmark(LandmarkType.LEFT_HIP, Point3D(hipX, hipY, 0f), confidence),
            LandmarkType.LEFT_KNEE to BodyLandmark(LandmarkType.LEFT_KNEE, Point3D(kneeX, kneeY, 0f), confidence),
            LandmarkType.LEFT_ANKLE to BodyLandmark(LandmarkType.LEFT_ANKLE, Point3D(ankleX, ankleY, 0f), confidence)
        )
        return PoseFrame(timestampMs = timestampMs, landmarks = landmarks)
    }

    private fun executeCycle(
        peakAnkleX: Float,
        peakAnkleY: Float,
        startMs: Long,
        exerciseConfig: ExerciseConfig = config
    ) {
        val f1 = createPoseFrame(startMs, 0f, 10f, 0f, 0f, 10f, 0f) // 90 deg resting (WAITING)
        val f2 = createPoseFrame(startMs + 200L, 0f, 10f, 0f, 0f, 10f, -8f) // 128.6 deg (EXTENDING)
        val f3 = createPoseFrame(startMs + 400L, 0f, 10f, 0f, 0f, peakAnkleX, peakAnkleY) // PEAK (180 or 135)
        val f4 = createPoseFrame(startMs + 600L, 0f, 10f, 0f, 0f, 10f, -5f) // 116.5 deg (PEAK -> RETURNING)
        val f5 = createPoseFrame(startMs + 800L, 0f, 10f, 0f, 0f, 10f, 0f) // 90 deg (RETURNING)
        val f6 = createPoseFrame(startMs + 1000L, 0f, 10f, 0f, 0f, 10f, 0f) // 90 deg (WAITING, Rep Complete!)

        repetitionCounter.processFrame(keypointFilter.filter(f1, exerciseConfig), exerciseConfig, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f2, exerciseConfig), exerciseConfig, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f3, exerciseConfig), exerciseConfig, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f4, exerciseConfig), exerciseConfig, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f5, exerciseConfig), exerciseConfig, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f6, exerciseConfig), exerciseConfig, sessionStateManager)
    }

    @Test
    fun validRepetition_incrementsCountAndClassifiesGoodForm() {
        executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = 1000L) // 180 deg peak >= 140 deg minRom

        val state = sessionStateManager.state.value
        assertEquals(1, state.repCount)
        assertEquals(1, state.goodRepCount)
        assertEquals(0, state.flaggedRepCount)
        assertEquals(FormFlag.GOOD, state.completedReps.first().formFlag)
    }

    @Test
    fun reducedRomRepetition_incrementsCountAndFlagsReducedRom() {
        // Reduced ROM: Peak angle 135 deg (peakAnkleX = 10f, peakAnkleY = -10f) < 140 deg minRomAngle
        executeCycle(peakAnkleX = 10f, peakAnkleY = -10f, startMs = 1000L)

        val state = sessionStateManager.state.value
        assertEquals(1, state.repCount)
        assertEquals(0, state.goodRepCount)
        assertEquals(1, state.flaggedRepCount)
        assertEquals(FormFlag.REDUCED_ROM, state.completedReps.first().formFlag)
    }

    @Test
    fun irregularTempoRepetition_flagsIrregularTempo() {
        val tempoConfig = ExerciseConfig(
            minKeypointConfidence = 0.5f,
            smoothingAlpha = 1.0f,
            minTempoSeconds = 1.0f
        )
        executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = 1000L, exerciseConfig = tempoConfig)

        val state = sessionStateManager.state.value
        assertEquals(1, state.repCount)
        assertEquals(0, state.goodRepCount)
        assertEquals(1, state.flaggedRepCount)
        assertEquals(FormFlag.IRREGULAR_TEMPO, state.completedReps.first().formFlag)
    }

    @Test
    fun lowConfidenceFrames_preventsFalsePositiveCounting() {
        val f1 = createPoseFrame(1000L, 0f, 10f, 0f, 0f, 10f, 0f, confidence = 0.2f)
        val f2 = createPoseFrame(2000L, 0f, 10f, 0f, 0f, 0f, -10f, confidence = 0.2f)
        val f3 = createPoseFrame(3000L, 0f, 10f, 0f, 0f, 10f, 0f, confidence = 0.2f)

        repetitionCounter.processFrame(keypointFilter.filter(f1, config), config, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f2, config), config, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f3, config), config, sessionStateManager)

        assertEquals(0, sessionStateManager.state.value.repCount)
    }

    @Test
    fun falsePositivePrevention_smallLegMovementDoesNotCountAsRep() {
        val f1 = createPoseFrame(1000L, 0f, 10f, 0f, 0f, 10f, 0f)
        val f2 = createPoseFrame(1500L, 0f, 10f, 0f, 0f, 10f, 3f)
        val f3 = createPoseFrame(2000L, 0f, 10f, 0f, 0f, 10f, 0f)

        repetitionCounter.processFrame(keypointFilter.filter(f1, config), config, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f2, config), config, sessionStateManager)
        repetitionCounter.processFrame(keypointFilter.filter(f3, config), config, sessionStateManager)

        assertEquals(0, sessionStateManager.state.value.repCount)
    }

    @Test
    fun multipleRepetitions_accumulatesTotalAndGoodRepCounts() {
        for (i in 1..5) {
            executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = i * 2000L)
        }

        val state = sessionStateManager.state.value
        assertEquals(5, state.repCount)
        assertEquals(5, state.goodRepCount)
        assertEquals(0, state.flaggedRepCount)
        assertEquals(5, state.completedReps.size)
    }

    @Test
    fun pauseResumeFlow_gatesCountingAndResumesAccurately() {
        // Rep 1 while active
        executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = 1000L)
        assertEquals(1, sessionStateManager.state.value.repCount)

        // Pause session
        sessionStateManager.pauseSession()
        assertTrue(sessionStateManager.state.value.isPaused)

        // Attempt rep while paused
        executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = 3000L)
        assertEquals(1, sessionStateManager.state.value.repCount)

        // Resume session
        sessionStateManager.resumeSession()

        // Rep 2 while resumed
        executeCycle(peakAnkleX = 0f, peakAnkleY = -10f, startMs = 5000L)
        assertEquals(2, sessionStateManager.state.value.repCount)
    }
}
