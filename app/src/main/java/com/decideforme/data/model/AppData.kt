package com.decideforme.data.model

import kotlinx.serialization.Serializable

/**
 * Root data structure for the entire app.
 * This single JSON file IS the database.
 */
@Serializable
data class AppData(
    val userProfile: UserProfile = UserProfile(),
    val categories: List<Category> = defaultCategories(),
    val decisionHistory: List<DecisionRecord> = emptyList(),
    val streaks: StreakData = StreakData(),
    val settings: AppSettings = AppSettings(),
    val sharedCategories: List<SharedCategory> = emptyList()
)

@Serializable
data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isSignedIn: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val isDefault: Boolean = true,
    val isEnabled: Boolean = true,
    val options: List<DecisionOption> = emptyList()
)

@Serializable
data class DecisionOption(
    val id: String,
    val name: String,
    val tags: OptionTags = OptionTags(),
    val weight: Double = 1.0,
    val timesShown: Int = 0,
    val timesAccepted: Int = 0,
    val timesRejected: Int = 0,
    val lastShown: Long = 0L,
    val isActive: Boolean = true
)

@Serializable
data class OptionTags(
    val mood: List<String> = emptyList(),       // lazy, energetic, neutral
    val budget: List<String> = emptyList(),     // free, cheap, moderate, splurge
    val weather: List<String> = emptyList(),    // hot, cold, rainy, any
    val time: List<String> = emptyList(),       // quick, medium, long
    val dayType: List<String> = emptyList(),    // weekday, weekend, any
    val timeOfDay: List<String> = emptyList(),  // morning, afternoon, evening, night
    val custom: Map<String, String> = emptyMap()
)

@Serializable
data class DecisionRecord(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val optionId: String,
    val optionName: String,
    val timestamp: Long,
    val wasAccepted: Boolean,
    val rejectedOptions: List<String> = emptyList()
)

@Serializable
data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastDecisionDate: String = "",
    val totalDecisions: Int = 0,
    val totalAccepted: Int = 0,
    val totalRejected: Int = 0
)

@Serializable
data class AppSettings(
    val themeMode: String = "system",           // light, dark, amoled, system
    val colorPalette: String = "dynamic",       // dynamic, ocean, sunset, forest, lavender
    val hapticEnabled: Boolean = true,
    val shakeToDecide: Boolean = true,
    val showConfetti: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val autoAcceptTimer: Boolean = false,
    val dietaryRestrictions: List<String> = emptyList(),
    val fitnessLevel: String = "moderate",      // beginner, moderate, advanced
    val currentWeather: String = "any"
)

@Serializable
data class SharedCategory(
    val categoryId: String,
    val shareCode: String,
    val partnerPreferences: List<DecisionOption> = emptyList()
)

// Default categories users start with
fun defaultCategories(): List<Category> = listOf(
    Category(
        id = "meals",
        name = "Meals",
        icon = "restaurant",
        options = defaultMealOptions()
    ),
    Category(
        id = "workouts",
        name = "Workouts",
        icon = "fitness_center",
        options = defaultWorkoutOptions()
    ),
    Category(
        id = "outfits",
        name = "Outfits",
        icon = "checkroom",
        options = emptyList()
    ),
    Category(
        id = "movies_shows",
        name = "Movies & Shows",
        icon = "movie",
        options = emptyList()
    ),
    Category(
        id = "date_night",
        name = "Date Night",
        icon = "favorite",
        options = defaultDateNightOptions()
    ),
    Category(
        id = "weekend",
        name = "Weekend Activities",
        icon = "weekend",
        options = defaultWeekendOptions()
    )
)

fun defaultMealOptions(): List<DecisionOption> = listOf(
    DecisionOption(id = "meal_1", name = "Pasta", tags = OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("medium"))),
    DecisionOption(id = "meal_2", name = "Stir Fry", tags = OptionTags(mood = listOf("energetic"), budget = listOf("cheap"), time = listOf("quick"))),
    DecisionOption(id = "meal_3", name = "Pizza", tags = OptionTags(mood = listOf("lazy"), budget = listOf("moderate"), time = listOf("quick"))),
    DecisionOption(id = "meal_4", name = "Salad", tags = OptionTags(mood = listOf("energetic"), budget = listOf("cheap"), time = listOf("quick"))),
    DecisionOption(id = "meal_5", name = "Tacos", tags = OptionTags(mood = listOf("neutral"), budget = listOf("cheap"), time = listOf("medium"))),
    DecisionOption(id = "meal_6", name = "Sushi", tags = OptionTags(mood = listOf("neutral"), budget = listOf("splurge"), time = listOf("medium"))),
    DecisionOption(id = "meal_7", name = "Burgers", tags = OptionTags(mood = listOf("lazy"), budget = listOf("moderate"), time = listOf("quick"))),
    DecisionOption(id = "meal_8", name = "Soup & Sandwich", tags = OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("quick"), weather = listOf("cold")))
)

fun defaultWorkoutOptions(): List<DecisionOption> = listOf(
    DecisionOption(id = "workout_1", name = "Morning Run", tags = OptionTags(mood = listOf("energetic"), time = listOf("medium"), timeOfDay = listOf("morning"))),
    DecisionOption(id = "workout_2", name = "Yoga", tags = OptionTags(mood = listOf("lazy", "neutral"), time = listOf("medium"), timeOfDay = listOf("morning", "evening"))),
    DecisionOption(id = "workout_3", name = "HIIT", tags = OptionTags(mood = listOf("energetic"), time = listOf("quick"))),
    DecisionOption(id = "workout_4", name = "Weight Training", tags = OptionTags(mood = listOf("energetic"), time = listOf("long"))),
    DecisionOption(id = "workout_5", name = "Walk", tags = OptionTags(mood = listOf("lazy"), time = listOf("medium"), weather = listOf("any"))),
    DecisionOption(id = "workout_6", name = "Swimming", tags = OptionTags(mood = listOf("energetic"), time = listOf("medium"), weather = listOf("hot")))
)

fun defaultDateNightOptions(): List<DecisionOption> = listOf(
    DecisionOption(id = "date_1", name = "Cook Together", tags = OptionTags(budget = listOf("cheap"), mood = listOf("neutral"))),
    DecisionOption(id = "date_2", name = "Movie Night In", tags = OptionTags(budget = listOf("free"), mood = listOf("lazy"))),
    DecisionOption(id = "date_3", name = "Restaurant", tags = OptionTags(budget = listOf("splurge"), mood = listOf("energetic"))),
    DecisionOption(id = "date_4", name = "Sunset Walk", tags = OptionTags(budget = listOf("free"), mood = listOf("neutral"), weather = listOf("any"))),
    DecisionOption(id = "date_5", name = "Game Night", tags = OptionTags(budget = listOf("free"), mood = listOf("energetic")))
)

fun defaultWeekendOptions(): List<DecisionOption> = listOf(
    DecisionOption(id = "weekend_1", name = "Hike", tags = OptionTags(mood = listOf("energetic"), budget = listOf("free"), weather = listOf("any"), dayType = listOf("weekend"))),
    DecisionOption(id = "weekend_2", name = "Museum", tags = OptionTags(mood = listOf("neutral"), budget = listOf("moderate"), dayType = listOf("weekend"))),
    DecisionOption(id = "weekend_3", name = "Brunch", tags = OptionTags(mood = listOf("lazy"), budget = listOf("moderate"), dayType = listOf("weekend"))),
    DecisionOption(id = "weekend_4", name = "Park Day", tags = OptionTags(mood = listOf("neutral"), budget = listOf("free"), weather = listOf("hot", "any"), dayType = listOf("weekend"))),
    DecisionOption(id = "weekend_5", name = "DIY Project", tags = OptionTags(mood = listOf("energetic"), budget = listOf("cheap"), dayType = listOf("weekend")))
)
