// Test Sample for Stopwatch Click Logic
// This file demonstrates the refined click logic functionality

package com.example.simplestopwatch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Test Sample demonstrating the refined stopwatch click logic
 *
 * Test Scenarios:
 * 1. Initial state: "Tap to start" - clicking starts timer
 * 2. Running state: "Tap to pause" - clicking pauses timer
 * 3. Paused state: "Tap to resume" - clicking resumes timer
 * 4. Reset button: Resets all state to initial values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchTestSample() {
    // State management - same as main implementation
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var pauseStartTime by remember { mutableStateOf(0L) }
    var totalPausedTime by remember { mutableStateOf(0L) }

    // Refined click logic functions
    fun startTimer() {
        println("🟢 Starting timer")
        isRunning = true
        pauseStartTime = 0L
    }

    fun pauseTimer() {
        println("⏸️ Pausing timer at ${elapsedTime}ms")
        isRunning = false
        pauseStartTime = System.currentTimeMillis()
    }

    fun resumeTimer() {
        if (pauseStartTime > 0) {
            val pauseDuration = System.currentTimeMillis() - pauseStartTime
            totalPausedTime += pauseDuration
            println("▶️ Resuming timer after ${pauseDuration}ms pause")
        }
        pauseStartTime = 0L
        isRunning = true
    }

    fun resetTimer() {
        println("🔄 Resetting timer")
        isRunning = false
        elapsedTime = 0L
        pauseStartTime = 0L
        totalPausedTime = 0L
    }

    // Refined click logic - handles all three states
    fun onTimeClick() {
        when {
            !isRunning && pauseStartTime > 0 -> {
                // Paused state - resume
                resumeTimer()
            }
            !isRunning && pauseStartTime == 0L -> {
                // Initial state - start
                startTimer()
            }
            isRunning -> {
                // Running state - pause
                pauseTimer()
            }
        }
    }

    // Timer logic
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(1000L)
                elapsedTime += 1000L
            }
        }
    }

    // Format time
    val formattedTime = remember(elapsedTime) {
        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    // Dynamic status text
    val statusText = remember(isRunning, pauseStartTime) {
        when {
            isRunning -> "Tap to pause"
            pauseStartTime > 0 -> "Tap to resume"
            else -> "Tap to start"
        }
    }

    // Debug info
    val debugInfo = remember(isRunning, pauseStartTime, totalPausedTime) {
        "State: ${if (isRunning) "Running" else if (pauseStartTime > 0) "Paused" else "Stopped"} | " +
        "Paused: ${totalPausedTime}ms"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { resetTimer() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Time display
            Text(
                text = formattedTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable { onTimeClick() }
                    .padding(16.dp)
            )

            // Status text
            Text(
                text = statusText,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Debug information
            Text(
                text = debugInfo,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchTestSamplePreview() {
    MaterialTheme {
        StopwatchTestSample()
    }
}

/**
 * Test Cases Documentation:
 *
 * 1. Initial State Test:
 *    - App starts with "00:00" and "Tap to start"
 *    - Click time → starts timer, shows "Tap to pause"
 *
 * 2. Running State Test:
 *    - Timer is running, shows "Tap to pause"
 *    - Click time → pauses timer, shows "Tap to resume"
 *
 * 3. Paused State Test:
 *    - Timer is paused, shows "Tap to resume"
 *    - Click time → resumes timer, shows "Tap to pause"
 *
 * 4. Reset Test:
 *    - Click reset button → returns to initial state
 *
 * 5. State Persistence Test:
 *    - Pause timer, wait, resume → time continues from where it left off
 *    - Multiple pause/resume cycles work correctly
 */
