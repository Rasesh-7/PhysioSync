package com.example.physiosync.voice

import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.state.SessionStateManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceCoachManagerTest {

    private lateinit var sessionStateManager: SessionStateManager

    @Before
    fun setUp() {
        sessionStateManager = SessionStateManager()
    }

    @Test
    fun sessionStateManager_emitsEvents_forVoiceCoachToObserve() = runTest(UnconfinedTestDispatcher()) {
        val emittedEvents = mutableListOf<SessionEvent>()
        val job = launch {
            sessionStateManager.events.collect { emittedEvents.add(it) }
        }

        sessionStateManager.startSession()
        sessionStateManager.recordRepetition(peakAngle = 172f, formFlag = FormFlag.GOOD, durationSeconds = 2.0f)
        sessionStateManager.pauseSession()

        assertTrue(emittedEvents.any { it is SessionEvent.SessionStarted })
        assertTrue(emittedEvents.any { it is SessionEvent.RepCompleted && it.formFlag == FormFlag.GOOD })
        assertTrue(emittedEvents.any { it is SessionEvent.SessionPaused })

        job.cancel()
    }
}
