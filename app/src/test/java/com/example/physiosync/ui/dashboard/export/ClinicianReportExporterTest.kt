package com.example.physiosync.ui.dashboard.export

import com.example.physiosync.core.model.FormFlag
import com.example.physiosync.core.model.RepetitionDetail
import com.example.physiosync.core.model.SessionState
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicianReportExporterTest {

    @Test
    fun testFormatMarkdownReport_emptySession() {
        val emptyState = SessionState(
            isSessionActive = false,
            isCompleted = false,
            repCount = 0,
            goodRepCount = 0,
            flaggedRepCount = 0,
            currentKneeAngle = 92.0f,
            completedReps = emptyList()
        )

        val report = ClinicianReportExporter.formatMarkdownReport(
            sessionState = emptyState,
            exerciseName = "Seated Knee Extension",
            targetMinAngle = 150f,
            targetMaxAngle = 180f
        )

        assertNotNull(report)
        assertTrue(report.contains("# PhysioSync — Clinical Rehabilitation Session Report"))
        assertTrue(report.contains("**Exercise:** Seated Knee Extension"))
        assertTrue(report.contains("- **Total Repetitions:** 0"))
        assertTrue(report.contains("_No completed repetitions recorded in this session._"))
        assertTrue(report.contains("Session initiated without completed repetitions."))
    }

    @Test
    fun testFormatMarkdownReport_completedSessionWithReps() {
        val completedReps = listOf(
            RepetitionDetail(repIndex = 1, peakAngle = 168.5f, formFlag = FormFlag.GOOD, durationSeconds = 2.4f),
            RepetitionDetail(repIndex = 2, peakAngle = 171.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.2f),
            RepetitionDetail(repIndex = 3, peakAngle = 142.0f, formFlag = FormFlag.REDUCED_ROM, durationSeconds = 1.8f),
            RepetitionDetail(repIndex = 4, peakAngle = 166.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.5f)
        )

        val state = SessionState(
            isSessionActive = false,
            isCompleted = true,
            repCount = 4,
            goodRepCount = 3,
            flaggedRepCount = 1,
            currentKneeAngle = 90.0f,
            completedReps = completedReps,
            sessionDurationMs = 65000L
        )

        val report = ClinicianReportExporter.formatMarkdownReport(
            sessionState = state,
            exerciseName = "Seated Knee Extension"
        )

        assertTrue(report.contains("- **Total Repetitions:** 4"))
        assertTrue(report.contains("- **Good Form Repetitions:** 3 (75% Compliance)"))
        assertTrue(report.contains("- **Flagged Repetitions:** 1"))
        assertTrue(report.contains("- **Maximum Knee Extension Achieved:** 171.0°"))
        assertTrue(report.contains("| #1 | 168.5° | 2.4s | GOOD |"))
        assertTrue(report.contains("| #3 | 142.0° | 1.8s | REDUCED_ROM |"))
        assertTrue(report.contains("Moderate compliance (75%)."))
    }

    @Test
    fun testFormatPlainTextReport_formatting() {
        val completedReps = listOf(
            RepetitionDetail(repIndex = 1, peakAngle = 165.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.0f)
        )

        val state = SessionState(
            isSessionActive = false,
            isCompleted = true,
            repCount = 1,
            goodRepCount = 1,
            flaggedRepCount = 0,
            completedReps = completedReps
        )

        val plainReport = ClinicianReportExporter.formatPlainTextReport(sessionState = state)

        assertTrue(plainReport.contains("PHYSIOSYNC CLINICAL REHABILITATION REPORT"))
        assertTrue(plainReport.contains("Total Reps: 1"))
        assertTrue(plainReport.contains("Good Reps:  1 (100% Compliance)"))
        assertTrue(plainReport.contains("Peak Extension: 165.0°"))
        assertTrue(plainReport.contains("Rep #1: Peak 165.0° | Time 2.0s | Status: GOOD"))
    }

    @Test
    fun testFormatMarkdownReport_highComplianceRecommendation() {
        val completedReps = listOf(
            RepetitionDetail(repIndex = 1, peakAngle = 175.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.0f),
            RepetitionDetail(repIndex = 2, peakAngle = 172.0f, formFlag = FormFlag.GOOD, durationSeconds = 2.1f)
        )

        val state = SessionState(
            repCount = 2,
            goodRepCount = 2,
            flaggedRepCount = 0,
            completedReps = completedReps
        )

        val report = ClinicianReportExporter.formatMarkdownReport(sessionState = state)
        assertTrue(report.contains("Excellent compliance (100%)."))
    }
}
