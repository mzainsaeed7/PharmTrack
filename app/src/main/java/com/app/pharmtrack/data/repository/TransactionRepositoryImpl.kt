package com.app.pharmtrack.data.repository

import androidx.room.withTransaction
import com.app.pharmtrack.data.local.AppDatabase
import com.app.pharmtrack.data.local.dao.MedicineDao
import com.app.pharmtrack.data.local.dao.TransactionDao
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val medicineDao: MedicineDao
) : TransactionRepository {

    override suspend fun addTransaction(transaction: StockTransaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun addTransactionAndUpdateStock(transaction: StockTransaction, newStock: Int) {
        database.withTransaction {
            transactionDao.insertTransaction(transaction.toEntity())
            medicineDao.updateStock(transaction.medicineId, newStock)
        }
    }

    override fun getTransactionsForMedicine(medicineId: Long): Flow<List<StockTransaction>> =
        transactionDao.getTransactionsForMedicine(medicineId).map { list -> list.map { it.toDomain() } }

    override fun getAllTransactions(): Flow<List<StockTransaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getRecentTransactions(limit: Int): Flow<List<StockTransaction>> =
        transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }

    override fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<StockTransaction>> =
        transactionDao.getTransactionsByDateRange(from, to).map { list -> list.map { it.toDomain() } }
}
