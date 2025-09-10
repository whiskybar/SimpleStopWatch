package com.example.simplestopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplestopwatch.ui.theme.SimpleStopWatchTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleStopWatchTheme {
                StopwatchScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen() {
    // State management using rememberSaveable for configuration change handling
    // This ensures timer state persists through device rotation and other config changes
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var elapsedTime by rememberSaveable { mutableStateOf(0L) } // Time in milliseconds
    var pauseStartTime by rememberSaveable { mutableStateOf(0L) } // When pause started
    var totalPausedTime by rememberSaveable { mutableStateOf(0L) } // Total time paused

    // Additional state for handling configuration changes properly
    var lastSystemTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var accumulatedPauseTime by rememberSaveable { mutableStateOf(0L) }

    // Refined click logic functions with configuration change handling
    fun startTimer() {
        isRunning = true
        pauseStartTime = 0L // Reset pause tracking
        lastSystemTime = System.currentTimeMillis() // Update reference time
        accumulatedPauseTime = 0L // Reset accumulated pause time
    }

    fun pauseTimer() {
        isRunning = false
        pauseStartTime = System.currentTimeMillis() // Record when pause started
        lastSystemTime = System.currentTimeMillis() // Update reference time
    }

    fun resumeTimer() {
        // Calculate time spent paused and add to total paused time
        if (pauseStartTime > 0) {
            val pauseDuration = System.currentTimeMillis() - pauseStartTime
            totalPausedTime += pauseDuration
            accumulatedPauseTime += pauseDuration
            pauseStartTime = 0L
        }
        isRunning = true
        lastSystemTime = System.currentTimeMillis() // Update reference time
    }

    fun resetTimer() {
        isRunning = false
        elapsedTime = 0L
        pauseStartTime = 0L
        totalPausedTime = 0L
        lastSystemTime = System.currentTimeMillis()
        accumulatedPauseTime = 0L
    }

    // Handle time text click - refined logic
    fun onTimeClick() {
        if (!isRunning) {
            // If not running, check if we have paused time to resume
            if (pauseStartTime > 0) {
                resumeTimer()
            } else {
                startTimer()
            }
        } else {
            // If running, pause the timer
            pauseTimer()
        }
    }

    // Precise timer logic using LaunchedEffect and Coroutines
    // Handles configuration changes by using system time as reference
    LaunchedEffect(isRunning) {
        if (isRunning) {
            // Main timer loop - updates every second for precision
            while (isRunning) {
                delay(1000L) // 1 second intervals
                elapsedTime += 1000L
            }
        }
    }

    // Handle configuration changes (rotation, etc.) by adjusting elapsed time
    LaunchedEffect(Unit) {
        // This effect runs once when the composable is first created
        // It helps maintain accurate timing across configuration changes
        if (isRunning && pauseStartTime == 0L) {
            // If timer was running before config change, ensure continuity
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - lastSystemTime
            if (timeSinceLastUpdate > 0) {
                // Adjust elapsed time based on actual system time passed
                elapsedTime += timeSinceLastUpdate
            }
            lastSystemTime = currentTime
        }
    }

    // Format time as MM:ss or HH:MM:SS based on duration
    // Note: elapsedTime represents actual running time, not including pauses
    val formattedTime = remember(elapsedTime) {
        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        if (hours > 0) {
            // Format as HH:MM:SS for times over 1 hour
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            // Format as MM:ss for times under 1 hour
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    // Status text based on current state
    val statusText = remember(isRunning, pauseStartTime) {
        when {
            isRunning -> "Tap to pause"
            pauseStartTime > 0 -> "Tap to resume"
            else -> "Tap to start"
        }
    }

    // Styled UI with dark theme support
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            // Reset button positioned at bottom with 16.dp padding
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalButton(
                        onClick = { resetTimer() },
                        modifier = Modifier.size(56.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Stopwatch",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large, bold, centered time display
                Text(
                    text = formattedTime,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .clickable { onTimeClick() }
                        .padding(24.dp)
                )

                // Status text with improved styling
                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Visual indicator for current state
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(12.dp)
                        .background(
                            color = when {
                                isRunning -> MaterialTheme.colorScheme.primary
                                pauseStartTime > 0 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    SimpleStopWatchTheme {
        StopwatchScreen()
    }
}