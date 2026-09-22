package com.example.physiosync.ui.dashboard.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.SessionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Task 17: Clinician Report Exporter for PhysioSync.
 *
 * Formats session telemetry and repetition breakdown into structured Markdown/Plain-text
 * clinical summary reports and enables transfer via:
 * 1. Android ClipboardManager -> Instant sync to laptop via vivo/iQOO Office Kit Shared Clipboard.
 * 2. Android Intent.ACTION_SEND -> System share sheet (Office Kit File Transfer, Email, Notes, EHR).
 *
 * The phone remains the single source of truth for all telemetry and calculations.
 */
object ClinicianReportExporter {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Formats a comprehensive Markdown clinical report from the given SessionState.
     */
    fun formatMarkdownReport(
        sessionState: SessionState,
        exerciseName: String = "Seated Knee Extension",
        targetMinAngle: Float = 150.0f,
        targetMaxAngle: Float = 180.0f
    ): String {
        val dateString = dateFormatter.format(Date(if (sessionState.sessionStartTimeMs > 0) sessionState.sessionStartTimeMs else System.currentTimeMillis()))
        val totalReps = sessionState.repCount
        val goodReps = sessionState.goodRepCount
        val flaggedReps = sessionState.flaggedRepCount
        val compliancePct = if (totalReps > 0) ((goodReps.toFloat() / totalReps.toFloat()) * 100).toInt() else 0

        val durationSeconds = if (sessionState.sessionStartTimeMs > 0 && sessionState.isSessionActive) {
            ((System.currentTimeMillis() - sessionState.sessionStartTimeMs) / 1000).coerceAtLeast(0)
        } else {
            sessionState.sessionDurationMs / 1000
        }
        val durationFormatted = String.format(Locale.US, "%02d:%02d", durationSeconds / 60, durationSeconds % 60)

        val peakAngle = if (sessionState.completedReps.isNotEmpty()) {
            sessionState.completedReps.maxOf { it.peakAngle }
        } else {
            sessionState.currentKneeAngle
        }

        val avgDuration = if (sessionState.completedReps.isNotEmpty()) {
            sessionState.completedReps.map { it.durationSeconds }.average().toFloat()
        } else {
            0.0f
        }

        val sb = StringBuilder()
        sb.appendLine("# PhysioSync — Clinical Rehabilitation Session Report")
        sb.appendLine()
        sb.appendLine("**Date/Time:** $dateString  ")
        sb.appendLine("**Exercise:** $exerciseName  ")
        sb.appendLine("**Target ROM:** ${targetMinAngle.toInt()}° – ${targetMaxAngle.toInt()}°  ")
        sb.appendLine("**Session Duration:** $durationFormatted  ")
        sb.appendLine()
        sb.appendLine("## 1. Executive Summary")
        sb.appendLine()
        sb.appendLine("- **Total Repetitions:** $totalReps")
        sb.appendLine("- **Good Form Repetitions:** $goodReps ($compliancePct% Compliance)")
        sb.appendLine("- **Flagged Repetitions:** $flaggedReps")
        sb.appendLine(String.format(Locale.US, "- **Maximum Knee Extension Achieved:** %.1f°", peakAngle))
        sb.appendLine(String.format(Locale.US, "- **Average Repetition Tempo:** %.1f seconds", avgDuration))
        sb.appendLine()
        sb.appendLine("## 2. Repetition Breakdown")
        sb.appendLine()

        if (sessionState.completedReps.isEmpty()) {
            sb.appendLine("_No completed repetitions recorded in this session._")
        } else {
            sb.appendLine("| Rep # | Peak Angle | Duration | Form Classification | Clinical Note |")
            sb.appendLine("|:-----:|:----------:|:--------:|:-------------------:|:--------------|")
            sessionState.completedReps.forEach { rep ->
                val note = when (rep.formFlag) {
                    FormFlag.GOOD -> "Target ROM reached with stable tempo"
                    FormFlag.REDUCED_ROM -> "Incomplete extension (< ${targetMinAngle.toInt()}°)"
                    FormFlag.IRREGULAR_TEMPO -> "Atypical movement velocity / irregular tempo"
                    FormFlag.LOW_CONFIDENCE -> "Low pose confidence tracking during movement"
                }
                sb.appendLine(
                    String.format(
                        Locale.US,
                        "| #%d | %.1f° | %.1fs | %s | %s |",
                        rep.repIndex,
                        rep.peakAngle,
                        rep.durationSeconds,
                        rep.formFlag.name,
                        note
                    )
                )
            }
        }

        sb.appendLine()
        sb.appendLine("## 3. Clinical Assessment & Recommendation")
        sb.appendLine()
        when {
            totalReps == 0 -> sb.appendLine("Session initiated without completed repetitions.")
            compliancePct >= 80 -> sb.appendLine("Excellent compliance ($compliancePct%). Patient demonstrated consistent motor control and reached target extension angle ($peakAngle°). Ready for progressive resistance or increased repetition volume.")
            compliancePct >= 50 -> sb.appendLine("Moderate compliance ($compliancePct%). Patient showed partial extension deficits. Recommend focusing on holding the peak extension for 1-2 seconds.")
            else -> sb.appendLine("Low compliance ($compliancePct%). High incidence of flagged reps ($flaggedReps/$totalReps). Recommend supervised physical cueing and reviewing seating posture.")
        }
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("_Generated on-device by PhysioSync (iQOO x Reskilll Hackathon). Mirrored & Transferred via Office Kit._")

        return sb.toString()
    }

    /**
     * Formats a clean plain-text report.
     */
    fun formatPlainTextReport(
        sessionState: SessionState,
        exerciseName: String = "Seated Knee Extension",
        targetMinAngle: Float = 150.0f,
        targetMaxAngle: Float = 180.0f
    ): String {
        val dateString = dateFormatter.format(Date(if (sessionState.sessionStartTimeMs > 0) sessionState.sessionStartTimeMs else System.currentTimeMillis()))
        val totalReps = sessionState.repCount
        val goodReps = sessionState.goodRepCount
        val flaggedReps = sessionState.flaggedRepCount
        val compliancePct = if (totalReps > 0) ((goodReps.toFloat() / totalReps.toFloat()) * 100).toInt() else 0

        val durationSeconds = if (sessionState.sessionStartTimeMs > 0 && sessionState.isSessionActive) {
            ((System.currentTimeMillis() - sessionState.sessionStartTimeMs) / 1000).coerceAtLeast(0)
        } else {
            sessionState.sessionDurationMs / 1000
        }
        val durationFormatted = String.format(Locale.US, "%02d:%02d", durationSeconds / 60, durationSeconds % 60)

        val peakAngle = if (sessionState.completedReps.isNotEmpty()) {
            sessionState.completedReps.maxOf { it.peakAngle }
        } else {
            sessionState.currentKneeAngle
        }

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("PHYSIOSYNC CLINICAL REHABILITATION REPORT")
        sb.appendLine("==================================================")
        sb.appendLine("Date/Time: $dateString")
        sb.appendLine("Exercise: $exerciseName")
        sb.appendLine("Target ROM: ${targetMinAngle.toInt()}° - ${targetMaxAngle.toInt()}°")
        sb.appendLine("Duration: $durationFormatted")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("SUMMARY:")
        sb.appendLine("Total Reps: $totalReps")
        sb.appendLine("Good Reps:  $goodReps ($compliancePct% Compliance)")
        sb.appendLine("Flagged:    $flaggedReps")
        sb.appendLine(String.format(Locale.US, "Peak Extension: %.1f°", peakAngle))
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("REPETITION BREAKDOWN:")

        if (sessionState.completedReps.isEmpty()) {
            sb.appendLine("No completed repetitions recorded.")
        } else {
            sessionState.completedReps.forEach { rep ->
                sb.appendLine(
                    String.format(
                        Locale.US,
                        "Rep #%d: Peak %.1f° | Time %.1fs | Status: %s",
                        rep.repIndex,
                        rep.peakAngle,
                        rep.durationSeconds,
                        rep.formFlag.name
                    )
                )
            }
        }
        sb.appendLine("==================================================")

        return sb.toString()
    }

    /**
     * Copies the report text to the system clipboard.
     * When paired via vivo/iQOO Office Kit, this automatically synchronizes to the laptop clipboard.
     */
    fun copyToClipboard(
        context: Context,
        reportText: String,
        label: String = "PhysioSync_Clinical_Report"
    ): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, reportText)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Creates an Intent for standard Android sharing (compatible with Office Kit File Share, Email, etc.).
     */
    fun createShareIntent(
        reportText: String,
        subject: String = "PhysioSync Rehabilitation Report"
    ): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
    }
}
