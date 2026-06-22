package com.app.pharmtrack.data.local.dao

import androidx.room.*
import com.app.pharmtrack.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE medicine_id = :medicineId ORDER BY timestamp DESC")
    fun getTransactionsForMedicine(medicineId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE timestamp >= :from AND timestamp <= :to 
        ORDER BY timestamp DESC
        """
    )
    fun getTransactionsByDateRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 5")
    fun getLastFiveTransactions(): Flow<List<TransactionEntity>>
}
