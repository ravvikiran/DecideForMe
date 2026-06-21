package com.decideforme.domain

import com.decideforme.data.model.DecisionRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzes decision patterns to infer user's mood and energy level.
 * Based entirely on their accept/reject behavior — no external APIs.
 */
@Singleton
class MoodTracker @Inject constructor() {

    data class MoodInsight(
        val currentMood: String,      // lazy, energetic, neutral
        val confidence: Float,         // 0-1 how confident we are
        val pattern: String,           // human-readable insight
        val suggestion: String         // context-aware suggestion
    )

    /**
     * Infer current mood from recent decision patterns (last 24h).
     */
    fun inferMood(recentHistory: List<DecisionRecord>): MoodInsight {
        if (recentHistory.isEmpty()) {
            return MoodInsight(
                currentMood = "neutral",
                confidence = 0f,
                pattern = "Not enough data yet",
                suggestion = "Make a few decisions and I'll learn your patterns"
            )
        }

        val today = LocalDate.now()
        val todayRecords = recentHistory.filter { record ->
            Instant.ofEpochMilli(record.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == today
        }

        if (todayRecords.isEmpty()) {
            return MoodInsight(
                currentMood = "neutral",
                confidence = 0.3f,
                pattern = "No decisions today yet",
                suggestion = "Ready to start the day?"
            )
        }

        // Count category patterns
        val categoryCount = todayRecords.groupBy { it.categoryName }.mapValues { it.value.size }
        val rejectionRate = todayRecords.count { !it.wasAccepted }.toFloat() / todayRecords.size

        // High rejection = picky / restless mood
        val mood = when {
            rejectionRate > 0.6f -> "indecisive"
            categoryCount.any { it.key.contains("Workout", ignoreCase = true) && it.value > 0 } &&
                    todayRecords.any { it.wasAccepted } -> "energetic"
            categoryCount.size == 1 && todayRecords.size > 2 -> "focused"
            rejectionRate < 0.2f && todayRecords.size > 1 -> "easygoing"
            else -> "neutral"
        }

        val pattern = when (mood) {
            "indecisive" -> "You're being picky today — ${(rejectionRate * 100).toInt()}% rejection rate"
            "energetic" -> "Active day! You're choosing energetic options"
            "focused" -> "Focused on ${categoryCount.maxByOrNull { it.value }?.key}"
            "easygoing" -> "Going with the flow — accepting most suggestions"
            else -> "Balanced decisions today"
        }

        val suggestion = when (mood) {
            "indecisive" -> "Try narrowing your category first"
            "energetic" -> "Great energy! I'll suggest more active options"
            "focused" -> "Want me to suggest something different?"
            "easygoing" -> "Love the decisiveness!"
            else -> "Keep it up!"
        }

        return MoodInsight(
            currentMood = mood,
            confidence = (todayRecords.size.toFloat() / 5f).coerceAtMost(1f),
            pattern = pattern,
            suggestion = suggestion
        )
    }

    /**
     * Weekly mood summary.
     */
    fun getWeeklyInsight(history: List<DecisionRecord>): String {
        val weekAgo = LocalDate.now().minusDays(7)
        val weekRecords = history.filter { record ->
            val date = Instant.ofEpochMilli(record.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            date.isAfter(weekAgo)
        }

        if (weekRecords.size < 5) return "Make more decisions this week for insights"

        val totalDecisions = weekRecords.size
        val accepted = weekRecords.count { it.wasAccepted }
        val avgAcceptRate = accepted.toFloat() / totalDecisions

        val favoriteCategory = weekRecords
            .filter { it.wasAccepted }
            .groupBy { it.categoryName }
            .maxByOrNull { it.value.size }
            ?.key ?: "None"

        val mostPicked = weekRecords
            .filter { it.wasAccepted }
            .groupBy { it.optionName }
            .maxByOrNull { it.value.size }

        return buildString {
            append("This week: $totalDecisions decisions, ${(avgAcceptRate * 100).toInt()}% accept rate. ")
            append("Most active in: $favoriteCategory. ")
            mostPicked?.let {
                append("Favorite pick: ${it.key} (${it.value.size}x).")
            }
        }
    }
}
