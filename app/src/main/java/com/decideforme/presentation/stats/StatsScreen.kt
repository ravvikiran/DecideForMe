package com.decideforme.presentation.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.DecisionRecord
import com.decideforme.data.model.StreakData
import com.decideforme.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val streaks: StreakData = StreakData(),
    val topAccepted: List<Pair<String, Int>> = emptyList(),
    val topRejected: List<Pair<String, Int>> = emptyList(),
    val categoryBreakdown: List<Pair<String, Int>> = emptyList(),
    val acceptRate: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: DecisionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                val accepted = data.decisionHistory.filter { it.wasAccepted }
                val rejected = data.decisionHistory.filter { !it.wasAccepted }

                val topAccepted = accepted
                    .groupBy { it.optionName }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key to it.value }

                val topRejected = rejected
                    .groupBy { it.optionName }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key to it.value }

                val categoryBreakdown = accepted
                    .groupBy { it.categoryName }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .map { it.key to it.value }

                val totalDecisions = data.streaks.totalDecisions
                val acceptRate = if (totalDecisions > 0) {
                    (data.streaks.totalAccepted.toFloat() / totalDecisions * 100).toInt()
                } else 0

                _uiState.value = StatsUiState(
                    streaks = data.streaks,
                    topAccepted = topAccepted,
                    topRejected = topRejected,
                    categoryBreakdown = categoryBreakdown,
                    acceptRate = acceptRate
                )
            }
        }
    }
}

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Stats & Streaks",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Streak cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                value = "${uiState.streaks.currentStreak}",
                label = "Current\nStreak",
                highlight = true
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EmojiEvents,
                value = "${uiState.streaks.longestStreak}",
                label = "Longest\nStreak"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.TouchApp,
                value = "${uiState.streaks.totalDecisions}",
                label = "Total\nDecisions"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accept rate
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                        text = "Accept Rate",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "How often you go with my first pick",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${uiState.acceptRate}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top picks
        if (uiState.topAccepted.isNotEmpty()) {
            Text(
                text = "Your Favorites",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.topAccepted.forEachIndexed { index, (name, count) ->
                RankItem(rank = index + 1, name = name, count = count, isAccepted = true)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top rejections
        if (uiState.topRejected.isNotEmpty()) {
            Text(
                text = "You Always Skip",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.topRejected.forEachIndexed { index, (name, count) ->
                RankItem(rank = index + 1, name = name, count = count, isAccepted = false)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category breakdown
        if (uiState.categoryBreakdown.isNotEmpty()) {
            Text(
                text = "Decisions by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            uiState.categoryBreakdown.forEach { (category, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = category, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (highlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RankItem(
    rank: Int,
    name: String,
    count: Int,
    isAccepted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(32.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isAccepted) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        ) {
            Text(
                text = "${count}x",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
