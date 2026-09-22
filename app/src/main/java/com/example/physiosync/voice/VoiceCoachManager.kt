package com.example.physiosync.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.state.SessionStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Locale

/**
 * Task 11: Voice / TTS Coaching Manager.
 * Event-driven, non-spammy Text-To-Speech coaching module.
 * Listens to `SessionStateManager.events` and provides timely spoken feedback.
 */
class VoiceCoachManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var lastSpokenTimeMs = 0L
    private val debounceDelayMs = 2500L
    private var eventsJob: Job? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceCoachManager", "US English Language is not supported for TTS")
            } else {
                isInitialized = true
                Log.d("VoiceCoachManager", "TTS Initialized successfully")
            }
        } else {
            Log.e("VoiceCoachManager", "TTS Initialization failed")
        }
    }

    fun observeSessionEvents(sessionStateManager: SessionStateManager, scope: CoroutineScope) {
        eventsJob?.cancel()
        eventsJob = sessionStateManager.events
            .onEach { event -> handleSessionEvent(event) }
            .launchIn(scope)
    }

    private fun handleSessionEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.SessionStarted -> speak("Session started. Position side profile in camera view.", force = true)
            is SessionEvent.SessionPaused -> speak("Session paused.", force = true)
            is SessionEvent.SessionResumed -> speak("Session resumed.", force = true)
            is SessionEvent.TargetGoalReached -> speak("Goal achieved! Target repetitions completed.", force = true)
            is SessionEvent.SessionEnded -> speak("Session completed. Great work!", force = true)
            is SessionEvent.RepCompleted -> {
                val message = when (event.formFlag) {
                    FormFlag.GOOD -> "Good repetition."
                    FormFlag.REDUCED_ROM -> "Extend your knee further."
                    FormFlag.IRREGULAR_TEMPO -> "Maintain steady tempo."
                    FormFlag.LOW_CONFIDENCE -> "Please reposition in camera view."
                }
                speak(message)
            }
            is SessionEvent.FormFlagged -> {
                val message = when (event.formFlag) {
                    FormFlag.REDUCED_ROM -> "Try to reach full extension."
                    FormFlag.IRREGULAR_TEMPO -> "Slow down your movement."
                    FormFlag.LOW_CONFIDENCE -> "Adjust your position."
                    FormFlag.GOOD -> return
                }
                speak(message)
            }
            is SessionEvent.CoachingEvent -> speak(event.message)
            else -> {}
        }
    }

    fun speak(text: String, force: Boolean = false) {
        if (!isInitialized || text.isBlank()) return

        val now = System.currentTimeMillis()
        if (!force && (now - lastSpokenTimeMs < debounceDelayMs)) {
            Log.d("VoiceCoachManager", "Debounced coaching speech: $text")
            return
        }

        lastSpokenTimeMs = now
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PhysioSync_TTS_$now")
    }

    fun shutdown() {
        eventsJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
