package com.example.physiosync.core.model

/**
 * Sealed hierarchy representing explicit error states in PhysioSync.
 * Prevents silent failures or invalid pose analysis.
 */
sealed class AppErrorState {
    object None : AppErrorState()

    object CameraPermissionDenied : AppErrorState() {
        val userActionMessage: String = "Camera permission required for exercise pose analysis. Please grant permission in settings."
    }

    data class CameraUnavailable(
        val reason: String = "Camera hardware unavailable or in use by another application."
    ) : AppErrorState()

    object NoPersonDetected : AppErrorState() {
        val guidanceMessage: String = "No person detected in frame. Please stand or sit in view of the camera."
    }

    data class PoorLightingOrLowConfidence(
        val confidence: Float = 0f
    ) : AppErrorState() {
        val guidanceMessage: String = "Low pose tracking confidence. Ensure good lighting and a clear side view."
    }

    object OfficeKitUnavailable : AppErrorState() {
        val fallbackMessage: String = "Office Kit screen mirroring unavailable. App continuing in standalone phone mode."
    }
}
