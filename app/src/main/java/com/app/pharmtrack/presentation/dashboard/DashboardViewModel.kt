package com.app.pharmtrack.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.ExpiryStatus
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockStatus
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
    val recentTransactions: List<StockTransaction> = emptyList(),
    val stockStatusDistribution: Map<String, Int> = emptyMap()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        medicineRepository.getAllMedicines(),
        medicineRepository.getTotalStockValue(),
        transactionRepository.getRecentTransactions(5)
    ) { allMeds, stockValue, recentTransactions ->
        
        val total = allMeds.size
        
        // Count low stock: current_stock <= reorder_level
        val lowStock = allMeds.count { it.currentStock <= it.reorderLevel }
        
        // Count expiring soon: warning or critical (excluding expired or no date)
        val expiringSoon = allMeds.count { 
            it.expiryStatus == ExpiryStatus.WARNING || 
            it.expiryStatus == ExpiryStatus.CRITICAL 
        }

        // Low stock list
        val lowStockMeds = allMeds.filter { it.currentStock <= it.reorderLevel }
        // Expiring list
        val expiringMeds = allMeds.filter { 
            it.expiryStatus == ExpiryStatus.WARNING || 
            it.expiryStatus == ExpiryStatus.CRITICAL ||
            it.expiryStatus == ExpiryStatus.EXPIRED
        }

        // Urgent Alerts: Union of low stock and expiring soon medicines, sorted by stock urgency first
        val alerts = (lowStockMeds + expiringMeds)
            .distinctBy { it.id }
            .sortedWith(compareBy<Medicine> { it.currentStock <= it.reorderLevel }
                .thenBy { it.currentStock }
                .thenBy { it.expiryDate ?: Long.MAX_VALUE }
            )
            .take(5)

        // Stock status distribution
        val safeCount = allMeds.count { it.stockStatus == StockStatus.SAFE }
        val lowCount = allMeds.count { it.stockStatus == StockStatus.LOW }
        val criticalCount = allMeds.count { it.stockStatus == StockStatus.CRITICAL_LOW }
        val outOfStockCount = allMeds.count { it.stockStatus == StockStatus.OUT_OF_STOCK }

        val distribution = mapOf(
            "Safe" to safeCount,
            "Low" to lowCount,
            "Critical" to criticalCount,
            "Out of Stock" to outOfStockCount
        )

        DashboardUiState(
            totalMedicines = total,
            lowStockCount = lowStock,
            expiringSoonCount = expiringSoon,
            totalStockValue = stockValue ?: 0.0,
            totalStockValueFormatted = StockStatusHelper.formatStockValue(stockValue),
            urgentAlerts = alerts,
            recentTransactions = recentTransactions,
            stockStatusDistribution = distribution
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )
}
