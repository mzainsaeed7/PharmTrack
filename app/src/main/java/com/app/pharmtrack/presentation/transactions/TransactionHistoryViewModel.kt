package com.app.pharmtrack.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.repository.TransactionRepository
import com.app.pharmtrack.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class DateFilter { TODAY, THIS_WEEK, THIS_MONTH, ALL }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(DateFilter.ALL)
    val filter: StateFlow<DateFilter> = _filter.asStateFlow()

    val transactions: StateFlow<List<StockTransaction>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                DateFilter.TODAY -> {
                    val from = DateUtils.startOfDay(System.currentTimeMillis())
                    val to = System.currentTimeMillis()
                    transactionRepository.getTransactionsByDateRange(from, to)
                }
                DateFilter.THIS_WEEK -> {
                    val from = DateUtils.startOfWeek()
                    val to = System.currentTimeMillis()
                    transactionRepository.getTransactionsByDateRange(from, to)
                }
                DateFilter.THIS_MONTH -> {
                    val from = DateUtils.startOfMonth()
                    val to = System.currentTimeMillis()
                    transactionRepository.getTransactionsByDateRange(from, to)
                }
                DateFilter.ALL -> {
                    transactionRepository.getAllTransactions()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(dateFilter: DateFilter) {
        _filter.value = dateFilter
    }
}
