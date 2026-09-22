package com.example.physiosync.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.physiosync.ui.theme.*

data class RepDetail(
    val repNumber: Int,
    val maxAngle: Float,
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
        RepDetail(1, 170f, FormStatus.GOOD, "Excellent range of motion"),
        RepDetail(2, 168f, FormStatus.GOOD, "Good control"),
        RepDetail(3, 150f, FormStatus.REDUCED_ROM, "Reduced extension (under 160°)"),
        RepDetail(4, 169f, FormStatus.GOOD, "Smooth execution"),
        RepDetail(5, 167f, FormStatus.GOOD, "Solid hold at peak"),
        RepDetail(6, 145f, FormStatus.REDUCED_ROM, "Partial extension"),
        RepDetail(7, 172f, FormStatus.GOOD, "Great effort"),
        RepDetail(8, 170f, FormStatus.GOOD, "Consistent tempo"),
        RepDetail(9, 168f, FormStatus.GOOD, "Good form"),
        RepDetail(10, 169f, FormStatus.GOOD, "Final rep completed well")
    )
)

@Composable
fun SessionReportScreen(
    reportData: SessionReportData,
    onDoneClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "SESSION REPORT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Great job completing your set!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Key Summary Metric Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportMetricCard(
                    title = "Good Reps",
                    value = "${reportData.goodRepsCount}/${reportData.totalRepsCount}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
                ReportMetricCard(
                    title = "Peak Angle",
                    value = "${reportData.maxKneeAngleAchieved.toInt()}°",
                    icon = Icons.Default.Speed,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Repetition Breakdown",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of detailed reps
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportData.repDetails) { rep ->
                    RepDetailRow(rep = rep)
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
                    text = "Complete & Return",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
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
        }
    }
}

@Composable
fun RepDetailRow(rep: RepDetail) {
    val (statusColor, statusText) = when (rep.status) {
        FormStatus.GOOD -> Pair(StatusSuccess, "Good")
        FormStatus.REDUCED_ROM -> Pair(StatusWarning, "Reduced ROM")
        FormStatus.IRREGULAR_TEMPO -> Pair(CoralAccent, "Tempo")
        FormStatus.LOW_CONFIDENCE -> Pair(StatusError, "Low Confidence")
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "Max Angle: ${rep.maxAngle.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = rep.note,
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
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}
