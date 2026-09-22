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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@Composable
fun ClinicianHeader(
    exerciseName: String,
    sessionState: SessionState,
    onToggleView: () -> Unit,
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

    val elapsedSeconds = sessionState.sessionDurationMs / 1000
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
            Text(
                text = "PHYSIOSYNC CLINICIAN DASHBOARD",
                color = ClinicianAccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
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
                    fontSize = 14.sp,
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
                        fontSize = 12.sp,
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
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianAccentCyan.copy(alpha = 0.6f), ClinicianAccentCyan.copy(alpha = 0.1f))
        ))
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
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KNEE ANGLE GAUGING",
                    color = ClinicianMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Target: ${targetMin.toInt()}°–${targetMax.toInt()}°",
                    color = ClinicianAccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Radial Arc Gauge Canvas
                Canvas(modifier = Modifier.size(170.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Arc angles: 135° to 405° (270° total sweep, covering 60° to 180° knee ROM)
                    val startArcAngle = 135f
                    val totalSweep = 270f

                    // 1. Background Arc Track
                    drawArc(
                        color = Color(0xFF0F172A),
                        startAngle = startArcAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 2. Target ROM Highlight Segment (targetMin to targetMax)
                    val targetMinRatio = ((targetMin - 60f) / 120f).coerceIn(0f, 1f)
                    val targetMaxRatio = ((targetMax - 60f) / 120f).coerceIn(0f, 1f)
                    val targetStartAngle = startArcAngle + (targetMinRatio * totalSweep)
                    val targetSweep = (targetMaxRatio - targetMinRatio) * totalSweep

                    drawArc(
                        color = ClinicianGoodGreen.copy(alpha = 0.25f),
                        startAngle = targetStartAngle,
                        sweepAngle = targetSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Butt)
                    )

                    // 3. Live Value Active Arc Sweep
                    val liveRatio = ((animatedAngle - 60f) / 120f).coerceIn(0f, 1f)
                    val liveSweep = liveRatio * totalSweep

                    drawArc(
                        color = angleColor,
                        startAngle = startArcAngle,
                        sweepAngle = liveSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // 4. Pointer Glowing Knob at current angle position
                    val pointerAngleRad = Math.toRadians((startArcAngle + liveSweep).toDouble())
                    val radius = diameter / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val pointerX = (center.x + radius * cos(pointerAngleRad)).toFloat()
                    val pointerY = (center.y + radius * sin(pointerAngleRad)).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(pointerX, pointerY)
                    )
                    drawCircle(
                        color = angleColor,
                        radius = 4.dp.toPx(),
                        center = Offset(pointerX, pointerY)
                    )
                }

                // Center Angle Readout Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f°", currentAngle),
                        color = angleColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isInTarget) ClinicianGoodGreen.copy(alpha = 0.2f) else Color(0xFF0F172A),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(0.5.dp, if (isInTarget) ClinicianGoodGreen else ClinicianMutedText, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isInTarget) "PEAK ROM REACHED" else "KNEE EXTENDING",
                            color = if (isInTarget) ClinicianGoodGreen else ClinicianMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.5f), ClinicianCardBorder)
        ))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title.uppercase(Locale.ROOT),
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = primaryValue,
                color = accentColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = secondaryText,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
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
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "BIOMECHANICAL STATE PIPELINE",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isActive) 1.5.dp else 0.5.dp,
                                color = if (isActive) stateColor else ClinicianCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.name,
                            color = if (isActive) stateColor else ClinicianMutedText,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Black else FontWeight.Medium
                        )
                    }

                    if (index < states.size - 1) {
                        Text(
                            text = "→",
                            color = ClinicianMutedText,
                            fontSize = 12.sp
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
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .border(1.dp, color, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RepetitionBreakdownList(
    completedReps: List<RepetitionDetail>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REPETITION BREAKDOWN (${completedReps.size})",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (completedReps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting rep completion...",
                        color = ClinicianMutedText,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("REP #", color = ClinicianMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("PEAK", color = ClinicianMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                    Text("TIME", color = ClinicianMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("STATUS", color = ClinicianMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Display latest up to 4 completed reps cleanly without nested LazyColumn scroll conflict
                val displayReps = completedReps.takeLast(4).reversed()
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    displayReps.forEach { rep ->
                        val isGood = rep.formFlag == FormFlag.GOOD
                        val statusColor = if (isGood) ClinicianGoodGreen else ClinicianWarningAmber

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("#${rep.repIndex}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(String.format(Locale.US, "%.1f°", rep.peakAngle), color = ClinicianAccentCyan, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                            Text(String.format(Locale.US, "%.1fs", rep.durationSeconds), color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(rep.formFlag.name, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ClinicianCardBg),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(
            colors = listOf(ClinicianCardBorder, ClinicianCardBorder.copy(alpha = 0.3f))
        ))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REAL-TIME CLINICAL EVENT FEED",
                color = ClinicianMutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (eventLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting session events...",
                        color = ClinicianMutedText,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Display top 3 latest event entries cleanly without scroll conflict
                val displayLogs = eventLogs.take(3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayLogs.forEach { entry ->
                        val badgeColor = when (entry.severity) {
                            LogSeverity.INFO -> ClinicianAccentCyan
                            LogSeverity.SUCCESS -> ClinicianGoodGreen
                            LogSeverity.WARNING -> ClinicianWarningAmber
                            LogSeverity.ALERT -> ClinicianAlertRed
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.timeFormatted,
                                color = ClinicianMutedText,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
    onPauseToggle: () -> Unit,
    onEndSession: () -> Unit,
    onRestart: () -> Unit,
    onToggleView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ClinicianCardBg, RoundedCornerShape(16.dp))
            .border(1.dp, ClinicianCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                fontSize = 12.sp,
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
            Text(
                text = "End Session",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Restart Session
        OutlinedButton(
            onClick = onRestart,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ClinicianMutedText)
        ) {
            Text(
                text = "Restart",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
