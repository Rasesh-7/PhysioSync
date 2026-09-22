package com.example.physiosync.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying Task 19 — PerformanceTracker metrics calculation.
 */
class PerformanceTrackerTest {

    private lateinit var tracker: PerformanceTracker

    @Before
    fun setUp() {
        tracker = PerformanceTracker(maxLatencyHistorySize = 5)
    }

    @Test
    fun recordFrame_updatesLastAndAverageLatency() {
        tracker.recordFrame(20L, 1000L)
        tracker.recordFrame(40L, 1100L)

        assertEquals(40L, tracker.lastInferenceLatencyMs)
        assertEquals(30.0f, tracker.averageInferenceLatencyMs, 0.01f)
    }

    @Test
    fun recordFrame_calculatesRollingFpsAccurately() {
        val baseTime = 10000L
        // Record 10 frames within 1 second window
        for (i in 0 until 10) {
            tracker.recordFrame(15L, baseTime + (i * 90))
        }

        assertEquals(10.0f, tracker.currentFps, 0.01f)
    }

    @Test
    fun recordFrame_evictsOldSamplesFromRollingWindow() {
        val baseTime = 10000L
        // Record 10 old frames
        for (i in 0 until 10) {
            tracker.recordFrame(15L, baseTime + (i * 50)) // 10000 to 10450
        }

        // Record 5 new frames 2 seconds later
        val newTime = baseTime + 2000L
        for (i in 0 until 5) {
            tracker.recordFrame(15L, newTime + (i * 100))
        }

        // Old frames are evicted from the 1000ms rolling window
        assertEquals(5.0f, tracker.currentFps, 0.01f)
    }

    @Test
    fun reset_clearsMetrics() {
        tracker.recordFrame(30L, 1000L)
        tracker.reset()

        assertEquals(0L, tracker.lastInferenceLatencyMs)
        assertEquals(0.0f, tracker.averageInferenceLatencyMs, 0.01f)
        assertEquals(0.0f, tracker.currentFps, 0.01f)
    }
}
