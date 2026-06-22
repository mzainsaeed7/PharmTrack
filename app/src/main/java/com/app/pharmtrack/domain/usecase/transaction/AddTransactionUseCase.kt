package com.app.pharmtrack.domain.usecase.transaction

import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.model.TransactionType
import com.app.pharmtrack.domain.repository.TransactionRepository
import javax.inject.Inject

sealed class StockResult {
    data class Success(val newStock: Int) : StockResult()
    data class Error(val message: String) : StockResult()
}

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        medicine: Medicine,
        type: TransactionType,
        quantity: Int,
        notes: String?
    ): StockResult {
        // Validate quantity
        if (quantity <= 0) {
            return StockResult.Error("Enter a valid quantity")
        }

        // Validate stock OUT doesn't exceed current stock
        if (type == TransactionType.OUT && quantity > medicine.currentStock) {
            return StockResult.Error("Insufficient stock. Available: ${medicine.currentStock}")
        }

        val newStock = if (type == TransactionType.IN) {
            medicine.currentStock + quantity
        } else {
            medicine.currentStock - quantity
        }

        try {
            // Insert transaction & update stock atomically
            transactionRepository.addTransactionAndUpdateStock(
                StockTransaction(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    type = type,
                    quantity = quantity,
                    notes = notes
                ),
                newStock
            )
            return StockResult.Success(newStock)
        } catch (e: Exception) {
            return StockResult.Error(e.message ?: "Failed to record stock movement")
        }
    }
}
