package com.example.simplestopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    // Interval alert feature state
    var intervalSeconds by rememberSaveable { mutableStateOf(30) } // Default 30 seconds
    var intervalText by rememberSaveable { mutableStateOf("30") } // Text field input
    var isFlashing by rememberSaveable { mutableStateOf(false) } // Flash animation state
    var lastFlashTime by rememberSaveable { mutableStateOf(0L) } // Prevent multiple flashes

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
        isFlashing = false // Clear flash state on reset
        lastFlashTime = 0L // Reset flash tracking
    }

    // Handle interval text input
    fun onIntervalTextChange(newText: String) {
        intervalText = newText
        // Update intervalSeconds if the text is a valid number
        val newInterval = newText.toIntOrNull()
        if (newInterval != null && newInterval > 0 && newInterval <= 3600) { // Max 1 hour
            intervalSeconds = newInterval
        }
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
            // Main timer loop - updates every 100ms for smooth millisecond display
            while (isRunning) {
                delay(100L) // 100ms intervals for smooth millisecond updates
                elapsedTime += 100L

                // Check for interval milestones and trigger flash (without blocking timer)
                val intervalMs = intervalSeconds * 1000L
                if (elapsedTime > 0 && elapsedTime % intervalMs < 100L && elapsedTime != lastFlashTime) {
                    isFlashing = true
                    lastFlashTime = elapsedTime
                    // Note: Flash duration is handled by separate LaunchedEffect below
                }
            }
        }
    }

    // Separate LaunchedEffect to handle flash duration without blocking timer
    LaunchedEffect(isFlashing) {
        if (isFlashing) {
            delay(500L) // 500ms flash duration
            isFlashing = false
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

    // Format time as MM:SS.X or HH:MM:SS.X based on duration
    // Note: elapsedTime represents actual running time, not including pauses
    val formattedTime = remember(elapsedTime) {
        val totalSeconds = elapsedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val hundredsOfMs = (elapsedTime % 1000) / 100 // Hundreds of milliseconds

        if (hours > 0) {
            // Format as HH:MM:SS.X for times over 1 hour
            String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, hundredsOfMs)
        } else {
            // Format as MM:SS.X for times under 1 hour
            String.format("%02d:%02d.%d", minutes, seconds, hundredsOfMs)
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

    // Flash animation - animate background color when interval is reached
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isFlashing) Color.Yellow else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 250),
        label = "flash_animation"
    )

    // Styled UI with dark theme support and flash animation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = animatedBackgroundColor // Apply flash animation to background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content - centered time display and status
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interval input field
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { onIntervalTextChange(it) },
                    label = { Text("Interval (seconds)") },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(bottom = 24.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

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

            // Reset button positioned at bottom with navigation bar clearance
            FilledTonalButton(
                onClick = { resetTimer() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp) // 32dp padding to clear navigation bar
                    .size(56.dp),
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

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    SimpleStopWatchTheme {
        StopwatchScreen()
    }
}