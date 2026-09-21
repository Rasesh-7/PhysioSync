package com.example.physiosync.core.model

/**
 * Pure Kotlin representations of pose landmarks and 2D/3D points.
 * Decoupled from ML Kit or any framework classes.
 */
data class Point2D(
    val x: Float,
    val y: Float
)

data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

enum class LandmarkType {
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_SHOULER,
    RIGHT_SHOULER,
    LEFT_EAR,
    RIGHT_EAR
}

data class BodyLandmark(
    val type: LandmarkType,
    val position: Point3D,
    val confidence: Float
) {
    val point2D: Point2D
        get() = Point2D(position.x, position.y)
}

data class PoseFrame(
    val landmarks: Map<LandmarkType, BodyLandmark>,
    val timestampMs: Long = System.currentTimeMillis()
) {
    fun getLandmark(type: LandmarkType): BodyLandmark? = landmarks[type]
}
