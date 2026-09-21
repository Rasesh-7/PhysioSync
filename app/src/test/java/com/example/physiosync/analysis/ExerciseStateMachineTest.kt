package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.ExerciseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExerciseStateMachineTest {

    private lateinit var stateMachine: ExerciseStateMachine
    private val config = ExerciseConfig()

    @Before
    fun setUp() {
        stateMachine = ExerciseStateMachine(config)
    }

    @Test
    fun `initial state is WAITING`() {
        assertEquals(ExerciseState.WAITING, stateMachine.currentState)
    }

    @Test
    fun `full repetition cycle transitions correctly`() {
        // Step 1: At rest in WAITING state
        val r1 = stateMachine.update(90f, config)
        assertEquals(ExerciseState.WAITING, r1.currentState)
        assertFalse(r1.isCycleCompleted)

        // Step 2: Extend past extensionMinAngle (120°) -> EXTENDING
        val r2 = stateMachine.update(125f, config)
        assertEquals(ExerciseState.EXTENDING, r2.currentState)

        // Step 3: Extend past peakTargetAngle (160°) -> PEAK
        val r3 = stateMachine.update(168f, config)
        assertEquals(ExerciseState.PEAK, r3.currentState)
        assertEquals(168f, r3.peakAngle, 0.01f)

        // Step 4: Return down past hysteresis -> RETURNING
        val r4 = stateMachine.update(155f, config)
        assertEquals(ExerciseState.RETURNING, r4.currentState)

        // Step 5: Return to resting position (<= 105°) -> WAITING & Cycle Complete
        val r5 = stateMachine.update(95f, config)
        assertEquals(ExerciseState.WAITING, r5.currentState)
        assertTrue(r5.isCycleCompleted)
        assertEquals(168f, r5.peakAngle, 0.01f)
    }

    @Test
    fun `aborted extension returns to WAITING without completing cycle`() {
        // Start extension
        stateMachine.update(125f, config)
        assertEquals(ExerciseState.EXTENDING, stateMachine.currentState)

        // Drop back down to rest without reaching peak
        val result = stateMachine.update(95f, config)
        assertEquals(ExerciseState.WAITING, result.currentState)
        assertFalse(result.isCycleCompleted)
    }

    @Test
    fun `null angle holds current state`() {
        stateMachine.update(125f, config)
        assertEquals(ExerciseState.EXTENDING, stateMachine.currentState)

        val result = stateMachine.update(null, config)
        assertEquals(ExerciseState.EXTENDING, result.currentState)
        assertFalse(result.isCycleCompleted)
    }

    @Test
    fun `reset sets state back to WAITING`() {
        stateMachine.update(165f, config)
        stateMachine.reset()
        assertEquals(ExerciseState.WAITING, stateMachine.currentState)
        assertEquals(0f, stateMachine.peakAngle, 0.01f)
    }
}
