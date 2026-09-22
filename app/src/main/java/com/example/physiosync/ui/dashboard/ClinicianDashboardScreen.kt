package com.example.physiosync.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.ui.dashboard.components.CameraViewportPipCard
import com.example.physiosync.ui.dashboard.components.ClinicianAccentCyan
import com.example.physiosync.ui.dashboard.components.ClinicianAlertRed
import com.example.physiosync.ui.dashboard.components.ClinicianControlBar
import com.example.physiosync.ui.dashboard.components.ClinicianDarkBg
import com.example.physiosync.ui.dashboard.components.ClinicianGoodGreen
import com.example.physiosync.ui.dashboard.components.ClinicianHeader
import com.example.physiosync.ui.dashboard.components.ExerciseStatePipeline
import com.example.physiosync.ui.dashboard.components.FormStatusBadge
import com.example.physiosync.ui.dashboard.components.KneeAngleGaugeCard
import com.example.physiosync.ui.dashboard.components.RealTimeEventLog
import com.example.physiosync.ui.dashboard.components.RepetitionBreakdownList
import com.example.physiosync.ui.dashboard.components.TelemetryMetricCard

/**
 * Full Clinician Dashboard / Coach View Screen (Task 13).
 * Provides live telemetry for clinician review with PiP camera preview,
 * Arc Gauge knee angle telemetry, and screen-mirroring optimization (Task 16).
 */
@Composable
fun ClinicianDashboardScreen(
    viewModel: ClinicianDashboardViewModel,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier,
    cameraContent: @Composable (BoxScope.() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState = uiState.sessionState

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header with Exercise Title, Status, and Patient View Switcher
            ClinicianHeader(
                exerciseName = "Seated Knee Extension",
                sessionState = sessionState,
                onToggleView = onToggleView
            )

            // 2. Primary Top Row: Camera PiP + Arc Gauge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Camera & Skeleton PiP Window
                CameraViewportPipCard(
                    modifier = Modifier.weight(1.1f),
                    cameraContent = cameraContent
                )

                // Knee Angle Dynamic Arc Gauge
                KneeAngleGaugeCard(
                    currentAngle = sessionState.currentKneeAngle,
                    targetMin = uiState.targetMinAngle,
                    targetMax = uiState.targetMaxAngle,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3. Telemetry Metric Grid
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

            // 4. Biomechanical State Machine Pipeline
            ExerciseStatePipeline(
                currentState = sessionState.currentState
            )

            // 5. Current Form Status Banner
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

            // 6. Split Row: Rep Breakdown + Real-Time Event Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RepetitionBreakdownList(
                    completedReps = sessionState.completedReps,
                    modifier = Modifier.weight(1f)
                )

                RealTimeEventLog(
                    eventLogs = uiState.eventLogs,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 7. Session Control Bar
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
        }
    }
}
