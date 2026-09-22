package com.example.physiosync.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.ui.theme.*

data class RepDetail(
    val repNumber: Int,
    val maxAngle: Float = 0f,
    val status: FormStatus,
    val note: String
)

data class SessionReportData(
    val exerciseName: String = "Seated Knee Extension",
    val totalRepsCount: Int = 10,
    val goodRepsCount: Int = 8,
    val flaggedRepsCount: Int = 2,
    val maxKneeAngleAchieved: Float = 168f,
    val averageAngle: Float = 160f,
    val sessionDurationSeconds: Int = 120,
    val repDetails: List<RepDetail> = listOf(
        RepDetail(1, 170f, FormStatus.GOOD, "Full extension & steady lift — Great posture!"),
        RepDetail(2, 168f, FormStatus.GOOD, "Good leg control and balance"),
        RepDetail(3, 150f, FormStatus.REDUCED_ROM, "Partial lift — Try lifting your leg higher next time"),
        RepDetail(4, 169f, FormStatus.GOOD, "Smooth and steady execution"),
        RepDetail(5, 167f, FormStatus.GOOD, "Solid knee extension"),
        RepDetail(6, 145f, FormStatus.REDUCED_ROM, "Leg was slightly bent — Aim for a straight leg at top"),
        RepDetail(7, 172f, FormStatus.GOOD, "Excellent posture and hold"),
        RepDetail(8, 170f, FormStatus.GOOD, "Controlled tempo throughout"),
        RepDetail(9, 168f, FormStatus.GOOD, "Good form maintained"),
        RepDetail(10, 169f, FormStatus.GOOD, "Strong final repetition!")
    )
)

@Composable
fun SessionReportScreen(
    reportData: SessionReportData,
    onDoneClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val postureScore = if (reportData.totalRepsCount > 0) {
        ((reportData.goodRepsCount.toFloat() / reportData.totalRepsCount.toFloat()) * 100).toInt()
    } else 100

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "POSTURE & PROGRESS REPORT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (postureScore >= 80) "Great Posture & Control!" else "Session Completed — Room for Growth",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key Summary Metric Grid (User-Centric)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Posture Score",
                    value = "$postureScore%",
                    subText = if (postureScore >= 80) "Excellent Form" else "Needs Attention",
                    icon = Icons.Default.Star,
                    accentColor = if (postureScore >= 80) StatusSuccess else StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                ReportMetricCard(
                    title = "Target Completed",
                    value = "${reportData.goodRepsCount} / ${reportData.totalRepsCount}",
                    subText = "Proper Reps",
                    icon = Icons.Default.CheckCircle,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // How to Improve Your Posture Card
            ImprovementTipsCard(reportData = reportData)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Repetition Feedback",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of detailed reps with posture feedback
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportData.repDetails) { rep ->
                    UserFriendlyRepRow(rep = rep)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDoneClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ImprovementTipsCard(reportData: SessionReportData) {
    val hasRomIssues = reportData.repDetails.any { it.status == FormStatus.REDUCED_ROM }
    val hasTempoIssues = reportData.repDetails.any { it.status == FormStatus.IRREGULAR_TEMPO }
    val hasConfidenceIssues = reportData.repDetails.any { it.status == FormStatus.LOW_CONFIDENCE }

    val tips = mutableListOf<String>()
    if (hasRomIssues) {
        tips.add("Aim to extend your leg fully straight at the top of each kick.")
    }
    if (hasTempoIssues) {
        tips.add("Maintain a smooth, controlled 2-second lift and lower phase.")
    }
    if (hasConfidenceIssues) {
        tips.add("Position your full side profile clearly in good lighting.")
    }
    if (tips.isEmpty()) {
        tips.add("Outstanding posture! Keep maintaining this steady form in your next session.")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HOW TO IMPROVE YOUR POSTURE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    subText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun UserFriendlyRepRow(rep: RepDetail) {
    val (statusColor, statusTitle, userAdvice) = when (rep.status) {
        FormStatus.GOOD -> Triple(
            StatusSuccess,
            "Good Posture",
            if (rep.note.isNotBlank() && !rep.note.contains("ROM", ignoreCase = true)) rep.note else "Full leg extension & controlled lift."
        )
        FormStatus.REDUCED_ROM -> Triple(
            StatusWarning,
            "Partial Extension",
            "Try lifting your leg a little higher to reach full extension."
        )
        FormStatus.IRREGULAR_TEMPO -> Triple(
            CoralAccent,
            "Fast Pace",
            "Slow down and pause for 1-2 seconds at peak extension."
        )
        FormStatus.LOW_CONFIDENCE -> Triple(
            StatusError,
            "Position Warning",
            "Keep side profile fully visible in camera frame."
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${rep.repNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = userAdvice,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusTitle,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}
