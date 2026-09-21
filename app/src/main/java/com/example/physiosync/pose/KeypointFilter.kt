package com.example.physiosync.pose

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame

/**
 * Applies confidence thresholding and Exponential Moving Average (EMA) temporal smoothing
 * to raw pose keypoint streams, eliminating high-frequency camera/inference jitter.
 */
class KeypointFilter(
    private val defaultConfig: ExerciseConfig = ExerciseConfig()
) {
    private val previousPositions = mutableMapOf<LandmarkType, Point3D>()

    /**
     * Filters out low-confidence keypoints and smooths high-confidence keypoint trajectories.
     */
    fun filter(rawFrame: PoseFrame?, config: ExerciseConfig = defaultConfig): PoseFrame? {
        if (rawFrame == null || rawFrame.landmarks.isEmpty()) {
            return null
        }

        val alpha = config.smoothingAlpha
        val minConfidence = config.minKeypointConfidence
        val smoothedLandmarks = mutableMapOf<LandmarkType, BodyLandmark>()

        // Identify landmark types present in current frame
        val currentTypes = rawFrame.landmarks.keys

        // Clear cached history for landmarks missing in current frame
        val missingTypes = previousPositions.keys - currentTypes
        missingTypes.forEach { previousPositions.remove(it) }

        for ((type, landmark) in rawFrame.landmarks) {
            // Reject keypoints below confidence threshold
            if (landmark.confidence < minConfidence) {
                previousPositions.remove(type)
                continue
            }

            val rawPos = landmark.position
            val prevPos = previousPositions[type]

            val smoothedPos = if (prevPos != null) {
                Point3D(
                    x = alpha * rawPos.x + (1f - alpha) * prevPos.x,
                    y = alpha * rawPos.y + (1f - alpha) * prevPos.y,
                    z = alpha * rawPos.z + (1f - alpha) * prevPos.z
                )
            } else {
                rawPos
            }

            previousPositions[type] = smoothedPos
            smoothedLandmarks[type] = landmark.copy(position = smoothedPos)
        }

        return PoseFrame(
            landmarks = smoothedLandmarks,
            timestampMs = rawFrame.timestampMs
        )
    }

    /**
     * Resets internal temporal smoothing history.
     */
    fun reset() {
        previousPositions.clear()
    }
}
