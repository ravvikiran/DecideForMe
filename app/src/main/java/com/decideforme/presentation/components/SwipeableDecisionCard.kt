package com.decideforme.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Tinder-style swipeable card for decisions.
 * Swipe right = accept, swipe left = reject (get another).
 * Adds a fun, interactive dimension to the decision flow.
 */
@Composable
fun SwipeableDecisionCard(
    optionName: String,
    categoryName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }
    val threshold = 200f

    // Determine swipe direction for visual feedback
    val swipeProgress = (offsetX / threshold).coerceIn(-1f, 1f)
    val rotation = swipeProgress * 10f
    val acceptAlpha = (swipeProgress).coerceIn(0f, 1f)
    val rejectAlpha = (-swipeProgress).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxWidth()) {
        // Accept indicator (right side)
        if (acceptAlpha > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
                    .graphicsLayer(alpha = acceptAlpha)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Reject indicator (left side)
        if (rejectAlpha > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .graphicsLayer(alpha = rejectAlpha)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Reject",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .rotate(rotation)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX > threshold -> {
                                        animatedOffset.animateTo(1000f)
                                        onAccept()
                                    }
                                    offsetX < -threshold -> {
                                        animatedOffset.animateTo(-1000f)
                                        onReject()
                                    }
                                    else -> {
                                        // Snap back
                                        animatedOffset.animateTo(
                                            0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy
                                            )
                                        )
                                    }
                                }
                                offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = optionName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "← Swipe to skip · Swipe to accept →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                )
            }
        }
    }
}
