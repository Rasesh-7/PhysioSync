package com.example.physiosync

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.physiosync.camera.CameraManager
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.PoseFrame
import com.example.physiosync.pose.KeypointFilter
import com.example.physiosync.pose.PoseDetectorManager
import com.example.physiosync.ui.camera.CameraPermissionHandler
import com.example.physiosync.ui.camera.CameraPreview
import com.example.physiosync.ui.camera.SkeletonOverlay
import com.example.physiosync.ui.theme.PhysioSyncTheme

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var poseDetectorManager: PoseDetectorManager
    private val exerciseConfig = ExerciseConfig()
    private val keypointFilter = KeypointFilter(exerciseConfig)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraManager = CameraManager()
        poseDetectorManager = PoseDetectorManager()

        setContent {
            PhysioSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CameraPermissionHandler {
                            var currentPoseFrame by remember { mutableStateOf<PoseFrame?>(null) }
                            var frameWidth by remember { mutableStateOf(480f) }
                            var frameHeight by remember { mutableStateOf(640f) }

                            Box(modifier = Modifier.fillMaxSize()) {
                                CameraPreview(
                                    cameraManager = cameraManager,
                                    onFrameAnalyzed = { imageProxy ->
                                        // ImageProxy height & width in rotated orientation
                                        val isRotated = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
                                        frameWidth = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
                                        frameHeight = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()

                                        poseDetectorManager.processImageProxy(imageProxy) { rawFrame ->
                                            val smoothedFrame = keypointFilter.filter(rawFrame, exerciseConfig)
                                            currentPoseFrame = smoothedFrame
                                            if (smoothedFrame != null) {
                                                Log.d("PhysioSyncPose", "Smoothed pose with ${smoothedFrame.landmarks.size} valid keypoints")
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
    }
}