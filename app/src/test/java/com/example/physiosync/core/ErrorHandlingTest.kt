package com.example.physiosync.core

import com.example.physiosync.analysis.KneeAngleCalculator
import com.example.physiosync.analysis.RepetitionCounter
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.AppErrorState
import com.example.physiosync.core.model.BodyLandmark
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.LandmarkType
import com.example.physiosync.core.model.Point3D
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager
import com.example.physiosync.pose.KeypointFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Task 18 — Error Handling states and low confidence / no-person protection.
 */
class ErrorHandlingTest {

    private val config = ExerciseConfig(minKeypointConfidence = 0.5f)
    private val keypointFilter = KeypointFilter(config)
    private val repetitionCounter = RepetitionCounter(config)
    private val sessionStateManager = SessionStateManager()

    @Test
    fun appErrorState_properties_matchExpectedDiagnosticMessages() {
        val permissionDenied = AppErrorState.CameraPermissionDenied
        val cameraUnavailable = AppErrorState.CameraUnavailable("Sensor locked")
        val noPerson = AppErrorState.NoPersonDetected
        val lowConfidence = AppErrorState.PoorLightingOrLowConfidence(0.2f)
        val officeKitUnavailable = AppErrorState.OfficeKitUnavailable

        assertTrue(permissionDenied.userActionMessage.contains("Camera permission required"))
        assertEquals("Sensor locked", cameraUnavailable.reason)
        assertTrue(noPerson.guidanceMessage.contains("No person detected"))
        assertEquals(0.2f, lowConfidence.confidence)
        assertTrue(officeKitUnavailable.fallbackMessage.contains("standalone phone mode"))
    }

    @Test
    fun emptyFrame_returnsNullInFilterAndAngleCalculator() {
        val emptyFrame = PoseFrame(landmarks = emptyMap())

        val filtered = keypointFilter.filter(emptyFrame, config)
        assertNull(filtered)

        val angleResult = KneeAngleCalculator.calculateKneeAngle(emptyFrame, config)
        assertNull(angleResult)
    }

    @Test
    fun lowConfidenceFrame_isFilteredOut_doesNotIncrementReps() {
        sessionStateManager.startSession()

        // Low confidence landmarks (< 0.5f)
        val lowConfLandmarks = mapOf(
            LandmarkType.LEFT_HIP to BodyLandmark(LandmarkType.LEFT_HIP, Point3D(0f, 10f, 0f), 0.2f),
            LandmarkType.LEFT_KNEE to BodyLandmark(LandmarkType.LEFT_KNEE, Point3D(0f, 0f, 0f), 0.3f),
            LandmarkType.LEFT_ANKLE to BodyLandmark(LandmarkType.LEFT_ANKLE, Point3D(10f, 0f, 0f), 0.1f)
        )
        val lowConfFrame = PoseFrame(landmarks = lowConfLandmarks)

        val filtered = keypointFilter.filter(lowConfFrame, config)
        val angleResult = KneeAngleCalculator.calculateKneeAngle(filtered ?: lowConfFrame, config)

        assertNull(angleResult)

        val repResult = repetitionCounter.processFrame(filtered, config, sessionStateManager)
        assertEquals(0, sessionStateManager.state.value.repCount)
    }
}
