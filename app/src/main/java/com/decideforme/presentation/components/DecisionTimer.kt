package com.decideforme.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A countdown timer that auto-accepts the decision after N seconds.
 * Forces commitment and eliminates second-guessing.
 * The "speed round" feature for power users who want zero time wasted.
 */
@Composable
fun DecisionTimer(
    totalSeconds: Int = 10,
    isRunning: Boolean = true,
    onTimeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(900, easing = LinearEasing),
        label = "timer_progress"
    )

    LaunchedEffect(isRunning) {
        if (isRunning) {
            remainingSeconds = totalSeconds
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            onTimeUp()
        }
    }

    val color = when {
        remainingSeconds > 6 -> MaterialTheme.colorScheme.primary
        remainingSeconds > 3 -> Color(0xFFFFA000)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background circle
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$remainingSeconds",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
