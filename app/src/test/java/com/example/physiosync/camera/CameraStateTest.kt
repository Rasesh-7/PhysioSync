package com.example.physiosync.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraStateTest {

    @Test
    fun `initial state of CameraManager is Uninitialized`() {
        val manager = CameraManager()
        assertEquals(CameraState.Uninitialized, manager.cameraState.value)
    }

    @Test
    fun `camera state sealed class hierarchy behaves as expected`() {
        val errorState: CameraState = CameraState.Error(IllegalStateException("Camera mock error"))
        assertTrue(errorState is CameraState.Error)
        assertEquals("Camera mock error", (errorState as CameraState.Error).throwable.message)
    }
}
