package com.decideforme.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.DecisionRecord
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.presentation.components.EmptyStateView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HistoryUiState(
    val allRecords: List<DecisionRecord> = emptyList(),
    val records: Map<String, List<DecisionRecord>> = emptyMap(),
    val viewMode: HistoryViewMode = HistoryViewMode.LIST
)

enum class HistoryViewMode { LIST, CALENDAR }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                val grouped = data.decisionHistory
                    .filter { it.wasAccepted }
                    .sortedByDescending { it.timestamp }
                    .groupBy { record ->
                        val date = Instant.ofEpochMilli(record.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        formatDate(date)
                    }
                _uiState.value = HistoryUiState(
                    allRecords = data.decisionHistory,
                    records = grouped,
                    viewMode = _uiState.value.viewMode
                )
            }
        }
    }

    fun setViewMode(mode: HistoryViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    private fun formatDate(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Decision History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // View mode toggle
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = uiState.viewMode == HistoryViewMode.LIST,
                    onClick = { viewModel.setViewMode(HistoryViewMode.LIST) },
                    label = { Icon(Icons.Default.List, contentDescription = "List view", modifier = Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = uiState.viewMode == HistoryViewMode.CALENDAR,
                    onClick = { viewModel.setViewMode(HistoryViewMode.CALENDAR) },
                    label = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar view", modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.allRecords.isEmpty()) {
            // Empty state
            EmptyStateView(
                title = "No decisions yet",
                subtitle = "Tap the Decide button to get started.\nYour history will appear here.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (uiState.viewMode) {
                HistoryViewMode.CALENDAR -> {
                    DecisionCalendarView(
                        records = uiState.allRecords,
                        onDaySelected = { /* Already handled inside the calendar */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                HistoryViewMode.LIST -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.records.forEach { (date, records) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(records) { record ->
                                HistoryCard(record)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(record: DecisionRecord) {
    val time = remember(record.timestamp) {
        Instant.ofEpochMilli(record.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.optionName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = record.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.rejectedOptions.isNotEmpty()) {
                    Text(
                        text = "Skipped ${record.rejectedOptions.size} before this",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
