package com.example.simplestopwatch

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var isFlashing by rememberSaveable { mutableStateOf(false) } // Flash animation state
    var lastFlashTime by rememberSaveable { mutableStateOf(0L) } // Prevent multiple flashes

    // UI enhancement state
    var showIntervalDropdown by rememberSaveable { mutableStateOf(false) }
    var fabScale by rememberSaveable { mutableStateOf(1f) }

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

    // Handle interval selection
    fun selectInterval(seconds: Int) {
        intervalSeconds = seconds
        showIntervalDropdown = false
    }

    // Get context for haptic feedback
    val context = LocalContext.current

    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Haptic feedback function
    fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Silently handle any haptic feedback errors
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
            // Trigger haptic feedback when flash starts
            triggerHaptic()
            delay(500L) // 500ms flash duration
            isFlashing = false
        }
    }

    // Handle FAB scale animation
    LaunchedEffect(fabScale) {
        if (fabScale < 1f) {
            delay(150L)
            fabScale = 1f
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

    // Dynamic font size based on screen width and time format
    val baseFontSize = remember(screenWidth) {
        when {
            screenWidth < 360.dp -> 48.sp // Small screens
            screenWidth < 400.dp -> 56.sp // Medium-small screens
            screenWidth < 480.dp -> 64.sp // Medium screens
            else -> 72.sp // Large screens
        }
    }

    val timeFontSize = remember(baseFontSize, formattedTime) {
        // Reduce font size if time format includes hours (longer text)
        if (formattedTime.contains(":")) {
            val colonCount = formattedTime.count { it == ':' }
            if (colonCount >= 2) { // HH:MM:SS.X format
                val reducedSize = baseFontSize * 0.85f
                if (reducedSize < 40.sp) 40.sp else reducedSize
            } else {
                baseFontSize
            }
        } else {
            baseFontSize
        }
    }

    // Enhanced animations
    val timeTextColor by animateColorAsState(
        targetValue = when {
            isRunning -> Color(0xFF4CAF50) // Green when running
            pauseStartTime > 0 -> Color(0xFFFF9800) // Orange when paused
            else -> Color(0xFFFFFFFF) // White when stopped
        },
        animationSpec = tween(durationMillis = 300),
        label = "time_text_color"
    )

    val timeTextScale by animateFloatAsState(
        targetValue = if (isRunning) 1.02f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "time_text_scale"
    )

    val fabScaleAnimation by animateFloatAsState(
        targetValue = fabScale,
        animationSpec = tween(durationMillis = 150),
        label = "fab_scale"
    )

    val timeTextBackgroundColor by animateColorAsState(
        targetValue = if (isFlashing) Color.Yellow.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "time_text_background"
    )

    // Gradient background
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F0F0F)
        )
    )

    // Enhanced UI with modern design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        // Main content card - wider to accommodate time display
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f) // Use 95% of screen width
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interval dropdown
                Box {
                    OutlinedButton(
                        onClick = { showIntervalDropdown = true },
                        modifier = Modifier.padding(bottom = 24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Interval: ${intervalSeconds}s")
                    }

                    DropdownMenu(
                        expanded = showIntervalDropdown,
                        onDismissRequest = { showIntervalDropdown = false }
                    ) {
                        listOf(10, 30, 60, 120).forEach { seconds ->
                            DropdownMenuItem(
                                text = { Text("${seconds}s") },
                                onClick = { selectInterval(seconds) }
                            )
                        }
                    }
                }

                // Enhanced time display with monospaced font and animations
                Text(
                    text = formattedTime,
                    fontSize = timeFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = timeTextColor,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1, // Prevent text wrapping
                    modifier = Modifier
                        .clickable { onTimeClick() }
                        .scale(timeTextScale)
                        .wrapContentWidth() // Allow text to size itself
                        .background(
                            timeTextBackgroundColor,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp) // Reduced padding
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

                // Enhanced visual indicator
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(16.dp)
                        .background(
                            color = when {
                                isRunning -> Color(0xFF4CAF50)
                                pauseStartTime > 0 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        // FloatingActionButton for reset
        FloatingActionButton(
            onClick = {
                fabScale = 0.8f
                resetTimer()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .scale(fabScaleAnimation),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset Stopwatch",
                modifier = Modifier.size(24.dp)
            )
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