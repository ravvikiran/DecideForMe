package com.decideforme.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Slot-machine style animation that cycles through options before
 * landing on the final decision. Creates a fun, tactile feel.
 */
@Composable
fun SlotMachineAnimation(
    options: List<String>,
    finalChoice: String,
    isAnimating: Boolean,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }
    val animatedOffset = remember { Animatable(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating && options.isNotEmpty()) {
            // Fast cycling phase
            val shuffled = options.shuffled()
            var speed = 50L
            repeat(20) { i ->
                currentIndex = i % shuffled.size
                displayedText = shuffled[currentIndex]
                speed += 15L // Gradually slow down
                delay(speed)
            }

            // Slow down phase
            repeat(8) { i ->
                currentIndex = i % shuffled.size
                displayedText = shuffled[currentIndex]
                speed += 40L
                delay(speed)
            }

            // Land on final choice
            displayedText = finalChoice
            delay(200)
            onAnimationComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            )
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // Decorative side fades
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color.Transparent,
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )

        Text(
            text = if (isAnimating || displayedText.isNotEmpty()) displayedText else "???",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Top/bottom slot borders
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
        }
    }
}
