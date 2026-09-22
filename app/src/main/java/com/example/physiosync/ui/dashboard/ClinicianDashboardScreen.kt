package com.example.physiosync.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalConfiguration
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

/**
 * Full Clinician Dashboard / Coach View Screen (Task 13 & Task 16).
 * Features responsive adaptive layout for:
 * - Portrait view on phone
 * - Widescreen 2-column view optimized for Office Kit laptop screen mirroring (Green Light)
 */
@Composable
fun ClinicianDashboardScreen(
    viewModel: ClinicianDashboardViewModel,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState = uiState.sessionState
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClinicianDarkBg
    ) { innerPadding ->
        if (isLandscape) {
            // Widescreen 2-Column Layout (Optimized for Laptop Mirroring via Office Kit)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: Live Telemetry, Angle Gauge, State Pipeline
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClinicianHeader(
                        exerciseName = "Seated Knee Extension",
                        sessionState = sessionState,
                        isMirrored = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryMetricCard(
                            title = "Total Reps",
                            primaryValue = "${sessionState.repCount}",
                            secondaryText = "Target: 10",
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
                            title = "Flagged",
                            primaryValue = "${sessionState.flaggedRepCount}",
                            secondaryText = if (sessionState.flaggedRepCount > 0) "Review" else "Optimal",
                            accentColor = if (sessionState.flaggedRepCount > 0) ClinicianAlertRed else ClinicianGoodGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    KneeAngleTelemetryCard(
                        currentAngle = sessionState.currentKneeAngle,
                        targetMin = uiState.targetMinAngle,
                        targetMax = uiState.targetMaxAngle
                    )

                    ExerciseStatePipeline(
                        currentState = sessionState.currentState
                    )

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
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }

                // Right Column: Repetition Breakdown, Event Feed, and Action Bar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RepetitionBreakdownList(
                        completedReps = sessionState.completedReps,
                        listHeight = 110.dp
                    )

                    RealTimeEventLog(
                        eventLogs = uiState.eventLogs,
                        listHeight = 110.dp
                    )

                    if (!uiState.isPresentationMode) {
                        ClinicianControlBar(
                            isPaused = sessionState.isPaused,
                            isSessionActive = sessionState.isSessionActive,
                            isPresentationMode = uiState.isPresentationMode,
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
                            onToggleView = onToggleView,
                            onTogglePresentation = {
                                viewModel.togglePresentationMode()
                            }
                        )
                    }
                }
            }
        } else {
            // Portrait Layout (Standard Mobile Phone Display)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClinicianHeader(
                    exerciseName = "Seated Knee Extension",
                    sessionState = sessionState,
                    isMirrored = uiState.isMirroredView
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                KneeAngleTelemetryCard(
                    currentAngle = sessionState.currentKneeAngle,
                    targetMin = uiState.targetMinAngle,
                    targetMax = uiState.targetMaxAngle
                )

                ExerciseStatePipeline(
                    currentState = sessionState.currentState
                )

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

                RepetitionBreakdownList(
                    completedReps = sessionState.completedReps,
                    listHeight = 130.dp
                )

                RealTimeEventLog(
                    eventLogs = uiState.eventLogs,
                    listHeight = 130.dp
                )

                if (!uiState.isPresentationMode) {
                    ClinicianControlBar(
                        isPaused = sessionState.isPaused,
                        isSessionActive = sessionState.isSessionActive,
                        isPresentationMode = uiState.isPresentationMode,
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
                        onToggleView = onToggleView,
                        onTogglePresentation = {
                            viewModel.togglePresentationMode()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
