package com.decideforme.presentation.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decideforme.data.model.Category
import com.decideforme.data.model.DecisionOption
import com.decideforme.data.model.SharedCategory
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.domain.DecisionEngine
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@Serializable
data class SharePayload(
    val categoryId: String,
    val categoryName: String,
    val options: List<DecisionOption>,
    val shareCode: String
)

data class ShareUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val shareCode: String = "",
    val qrBitmap: Bitmap? = null,
    val isSharing: Boolean = false,
    val importResult: String? = null,
    val mergedDecision: DecisionOption? = null
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val repository: DecisionRepository,
    private val decisionEngine: DecisionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true; encodeDefaults = true }

    init {
        viewModelScope.launch {
            repository.initialize()
            repository.appData.collect { data ->
                _uiState.value = _uiState.value.copy(
                    categories = data.categories.filter { it.isEnabled }
                )
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun generateShareCode() {
        val category = _uiState.value.selectedCategory ?: return
        val shareCode = UUID.randomUUID().toString().take(8).uppercase()

        val payload = SharePayload(
            categoryId = category.id,
            categoryName = category.name,
            options = category.options,
            shareCode = shareCode
        )

        val payloadJson = json.encodeToString(payload)
        val qrBitmap = generateQrCode(payloadJson)

        _uiState.value = _uiState.value.copy(
            shareCode = shareCode,
            qrBitmap = qrBitmap,
            isSharing = true
        )
    }

    fun importShareData(jsonString: String) {
        viewModelScope.launch {
            try {
                // Input validation
                if (jsonString.length > 500_000) {
                    _uiState.value = _uiState.value.copy(
                        importResult = "Import failed: data too large"
                    )
                    return@launch
                }

                val payload = json.decodeFromString<SharePayload>(jsonString)

                // Validate payload integrity
                if (payload.options.size > 200) {
                    _uiState.value = _uiState.value.copy(
                        importResult = "Import failed: too many options"
                    )
                    return@launch
                }

                // Sanitize options: clamp weights and trim names
                val sanitizedOptions = payload.options.map { option ->
                    option.copy(
                        name = option.name.take(100),
                        weight = option.weight.coerceIn(0.1, 5.0)
                    )
                }
                
                // Save partner preferences
                val shared = SharedCategory(
                    categoryId = payload.categoryId,
                    shareCode = payload.shareCode,
                    partnerPreferences = sanitizedOptions
                )

                val currentData = repository.currentData
                val updatedShared = currentData.sharedCategories
                    .filter { it.categoryId != payload.categoryId } + shared

                // Update app data
                val updatedData = currentData.copy(sharedCategories = updatedShared)
                repository.importData(json.encodeToString(updatedData))

                _uiState.value = _uiState.value.copy(
                    importResult = "Merged ${payload.options.size} options from partner for '${payload.categoryName}'"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    importResult = "Failed to import: ${e.message}"
                )
            }
        }
    }

    /**
     * Decide something both partners would accept.
     * Merges both preference sets and finds options with high combined weight.
     */
    fun decideForCouple(categoryId: String) {
        val currentData = repository.currentData
        val myCategory = currentData.categories.find { it.id == categoryId } ?: return
        val shared = currentData.sharedCategories.find { it.categoryId == categoryId }

        if (shared == null) {
            // No partner data — just use own preferences
            val decision = decisionEngine.decide(myCategory)
            _uiState.value = _uiState.value.copy(mergedDecision = decision)
            return
        }

        // Merge options: find common options and combine weights
        val partnerMap = shared.partnerPreferences.associateBy { it.name.lowercase() }
        val mergedOptions = myCategory.options.map { myOption ->
            val partnerOption = partnerMap[myOption.name.lowercase()]
            if (partnerOption != null) {
                // Both have this option — average the weights, boost it
                myOption.copy(
                    weight = (myOption.weight + partnerOption.weight) / 2 * 1.5
                )
            } else {
                // Only I have this — lower priority
                myOption.copy(weight = myOption.weight * 0.6)
            }
        }

        // Also add partner-only options with lower weight
        val myOptionNames = myCategory.options.map { it.name.lowercase() }.toSet()
        val partnerOnly = shared.partnerPreferences
            .filter { it.name.lowercase() !in myOptionNames }
            .map { it.copy(weight = it.weight * 0.4) }

        val allOptions = mergedOptions + partnerOnly
        val mergedCategory = myCategory.copy(options = allOptions)
        val decision = decisionEngine.decide(mergedCategory)

        _uiState.value = _uiState.value.copy(mergedDecision = decision)
    }

    fun shareViaIntent(context: Context) {
        val category = _uiState.value.selectedCategory ?: return
        val payload = SharePayload(
            categoryId = category.id,
            categoryName = category.name,
            options = category.options,
            shareCode = _uiState.value.shareCode
        )
        val payloadJson = json.encodeToString(payload)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DecideForMe - ${category.name}")
            putExtra(Intent.EXTRA_TEXT, payloadJson)
        }
        context.startActivity(Intent.createChooser(intent, "Share category via"))
    }

    fun dismissImportResult() {
        _uiState.value = _uiState.value.copy(importResult = null)
    }

    fun dismissSharing() {
        _uiState.value.qrBitmap?.recycle()
        _uiState.value = _uiState.value.copy(isSharing = false, qrBitmap = null)
    }

    private fun generateQrCode(content: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
