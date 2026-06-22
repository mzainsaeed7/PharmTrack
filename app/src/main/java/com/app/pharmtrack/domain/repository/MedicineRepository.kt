package com.app.pharmtrack.domain.repository

import com.app.pharmtrack.domain.model.Medicine
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {

    fun getAllMedicines(): Flow<List<Medicine>>
    fun getMedicineById(id: Long): Flow<Medicine?>
    fun getLowStockMedicines(): Flow<List<Medicine>>
    fun getLowStockCount(): Flow<Int>
    fun getExpiringSoonMedicines(): Flow<List<Medicine>>
    fun getExpiringSoonCount(): Flow<Int>
    fun getExpiredMedicines(): Flow<List<Medicine>>
    fun getTotalMedicineCount(): Flow<Int>
    fun getTotalStockValue(): Flow<Double?>
    fun searchMedicines(query: String): Flow<List<Medicine>>

    suspend fun addMedicine(medicine: Medicine): Long
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(medicine: Medicine)
    suspend fun getMedicineByIdOnce(id: Long): Medicine?
}
