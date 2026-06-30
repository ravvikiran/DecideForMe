package com.decideforme.presentation.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.AppSettings
import com.decideforme.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DecisionRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _exportJson = MutableStateFlow<String?>(null)
    val exportJson: StateFlow<String?> = _exportJson.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                _settings.value = data.settings
            }
        }
    }

    fun updateTheme(mode: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(themeMode = mode)
            repository.updateSettings(updated)
        }
    }

    fun updateColorPalette(palette: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(colorPalette = palette)
            repository.updateSettings(updated)
        }
    }

    fun toggleHaptic(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(hapticEnabled = enabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleShakeToDecide(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(shakeToDecide = enabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleConfetti(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(showConfetti = enabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(dailyReminderEnabled = enabled)
            repository.updateSettings(updated)
        }
    }

    fun toggleAutoAcceptTimer(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _settings.value.copy(autoAcceptTimer = enabled)
            repository.updateSettings(updated)
        }
    }

    fun updateWeather(weather: String) {
        viewModelScope.launch {
            val updated = _settings.value.copy(currentWeather = weather)
            repository.updateSettings(updated)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _exportJson.value = repository.exportData()
        }
    }

    fun clearExport() {
        _exportJson.value = null
    }

    fun importData(json: String) {
        viewModelScope.launch {
            repository.importData(json)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance section
        SettingsSection(title = "Appearance") {
            // Theme mode
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val themes = listOf("system" to "System", "light" to "Light", "dark" to "Dark", "amoled" to "AMOLED")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { (value, label) ->
                    FilterChip(
                        selected = settings.themeMode == value,
                        onClick = { viewModel.updateTheme(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color palette
            Text(
                text = "Color Palette",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val palettes = listOf("dynamic" to "Dynamic", "ocean" to "Ocean", "sunset" to "Sunset", "forest" to "Forest", "lavender" to "Lavender")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                palettes.forEach { (value, label) ->
                    FilterChip(
                        selected = settings.colorPalette == value,
                        onClick = { viewModel.updateColorPalette(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interaction section
        SettingsSection(title = "Interaction") {
            SettingsSwitch(
                title = "Haptic Feedback",
                subtitle = "Vibrate on decisions",
                checked = settings.hapticEnabled,
                onCheckedChange = viewModel::toggleHaptic
            )
            SettingsSwitch(
                title = "Shake to Decide",
                subtitle = "Shake your phone for a random decision",
                checked = settings.shakeToDecide,
                onCheckedChange = viewModel::toggleShakeToDecide
            )
            SettingsSwitch(
                title = "Confetti",
                subtitle = "Celebrate streaks with confetti",
                checked = settings.showConfetti,
                onCheckedChange = viewModel::toggleConfetti
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Context section
        SettingsSection(title = "Context") {
            Text(
                text = "Current Weather/Mood",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val weatherOptions = listOf("any" to "Any", "hot" to "Hot", "cold" to "Cold", "rainy" to "Rainy")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weatherOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = settings.currentWeather == value,
                        onClick = { viewModel.updateWeather(value) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data section
        SettingsSection(title = "Data") {
            OutlinedButton(
                onClick = {
                    viewModel.exportData()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export All Data (JSON)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { /* Import flow - would open file picker */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Data")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications section
        SettingsSection(title = "Reminders") {
            SettingsSwitch(
                title = "Daily Reminder",
                subtitle = "Get a nudge to keep your streak alive",
                checked = settings.dailyReminderEnabled,
                onCheckedChange = { viewModel.toggleDailyReminder(it) }
            )
            SettingsSwitch(
                title = "Auto-Accept Timer",
                subtitle = "Auto-accept after 10s (speed round mode)",
                checked = settings.autoAcceptTimer,
                onCheckedChange = { viewModel.toggleAutoAcceptTimer(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About
        SettingsSection(title = "About") {
            Text(
                text = "DecideForMe v1.0.0",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "All data stored locally. No cloud. No tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Made with ♥ and zero decision fatigue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
