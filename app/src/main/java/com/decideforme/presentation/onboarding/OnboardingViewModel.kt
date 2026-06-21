package com.decideforme.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.Category
import com.decideforme.data.model.UserProfile
import com.decideforme.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val userName: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val dietaryRestrictions: Set<String> = emptySet(),
    val fitnessLevel: String = "moderate",
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
        }
    }

    fun nextPage() {
        _uiState.value = _uiState.value.copy(
            currentPage = (_uiState.value.currentPage + 1).coerceAtMost(_uiState.value.totalPages - 1)
        )
    }

    fun previousPage() {
        _uiState.value = _uiState.value.copy(
            currentPage = (_uiState.value.currentPage - 1).coerceAtLeast(0)
        )
    }

    fun setUserName(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun toggleCategory(categoryId: String) {
        val current = _uiState.value.selectedCategories.toMutableSet()
        if (categoryId in current) current.remove(categoryId) else current.add(categoryId)
        _uiState.value = _uiState.value.copy(selectedCategories = current)
    }

    fun toggleDietaryRestriction(restriction: String) {
        val current = _uiState.value.dietaryRestrictions.toMutableSet()
        if (restriction in current) current.remove(restriction) else current.add(restriction)
        _uiState.value = _uiState.value.copy(dietaryRestrictions = current)
    }

    fun setFitnessLevel(level: String) {
        _uiState.value = _uiState.value.copy(fitnessLevel = level)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            // Update profile
            repository.updateProfile(
                UserProfile(
                    displayName = _uiState.value.userName,
                    onboardingCompleted = true,
                    createdAt = System.currentTimeMillis()
                )
            )

            // Enable only selected categories
            val selectedIds = _uiState.value.selectedCategories
            if (selectedIds.isNotEmpty()) {
                val updatedCategories = repository.currentData.categories.map { category ->
                    category.copy(isEnabled = category.id in selectedIds)
                }
                repository.updateCategories(updatedCategories)
            }

            // Update settings
            val settings = repository.currentData.settings.copy(
                dietaryRestrictions = _uiState.value.dietaryRestrictions.toList(),
                fitnessLevel = _uiState.value.fitnessLevel
            )
            repository.updateSettings(settings)

            repository.completeOnboarding()
            _uiState.value = _uiState.value.copy(isComplete = true)
        }
    }

    fun skipOnboarding() {
        viewModelScope.launch {
            repository.completeOnboarding()
            _uiState.value = _uiState.value.copy(isComplete = true)
        }
    }
}
