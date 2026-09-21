package com.example.physiosync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.physiosync.analysis.JointAngleResult
import com.example.physiosync.analysis.RepetitionCounter
import com.example.physiosync.camera.CameraManager
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.core.state.SessionStateManager
import com.example.physiosync.pose.KeypointFilter
import com.example.physiosync.pose.PoseDetectorManager
import com.example.physiosync.ui.camera.CameraPermissionHandler
import com.example.physiosync.ui.camera.CameraPreview
import com.example.physiosync.ui.camera.SkeletonOverlay
import com.example.physiosync.ui.theme.PhysioSyncTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var poseDetectorManager: PoseDetectorManager
    private val exerciseConfig = ExerciseConfig()
    private val keypointFilter = KeypointFilter(exerciseConfig)
    private val sessionStateManager = SessionStateManager()
    private val repetitionCounter = RepetitionCounter(exerciseConfig)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraManager = CameraManager()
        poseDetectorManager = PoseDetectorManager()
        sessionStateManager.startSession()

        setContent {
            PhysioSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CameraPermissionHandler {
                            var currentPoseFrame by remember { mutableStateOf<PoseFrame?>(null) }
                            var currentAngleResult by remember { mutableStateOf<JointAngleResult?>(null) }
                            var frameWidth by remember { mutableStateOf(480f) }
                            var frameHeight by remember { mutableStateOf(640f) }

                            val sessionState by sessionStateManager.state.collectAsState()

                            Box(modifier = Modifier.fillMaxSize()) {
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
                                                    "PhysioSyncReps",
                                                    String.format(
                                                        Locale.US,
                                                        "Reps: %d | Angle: %.1f° | State: %s",
                                                        sessionState.repCount,
                                                        repResult.angleResult.angleDegrees,
                                                        repResult.transition.currentState.name
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )

                                SkeletonOverlay(
                                    poseFrame = currentPoseFrame,
                                    imageWidth = frameWidth,
                                    imageHeight = frameHeight,
                                    minConfidence = exerciseConfig.minKeypointConfidence,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Live Repetition Counter & State Status Card
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 16.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(horizontal = 24.dp, vertical = 14.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "REPS: ${sessionState.repCount}",
                                            color = Color(0xFF76FF03),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val angleText = if (currentAngleResult != null) {
                                            val legLabel = if (currentAngleResult!!.isLeftLeg) "Left" else "Right"
                                            String.format(Locale.US, "Angle: %.1f° (%s)", currentAngleResult!!.angleDegrees, legLabel)
                                        } else {
                                            "Position in view..."
                                        }

                                        val stateColor = when (sessionState.currentState) {
                                            ExerciseState.WAITING -> Color(0xFFB0BEC5)
                                            ExerciseState.EXTENDING -> Color(0xFFFFD54F)
                                            ExerciseState.PEAK -> Color(0xFF76FF03)
                                            ExerciseState.RETURNING -> Color(0xFF00E5FF)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = angleText,
                                                color = Color(0xFF00E5FF),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = sessionState.currentState.name,
                                                color = stateColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.shutdown()
        poseDetectorManager.close()
        keypointFilter.reset()
        repetitionCounter.reset()
    }
}