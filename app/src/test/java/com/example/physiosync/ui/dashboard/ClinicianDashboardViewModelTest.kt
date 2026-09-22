package com.example.physiosync.ui.dashboard

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.RepetitionDetail
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.state.SessionStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClinicianDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var config: ExerciseConfig
    private lateinit var viewModel: ClinicianDashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionStateManager = SessionStateManager()
        config = ExerciseConfig(
            minRomAngle = 145.0f,
            peakTargetAngle = 165.0f
        )
        viewModel = ClinicianDashboardViewModel(
            sessionStateManager = sessionStateManager,
            exerciseConfig = config
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial UI state reflects configuration and empty session`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(145.0f, state.targetMinAngle, 0.01f)
        assertEquals(165.0f, state.targetMaxAngle, 0.01f)
        assertEquals(0, state.sessionState.repCount)
        assertEquals(0, state.goodRepPercentage)
        assertEquals(0.0f, state.averageRepDurationSeconds, 0.01f)
        assertTrue(state.eventLogs.isEmpty())
    }

    @Test
    fun `session state updates compute compliance percentage and average duration correctly`() = runTest {
        sessionStateManager.startSession()
        testDispatcher.scheduler.advanceUntilIdle()

        val rep1 = RepetitionDetail(
            repIndex = 1,
            peakAngle = 168.0f,
            formFlag = FormFlag.GOOD,
            durationSeconds = 2.0f
        )
        val rep2 = RepetitionDetail(
            repIndex = 2,
            peakAngle = 140.0f,
            formFlag = FormFlag.REDUCED_ROM,
            durationSeconds = 3.0f
        )

        sessionStateManager.recordRepetition(
            peakAngle = 168.0f,
            formFlag = FormFlag.GOOD,
            durationSeconds = 2.0f
        )
        sessionStateManager.recordRepetition(
            peakAngle = 140.0f,
            formFlag = FormFlag.REDUCED_ROM,
            durationSeconds = 3.0f
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.sessionState.repCount)
        assertEquals(1, state.sessionState.goodRepCount)
        assertEquals(1, state.sessionState.flaggedRepCount)
        assertEquals(50, state.goodRepPercentage) // 1 out of 2 = 50%
        assertEquals(2.5f, state.averageRepDurationSeconds, 0.01f) // (2.0 + 3.0)/2 = 2.5s
    }

    @Test
    fun `session events are received and formatted in clinician log feed`() = runTest {
        sessionStateManager.startSession()
        testDispatcher.scheduler.advanceUntilIdle()

        sessionStateManager.emitCoachingMessage("Extend your knee further")
        testDispatcher.scheduler.advanceUntilIdle()

        sessionStateManager.recordRepetition(
            peakAngle = 138.0f,
            formFlag = FormFlag.REDUCED_ROM,
            durationSeconds = 2.5f
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val logs = viewModel.uiState.value.eventLogs
        assertTrue("Expected log entries in feed", logs.isNotEmpty())

        val flagLog = logs.find { it.title.contains("REDUCED_ROM") }
        assertNotNull(flagLog)
        assertEquals(LogSeverity.ALERT, flagLog?.severity)

        val coachLog = logs.find { it.title.contains("Coaching") }
        assertNotNull(coachLog)
        assertEquals("\"Extend your knee further\"", coachLog?.detail)
    }

    @Test
    fun `pause, resume, and end controls update session state accordingly`() = runTest {
        sessionStateManager.startSession()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.sessionState.isSessionActive)

        viewModel.pauseSession()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.sessionState.isPaused)

        viewModel.resumeSession()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.sessionState.isPaused)

        viewModel.endSession()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.sessionState.isCompleted)
    }
}
