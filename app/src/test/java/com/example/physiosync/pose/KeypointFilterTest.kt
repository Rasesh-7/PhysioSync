package com.example.physiosync.pose

import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class KeypointFilterTest {

    private lateinit var filter: KeypointFilter
    private val config = ExerciseConfig(minKeypointConfidence = 0.5f, smoothingAlpha = 0.3f)

    @Before
    fun setUp() {
        filter = KeypointFilter(config)
    }

    @Test
    fun `first frame passes raw position as initial smoothed position`() {
        val landmark = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(100f, 200f, 0f),
            confidence = 0.9f
        )
        val frame = PoseFrame(mapOf(LandmarkType.LEFT_KNEE to landmark))

        val filtered = filter.filter(frame, config)
        assertNotNull(filtered)

        val smoothedLandmark = filtered?.getLandmark(LandmarkType.LEFT_KNEE)
        assertEquals(100f, smoothedLandmark?.position?.x ?: 0f, 0.001f)
        assertEquals(200f, smoothedLandmark?.position?.y ?: 0f, 0.001f)
    }

    @Test
    fun `subsequent frame applies EMA smoothing formula correctly`() {
        val landmark1 = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(100f, 200f, 0f),
            confidence = 0.9f
        )
        val frame1 = PoseFrame(mapOf(LandmarkType.LEFT_KNEE to landmark1))
        filter.filter(frame1, config)

        // Frame 2 with raw x = 200f
        val landmark2 = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(200f, 300f, 0f),
            confidence = 0.9f
        )
        val frame2 = PoseFrame(mapOf(LandmarkType.LEFT_KNEE to landmark2))

        val filtered2 = filter.filter(frame2, config)
        val smoothed = filtered2?.getLandmark(LandmarkType.LEFT_KNEE)

        // Expected x = 0.3 * 200 + 0.7 * 100 = 60 + 70 = 130f
        assertEquals(130f, smoothed?.position?.x ?: 0f, 0.01f)
        // Expected y = 0.3 * 300 + 0.7 * 200 = 90 + 140 = 230f
        assertEquals(230f, smoothed?.position?.y ?: 0f, 0.01f)
    }

    @Test
    fun `low confidence landmark is rejected by filter`() {
        val lowConfLandmark = BodyLandmark(
            type = LandmarkType.LEFT_ANKLE,
            position = Point3D(100f, 200f, 0f),
            confidence = 0.2f // Below minConfidence 0.5
        )
        val frame = PoseFrame(mapOf(LandmarkType.LEFT_ANKLE to lowConfLandmark))

        val filtered = filter.filter(frame, config)
        assertNotNull(filtered)
        assertNull(filtered?.getLandmark(LandmarkType.LEFT_ANKLE))
    }

    @Test
    fun `reset clears previous position history`() {
        val landmark1 = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(100f, 200f, 0f),
            confidence = 0.9f
        )
        filter.filter(PoseFrame(mapOf(LandmarkType.LEFT_KNEE to landmark1)), config)

        filter.reset()

        // Frame after reset should initialize raw position without EMA blending
        val landmark2 = BodyLandmark(
            type = LandmarkType.LEFT_KNEE,
            position = Point3D(300f, 400f, 0f),
            confidence = 0.9f
        )
        val filtered = filter.filter(PoseFrame(mapOf(LandmarkType.LEFT_KNEE to landmark2)), config)
        val smoothed = filtered?.getLandmark(LandmarkType.LEFT_KNEE)

        assertEquals(300f, smoothed?.position?.x ?: 0f, 0.01f)
    }
}
