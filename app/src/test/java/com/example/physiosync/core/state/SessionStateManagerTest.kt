package com.example.physiosync.core.state

import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionStateManagerTest {

    private lateinit var manager: SessionStateManager

    @Before
    fun setUp() {
        manager = SessionStateManager()
    }

    @Test
    fun `initial state is inactive and zeroed`() {
        val state = manager.state.value
        assertFalse(state.isSessionActive)
        assertFalse(state.isPaused)
        assertFalse(state.isCompleted)
        assertEquals(0, state.repCount)
        assertEquals(0, state.goodRepCount)
        assertEquals(0, state.flaggedRepCount)
        assertEquals(ExerciseState.WAITING, state.currentState)
    }

    @Test
    fun `startSession activates session and emits SessionStarted event`() = runTest {
        manager.startSession()
        val state = manager.state.value
        assertTrue(state.isSessionActive)
        assertFalse(state.isPaused)
        assertTrue(state.sessionStartTimeMs > 0)
    }

    @Test
    fun `updateAngleAndState updates current angle and exercise state`() {
        manager.startSession()
        manager.updateAngleAndState(165.0f, ExerciseState.PEAK)
        val state = manager.state.value
        assertEquals(165.0f, state.currentKneeAngle, 0.01f)
        assertEquals(ExerciseState.PEAK, state.currentState)
    }

    @Test
    fun `recordRepetition updates good and flagged counts correctly`() {
        manager.startSession()

        manager.recordRepetition(peakAngle = 168.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.5f)
        assertEquals(1, manager.state.value.repCount)
        assertEquals(1, manager.state.value.goodRepCount)
        assertEquals(0, manager.state.value.flaggedRepCount)

        manager.recordRepetition(peakAngle = 145.0f, formFlag = FormFlag.REDUCED_ROM, durationSeconds = 2.0f)
        assertEquals(2, manager.state.value.repCount)
        assertEquals(1, manager.state.value.goodRepCount)
        assertEquals(1, manager.state.value.flaggedRepCount)
        assertEquals(2, manager.state.value.completedReps.size)
    }

    @Test
    fun `pause and resume updates state correctly`() {
        manager.startSession()
        manager.pauseSession()
        assertTrue(manager.state.value.isPaused)

        // Updates while paused should be ignored
        manager.updateAngleAndState(170.0f, ExerciseState.PEAK)
        assertEquals(0.0f, manager.state.value.currentKneeAngle, 0.01f)

        manager.resumeSession()
        assertFalse(manager.state.value.isPaused)
    }

    @Test
    fun `endSession marks completed and stops session`() {
        manager.startSession()
        manager.recordRepetition(165.0f, FormFlag.GOOD, 2.0f)
        manager.endSession()

        val state = manager.state.value
        assertFalse(state.isSessionActive)
        assertTrue(state.isCompleted)
        assertEquals(1, state.repCount)
    }
}
