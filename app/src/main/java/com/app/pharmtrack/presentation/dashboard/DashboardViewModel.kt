package com.app.pharmtrack.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.repository.MedicineRepository
import com.app.pharmtrack.domain.repository.TransactionRepository
import com.app.pharmtrack.utils.StockStatusHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val totalMedicines: Int = 0,
    val lowStockCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val totalStockValueFormatted: String = "PKR 0",
    val urgentAlerts: List<Medicine> = emptyList(),
    val recentTransactions: List<StockTransaction> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        medicineRepository.getTotalMedicineCount(),
        medicineRepository.getLowStockCount(),
        medicineRepository.getExpiringSoonCount(),
        medicineRepository.getTotalStockValue(),
        medicineRepository.getLowStockMedicines(),
        medicineRepository.getExpiringSoonMedicines(),
        transactionRepository.getRecentTransactions(5)
    ) { array ->
        val total = array[0] as Int
        val lowStock = array[1] as Int
        val expiringSoon = array[2] as Int
        val stockValue = array[3] as? Double
        @Suppress("UNCHECKED_CAST")
        val lowStockMeds = array[4] as List<Medicine>
        @Suppress("UNCHECKED_CAST")
        val expiringMeds = array[5] as List<Medicine>
        @Suppress("UNCHECKED_CAST")
        val recentTransactions = array[6] as List<StockTransaction>

        // Urgent Alerts: Union of low stock and expiring soon medicines, sorted by stock urgency first
        val alerts = (lowStockMeds + expiringMeds)
            .distinctBy { it.id }
            .sortedWith(compareBy<Medicine> { it.currentStock <= it.reorderLevel }
                .thenBy { it.currentStock }
                .thenBy { it.expiryDate ?: Long.MAX_VALUE }
            )
            .take(5)

        DashboardUiState(
            totalMedicines = total,
            lowStockCount = lowStock,
            expiringSoonCount = expiringSoon,
            totalStockValue = stockValue ?: 0.0,
            totalStockValueFormatted = StockStatusHelper.formatStockValue(stockValue),
            urgentAlerts = alerts,
            recentTransactions = recentTransactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
