package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RepetitionCounterTest {

    private lateinit var counter: RepetitionCounter
    private lateinit var stateManager: SessionStateManager
    private val config = ExerciseConfig(minKeypointConfidence = 0.5f, minTempoSeconds = 0.0f)

    @Before
    fun setUp() {
        counter = RepetitionCounter(config)
        stateManager = SessionStateManager()
        stateManager.startSession()
    }

    private fun createPoseFrame(angleDegrees: Float): PoseFrame {
        // Create 3 points at origin (0,0) forming requested angle at Knee (0,0)
        // Knee = (0,0), Hip = (0, 100)
        val knee = BodyLandmark(LandmarkType.LEFT_KNEE, Point3D(0f, 0f), 0.9f)
        val hip = BodyLandmark(LandmarkType.LEFT_HIP, Point3D(0f, 100f), 0.9f)

        // Calculate ankle point for given angle relative to vertical Hip
        val rad = angleDegrees * (Math.PI / 180.0)
        val ankleX = (100.0 * Math.sin(rad)).toFloat()
        val ankleY = (100.0 * Math.cos(rad)).toFloat()
        val ankle = BodyLandmark(LandmarkType.LEFT_ANKLE, Point3D(ankleX, ankleY), 0.9f)

        return PoseFrame(
            mapOf(
                LandmarkType.LEFT_HIP to hip,
                LandmarkType.LEFT_KNEE to knee,
                LandmarkType.LEFT_ANKLE to ankle
            )
        )
    }

    @Test
    fun `incomplete movement cycle does not increment rep count`() {
        // WAITING (90°) -> EXTENDING (130°)
        counter.processFrame(createPoseFrame(90f), config, stateManager)
        counter.processFrame(createPoseFrame(130f), config, stateManager)

        assertEquals(0, stateManager.state.value.repCount)
    }

    @Test
    fun `full movement cycle increments rep count and updates SessionState`() {
        // Step 1: WAITING (90°)
        counter.processFrame(createPoseFrame(90f), config, stateManager)

        // Step 2: EXTENDING (125°)
        counter.processFrame(createPoseFrame(125f), config, stateManager)

        // Step 3: PEAK (165°)
        counter.processFrame(createPoseFrame(165f), config, stateManager)

        // Step 4: RETURNING (140°)
        counter.processFrame(createPoseFrame(140f), config, stateManager)

        // Step 5: Complete cycle back to rest (90°) -> WAITING
        counter.processFrame(createPoseFrame(90f), config, stateManager)

        val state = stateManager.state.value
        assertEquals(1, state.repCount)
        assertEquals(1, state.goodRepCount)
        assertEquals(0, state.flaggedRepCount)
        assertEquals(1, state.completedReps.size)
        assertEquals(165f, state.completedReps[0].peakAngle, 1.0f)
    }

    @Test
    fun `multiple repetitions increment counter accurately`() {
        fun simulateRep(peakAngle: Float) {
            counter.processFrame(createPoseFrame(90f), config, stateManager)
            counter.processFrame(createPoseFrame(125f), config, stateManager)
            counter.processFrame(createPoseFrame(peakAngle), config, stateManager)
            counter.processFrame(createPoseFrame(140f), config, stateManager)
            counter.processFrame(createPoseFrame(90f), config, stateManager)
        }

        simulateRep(165f) // Rep 1: Good
        simulateRep(168f) // Rep 2: Good

        val state = stateManager.state.value
        assertEquals(2, state.repCount)
        assertEquals(2, state.goodRepCount)
    }
}
