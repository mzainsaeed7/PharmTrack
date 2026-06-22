package com.app.pharmtrack.presentation.medicines.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.usecase.medicine.DeleteMedicineUseCase
import com.app.pharmtrack.domain.usecase.medicine.GetMedicineUseCase
import com.app.pharmtrack.domain.usecase.transaction.GetTransactionsByMedicineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicineDetailUiState(
    val medicine: Medicine? = null,
    val transactions: List<StockTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    private val getMedicineUseCase: GetMedicineUseCase,
    private val getTransactionsByMedicineUseCase: GetTransactionsByMedicineUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase
) : ViewModel() {

    private val _medicineId = MutableStateFlow<Long>(-1L)
    private val _isDeleted = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MedicineDetailUiState> = combine(
        _medicineId.flatMapLatest { id ->
            if (id == -1L) flowOf(null) else getMedicineUseCase(id)
        },
        _medicineId.flatMapLatest { id ->
            if (id == -1L) flowOf(emptyList()) else getTransactionsByMedicineUseCase(id)
        },
        _isDeleted,
        _error
    ) { medicine, transactions, isDeleted, error ->
        MedicineDetailUiState(
            medicine = medicine,
            transactions = transactions,
            isLoading = medicine == null && _medicineId.value != -1L,
            isDeleted = isDeleted,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MedicineDetailUiState())

    fun setMedicineId(id: Long) {
        _medicineId.value = id
    }

    fun deleteMedicine() {
        val medicine = uiState.value.medicine ?: return
        viewModelScope.launch {
            try {
                deleteMedicineUseCase(medicine)
                _isDeleted.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete medicine"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
