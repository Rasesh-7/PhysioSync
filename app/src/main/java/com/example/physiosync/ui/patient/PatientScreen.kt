package com.example.physiosync.ui.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.ui.theme.*

/**
 * Shared state data holding patient session progress.
 * Consumed by Patient UI without duplicating AI logic.
 */
data class PatientSessionUiState(
    val exerciseName: String = "Seated Knee Extension",
    val currentAngle: Float = 90f,
    val targetAngle: Float = 170f,
    val completedReps: Int = 0,
    val targetReps: Int = 10,
    val currentState: String = "WAITING", // WAITING, EXTENDING, PEAK, RETURNING
    val formStatus: FormStatus = FormStatus.GOOD,
    val feedbackMessage: String = "Align your side to camera & begin extending knee",
    val isPaused: Boolean = false,
    val isCameraReady: Boolean = true,
    val isLowConfidence: Boolean = false
)

enum class FormStatus {
    GOOD, REDUCED_ROM, IRREGULAR_TEMPO, LOW_CONFIDENCE
}

@Composable
fun PatientScreen(
    sessionState: PatientSessionUiState,
    onPauseClicked: () -> Unit,
    onEndSessionClicked: () -> Unit,
    modifier: Modifier = Modifier,
    cameraContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar / Header
            PatientHeaderSection(
                exerciseName = sessionState.exerciseName,
                isPaused = sessionState.isPaused,
                isLowConfidence = sessionState.isLowConfidence
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Main Live Camera / Skeleton Preview Viewport
            CameraViewportSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                sessionState = sessionState,
                cameraContent = cameraContent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Real-time Coaching & Form Card
            CoachingCard(
                feedbackMessage = sessionState.feedbackMessage,
                formStatus = sessionState.formStatus,
                currentState = sessionState.currentState
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Session Controls (Pause / End Session)
            SessionControlsSection(
                isPaused = sessionState.isPaused,
                onPauseClicked = onPauseClicked,
                onEndSessionClicked = onEndSessionClicked
            )
        }
    }
}

@Composable
fun PatientHeaderSection(
    exerciseName: String,
    isPaused: Boolean,
    isLowConfidence: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PHYSIOSYNC REHAB",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Live status badge
            val badgeColor = when {
                isPaused -> StatusWarning
                isLowConfidence -> StatusWarning
                else -> StatusSuccess
            }
            val statusText = when {
                isPaused -> "Paused"
                isLowConfidence -> "Low Tracking"
                else -> "Active"
            }

            Surface(
                shape = CircleShape,
                color = badgeColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
            }
        }
    }
}

@Composable
fun CameraViewportSection(
    sessionState: PatientSessionUiState,
    modifier: Modifier = Modifier,
    cameraContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Camera Preview + Skeleton Overlay Content
        cameraContent?.invoke(this)

        // Overlay metric cards over camera feed
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top overlay metrics: Angle and Reps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Angle Metric Pill
                MetricOverlayBadge(
                    label = "Knee Angle",
                    value = "${sessionState.currentAngle.toInt()}°",
                    subValue = "Target: ${sessionState.targetAngle.toInt()}°",
                    accentColor = MaterialTheme.colorScheme.primary
                )

                // Rep Count Badge
                MetricOverlayBadge(
                    label = "Reps Count",
                    value = "${sessionState.completedReps}/${sessionState.targetReps}",
                    subValue = "Completed",
                    accentColor = StatusSuccess
                )
            }

            // Center placeholder indicator when paused or low tracking confidence
            if (sessionState.isLowConfidence || sessionState.isPaused) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (sessionState.isPaused) Icons.Default.Pause else Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (sessionState.isPaused) "Session Paused" else "Position patient side profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom Overlay: Movement Stage Progress Indicator
            MovementStateBar(currentState = sessionState.currentState)
        }
    }
}

@Composable
fun MetricOverlayBadge(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NeutralDarkSurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun MovementStateBar(currentState: String) {
    val stages = listOf("WAITING", "EXTENDING", "PEAK", "RETURNING")

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NeutralDarkSurface.copy(alpha = 0.85f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEach { stage ->
                val isActive = stage.equals(currentState, ignoreCase = true)
                val activeColor = when (stage) {
                    "PEAK" -> StatusSuccess
                    "EXTENDING" -> MaterialTheme.colorScheme.primary
                    "RETURNING" -> CoralAccent
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) activeColor else Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stage,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal
                        ),
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun CoachingCard(
    feedbackMessage: String,
    formStatus: FormStatus,
    currentState: String
) {
    val (statusColor, icon) = when (formStatus) {
        FormStatus.GOOD -> Pair(StatusSuccess, Icons.Default.CheckCircle)
        FormStatus.REDUCED_ROM -> Pair(StatusWarning, Icons.Default.Info)
        FormStatus.IRREGULAR_TEMPO -> Pair(CoralAccent, Icons.Default.Warning)
        FormStatus.LOW_CONFIDENCE -> Pair(StatusError, Icons.Default.Warning)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.12f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = statusColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Voice Coach",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI COACH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feedbackMessage,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SessionControlsSection(
    isPaused: Boolean,
    onPauseClicked: () -> Unit,
    onEndSessionClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pause / Resume Button
        Button(
            onClick = onPauseClicked,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume Session" else "Pause Session"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isPaused) "Resume" else "Pause",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // End Session Button
        OutlinedButton(
            onClick = onEndSessionClicked,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = StatusError
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusError.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "End Session",
                tint = StatusError
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "End Session",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StatusError
            )
        }
    }
}
