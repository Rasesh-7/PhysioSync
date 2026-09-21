package com.example.physiosync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.physiosync.ui.patient.FormStatus
import com.example.physiosync.ui.patient.PatientScreen
import com.example.physiosync.ui.patient.PatientSessionUiState
import com.example.physiosync.ui.patient.SessionReportData
import com.example.physiosync.ui.patient.SessionReportScreen
import com.example.physiosync.ui.theme.PhysioSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhysioSyncTheme {
                var currentScreen by remember { mutableStateOf("PATIENT") }
                var sessionState by remember { mutableStateOf(PatientSessionUiState()) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "PATIENT" -> {
                            PatientScreen(
                                sessionState = sessionState,
                                onPauseClicked = {
                                    sessionState = sessionState.copy(isPaused = !sessionState.isPaused)
                                },
                                onEndSessionClicked = {
                                    currentScreen = "REPORT"
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        "REPORT" -> {
                            SessionReportScreen(
                                reportData = SessionReportData(
                                    totalRepsCount = sessionState.targetReps,
                                    goodRepsCount = sessionState.completedReps,
                                    flaggedRepsCount = sessionState.targetReps - sessionState.completedReps
                                ),
                                onDoneClicked = {
                                    currentScreen = "PATIENT"
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}