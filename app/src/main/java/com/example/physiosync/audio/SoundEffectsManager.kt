package com.example.physiosync.audio

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.state.SessionStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Audio Cue Manager for PhysioSync.
 * Plays crisp, subtle sound effects for repetition completion, peak extension, and session events.
 */
class SoundEffectsManager {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 75)
    } catch (e: Exception) {
        Log.e("SoundEffectsManager", "Failed to initialize ToneGenerator", e)
        null
    }

    private var eventsJob: Job? = null

    fun observeSessionEvents(sessionStateManager: SessionStateManager, scope: CoroutineScope) {
        eventsJob?.cancel()
        eventsJob = sessionStateManager.events
            .onEach { event ->
                when (event) {
                    is SessionEvent.RepCompleted -> {
                        if (event.formFlag == FormFlag.GOOD) {
                            playRepSuccessTone()
                        } else {
                            playWarningTone()
                        }
                    }
                    is SessionEvent.TargetGoalReached -> {
                        playTargetReachedFanfare()
                    }
                    is SessionEvent.SessionEnded -> {
                        playSessionCompleteTone()
                    }
                    else -> {}
                }
            }
            .launchIn(scope)
    }

    fun playRepSuccessTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (e: Exception) {
            Log.w("SoundEffectsManager", "Error playing rep tone", e)
        }
    }

    fun playTargetReachedFanfare() {
        try {
            // Distinct triumphant sound effect for goal completion
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 400)
        } catch (e: Exception) {
            Log.w("SoundEffectsManager", "Error playing target reached tone", e)
        }
    }

    fun playWarningTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
        } catch (e: Exception) {
            Log.w("SoundEffectsManager", "Error playing warning tone", e)
        }
    }

    fun playSessionCompleteTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 300)
        } catch (e: Exception) {
            Log.w("SoundEffectsManager", "Error playing session complete tone", e)
        }
    }

    fun release() {
        eventsJob?.cancel()
        toneGenerator?.release()
        toneGenerator = null
    }
}
