package com.app.pharmtrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.app.pharmtrack.data.local.dao.MedicineDao
import com.app.pharmtrack.data.local.dao.TransactionDao
import com.app.pharmtrack.data.local.entity.MedicineEntity
import com.app.pharmtrack.data.local.entity.TransactionEntity

@Database(
    entities = [MedicineEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun transactionDao(): TransactionDao
}
