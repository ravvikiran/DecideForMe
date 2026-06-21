package com.decideforme.domain

import com.decideforme.data.model.Category
import com.decideforme.data.model.DecisionOption
import com.decideforme.data.model.OptionTags
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides intelligent option suggestions based on category type.
 * Helps users quickly populate their categories without having to
 * think of everything manually — reducing decision fatigue from the start.
 */
@Singleton
class SmartSuggestions @Inject constructor() {

    data class Suggestion(
        val name: String,
        val tags: OptionTags = OptionTags()
    )

    fun getSuggestionsForCategory(categoryId: String): List<Suggestion> {
        return when (categoryId) {
            "meals" -> mealSuggestions
            "workouts" -> workoutSuggestions
            "outfits" -> outfitSuggestions
            "movies_shows" -> movieSuggestions
            "date_night" -> dateNightSuggestions
            "weekend" -> weekendSuggestions
            else -> generalSuggestions
        }
    }

    private val mealSuggestions = listOf(
        Suggestion("Chicken Alfredo", OptionTags(mood = listOf("lazy"), budget = listOf("moderate"), time = listOf("medium"))),
        Suggestion("Grilled Salmon", OptionTags(mood = listOf("energetic"), budget = listOf("splurge"), time = listOf("medium"))),
        Suggestion("Buddha Bowl", OptionTags(mood = listOf("energetic"), budget = listOf("moderate"), time = listOf("medium"))),
        Suggestion("Ramen", OptionTags(mood = listOf("neutral"), budget = listOf("moderate"), time = listOf("medium"), weather = listOf("cold"))),
        Suggestion("Breakfast for Dinner", OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("quick"))),
        Suggestion("Thai Curry", OptionTags(mood = listOf("neutral"), budget = listOf("moderate"), time = listOf("medium"))),
        Suggestion("Grilled Cheese & Tomato Soup", OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("quick"), weather = listOf("cold"))),
        Suggestion("Poke Bowl", OptionTags(mood = listOf("energetic"), budget = listOf("moderate"), time = listOf("quick"))),
        Suggestion("BBQ Night", OptionTags(mood = listOf("energetic"), budget = listOf("moderate"), time = listOf("long"), weather = listOf("hot"))),
        Suggestion("Leftovers", OptionTags(mood = listOf("lazy"), budget = listOf("free"), time = listOf("quick"))),
        Suggestion("Smoothie Bowl", OptionTags(mood = listOf("energetic"), budget = listOf("cheap"), time = listOf("quick"), timeOfDay = listOf("morning"))),
        Suggestion("Mac & Cheese", OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("quick"))),
        Suggestion("Spaghetti Bolognese", OptionTags(mood = listOf("neutral"), budget = listOf("cheap"), time = listOf("medium"))),
        Suggestion("Fish Tacos", OptionTags(mood = listOf("neutral"), budget = listOf("moderate"), time = listOf("medium"))),
        Suggestion("Quesadillas", OptionTags(mood = listOf("lazy"), budget = listOf("cheap"), time = listOf("quick")))
    )

    private val workoutSuggestions = listOf(
        Suggestion("Pilates", OptionTags(mood = listOf("neutral"), time = listOf("medium"))),
        Suggestion("Cycling", OptionTags(mood = listOf("energetic"), time = listOf("medium"), weather = listOf("any"))),
        Suggestion("Jump Rope", OptionTags(mood = listOf("energetic"), time = listOf("quick"))),
        Suggestion("Dance Workout", OptionTags(mood = listOf("energetic"), time = listOf("medium"))),
        Suggestion("Rock Climbing", OptionTags(mood = listOf("energetic"), time = listOf("long"), budget = listOf("moderate"))),
        Suggestion("Stretching", OptionTags(mood = listOf("lazy"), time = listOf("quick"), timeOfDay = listOf("morning", "evening"))),
        Suggestion("Kickboxing", OptionTags(mood = listOf("energetic"), time = listOf("medium"))),
        Suggestion("Stairmaster", OptionTags(mood = listOf("energetic"), time = listOf("medium"))),
        Suggestion("Rowing", OptionTags(mood = listOf("energetic"), time = listOf("medium"))),
        Suggestion("Rest Day", OptionTags(mood = listOf("lazy"), time = listOf("quick")))
    )

    private val outfitSuggestions = listOf(
        Suggestion("Business Casual", OptionTags(dayType = listOf("weekday"))),
        Suggestion("Athleisure", OptionTags(mood = listOf("lazy", "energetic"))),
        Suggestion("All Black", OptionTags(mood = listOf("neutral"))),
        Suggestion("Jeans & Nice Top", OptionTags(mood = listOf("neutral"))),
        Suggestion("Dress Up", OptionTags(mood = listOf("energetic"), dayType = listOf("weekend"))),
        Suggestion("Comfy & Cozy", OptionTags(mood = listOf("lazy"), weather = listOf("cold"))),
        Suggestion("Summer Vibes", OptionTags(weather = listOf("hot"))),
        Suggestion("Smart Casual", OptionTags(dayType = listOf("weekday", "weekend"))),
        Suggestion("Monochrome", OptionTags(mood = listOf("neutral"))),
        Suggestion("Layered Look", OptionTags(weather = listOf("cold")))
    )

    private val movieSuggestions = listOf(
        Suggestion("Comedy", OptionTags(mood = listOf("lazy", "neutral"))),
        Suggestion("Action/Thriller", OptionTags(mood = listOf("energetic"))),
        Suggestion("Documentary", OptionTags(mood = listOf("neutral"))),
        Suggestion("Animated Movie", OptionTags(mood = listOf("lazy"))),
        Suggestion("Horror", OptionTags(mood = listOf("energetic"), timeOfDay = listOf("evening", "night"))),
        Suggestion("Rom-Com", OptionTags(mood = listOf("lazy", "neutral"))),
        Suggestion("Sci-Fi", OptionTags(mood = listOf("energetic"))),
        Suggestion("True Crime Series", OptionTags(mood = listOf("neutral"), time = listOf("long"))),
        Suggestion("Reality TV", OptionTags(mood = listOf("lazy"), time = listOf("quick"))),
        Suggestion("Classic Film", OptionTags(mood = listOf("neutral"), time = listOf("long")))
    )

    private val dateNightSuggestions = listOf(
        Suggestion("Picnic in the Park", OptionTags(budget = listOf("cheap"), weather = listOf("hot", "any"))),
        Suggestion("Karaoke Night", OptionTags(budget = listOf("moderate"), mood = listOf("energetic"))),
        Suggestion("Spa Night at Home", OptionTags(budget = listOf("cheap"), mood = listOf("lazy"))),
        Suggestion("Stargazing", OptionTags(budget = listOf("free"), timeOfDay = listOf("night"))),
        Suggestion("Art Class", OptionTags(budget = listOf("moderate"), mood = listOf("energetic"))),
        Suggestion("Farmers Market", OptionTags(budget = listOf("moderate"), timeOfDay = listOf("morning"), dayType = listOf("weekend"))),
        Suggestion("Mini Golf", OptionTags(budget = listOf("moderate"), mood = listOf("energetic"))),
        Suggestion("Bookstore + Coffee", OptionTags(budget = listOf("cheap"), mood = listOf("neutral"))),
        Suggestion("Cook a New Recipe", OptionTags(budget = listOf("moderate"), mood = listOf("energetic"))),
        Suggestion("Drive & Explore", OptionTags(budget = listOf("cheap"), time = listOf("long")))
    )

    private val weekendSuggestions = listOf(
        Suggestion("Beach Day", OptionTags(weather = listOf("hot"), budget = listOf("free"))),
        Suggestion("Farmers Market", OptionTags(timeOfDay = listOf("morning"), budget = listOf("moderate"))),
        Suggestion("Board Games", OptionTags(mood = listOf("lazy"), budget = listOf("free"), weather = listOf("rainy"))),
        Suggestion("Bike Ride", OptionTags(mood = listOf("energetic"), budget = listOf("free"), weather = listOf("any"))),
        Suggestion("Cooking Challenge", OptionTags(mood = listOf("energetic"), budget = listOf("moderate"))),
        Suggestion("Thrift Shopping", OptionTags(mood = listOf("neutral"), budget = listOf("cheap"))),
        Suggestion("Photography Walk", OptionTags(mood = listOf("neutral"), budget = listOf("free"))),
        Suggestion("Volunteer", OptionTags(mood = listOf("energetic"), budget = listOf("free"))),
        Suggestion("Movie Marathon", OptionTags(mood = listOf("lazy"), budget = listOf("free"), weather = listOf("rainy"))),
        Suggestion("Try a New Cafe", OptionTags(mood = listOf("neutral"), budget = listOf("moderate")))
    )

    private val generalSuggestions = listOf(
        Suggestion("Option A"),
        Suggestion("Option B"),
        Suggestion("Option C"),
        Suggestion("Something New"),
        Suggestion("The Usual")
    )
}
