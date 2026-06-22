package com.app.pharmtrack.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.TransactionType
import com.app.pharmtrack.domain.repository.MedicineRepository
import com.app.pharmtrack.domain.usecase.transaction.AddTransactionUseCase
import com.app.pharmtrack.domain.usecase.transaction.StockResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockBottomSheetViewModel @Inject constructor(

    private val medicineRepository: MedicineRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _medicineId = MutableStateFlow<Long>(-1L)
    
    private val _transactionType = MutableStateFlow<TransactionType>(TransactionType.IN)
    val transactionType: StateFlow<TransactionType> = _transactionType.asStateFlow()

    val medicine: StateFlow<Medicine?> = _medicineId
        .flatMapLatest { id ->
            if (id == -1L) flowOf(null) else medicineRepository.getMedicineById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val quantityStr = MutableStateFlow("")
    val notes = MutableStateFlow("")

    val previewNewStock: StateFlow<Int?> = combine(medicine, quantityStr, _transactionType) { med, qtyStr, type ->
        if (med == null) return@combine null
        val qty = qtyStr.toIntOrNull() ?: 0
        if (type == TransactionType.IN) {
            med.currentStock + qty
        } else {
            med.currentStock - qty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setup(medicineId: Long, typeStr: String) {
        _medicineId.value = medicineId
        _transactionType.value = try {
            TransactionType.valueOf(typeStr)
        } catch (e: Exception) {
            TransactionType.IN
        }
    }

    fun confirmTransaction() {
        val med = medicine.value ?: return
        val qty = quantityStr.value.toIntOrNull()
        if (qty == null || qty <= 0) {
            _error.value = "Enter a valid quantity"
            return
        }

        viewModelScope.launch {
            val result = addTransactionUseCase(
                medicine = med,
                type = _transactionType.value,
                quantity = qty,
                notes = notes.value.trim().ifBlank { null }
            )
            when (result) {
                is StockResult.Success -> {
                    _isSaved.value = true
                }
                is StockResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
