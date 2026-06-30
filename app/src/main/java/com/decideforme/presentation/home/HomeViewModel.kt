package com.decideforme.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.AppData
import com.decideforme.data.model.Category
import com.decideforme.data.model.DecisionOption
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.domain.DecisionEngine
import com.decideforme.domain.MoodTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val currentStreak: Int = 0,
    val todayDecisions: Int = 0,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val currentDecision: DecisionOption? = null,
    val rejectedThisSession: List<String> = emptyList(),
    val isDeciding: Boolean = false,
    val showResult: Boolean = false,
    val greeting: String = "",
    val currentMood: String = "neutral",
    val moodInsight: String = "",
    val lastDecision: DecisionOption? = null,
    val noMoreOptions: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DecisionRepository,
    private val decisionEngine: DecisionEngine,
    private val moodTracker: MoodTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                updateState(data)
            }
        }
    }

    private fun updateState(data: AppData) {
        val enabledCategories = data.categories.filter { it.isEnabled && it.options.isNotEmpty() }

        // Use device local timezone for "today" calculation
        val todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val todayDecisions = data.decisionHistory.count {
            it.timestamp >= todayStart && it.wasAccepted
        }

        // Get mood insight
        val insight = moodTracker.inferMood(data.decisionHistory.takeLast(20))

        _uiState.value = _uiState.value.copy(
            userName = data.userProfile.displayName,
            currentStreak = data.streaks.currentStreak,
            todayDecisions = todayDecisions,
            categories = enabledCategories,
            greeting = getGreeting(data.userProfile.displayName),
            moodInsight = insight.pattern
        )
    }

    fun setMood(mood: String) {
        _uiState.value = _uiState.value.copy(currentMood = mood)
    }

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            rejectedThisSession = emptyList(),
            showResult = false,
            noMoreOptions = false
        )
    }

    fun decide() {
        val category = _uiState.value.selectedCategory ?: run {
            // Auto-pick a random enabled category
            val categories = _uiState.value.categories
            if (categories.isEmpty()) return
            val picked = categories.random()
            _uiState.value = _uiState.value.copy(selectedCategory = picked)
            picked
        }

        val recentIds = repository.currentData.decisionHistory
            .filter { it.categoryId == category.id }
            .sortedByDescending { it.timestamp }
            .take(3)
            .map { it.optionId }

        val context = DecisionEngine.DecisionContext(
            weather = repository.currentData.settings.currentWeather,
            mood = _uiState.value.currentMood,
            recentOptionIds = recentIds
        )

        val decision = decisionEngine.decide(
            category = category,
            context = context,
            excludeIds = _uiState.value.rejectedThisSession
        )

        if (decision == null) {
            // No more options available — inform user
            _uiState.value = _uiState.value.copy(
                noMoreOptions = true,
                showResult = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                currentDecision = decision,
                isDeciding = false,
                showResult = true,
                noMoreOptions = false
            )
        }
    }

    fun acceptDecision() {
        val decision = _uiState.value.currentDecision ?: return
        val category = _uiState.value.selectedCategory ?: return

        viewModelScope.launch {
            repository.recordDecision(
                categoryId = category.id,
                categoryName = category.name,
                option = decision,
                wasAccepted = true,
                rejectedOptions = _uiState.value.rejectedThisSession
            )
            _uiState.value = _uiState.value.copy(
                showResult = false,
                currentDecision = null,
                lastDecision = decision,
                selectedCategory = null,
                rejectedThisSession = emptyList()
            )
        }
    }

    fun rejectAndGetAnother() {
        val decision = _uiState.value.currentDecision ?: return
        val category = _uiState.value.selectedCategory ?: return

        viewModelScope.launch {
            repository.recordDecision(
                categoryId = category.id,
                categoryName = category.name,
                option = decision,
                wasAccepted = false
            )
        }

        _uiState.value = _uiState.value.copy(
            rejectedThisSession = _uiState.value.rejectedThisSession + decision.id
        )
        decide()
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(
            showResult = false,
            currentDecision = null,
            selectedCategory = null,
            rejectedThisSession = emptyList(),
            noMoreOptions = false
        )
    }

    fun undoLastDecision() {
        viewModelScope.launch {
            repository.undoLastDecision()
            _uiState.value = _uiState.value.copy(lastDecision = null)
        }
    }

    private fun getGreeting(name: String): String {
        val hour = java.time.LocalDateTime.now().hour
        val timeGreeting = when {
            hour in 5..11 -> "Good morning"
            hour in 12..16 -> "Good afternoon"
            hour in 17..20 -> "Good evening"
            else -> "Hey there"
        }
        return if (name.isNotBlank()) "$timeGreeting, $name" else timeGreeting
    }
}
