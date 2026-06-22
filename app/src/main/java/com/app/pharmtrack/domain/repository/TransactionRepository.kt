package com.app.pharmtrack.domain.repository

import com.app.pharmtrack.domain.model.StockTransaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    suspend fun addTransaction(transaction: StockTransaction): Long
    suspend fun addTransactionAndUpdateStock(transaction: StockTransaction, newStock: Int)
    fun getTransactionsForMedicine(medicineId: Long): Flow<List<StockTransaction>>
    fun getAllTransactions(): Flow<List<StockTransaction>>
    fun getRecentTransactions(limit: Int = 5): Flow<List<StockTransaction>>
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<StockTransaction>>
}
