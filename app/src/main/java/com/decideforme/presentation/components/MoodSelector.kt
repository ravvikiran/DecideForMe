package com.decideforme.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quick mood selector shown on home screen.
 * Allows user to set their current mood which influences
 * the decision engine's weighting.
 */
@Composable
fun MoodSelector(
    currentMood: String,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    data class MoodOption(val emoji: String, val value: String, val label: String)

    val moods = listOf(
        MoodOption("😴", "lazy", "Lazy"),
        MoodOption("😊", "neutral", "Neutral"),
        MoodOption("⚡", "energetic", "Energetic")
    )

    Column(modifier = modifier) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moods.forEach { mood ->
                val isSelected = currentMood == mood.value
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "mood_bg"
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMoodSelected(mood.value) },
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mood.emoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mood.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Time-of-day context display.
 * Shows what time context is being used for decisions.
 */
@Composable
fun ContextBadge(
    timeOfDay: String,
    weather: String,
    dayType: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val timeEmoji = when (timeOfDay) {
            "morning" -> "🌅"
            "afternoon" -> "☀️"
            "evening" -> "🌇"
            "night" -> "🌙"
            else -> "⏰"
        }
        val weatherEmoji = when (weather) {
            "hot" -> "🔥"
            "cold" -> "❄️"
            "rainy" -> "🌧️"
            else -> ""
        }
        val dayEmoji = when (dayType) {
            "weekend" -> "🎉"
            else -> ""
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Text(
                text = "$timeEmoji $timeOfDay${if (weatherEmoji.isNotEmpty()) " $weatherEmoji" else ""}${if (dayEmoji.isNotEmpty()) " $dayEmoji" else ""}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
