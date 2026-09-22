package com.example.physiosync.ui.dashboard.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.core.model.ExerciseState
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.RepetitionDetail
import com.example.physiosync.core.model.SessionState
import com.example.physiosync.ui.dashboard.ClinicianLogEntry
import com.example.physiosync.ui.dashboard.LogSeverity
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// Clinician Dashboard Theme Colors (Glassmorphic Dark Theme)
val ClinicianDarkBg = Color(0xFF0B132B)
val ClinicianCardBg = Color(0xFF1C2541).copy(alpha = 0.95f)
val ClinicianCardBorder = Color(0xFF3A506B).copy(alpha = 0.6f)
val ClinicianAccentCyan = Color(0xFF00E5FF)
val ClinicianGoodGreen = Color(0xFF10B981)
val ClinicianWarningAmber = Color(0xFFF59E0B)
val ClinicianAlertRed = Color(0xFFEF4444)
val ClinicianMutedText = Color(0xFF94A3B8)
val ClinicianPurple = Color(0xFF8B5CF6)

@Composable
fun ClinicianHeader(
    exerciseName: String,
    sessionState: SessionState,
    onToggleView: () -> Unit = {},
    isMirrored: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        sessionState.isCompleted -> "COMPLETED"
        sessionState.isPaused -> "PAUSED"
        sessionState.isSessionActive -> "LIVE MONITORING"
        else -> "READY"
    }

    val statusColor = when {
        sessionState.isCompleted -> ClinicianAccentCyan
        sessionState.isPaused -> ClinicianWarningAmber
        sessionState.isSessionActive -> ClinicianGoodGreen
        else -> ClinicianMutedText
    }

    val elapsedSeconds = if (sessionState.sessionStartTimeMs > 0 && sessionState.isSessionActive) {
        ((System.currentTimeMillis() - sessionState.sessionStartTimeMs) / 1000).coerceAtLeast(0)
    } else {
        sessionState.sessionDurationMs / 1000
    }
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val durationFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, ClinicianAccentCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PHYSIOSYNC CLINICIAN DASHBOARD",
                    color = ClinicianAccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                if (isMirrored) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(ClinicianPurple.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(0.5.dp, ClinicianPurple, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OFFICE KIT MIRROR",
                            color = ClinicianPurple,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = exerciseName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Timer Badge
            Box(
                modifier = Modifier
                    .background(Color(0xFF0B132B), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = durationFormatted,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Live Status Indicator
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Camera View Toggle Button
            Button(
                onClick = onToggleView,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClinicianAccentCyan.copy(alpha = 0.2f),
                    contentColor = ClinicianAccentCyan
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClinicianAccentCyan)
            ) {
                Text(text = "Patient View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CameraViewportPipCard(
    modifier: Modifier = Modifier,
    cameraContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(ClinicianAccentCyan.copy(alpha = 0.6f), ClinicianAccentCyan.copy(alpha = 0.1f))
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            cameraContent?.invoke(this)

            // Header Pill overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ClinicianGoodGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE CAMERA & POSE PIP",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun KneeAngleGaugeCard(
    currentAngle: Float,
    targetMin: Float,
    targetMax: Float,
    modifier: Modifier = Modifier
) {
    val isInTarget = currentAngle in targetMin..targetMax
    val angleColor by animateColorAsState(
        targetValue = if (isInTarget) ClinicianGoodGreen else ClinicianAccentCyan,
        animationSpec = tween(250),
        label = "angleColor"
    )

    val animatedAngle by animateFloatAsState(
        targetValue = currentAngle.coerceIn(60f, 180f),
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "animatedAngle"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KNEE JOINT ANGLE",
                    color = ClinicianMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${targetMin.toInt()}°–${targetMax.toInt()}°",
                    color = ClinicianMutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val targetStartAngle = 135f + ((targetMin - 60f) / 120f) * 270f
                    val targetSweepAngle = ((targetMax - targetMin) / 120f) * 270f
                    drawArc(
                        color = ClinicianGoodGreen.copy(alpha = 0.25f),
                        startAngle = targetStartAngle,
                        sweepAngle = targetSweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val angleProgress = ((animatedAngle - 60f) / 120f).coerceIn(0f, 1f)
                    val currentSweepAngle = angleProgress * 270f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ClinicianAccentCyan, angleColor, angleColor)
                        ),
                        startAngle = 135f,
                        sweepAngle = currentSweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f°", currentAngle),
                        color = angleColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isInTarget) "TARGET REACHED" else "EXTENDING",
                        color = if (isInTarget) ClinicianGoodGreen else ClinicianMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Status Pill
            Box(
                modifier = Modifier
                    .background(
                        if (isInTarget) ClinicianGoodGreen.copy(alpha = 0.15f) else Color(0xFF0F172A),
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        0.5.dp,
                        if (isInTarget) ClinicianGoodGreen else Color(0xFF334155),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isInTarget) "✓ Target Extension Achieved" else "Target: ${targetMin.toInt()}° - ${targetMax.toInt()}°",
                    color = if (isInTarget) ClinicianGoodGreen else ClinicianMutedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun KneeAngleTelemetryCard(
    currentAngle: Float,
    targetMin: Float,
    targetMax: Float,
    modifier: Modifier = Modifier
) {
    val isInTarget = currentAngle in targetMin..targetMax
    val angleColor by animateColorAsState(
        targetValue = if (isInTarget) ClinicianGoodGreen else ClinicianAccentCyan,
        animationSpec = tween(200),
        label = "angleColor"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ClinicianCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KNEE JOINT ANGLE",
                    color = ClinicianMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Target: ${targetMin.toInt()}°–${targetMax.toInt()}°",
                    color = ClinicianMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f°", currentAngle),
                    color = angleColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                if (isInTarget) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .background(ClinicianGoodGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TARGET REACHED",
                            color = ClinicianGoodGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val normalizedProgress = ((currentAngle - 90f) / 90f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { normalizedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = angleColor,
                trackColor = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    primaryValue: String,
    secondaryText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = primaryValue,
                color = accentColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = secondaryText,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ExerciseStatePipeline(
    currentState: ExerciseState,
    modifier: Modifier = Modifier
) {
    val states = listOf(
        ExerciseState.WAITING,
        ExerciseState.EXTENDING,
        ExerciseState.PEAK,
        ExerciseState.RETURNING
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "BIOMECHANICAL STATE PIPELINE",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                states.forEachIndexed { index, state ->
                    val isActive = state == currentState
                    val stateColor = when (state) {
                        ExerciseState.WAITING -> Color(0xFF64748B)
                        ExerciseState.EXTENDING -> ClinicianWarningAmber
                        ExerciseState.PEAK -> ClinicianGoodGreen
                        ExerciseState.RETURNING -> ClinicianAccentCyan
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isActive) stateColor.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isActive) 1.5.dp else 0.5.dp,
                                color = if (isActive) stateColor else ClinicianCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.name,
                            color = if (isActive) stateColor else ClinicianMutedText,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Black else FontWeight.Medium
                        )
                    }

                    if (index < states.size - 1) {
                        Text(
                            text = "→",
                            color = ClinicianMutedText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormStatusBadge(
    formFlag: FormFlag,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (formFlag) {
        FormFlag.GOOD -> ClinicianGoodGreen to "GOOD FORM"
        FormFlag.REDUCED_ROM -> ClinicianWarningAmber to "REDUCED ROM"
        FormFlag.IRREGULAR_TEMPO -> ClinicianAlertRed to "IRREGULAR TEMPO"
        FormFlag.LOW_CONFIDENCE -> ClinicianMutedText to "LOW POSE CONFIDENCE"
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RepetitionBreakdownList(
    completedReps: List<RepetitionDetail>,
    listHeight: Dp = 140.dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "REPETITION BREAKDOWN (${completedReps.size})",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (completedReps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(listHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting completed repetitions...",
                        color = ClinicianMutedText,
                        fontSize = 12.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("REP #", color = ClinicianMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("PEAK", color = ClinicianMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("TIME", color = ClinicianMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("STATUS", color = ClinicianMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier.height(listHeight)
                ) {
                    items(completedReps.reversed()) { rep ->
                        val isGood = rep.formFlag == FormFlag.GOOD
                        val statusColor = if (isGood) ClinicianGoodGreen else ClinicianWarningAmber

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${rep.repIndex}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(String.format(Locale.US, "%.1f°", rep.peakAngle), color = ClinicianAccentCyan, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                            Text(String.format(Locale.US, "%.1fs", rep.durationSeconds), color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text(rep.formFlag.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeEventLog(
    eventLogs: List<ClinicianLogEntry>,
    listHeight: Dp = 140.dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "REAL-TIME CLINICAL EVENT FEED",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (eventLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(listHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting session events...",
                        color = ClinicianMutedText,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(listHeight)
                ) {
                    items(eventLogs) { entry ->
                        val badgeColor = when (entry.severity) {
                            LogSeverity.INFO -> ClinicianAccentCyan
                            LogSeverity.SUCCESS -> ClinicianGoodGreen
                            LogSeverity.WARNING -> ClinicianWarningAmber
                            LogSeverity.ALERT -> ClinicianAlertRed
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = entry.timeFormatted,
                                color = ClinicianMutedText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = entry.detail,
                                    color = ClinicianMutedText,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicianControlBar(
    isPaused: Boolean,
    isSessionActive: Boolean,
    isPresentationMode: Boolean = false,
    onPauseToggle: () -> Unit,
    onEndSession: () -> Unit,
    onRestart: () -> Unit = {},
    onToggleView: () -> Unit = {},
    onTogglePresentation: () -> Unit = {},
    onExportReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ClinicianCardBg, RoundedCornerShape(16.dp))
            .border(1.dp, ClinicianCardBorder, RoundedCornerShape(16.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Switch to Camera View
        OutlinedButton(
            onClick = onToggleView,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Camera", color = ClinicianAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Export Report (Task 17 Office Kit Transfer)
        OutlinedButton(
            onClick = onExportReport,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Export", color = ClinicianGoodGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Presentation Mode Toggle
        OutlinedButton(
            onClick = onTogglePresentation,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isPresentationMode) "Compact" else "Present",
                color = ClinicianPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Pause / Resume
        Button(
            onClick = onPauseToggle,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPaused) ClinicianGoodGreen else ClinicianWarningAmber,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = if (isPaused) "Resume" else "Pause",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // End Session
        Button(
            onClick = onEndSession,
            enabled = isSessionActive,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClinicianAlertRed,
                contentColor = Color.White
            )
        ) {
            Text("End", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Restart
        OutlinedButton(
            onClick = onRestart,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Restart", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ClinicianReportDialog(
    reportText: String,
    onDismiss: () -> Unit,
    onCopyToClipboard: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClinicianDarkBg),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ClinicianCardBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OFFICE KIT REPORT EXPORT",
                            color = ClinicianAccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Clinical Session Summary",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable text preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .border(1.dp, ClinicianCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = reportText,
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCopyToClipboard,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClinicianPurple,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Copy (Office Kit)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ClinicianGoodGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Share File",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = ClinicianMutedText, fontSize = 11.sp)
                }
            }
        }
    }
}
