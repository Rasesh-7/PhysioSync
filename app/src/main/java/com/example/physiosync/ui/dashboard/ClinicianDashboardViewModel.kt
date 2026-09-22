package com.example.physiosync.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.physiosync.core.config.ExerciseConfig
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionEvent
import com.example.physiosync.core.model.SessionState
import com.example.physiosync.core.state.SessionStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Visual severity level for clinician event log entries.
 */
enum class LogSeverity {
    INFO,
    SUCCESS,
    WARNING,
    ALERT
}

/**
 * Formatted log entry presented in the clinician dashboard live feed.
 */
data class ClinicianLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampMs: Long,
    val timeFormatted: String,
    val title: String,
    val detail: String,
    val severity: LogSeverity
)

/**
 * Immutable UI State for the Clinician Dashboard.
 */
data class ClinicianDashboardUiState(
    val sessionState: SessionState = SessionState(),
    val eventLogs: List<ClinicianLogEntry> = emptyList(),
    val goodRepPercentage: Int = 0,
    val averageRepDurationSeconds: Float = 0.0f,
    val targetMinAngle: Float = 150.0f,
    val targetMaxAngle: Float = 180.0f,
    val isMirroredView: Boolean = false
)

/**
 * ViewModel powering the Clinician Dashboard / Coach View (Task 13).
 * Consumes the single source of truth SessionStateManager.
 */
class ClinicianDashboardViewModel(
    private val sessionStateManager: SessionStateManager,
    private val exerciseConfig: ExerciseConfig = ExerciseConfig()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClinicianDashboardUiState(
            targetMinAngle = exerciseConfig.minRomAngle,
            targetMaxAngle = exerciseConfig.peakTargetAngle
        )
    )
    val uiState: StateFlow<ClinicianDashboardUiState> = _uiState.asStateFlow()

    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        // Collect session state changes
        viewModelScope.launch {
            sessionStateManager.state.collect { sessionState ->
                val goodPercentage = if (sessionState.repCount > 0) {
                    ((sessionState.goodRepCount.toFloat() / sessionState.repCount.toFloat()) * 100).toInt()
                } else {
                    0
                }

                val avgDuration = if (sessionState.completedReps.isNotEmpty()) {
                    sessionState.completedReps.map { it.durationSeconds }.average().toFloat()
                } else {
                    0.0f
                }

                _uiState.update { current ->
                    current.copy(
                        sessionState = sessionState,
                        goodRepPercentage = goodPercentage,
                        averageRepDurationSeconds = avgDuration
                    )
                }
            }
        }

        // Collect session events into clinician event feed
        viewModelScope.launch {
            sessionStateManager.events.collect { event ->
                val entry = formatLogEntry(event)
                _uiState.update { current ->
                    current.copy(
                        eventLogs = (listOf(entry) + current.eventLogs).take(MAX_LOG_ENTRIES)
                    )
                }
            }
        }
    }

    private fun formatLogEntry(event: SessionEvent): ClinicianLogEntry {
        val timeString = timeFormatter.format(Date(getEventTimestamp(event)))
        return when (event) {
            is SessionEvent.SessionStarted -> ClinicianLogEntry(
                timestampMs = event.timestampMs,
                timeFormatted = timeString,
                title = "Session Started",
                detail = "Physiotherapy monitoring active",
                severity = LogSeverity.INFO
            )
            is SessionEvent.RepCompleted -> {
                val isGood = event.formFlag == FormFlag.GOOD
                ClinicianLogEntry(
                    timestampMs = event.timestampMs,
                    timeFormatted = timeString,
                    title = "Rep #${event.repIndex} Completed",
                    detail = String.format(
                        Locale.US,
                        "Peak: %.1f° | Duration: %.1fs | Form: %s",
                        event.peakAngle,
                        event.durationSeconds,
                        event.formFlag.name
                    ),
                    severity = if (isGood) LogSeverity.SUCCESS else LogSeverity.WARNING
                )
            }
            is SessionEvent.FormFlagged -> ClinicianLogEntry(
                timestampMs = event.timestampMs,
                timeFormatted = timeString,
                title = "Form Flag: ${event.formFlag.name}",
                detail = event.reason,
                severity = LogSeverity.ALERT
            )
            is SessionEvent.CoachingEvent -> ClinicianLogEntry(
                timestampMs = event.timestampMs,
                timeFormatted = timeString,
                title = "Voice Coaching Cue",
                detail = "\"${event.message}\"",
                severity = LogSeverity.INFO
            )
            is SessionEvent.SessionPaused -> ClinicianLogEntry(
                timestampMs = System.currentTimeMillis(),
                timeFormatted = timeString,
                title = "Session Paused",
                detail = "Movement tracking held",
                severity = LogSeverity.WARNING
            )
            is SessionEvent.SessionResumed -> ClinicianLogEntry(
                timestampMs = System.currentTimeMillis(),
                timeFormatted = timeString,
                title = "Session Resumed",
                detail = "Movement tracking active",
                severity = LogSeverity.INFO
            )
            is SessionEvent.SessionEnded -> ClinicianLogEntry(
                timestampMs = event.timestampMs,
                timeFormatted = timeString,
                title = "Session Concluded",
                detail = "Total: ${event.totalReps} (Good: ${event.goodReps}, Flagged: ${event.flaggedReps})",
                severity = LogSeverity.SUCCESS
            )
        }
    }

    private fun getEventTimestamp(event: SessionEvent): Long {
        return when (event) {
            is SessionEvent.SessionStarted -> event.timestampMs
            is SessionEvent.RepCompleted -> event.timestampMs
            is SessionEvent.FormFlagged -> event.timestampMs
            is SessionEvent.CoachingEvent -> event.timestampMs
            is SessionEvent.SessionPaused -> System.currentTimeMillis()
            is SessionEvent.SessionResumed -> System.currentTimeMillis()
            is SessionEvent.SessionEnded -> event.timestampMs
        }
    }

    fun pauseSession() {
        sessionStateManager.pauseSession()
    }

    fun resumeSession() {
        sessionStateManager.resumeSession()
    }

    fun endSession() {
        sessionStateManager.endSession()
    }

    fun restartSession() {
        sessionStateManager.startSession()
    }

    companion object {
        private const val MAX_LOG_ENTRIES = 50
    }
}
