package com.decideforme.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.Category
import com.decideforme.data.model.DecisionOption
import com.decideforme.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                _uiState.value = CategoriesUiState(categories = data.categories)
            }
        }
    }

    fun toggleCategory(categoryId: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleCategory(categoryId, enabled)
        }
    }

    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            repository.addCustomCategory(name, icon)
        }
    }

    fun addOption(categoryId: String, option: DecisionOption) {
        viewModelScope.launch {
            repository.addOptionToCategory(categoryId, option)
        }
    }

    fun removeOption(categoryId: String, optionId: String) {
        viewModelScope.launch {
            repository.removeOption(categoryId, optionId)
        }
    }
}
