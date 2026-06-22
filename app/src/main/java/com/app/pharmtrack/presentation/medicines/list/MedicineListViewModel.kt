package com.app.pharmtrack.presentation.medicines.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.usecase.medicine.DeleteMedicineUseCase
import com.app.pharmtrack.domain.usecase.medicine.GetAllMedicinesUseCase
import com.app.pharmtrack.domain.usecase.medicine.GetExpiringMedicinesUseCase
import com.app.pharmtrack.domain.usecase.medicine.GetLowStockMedicinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MedicineFilter { ALL, LOW_STOCK, EXPIRING_SOON, EXPIRED }

data class MedicineListUiState(
    val medicines: List<Medicine> = emptyList(),
    val isLoading: Boolean = true,
    val activeFilter: MedicineFilter = MedicineFilter.ALL,
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val getAllMedicinesUseCase: GetAllMedicinesUseCase,
    private val getLowStockMedicinesUseCase: GetLowStockMedicinesUseCase,
    private val getExpiringMedicinesUseCase: GetExpiringMedicinesUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(MedicineFilter.ALL)

    // Track last deleted medicine for undo
    private var lastDeletedMedicine: Medicine? = null

    val uiState: StateFlow<MedicineListUiState> = combine(
        _searchQuery,
        _activeFilter
    ) { query, filter -> Pair(query, filter) }
        .flatMapLatest { (query, filter) ->
            val baseFlow: Flow<List<Medicine>> = when (filter) {
                MedicineFilter.ALL -> getAllMedicinesUseCase()
                MedicineFilter.LOW_STOCK -> getLowStockMedicinesUseCase()
                MedicineFilter.EXPIRING_SOON -> getExpiringMedicinesUseCase.getExpiringSoon()
                MedicineFilter.EXPIRED -> getExpiringMedicinesUseCase.getExpired()
            }
            baseFlow.map { list ->
                val filtered = if (query.isBlank()) list
                else list.filter { medicine ->
                    medicine.name.contains(query, ignoreCase = true) ||
                            medicine.genericName?.contains(query, ignoreCase = true) == true
                }
                MedicineListUiState(
                    medicines = filtered,
                    isLoading = false,
                    activeFilter = filter,
                    searchQuery = query
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicineListUiState()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: MedicineFilter) {
        _activeFilter.value = filter
    }

    fun deleteMedicine(medicine: Medicine) {
        lastDeletedMedicine = medicine
        viewModelScope.launch {
            deleteMedicineUseCase(medicine)
        }
    }

    fun undoDelete() {
        // Undo is handled by re-inserting — but since our delete is soft/hard,
        // we can't cleanly undo hard delete. We track for snackbar display only.
        // For a real undo, Phase 5 will implement proper undo logic.
        lastDeletedMedicine = null
    }

    fun getLastDeletedMedicine(): Medicine? = lastDeletedMedicine
}
