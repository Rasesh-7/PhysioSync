package com.example.physiosync.camera

/**
 * State representing the lifecycle and availability of the device camera.
 */
sealed interface CameraState {
    data object Uninitialized : CameraState
    data object Initializing : CameraState
    data object Ready : CameraState
    data object PermissionDenied : CameraState
    data class Error(val throwable: Throwable) : CameraState
}
