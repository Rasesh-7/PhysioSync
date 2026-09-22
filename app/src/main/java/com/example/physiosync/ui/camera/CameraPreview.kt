package com.example.physiosync.ui.camera

import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.physiosync.camera.CameraManager

/**
 * Jetpack Compose composable for displaying live CameraX preview and connecting frame analysis stream.
 */
@Composable
fun CameraPreview(
    cameraManager: CameraManager,
    modifier: Modifier = Modifier,
    onFrameAnalyzed: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, cameraManager.isFrontCamera) {
        cameraManager.bindCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onFrameAnalyzed = onFrameAnalyzed
        )

        onDispose {
            cameraManager.unbind()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
