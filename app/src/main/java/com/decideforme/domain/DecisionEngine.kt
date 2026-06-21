package com.decideforme.domain

import com.decideforme.data.model.Category
import com.decideforme.data.model.DecisionOption
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Smart decision engine that factors in:
 * - Learned weights (accept/reject history)
 * - Time of day
 * - Day of week
 * - Recent history (avoid repeats)
 * - Weather/mood (user-set)
 * - Contextual tags
 */
@Singleton
class DecisionEngine @Inject constructor() {

    data class DecisionContext(
        val timeOfDay: TimeOfDay = getCurrentTimeOfDay(),
        val dayType: DayType = getCurrentDayType(),
        val weather: String = "any",
        val mood: String = "neutral",
        val recentOptionIds: List<String> = emptyList()
    )

    enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }
    enum class DayType { WEEKDAY, WEEKEND }

    fun decide(
        category: Category,
        context: DecisionContext = DecisionContext(),
        excludeIds: List<String> = emptyList()
    ): DecisionOption? {
        val activeOptions = category.options.filter { it.isActive && it.id !in excludeIds }
        if (activeOptions.isEmpty()) return null

        val scoredOptions = activeOptions.map { option ->
            val score = calculateScore(option, context)
            option to score
        }

        // Weighted random selection based on scores
        val totalScore = scoredOptions.sumOf { it.second }
        if (totalScore <= 0.0) return activeOptions.random()

        val rand = Random.nextDouble() * totalScore
        var cumulative = 0.0

        for ((option, score) in scoredOptions) {
            cumulative += score
            if (rand <= cumulative) return option
        }

        return scoredOptions.lastOrNull()?.first
    }

    private fun calculateScore(option: DecisionOption, context: DecisionContext): Double {
        var score = option.weight

        // Time-of-day relevance
        if (option.tags.timeOfDay.isNotEmpty()) {
            val timeStr = when (context.timeOfDay) {
                TimeOfDay.MORNING -> "morning"
                TimeOfDay.AFTERNOON -> "afternoon"
                TimeOfDay.EVENING -> "evening"
                TimeOfDay.NIGHT -> "night"
            }
            if (timeStr in option.tags.timeOfDay) {
                score *= 1.5
            } else {
                score *= 0.3
            }
        }

        // Day type relevance
        if (option.tags.dayType.isNotEmpty()) {
            val dayStr = when (context.dayType) {
                DayType.WEEKDAY -> "weekday"
                DayType.WEEKEND -> "weekend"
            }
            if (dayStr in option.tags.dayType || "any" in option.tags.dayType) {
                score *= 1.3
            } else {
                score *= 0.4
            }
        }

        // Weather match
        if (option.tags.weather.isNotEmpty() && context.weather != "any") {
            if (context.weather in option.tags.weather || "any" in option.tags.weather) {
                score *= 1.4
            } else {
                score *= 0.3
            }
        }

        // Mood match
        if (option.tags.mood.isNotEmpty() && context.mood != "neutral") {
            if (context.mood in option.tags.mood) {
                score *= 1.5
            } else {
                score *= 0.5
            }
        }

        // Recency penalty - don't repeat recent choices
        if (option.id in context.recentOptionIds) {
            score *= 0.2
        }

        // Time since last shown - boost options not seen in a while
        val hoursSinceShown = (System.currentTimeMillis() - option.lastShown) / (1000 * 60 * 60)
        if (hoursSinceShown > 48) {
            score *= 1.2
        }

        return score.coerceAtLeast(0.01)
    }

    companion object {
        fun getCurrentTimeOfDay(): TimeOfDay {
            val hour = LocalDateTime.now().hour
            return when {
                hour in 5..11 -> TimeOfDay.MORNING
                hour in 12..16 -> TimeOfDay.AFTERNOON
                hour in 17..20 -> TimeOfDay.EVENING
                else -> TimeOfDay.NIGHT
            }
        }

        fun getCurrentDayType(): DayType {
            val today = LocalDate.now().dayOfWeek
            return if (today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY) {
                DayType.WEEKEND
            } else {
                DayType.WEEKDAY
            }
        }
    }
}
