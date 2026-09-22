package com.example.physiosync.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.ui.dashboard.components.ClinicianAccentCyan
import com.example.physiosync.ui.dashboard.components.ClinicianAlertRed
import com.example.physiosync.ui.dashboard.components.ClinicianControlBar
import com.example.physiosync.ui.dashboard.components.ClinicianDarkBg
import com.example.physiosync.ui.dashboard.components.ClinicianGoodGreen
import com.example.physiosync.ui.dashboard.components.ClinicianHeader
import com.example.physiosync.ui.dashboard.components.ClinicianWarningAmber
import com.example.physiosync.ui.dashboard.components.ExerciseStatePipeline
import com.example.physiosync.ui.dashboard.components.FormStatusBadge
import com.example.physiosync.ui.dashboard.components.KneeAngleTelemetryCard
import com.example.physiosync.ui.dashboard.components.RealTimeEventLog
import com.example.physiosync.ui.dashboard.components.RepetitionBreakdownList
import com.example.physiosync.ui.dashboard.components.TelemetryMetricCard
import java.util.Locale

/**
 * Full Clinician Dashboard / Coach View Screen (Task 13).
 * Provides live telemetry for clinician review and is designed for seamless
 * mirroring to laptop displays via Office Kit (Task 16).
 */
@Composable
fun ClinicianDashboardScreen(
    viewModel: ClinicianDashboardViewModel,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState = uiState.sessionState
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClinicianDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with Exercise Title and Live Status
            ClinicianHeader(
                exerciseName = "Seated Knee Extension",
                sessionState = sessionState
            )

            // Primary Telemetry Metric Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryMetricCard(
                    title = "Total Reps",
                    primaryValue = "${sessionState.repCount}",
                    secondaryText = "Goal: 10 reps",
                    accentColor = ClinicianAccentCyan,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricCard(
                    title = "Good Form",
                    primaryValue = "${sessionState.goodRepCount}",
                    secondaryText = "${uiState.goodRepPercentage}% compliance",
                    accentColor = ClinicianGoodGreen,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricCard(
                    title = "Flagged Reps",
                    primaryValue = "${sessionState.flaggedRepCount}",
                    secondaryText = if (sessionState.flaggedRepCount > 0) "Needs review" else "All good",
                    accentColor = if (sessionState.flaggedRepCount > 0) ClinicianAlertRed else ClinicianGoodGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Real-Time Knee Angle Telemetry Card
            KneeAngleTelemetryCard(
                currentAngle = sessionState.currentKneeAngle,
                targetMin = uiState.targetMinAngle,
                targetMax = uiState.targetMaxAngle
            )

            // State Machine Phase Progression
            ExerciseStatePipeline(
                currentState = sessionState.currentState
            )

            // Current Form Status Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormStatusBadge(formFlag = sessionState.currentForm)

                if (sessionState.latestCoachingMessage != null) {
                    Text(
                        text = "\"${sessionState.latestCoachingMessage}\"",
                        color = ClinicianAccentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Repetition Breakdown Table
            RepetitionBreakdownList(
                completedReps = sessionState.completedReps
            )

            // Real-Time Clinical Event Feed
            RealTimeEventLog(
                eventLogs = uiState.eventLogs
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Clinician Control Bar
            ClinicianControlBar(
                isPaused = sessionState.isPaused,
                isSessionActive = sessionState.isSessionActive,
                onPauseToggle = {
                    if (sessionState.isPaused) {
                        viewModel.resumeSession()
                    } else {
                        viewModel.pauseSession()
                    }
                },
                onEndSession = {
                    viewModel.endSession()
                },
                onRestart = {
                    viewModel.restartSession()
                },
                onToggleView = onToggleView
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
