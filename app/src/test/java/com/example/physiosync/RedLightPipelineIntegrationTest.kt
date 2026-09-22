package com.example.physiosync

import com.example.physiosync.analysis.RepetitionCounter
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.state.SessionStateManager
import com.example.physiosync.pose.KeypointFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-End Integration Test for Task 14 — Red Light Integration (Phone-Only Pipeline).
 * Verifies that Keypoint Filtering, Knee Angle Calculation, Exercise State Machine,
 * Repetition Counting, Form Classification, and Session State Manager work in complete harmony.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RedLightPipelineIntegrationTest {

    private lateinit var config: ExerciseConfig
    private lateinit var keypointFilter: KeypointFilter
    private lateinit var repetitionCounter: RepetitionCounter
    private lateinit var sessionStateManager: SessionStateManager

    @Before
    fun setUp() {
        config = ExerciseConfig(
            minKeypointConfidence = 0.5f,
            smoothingAlpha = 1.0f, // 1.0f for instantaneous synthetic frame resolution in unit tests
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

    @Test
    fun redLightPipeline_fullRepetitionCycle_updatesStateAndEmitsEventsCorrectly() = runTest {
        val emittedEvents = mutableListOf<SessionEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            sessionStateManager.events.collect { emittedEvents.add(it) }
        }

        // 1. Initial State: Frame 1 (Resting / WAITING, 90 deg)
        val frame1 = createPoseFrame(1000L, 0f, 10f, 0f, 0f, 10f, 0f)
        val frame1Filtered = keypointFilter.filter(frame1, config)
        val result1 = repetitionCounter.processFrame(frame1Filtered, config, sessionStateManager)

        assertEquals(ExerciseState.WAITING, result1.transition.currentState)
        assertEquals(0, sessionStateManager.state.value.repCount)

        // 2. Extending State: Frame 2 (Extending, 135 deg)
        val frame2 = createPoseFrame(1500L, 0f, 10f, 0f, 0f, 10f, -10f)
        val frame2Filtered = keypointFilter.filter(frame2, config)
        val result2 = repetitionCounter.processFrame(frame2Filtered, config, sessionStateManager)

        assertEquals(ExerciseState.EXTENDING, result2.transition.currentState)

        // 3. Peak Extension State: Frame 3 (Peak, 180 deg straight extension)
        val frame3 = createPoseFrame(2000L, 0f, 10f, 0f, 0f, 0f, -10f)
        val frame3Filtered = keypointFilter.filter(frame3, config)
        val result3 = repetitionCounter.processFrame(frame3Filtered, config, sessionStateManager)

        assertEquals(ExerciseState.PEAK, result3.transition.currentState)

        // 4. Returning State: Frame 4 (Knee returning downwards, 135 deg)
        val frame4 = createPoseFrame(2500L, 0f, 10f, 0f, 0f, 10f, -10f)
        val frame4Filtered = keypointFilter.filter(frame4, config)
        val result4 = repetitionCounter.processFrame(frame4Filtered, config, sessionStateManager)

        assertEquals(ExerciseState.RETURNING, result4.transition.currentState)

        // 5. Rest & Cycle Completion: Frame 5 (Returned to resting position, 90 deg)
        val frame5 = createPoseFrame(3000L, 0f, 10f, 0f, 0f, 10f, 0f)
        val frame5Filtered = keypointFilter.filter(frame5, config)
        val result5 = repetitionCounter.processFrame(frame5Filtered, config, sessionStateManager)

        assertEquals(ExerciseState.WAITING, result5.transition.currentState)
        assertTrue(result5.transition.isCycleCompleted)

        // 6. Verify SessionStateManager updated correctly
        val state = sessionStateManager.state.value
        assertEquals(1, state.repCount)
        assertEquals(1, state.goodRepCount)
        assertEquals(0, state.flaggedRepCount)
        assertEquals(1, state.completedReps.size)
        assertEquals(FormFlag.GOOD, state.completedReps.first().formFlag)

        // 7. Verify Event Emission
        val repCompletedEvent = emittedEvents.filterIsInstance<SessionEvent.RepCompleted>().firstOrNull()
        assertNotNull(repCompletedEvent)
        assertEquals(1, repCompletedEvent?.repIndex)
        assertEquals(FormFlag.GOOD, repCompletedEvent?.formFlag)

        collectJob.cancel()
    }

    @Test
    fun redLightPipeline_pauseState_blocksRepetitionCounting() = runTest {
        // Pause the session
        sessionStateManager.pauseSession()
        assertTrue(sessionStateManager.state.value.isPaused)

        // Send a peak frame
        val framePeak = createPoseFrame(2000L, 0f, 10f, 0f, 0f, 0f, -10f)
        val result = repetitionCounter.processFrame(framePeak, config, sessionStateManager)

        // Rep count must remain 0 while paused
        assertEquals(0, sessionStateManager.state.value.repCount)
    }

    @Test
    fun redLightPipeline_endSession_finalizesDurationAndMarksCompleted() {
        sessionStateManager.endSession()

        val state = sessionStateManager.state.value
        assertEquals(false, state.isSessionActive)
        assertEquals(true, state.isCompleted)
        assertTrue(state.sessionDurationMs >= 0L)
    }
}
