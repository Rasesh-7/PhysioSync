package com.example.physiosync.ui.camera

import androidx.compose.ui.geometry.Offset
import com.example.physiosync.core.model.Point2D

/**
 * Coordinate transform utility scaling raw image frame coordinates into Canvas screen space.
 * Accounts for aspect ratio scaling (CenterCrop / Fill) so keypoints overlay accurately over body image.
 */
object SkeletonTransform {

    fun scalePoint(
        point: Point2D,
        imageWidth: Float,
        imageHeight: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isFlippedHorizontally: Boolean = false
    ): Offset {
        if (imageWidth <= 0f || imageHeight <= 0f || canvasWidth <= 0f || canvasHeight <= 0f) {
            return Offset(point.x, point.y)
        }

        // Calculate scale factor for aspect fill (center crop)
        val scaleX = canvasWidth / imageWidth
        val scaleY = canvasHeight / imageHeight
        val scale = maxOf(scaleX, scaleY)

        val offsetX = (canvasWidth - imageWidth * scale) / 2f
        val offsetY = (canvasHeight - imageHeight * scale) / 2f

        var x = point.x * scale + offsetX
        val y = point.y * scale + offsetY

        if (isFlippedHorizontally) {
            x = canvasWidth - x
        }

        return Offset(x, y)
    }
}
