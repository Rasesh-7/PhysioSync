package com.example.physiosync.pose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.physiosync.core.model.PoseFrame
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

/**
 * Manages ML Kit PoseDetector execution over CameraX frames.
 * Ensures strict ImageProxy resource cleanup to avoid backpressure leaks.
 */
class PoseDetectorManager(
    private val detector: PoseDetector = createDefaultDetector()
) {
    companion object {
        fun createDefaultDetector(): PoseDetector {
            val options = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build()
            return PoseDetection.getClient(options)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onPoseDetected: (PoseFrame?) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            onPoseDetected(null)
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                if (pose != null && pose.allPoseLandmarks.isNotEmpty()) {
                    val frame = PoseMapper.toDomainPoseFrame(pose)
                    onPoseDetected(frame)
                } else {
                    onPoseDetected(null)
                }
            }
            .addOnFailureListener {
                onPoseDetected(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        detector.close()
    }
}
