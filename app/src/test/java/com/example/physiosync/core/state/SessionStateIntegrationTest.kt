package com.example.physiosync.core.state

import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionStateIntegrationTest {

    private lateinit var manager: SessionStateManager

    @Before
    fun setUp() {
        manager = SessionStateManager()
    }

    @Test
    fun `full session lifecycle emits correct sequence of events`() = runTest {
        val emittedEvents = mutableListOf<SessionEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.events.toList(emittedEvents)
        }

        // 1. Start Session
        manager.startSession()
        assertTrue(emittedEvents.last() is SessionEvent.SessionStarted)

        // 2. Repetition Completed
        manager.recordRepetition(peakAngle = 168f, formFlag = FormFlag.GOOD, durationSeconds = 2.4f)
        assertTrue(emittedEvents.last() is SessionEvent.RepCompleted)

        // 3. Form Flagged Repetition
        manager.recordRepetition(peakAngle = 135f, formFlag = FormFlag.REDUCED_ROM, durationSeconds = 2.0f)
        assertTrue(emittedEvents.any { it is SessionEvent.FormFlagged })

        // 4. Pause Session
        manager.pauseSession()
        assertTrue(emittedEvents.last() is SessionEvent.SessionPaused)
        assertTrue(manager.state.value.isPaused)

        // 5. Resume Session
        manager.resumeSession()
        assertTrue(emittedEvents.last() is SessionEvent.SessionResumed)
        assertFalse(manager.state.value.isPaused)

        // 6. End Session
        manager.endSession()
        assertTrue(emittedEvents.last() is SessionEvent.SessionEnded)
        assertTrue(manager.state.value.isCompleted)

        collectJob.cancel()
    }

    @Test
    fun `session state summary correlates with completed reps`() {
        manager.startSession()
        manager.recordRepetition(165f, FormFlag.GOOD, 2.5f)
        manager.recordRepetition(135f, FormFlag.REDUCED_ROM, 2.0f)
        manager.recordRepetition(168f, FormFlag.GOOD, 2.8f)

        val state = manager.state.value
        assertEquals(3, state.repCount)
        assertEquals(2, state.goodRepCount)
        assertEquals(1, state.flaggedRepCount)
        assertEquals(3, state.completedReps.size)
    }
}
