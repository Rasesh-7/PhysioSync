package com.example.physiosync.performance

import java.util.ArrayDeque

/**
 * High-efficiency, thread-safe performance tracker for PhysioSync.
 * Measures real-time pose inference latency (ms) and frame processing rate (FPS).
 */
class PerformanceTracker(
    private val maxLatencyHistorySize: Int = 30
) {
    private val latencyHistory = ArrayDeque<Long>()
    private val frameTimestamps = ArrayDeque<Long>()

    var lastInferenceLatencyMs: Long = 0L
        private set

    var currentFps: Float = 0.0f
        private set

    val averageInferenceLatencyMs: Float
        get() = synchronized(this) {
            if (latencyHistory.isEmpty()) 0.0f
            else latencyHistory.average().toFloat()
        }

    /**
     * Records a completed frame analysis latency and updates rolling FPS calculations.
     */
    fun recordFrame(latencyMs: Long, timestampMs: Long = System.currentTimeMillis()) {
        synchronized(this) {
            lastInferenceLatencyMs = latencyMs

            // Track latency moving average
            latencyHistory.addLast(latencyMs)
            if (latencyHistory.size > maxLatencyHistorySize) {
                latencyHistory.removeFirst()
            }

            // Track FPS over rolling 1000ms window
            frameTimestamps.addLast(timestampMs)
            val windowStart = timestampMs - 1000L
            while (frameTimestamps.isNotEmpty() && frameTimestamps.first < windowStart) {
                frameTimestamps.removeFirst()
            }

            val windowDurationSec = 1.0f
            currentFps = frameTimestamps.size / windowDurationSec
        }
    }

    /**
     * Resets tracking metrics.
     */
    fun reset() {
        synchronized(this) {
            latencyHistory.clear()
            frameTimestamps.clear()
            lastInferenceLatencyMs = 0L
            currentFps = 0.0f
        }
    }
}
