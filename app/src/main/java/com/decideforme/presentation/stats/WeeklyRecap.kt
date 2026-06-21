package com.decideforme.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decideforme.data.model.DecisionRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Weekly recap card shown in Stats tab.
 * Provides fun, shareable insights about the week's decisions.
 */
@Composable
fun WeeklyRecapCard(
    records: List<DecisionRecord>,
    modifier: Modifier = Modifier
) {
    val weekAgo = LocalDate.now().minusDays(7)
    val weekRecords = records.filter { record ->
        val date = Instant.ofEpochMilli(record.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        date.isAfter(weekAgo)
    }

    if (weekRecords.size < 3) return

    val accepted = weekRecords.filter { it.wasAccepted }
    val rejected = weekRecords.filter { !it.wasAccepted }
    val decisiveness = if (weekRecords.isNotEmpty()) {
        (accepted.size.toFloat() / weekRecords.size * 100).toInt()
    } else 0

    val mostDecisiveDay = accepted
        .groupBy { record ->
            Instant.ofEpochMilli(record.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        .maxByOrNull { it.value.size }

    val funFact = generateFunFact(accepted, rejected)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Recap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat("${accepted.size}", "Accepted")
                MiniStat("${rejected.size}", "Skipped")
                MiniStat("$decisiveness%", "Decisive")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Most decisive day
            mostDecisiveDay?.let { (day, decisions) ->
                Text(
                    text = "🏆 Most active day: $day (${decisions.size} decisions)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fun fact
            Text(
                text = funFact,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
    }
}

private fun generateFunFact(accepted: List<DecisionRecord>, rejected: List<DecisionRecord>): String {
    val topPick = accepted
        .groupBy { it.optionName }
        .maxByOrNull { it.value.size }

    val topReject = rejected
        .groupBy { it.optionName }
        .maxByOrNull { it.value.size }

    return buildString {
        topPick?.let { (name, picks) ->
            if (picks.size >= 2) append("💚 You really love $name (${picks.size}x this week). ")
        }
        topReject?.let { (name, rejects) ->
            if (rejects.size >= 2) append("🚫 You skipped $name ${rejects.size} times — maybe remove it?")
        }
        if (isEmpty()) append("✨ Solid week of decisive living!")
    }
}
