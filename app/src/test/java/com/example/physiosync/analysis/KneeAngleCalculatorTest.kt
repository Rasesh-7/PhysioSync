package com.example.physiosync.analysis

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point2D
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KneeAngleCalculatorTest {

    private val config = ExerciseConfig(minKeypointConfidence = 0.5f)

    @Test
    fun `computeAngleBetweenPoints calculates 90 degree right angle`() {
        val hip = Point2D(0f, 10f)
        val knee = Point2D(0f, 0f)
        val ankle = Point2D(10f, 0f)

        val angle = KneeAngleCalculator.computeAngleBetweenPoints(hip, knee, ankle)
        assertEquals(90f, angle, 0.01f)
    }

    @Test
    fun `computeAngleBetweenPoints calculates 180 degree straight angle`() {
        val hip = Point2D(0f, 10f)
        val knee = Point2D(0f, 0f)
        val ankle = Point2D(0f, -10f)

        val angle = KneeAngleCalculator.computeAngleBetweenPoints(hip, knee, ankle)
        assertEquals(180f, angle, 0.01f)
    }

    @Test
    fun `computeAngleBetweenPoints calculates 45 degree acute angle`() {
        val hip = Point2D(0f, 10f)
        val knee = Point2D(0f, 0f)
        val ankle = Point2D(10f, 10f)

        val angle = KneeAngleCalculator.computeAngleBetweenPoints(hip, knee, ankle)
        assertEquals(45f, angle, 0.01f)
    }

    @Test
    fun `computeAngleBetweenPoints calculates 135 degree obtuse angle`() {
        val hip = Point2D(0f, 10f)
        val knee = Point2D(0f, 0f)
        val ankle = Point2D(10f, -10f)

        val angle = KneeAngleCalculator.computeAngleBetweenPoints(hip, knee, ankle)
        assertEquals(135f, angle, 0.01f)
    }

    @Test
    fun `calculateKneeAngle rejects low confidence frames`() {
        val hip = BodyLandmark(LandmarkType.LEFT_HIP, Point3D(0f, 10f), 0.3f) // Low confidence
        val knee = BodyLandmark(LandmarkType.LEFT_KNEE, Point3D(0f, 0f), 0.9f)
        val ankle = BodyLandmark(LandmarkType.LEFT_ANKLE, Point3D(10f, 0f), 0.9f)

        val frame = PoseFrame(
            mapOf(
                LandmarkType.LEFT_HIP to hip,
                LandmarkType.LEFT_KNEE to knee,
                LandmarkType.LEFT_ANKLE to ankle
            )
        )

        val result = KneeAngleCalculator.calculateKneeAngle(frame, config)
        assertNull(result)
    }

    @Test
    fun `calculateKneeAngle selects side with higher average confidence`() {
        // Left leg with avg confidence 0.7
        val leftHip = BodyLandmark(LandmarkType.LEFT_HIP, Point3D(0f, 10f), 0.7f)
        val leftKnee = BodyLandmark(LandmarkType.LEFT_KNEE, Point3D(0f, 0f), 0.7f)
        val leftAnkle = BodyLandmark(LandmarkType.LEFT_ANKLE, Point3D(10f, 0f), 0.7f)

        // Right leg with avg confidence 0.95
        val rightHip = BodyLandmark(LandmarkType.RIGHT_HIP, Point3D(0f, 10f), 0.95f)
        val rightKnee = BodyLandmark(LandmarkType.RIGHT_KNEE, Point3D(0f, 0f), 0.95f)
        val rightAnkle = BodyLandmark(LandmarkType.RIGHT_ANKLE, Point3D(0f, -10f), 0.95f)

        val frame = PoseFrame(
            mapOf(
                LandmarkType.LEFT_HIP to leftHip,
                LandmarkType.LEFT_KNEE to leftKnee,
                LandmarkType.LEFT_ANKLE to leftAnkle,
                LandmarkType.RIGHT_HIP to rightHip,
                LandmarkType.RIGHT_KNEE to rightKnee,
                LandmarkType.RIGHT_ANKLE to rightAnkle
            )
        )

        val result = KneeAngleCalculator.calculateKneeAngle(frame, config)
        assertNotNull(result)
        // Should pick Right leg because 0.95 > 0.7
        assertTrue(!result!!.isLeftLeg)
        assertEquals(180f, result.angleDegrees, 0.01f)
    }
}
