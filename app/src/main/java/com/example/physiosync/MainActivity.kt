package com.example.physiosync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.physiosync.analysis.JointAngleResult
import com.example.physiosync.analysis.RepetitionCounter
import com.example.physiosync.camera.CameraManager
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager
import com.example.physiosync.pose.KeypointFilter
import com.example.physiosync.pose.PoseDetectorManager
import com.example.physiosync.ui.camera.CameraPermissionHandler
import com.example.physiosync.ui.camera.CameraPreview
import com.example.physiosync.ui.camera.SkeletonOverlay
import com.example.physiosync.ui.dashboard.ClinicianDashboardScreen
import com.example.physiosync.ui.dashboard.ClinicianDashboardViewModel
import com.example.physiosync.ui.patient.FormStatus
import com.example.physiosync.ui.patient.PatientScreen
import com.example.physiosync.ui.patient.PatientSessionUiState
import com.example.physiosync.ui.patient.RepDetail
import com.example.physiosync.ui.patient.SessionReportData
import com.example.physiosync.ui.patient.SessionReportScreen
import com.example.physiosync.ui.theme.PhysioSyncTheme
import com.example.physiosync.voice.VoiceCoachManager
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var poseDetectorManager: PoseDetectorManager
    private lateinit var voiceCoachManager: VoiceCoachManager
    private val exerciseConfig = ExerciseConfig()
    private val keypointFilter = KeypointFilter(exerciseConfig)
    private val sessionStateManager = SessionStateManager()
    private val repetitionCounter = RepetitionCounter(exerciseConfig)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraManager = CameraManager()
        poseDetectorManager = PoseDetectorManager()
        voiceCoachManager = VoiceCoachManager(this)
        voiceCoachManager.observeSessionEvents(sessionStateManager, lifecycleScope)

        sessionStateManager.startSession()

        setContent {
            PhysioSyncTheme {
                var currentScreen by remember { mutableStateOf("PATIENT") } // PATIENT, CLINICIAN, REPORT
                val sessionState by sessionStateManager.state.collectAsState()
                val dashboardViewModel = remember {
                    ClinicianDashboardViewModel(
                        sessionStateManager = sessionStateManager,
                        exerciseConfig = exerciseConfig
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "PATIENT", "CLINICIAN" -> {
                                CameraPermissionHandler {
                                    var currentPoseFrame by remember { mutableStateOf<PoseFrame?>(null) }
                                    var currentAngleResult by remember { mutableStateOf<JointAngleResult?>(null) }
                                    var frameWidth by remember { mutableStateOf(480f) }
                                    var frameHeight by remember { mutableStateOf(640f) }

                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // Continuous camera capture and pose estimation pipeline (single source of truth)
                                        CameraPreview(
                                            cameraManager = cameraManager,
                                            onFrameAnalyzed = { imageProxy ->
                                                val isRotated = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
                                                frameWidth = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
                                                frameHeight = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()

                                                poseDetectorManager.processImageProxy(imageProxy) { rawFrame ->
                                                    val smoothedFrame = keypointFilter.filter(rawFrame, exerciseConfig)
                                                    currentPoseFrame = smoothedFrame

                                                    val repResult = repetitionCounter.processFrame(
                                                        poseFrame = smoothedFrame,
                                                        config = exerciseConfig,
                                                        sessionStateManager = sessionStateManager
                                                    )
                                                    currentAngleResult = repResult.angleResult

                                                    if (repResult.angleResult != null) {
                                                        Log.d(
                                                            "PhysioSyncSession",
                                                            String.format(
                                                                Locale.US,
                                                                "Reps: %d (Good: %d, Flagged: %d) | Form: %s | State: %s",
                                                                sessionState.repCount,
                                                                sessionState.goodRepCount,
                                                                sessionState.flaggedRepCount,
                                                                sessionState.currentForm.name,
                                                                repResult.transition.currentState.name
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        )

                                        if (currentScreen == "CLINICIAN") {
                                            // Clinician Dashboard / Coach View (Task 13 / Office Kit Mirroring Target)
                                            ClinicianDashboardScreen(
                                                viewModel = dashboardViewModel,
                                                onToggleView = { currentScreen = "PATIENT" },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            // Patient Screen with Skeleton Overlay
                                            val formStatus = when (sessionState.currentForm) {
                                                FormFlag.GOOD -> FormStatus.GOOD
                                                FormFlag.REDUCED_ROM -> FormStatus.REDUCED_ROM
                                                FormFlag.IRREGULAR_TEMPO -> FormStatus.IRREGULAR_TEMPO
                                                FormFlag.LOW_CONFIDENCE -> FormStatus.LOW_CONFIDENCE
                                            }

                                            val patientUiState = PatientSessionUiState(
                                                exerciseName = "Seated Knee Extension",
                                                currentAngle = sessionState.currentKneeAngle,
                                                targetAngle = exerciseConfig.peakTargetAngle,
                                                completedReps = sessionState.repCount,
                                                targetReps = 10,
                                                currentState = sessionState.currentState.name,
                                                formStatus = formStatus,
                                                feedbackMessage = (sessionState.latestCoachingMessage ?: "").ifEmpty {
                                                    if (currentAngleResult != null) "Form: ${sessionState.currentForm.description}"
                                                    else "Position side profile in camera view"
                                                },
                                                isPaused = sessionState.isPaused,
                                                isLowConfidence = sessionState.currentForm == FormFlag.LOW_CONFIDENCE
                                            )

                                            PatientScreen(
                                                sessionState = patientUiState,
                                                onPauseClicked = {
                                                    if (sessionState.isPaused) {
                                                        sessionStateManager.resumeSession()
                                                    } else {
                                                        sessionStateManager.pauseSession()
                                                    }
                                                },
                                                onEndSessionClicked = {
                                                    sessionStateManager.endSession()
                                                    currentScreen = "REPORT"
                                                },
                                                cameraContent = {
                                                    SkeletonOverlay(
                                                        poseFrame = currentPoseFrame,
                                                        imageWidth = frameWidth,
                                                        imageHeight = frameHeight,
                                                        minConfidence = exerciseConfig.minKeypointConfidence,
                                                        isFrontCamera = cameraManager.isFrontCamera,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Top Bar: Clinician Dashboard View Switcher Button
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(top = 16.dp, end = 16.dp)
                                            ) {
                                                Button(
                                                    onClick = { currentScreen = "CLINICIAN" },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF1E293B),
                                                        contentColor = Color(0xFF00E5FF)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "Clinician View",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "REPORT" -> {
                                val repDetails = sessionState.completedReps.map { detail ->
                                    val status = when (detail.formFlag) {
                                        FormFlag.GOOD -> FormStatus.GOOD
                                        FormFlag.REDUCED_ROM -> FormStatus.REDUCED_ROM
                                        FormFlag.IRREGULAR_TEMPO -> FormStatus.IRREGULAR_TEMPO
                                        FormFlag.LOW_CONFIDENCE -> FormStatus.LOW_CONFIDENCE
                                    }
                                    RepDetail(
                                        repNumber = detail.repIndex,
                                        maxAngle = detail.peakAngle,
                                        status = status,
                                        note = detail.formFlag.description
                                    )
                                }

                                val maxAngle = sessionState.completedReps.maxOfOrNull { it.peakAngle } ?: 0f
                                val avgAngle = if (sessionState.completedReps.isNotEmpty()) sessionState.completedReps.map { it.peakAngle }.average().toFloat() else 0f

                                SessionReportScreen(
                                    reportData = SessionReportData(
                                        exerciseName = "Seated Knee Extension",
                                        totalRepsCount = sessionState.repCount,
                                        goodRepsCount = sessionState.goodRepCount,
                                        flaggedRepsCount = sessionState.flaggedRepCount,
                                        maxKneeAngleAchieved = maxAngle,
                                        averageAngle = avgAngle,
                                        sessionDurationSeconds = (sessionState.sessionDurationMs / 1000).toInt(),
                                        repDetails = if (repDetails.isNotEmpty()) repDetails else listOf(
                                            RepDetail(1, 170f, FormStatus.GOOD, "Sample rep - excellent ROM")
                                        )
                                    ),
                                    onDoneClicked = {
                                        repetitionCounter.reset()
                                        keypointFilter.reset()
                                        sessionStateManager.startSession()
                                        currentScreen = "PATIENT"
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceCoachManager.isInitialized) {
            voiceCoachManager.shutdown()
        }
        cameraManager.shutdown()
        poseDetectorManager.close()
        keypointFilter.reset()
        repetitionCounter.reset()
    }
}