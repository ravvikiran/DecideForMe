package com.decideforme.data.repository

import android.content.Context
import com.decideforme.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class DecisionRepository(
    private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataFile: File
        get() = File(context.filesDir, "decideforme_data.json")

    private val mutex = Mutex()
    private val _appData = MutableStateFlow(AppData())
    val appData: Flow<AppData> = _appData.asStateFlow()

    val currentData: AppData get() = _appData.value

    private var isInitialized = false

    suspend fun initialize() {
        if (isInitialized) return
        mutex.withLock {
            if (isInitialized) return // Double-check after acquiring lock
            val data = loadData()
            _appData.value = data
            isInitialized = true
        }
    }

    private suspend fun loadData(): AppData = withContext(Dispatchers.IO) {
        try {
            if (dataFile.exists()) {
                val content = dataFile.readText()
                json.decodeFromString<AppData>(content)
            } else {
                AppData()
            }
        } catch (e: Exception) {
            // If JSON is corrupted, try reading backup
            val backupFile = File(context.filesDir, "decideforme_data.json.bak")
            if (backupFile.exists()) {
                try {
                    val content = backupFile.readText()
                    json.decodeFromString<AppData>(content)
                } catch (_: Exception) {
                    AppData()
                }
            } else {
                AppData()
            }
        }
    }

    /**
     * Atomic file write: write to temp file, then rename.
     * Prevents data corruption if app crashes mid-write.
     */
    private suspend fun saveData(data: AppData) {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(data)
                val tempFile = File(context.filesDir, "decideforme_data.json.tmp")
                val backupFile = File(context.filesDir, "decideforme_data.json.bak")

                // Write to temp file
                tempFile.writeText(content)

                // Backup current file
                if (dataFile.exists()) {
                    dataFile.copyTo(backupFile, overwrite = true)
                }

                // Atomic rename
                tempFile.renameTo(dataFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Emit new state — called within mutex so ordering is guaranteed
        _appData.value = data
    }

    suspend fun updateProfile(profile: UserProfile) {
        mutex.withLock {
            val updated = _appData.value.copy(userProfile = profile)
            saveData(updated)
        }
    }

    suspend fun completeOnboarding() {
        mutex.withLock {
            val updated = _appData.value.copy(
                userProfile = _appData.value.userProfile.copy(onboardingCompleted = true)
            )
            saveData(updated)
        }
    }

    suspend fun updateCategories(categories: List<Category>) {
        mutex.withLock {
            val updated = _appData.value.copy(categories = categories)
            saveData(updated)
        }
    }

    suspend fun addOptionToCategory(categoryId: String, option: DecisionOption) {
        mutex.withLock {
            val categories = _appData.value.categories.map { category ->
                if (category.id == categoryId) {
                    category.copy(options = category.options + option)
                } else category
            }
            val updated = _appData.value.copy(categories = categories)
            saveData(updated)
        }
    }

    suspend fun removeOption(categoryId: String, optionId: String) {
        mutex.withLock {
            val categories = _appData.value.categories.map { category ->
                if (category.id == categoryId) {
                    category.copy(options = category.options.filter { it.id != optionId })
                } else category
            }
            val updated = _appData.value.copy(categories = categories)
            saveData(updated)
        }
    }

    suspend fun recordDecision(
        categoryId: String,
        categoryName: String,
        option: DecisionOption,
        wasAccepted: Boolean,
        rejectedOptions: List<String> = emptyList()
    ) {
        mutex.withLock {
            val record = DecisionRecord(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                categoryName = categoryName,
                optionId = option.id,
                optionName = option.name,
                timestamp = System.currentTimeMillis(),
                wasAccepted = wasAccepted,
                rejectedOptions = rejectedOptions
            )

            // Update option weights
            val categories = _appData.value.categories.map { category ->
                if (category.id == categoryId) {
                    category.copy(options = category.options.map { opt ->
                        when {
                            opt.id == option.id && wasAccepted -> opt.copy(
                                weight = (opt.weight * 1.1).coerceAtMost(5.0),
                                timesShown = opt.timesShown + 1,
                                timesAccepted = opt.timesAccepted + 1,
                                lastShown = System.currentTimeMillis()
                            )
                            opt.id == option.id && !wasAccepted -> opt.copy(
                                weight = (opt.weight * 0.9).coerceAtLeast(0.1),
                                timesShown = opt.timesShown + 1,
                                timesRejected = opt.timesRejected + 1,
                                lastShown = System.currentTimeMillis()
                            )
                            else -> opt
                        }
                    })
                } else category
            }

            // Update streaks using device local timezone
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val streaks = _appData.value.streaks

            val newStreak = when {
                streaks.lastDecisionDate == today -> streaks.currentStreak
                streaks.lastDecisionDate == yesterday -> streaks.currentStreak + 1
                streaks.lastDecisionDate.isEmpty() -> 1
                else -> 1 // Streak broken
            }

            val updatedStreaks = streaks.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(streaks.longestStreak, newStreak),
                lastDecisionDate = today,
                totalDecisions = streaks.totalDecisions + 1,
                totalAccepted = if (wasAccepted) streaks.totalAccepted + 1 else streaks.totalAccepted,
                totalRejected = if (!wasAccepted) streaks.totalRejected + 1 else streaks.totalRejected
            )

            // Prune history: keep last 1000 entries to prevent unbounded growth
            val newHistory = (_appData.value.decisionHistory + record).takeLast(MAX_HISTORY_ENTRIES)

            val updated = _appData.value.copy(
                categories = categories,
                decisionHistory = newHistory,
                streaks = updatedStreaks
            )
            saveData(updated)
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        mutex.withLock {
            val updated = _appData.value.copy(settings = settings)
            saveData(updated)
        }
    }

    suspend fun exportData(): String {
        return json.encodeToString(_appData.value)
    }

    suspend fun importData(jsonString: String): Boolean {
        return try {
            val data = json.decodeFromString<AppData>(jsonString)
            mutex.withLock {
                saveData(data)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addCustomCategory(name: String, icon: String): Category {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = icon,
            isDefault = false
        )
        mutex.withLock {
            val updated = _appData.value.copy(
                categories = _appData.value.categories + category
            )
            saveData(updated)
        }
        return category
    }

    suspend fun toggleCategory(categoryId: String, enabled: Boolean) {
        mutex.withLock {
            val categories = _appData.value.categories.map { category ->
                if (category.id == categoryId) category.copy(isEnabled = enabled)
                else category
            }
            val updated = _appData.value.copy(categories = categories)
            saveData(updated)
        }
    }

    /**
     * Removes the last decision from history.
     * Note: Does not reverse weight changes (simple undo).
     */
    suspend fun undoLastDecision(): Boolean {
        mutex.withLock {
            val history = _appData.value.decisionHistory
            if (history.isEmpty()) return false
            val updated = _appData.value.copy(
                decisionHistory = history.dropLast(1)
            )
            saveData(updated)
            return true
        }
    }

    companion object {
        private const val MAX_HISTORY_ENTRIES = 1000
    }
}
