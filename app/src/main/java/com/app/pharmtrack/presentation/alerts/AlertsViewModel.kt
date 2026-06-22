package com.app.pharmtrack.presentation.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    val lowStockMedicines: StateFlow<List<Medicine>> = medicineRepository.getLowStockMedicines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val expiringSoonMedicines: StateFlow<List<Medicine>> = medicineRepository.getExpiringSoonMedicines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val expiredMedicines: StateFlow<List<Medicine>> = medicineRepository.getExpiredMedicines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
