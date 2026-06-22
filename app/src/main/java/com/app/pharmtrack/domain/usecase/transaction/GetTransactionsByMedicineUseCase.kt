package com.app.pharmtrack.domain.usecase.transaction

import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsByMedicineUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(medicineId: Long): Flow<List<StockTransaction>> =
        repository.getTransactionsForMedicine(medicineId)
}
