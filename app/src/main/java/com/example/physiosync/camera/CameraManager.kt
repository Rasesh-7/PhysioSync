package com.example.physiosync.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Clean wrapper for CameraX lifecycle binding, preview rendering, and image analysis stream.
 * Uses `STRATEGY_KEEP_ONLY_LATEST` to guarantee low latency real-time frame processing.
 * Safely defaults to Front Camera with automatic fallback to Back Camera if unavailable.
 */
class CameraManager(
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) {
    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Uninitialized)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        private set

    val isFrontCamera: Boolean
        get() = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    fun bindCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onFrameAnalyzed: (ImageProxy) -> Unit
    ) {
        _cameraState.value = CameraState.Initializing

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                provider.unbindAll()

                // Check camera availability and fallback to back camera if front camera is absent
                val targetSelector = when {
                    provider.hasCamera(cameraSelector) -> cameraSelector
                    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    else -> cameraSelector
                }

                cameraSelector = targetSelector

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            onFrameAnalyzed(imageProxy)
                        }
                    }

                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                _cameraState.value = CameraState.Ready
                Log.d("CameraManager", "Camera bound successfully: isFront=$isFrontCamera")
            } catch (e: Exception) {
                Log.e("CameraManager", "Failed to bind camera with selector: $cameraSelector", e)
                // If front camera failed, retry binding with back camera
                if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                    try {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        bindCamera(context, lifecycleOwner, previewView, onFrameAnalyzed)
                        return@addListener
                    } catch (fallbackEx: Exception) {
                        Log.e("CameraManager", "Fallback to back camera failed", fallbackEx)
                    }
                }
                _cameraState.value = CameraState.Error(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun switchCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onFrameAnalyzed: (ImageProxy) -> Unit
    ) {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindCamera(context, lifecycleOwner, previewView, onFrameAnalyzed)
    }

    fun unbind() {
        try {
            cameraProvider?.unbindAll()
            _cameraState.value = CameraState.Uninitialized
        } catch (e: Exception) {
            _cameraState.value = CameraState.Error(e)
        }
    }

    fun shutdown() {
        unbind()
        if (!analysisExecutor.isShutdown) {
            analysisExecutor.shutdown()
        }
    }
}
