package com.example.physiosync.pose

import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PoseMapperTest {

    @Test
    fun `PoseFrame correctly exposes landmark by type`() {
        val hipLandmark = BodyLandmark(
            type = LandmarkType.LEFT_HIP,
            position = Point3D(100f, 200f, 0f),
            confidence = 0.95f
        )
        val kneeLandmark = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(120f, 350f, 0f),
            confidence = 0.92f
        )
        val frame = PoseFrame(
            landmarks = mapOf(
                LandmarkType.LEFT_HIP to hipLandmark,
                LandmarkType.LEFT_KNEE to kneeLandmark
            )
        )

        val retrievedHip = frame.getLandmark(LandmarkType.LEFT_HIP)
        assertNotNull(retrievedHip)
        assertEquals(0.95f, retrievedHip?.confidence ?: 0f, 0.001f)
        assertEquals(100f, retrievedHip?.position?.x ?: 0f, 0.001f)

        val retrievedAnkle = frame.getLandmark(LandmarkType.LEFT_ANKLE)
        assertNull(retrievedAnkle)
    }

    @Test
    fun `Point2D extraction from BodyLandmark matches 3D x and y`() {
        val landmark = BodyLandmark(
            type = LandmarkType.RIGHT_KNEE,
            position = Point3D(150.5f, 220.3f, 12.1f),
            confidence = 0.88f
        )

        val point2D = landmark.point2D
        assertEquals(150.5f, point2D.x, 0.001f)
        assertEquals(220.3f, point2D.y, 0.001f)
    }
}
