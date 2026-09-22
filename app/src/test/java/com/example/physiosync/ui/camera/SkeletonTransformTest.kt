package com.example.physiosync.ui.camera

import com.example.physiosync.core.model.Point2D
import org.junit.Assert.assertEquals
import org.junit.Test

class SkeletonTransformTest {

    @Test
    fun `scalePoint scales coordinates proportionally`() {
        val point = Point2D(240f, 320f)
        val imageWidth = 480f
        val imageHeight = 640f
        val canvasWidth = 1080f
        val canvasHeight = 1440f

        val offset = SkeletonTransform.scalePoint(
            point = point,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )

        // Midpoint 240/480 -> 540 in 1080 canvas
        assertEquals(540f, offset.x, 0.1f)
        // Midpoint 320/640 -> 720 in 1440 canvas
        assertEquals(720f, offset.y, 0.1f)
    }

    @Test
    fun `scalePoint handles horizontal flipping for front camera`() {
        val point = Point2D(100f, 320f)
        val imageWidth = 480f
        val imageHeight = 640f
        val canvasWidth = 480f
        val canvasHeight = 640f

        val offsetFlipped = SkeletonTransform.scalePoint(
            point = point,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            isFlippedHorizontally = true
        )

        // Flipped x = 480 - 100 = 380
        assertEquals(380f, offsetFlipped.x, 0.1f)
        assertEquals(320f, offsetFlipped.y, 0.1f)
    }
}
