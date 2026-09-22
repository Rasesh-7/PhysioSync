package com.example.physiosync.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.PoseFrame

/**
 * Real-time Jetpack Compose overlay drawing joint points and connecting bones.
 */
@Composable
fun SkeletonOverlay(
    poseFrame: PoseFrame?,
    modifier: Modifier = Modifier,
    imageWidth: Float = 480f,
    imageHeight: Float = 640f,
    minConfidence: Float = 0.5f,
    isFrontCamera: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (poseFrame == null || poseFrame.landmarks.isEmpty()) return@Canvas

        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define bone connections (Start landmark type -> End landmark type, Bone Color)
        val boneConnections = listOf(
            // Torso & Shoulders
            Triple(LandmarkType.LEFT_SHOULER, LandmarkType.RIGHT_SHOULER, Color(0xFFD500F9)),
            Triple(LandmarkType.LEFT_HIP, LandmarkType.RIGHT_HIP, Color(0xFFD500F9)),
            Triple(LandmarkType.LEFT_SHOULER, LandmarkType.LEFT_HIP, Color(0xFF00E5FF)),
            Triple(LandmarkType.RIGHT_SHOULER, LandmarkType.RIGHT_HIP, Color(0xFF76FF03)),

            // Left Leg (Primary for knee extension when patient side-view left)
            Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, Color(0xFF00E5FF)),
            Triple(LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE, Color(0xFF00E5FF)),

            // Right Leg
            Triple(LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE, Color(0xFF76FF03)),
            Triple(LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE, Color(0xFF76FF03)),

            // Head references
            Triple(LandmarkType.LEFT_EAR, LandmarkType.LEFT_SHOULER, Color(0x8000E5FF)),
            Triple(LandmarkType.RIGHT_EAR, LandmarkType.RIGHT_SHOULER, Color(0x8076FF03))
        )

        // 1. Draw Bone Connection Lines
        for ((startType, endType, color) in boneConnections) {
            val startLandmark = poseFrame.getLandmark(startType)
            val endLandmark = poseFrame.getLandmark(endType)

            if (startLandmark != null && endLandmark != null &&
                startLandmark.confidence >= minConfidence && endLandmark.confidence >= minConfidence
            ) {
                val startOffset = SkeletonTransform.scalePoint(
                    point = startLandmark.point2D,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    isFlippedHorizontally = isFrontCamera
                )
                val endOffset = SkeletonTransform.scalePoint(
                    point = endLandmark.point2D,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    isFlippedHorizontally = isFrontCamera
                )

                drawLine(
                    color = color,
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2. Draw Joint Markers
        poseFrame.landmarks.values.forEach { landmark ->
            if (landmark.confidence >= minConfidence) {
                val offset = SkeletonTransform.scalePoint(
                    point = landmark.point2D,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight,
                    isFlippedHorizontally = isFrontCamera
                )

                val jointColor = when (landmark.type) {
                    LandmarkType.LEFT_KNEE, LandmarkType.RIGHT_KNEE -> Color(0xFFFF3D00) // Highlight knees in red/orange
                    LandmarkType.LEFT_HIP, LandmarkType.LEFT_ANKLE -> Color(0xFF00E5FF)
                    LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_ANKLE -> Color(0xFF76FF03)
                    else -> Color.White
                }

                // Inner circle
                drawCircle(
                    color = jointColor,
                    radius = 12f,
                    center = offset
                )
                // Outer ring
                drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = offset,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            }
        }
    }
}
