package com.app.pharmtrack.presentation.medicines.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import com.app.pharmtrack.domain.usecase.medicine.AddMedicineUseCase
import com.app.pharmtrack.domain.usecase.medicine.UpdateMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val medicine: Medicine? = null,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
    val stockError: String? = null,
    val reorderError: String? = null,
    val pricingWarning: String? = null,
    val expiryWarning: String? = null,
    val error: String? = null
)

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val addMedicineUseCase: AddMedicineUseCase,
    private val updateMedicineUseCase: UpdateMedicineUseCase,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    fun loadMedicine(medicineId: Long) {
        if (medicineId == -1L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val medicine = medicineRepository.getMedicineByIdOnce(medicineId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isEditMode = true,
                medicine = medicine
            )
        }
    }

    fun saveMedicine(
        name: String,
        genericName: String,
        category: String,
        manufacturer: String,
        unitType: String,
        purchasePriceStr: String,
        sellingPriceStr: String,
        currentStockStr: String,
        reorderLevelStr: String,
        batchNumber: String,
        expiryDate: Long?,
        existingId: Long = -1L
    ) {
        // ── Validation ──────────────────────────────────────────────────────
        var hasError = false

        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Medicine name is required")
            hasError = true
        } else {
            _uiState.value = _uiState.value.copy(nameError = null)
        }

        val currentStock = currentStockStr.toIntOrNull()
        if (currentStock == null) {
            _uiState.value = _uiState.value.copy(stockError = "Enter a valid stock quantity")
            hasError = true
        } else {
            _uiState.value = _uiState.value.copy(stockError = null)
        }

        val reorderLevel = reorderLevelStr.toIntOrNull()
        if (reorderLevel == null) {
            _uiState.value = _uiState.value.copy(reorderError = "Enter a valid reorder level")
            hasError = true
        } else {
            _uiState.value = _uiState.value.copy(reorderError = null)
        }

        if (hasError) return

        val purchasePrice = purchasePriceStr.toDoubleOrNull()
        val sellingPrice = sellingPriceStr.toDoubleOrNull()

        val pricingWarning = if (purchasePrice != null && sellingPrice != null
            && sellingPrice < purchasePrice
        ) "Selling price is lower than purchase price" else null

        val expiryWarning = if (expiryDate != null && expiryDate < System.currentTimeMillis())
            "This medicine may already be expired" else null

        _uiState.value = _uiState.value.copy(
            pricingWarning = pricingWarning,
            expiryWarning = expiryWarning
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val medicine = Medicine(
                    id = if (existingId == -1L) 0L else existingId,
                    name = name.trim(),
                    genericName = genericName.trim().ifBlank { null },
                    category = category.trim().ifBlank { null },
                    manufacturer = manufacturer.trim().ifBlank { null },
                    unitType = unitType.trim().ifBlank { null },
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    currentStock = currentStock!!,
                    reorderLevel = reorderLevel!!,
                    batchNumber = batchNumber.trim().ifBlank { null },
                    expiryDate = expiryDate
                )
                if (existingId == -1L) {
                    addMedicineUseCase(medicine)
                } else {
                    updateMedicineUseCase(medicine)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save medicine"
                )
            }
        }
    }

    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            nameError = null,
            stockError = null,
            reorderError = null,
            error = null
        )
    }
}
