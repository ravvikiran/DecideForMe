package com.decideforme.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val speed: Float,
    val amplitude: Float,
    val phase: Float
)

/**
 * Celebratory confetti overlay. Triggered on streaks and milestones.
 * Pure Compose Canvas drawing with animation — no Lottie needed for this one.
 */
@Composable
fun ConfettiAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isPlaying) return

    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA),
        Color(0xFFFCBF49),
        Color(0xFF2EC4B6)
    )

    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                color = colors.random(),
                size = Random.nextFloat() * 12f + 4f,
                rotation = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 3f + 1.5f,
                amplitude = Random.nextFloat() * 40f + 10f,
                phase = Random.nextFloat() * 6.28f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { particle ->
            val currentY = ((particle.y + progress * particle.speed) % 1.3f) * canvasHeight
            val wobble = sin((progress * 10f + particle.phase).toDouble()).toFloat() * particle.amplitude
            val currentX = particle.x * canvasWidth + wobble

            if (currentY in 0f..canvasHeight) {
                rotate(
                    degrees = particle.rotation + progress * 720f * (if (particle.speed > 2f) 1f else -1f),
                    pivot = Offset(currentX, currentY)
                ) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
