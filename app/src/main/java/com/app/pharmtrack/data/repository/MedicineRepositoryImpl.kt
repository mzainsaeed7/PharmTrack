package com.app.pharmtrack.data.repository

import com.app.pharmtrack.data.local.dao.MedicineDao
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepositoryImpl @Inject constructor(
    private val dao: MedicineDao
) : MedicineRepository {

    override fun getAllMedicines(): Flow<List<Medicine>> =
        dao.getAllMedicines().map { list -> list.map { it.toDomain() } }

    override fun getMedicineById(id: Long): Flow<Medicine?> =
        dao.getMedicineById(id).map { it?.toDomain() }

    override fun getLowStockMedicines(): Flow<List<Medicine>> =
        dao.getLowStockMedicines().map { list -> list.map { it.toDomain() } }

    override fun getLowStockCount(): Flow<Int> = dao.getLowStockCount()

    override fun getExpiringSoonMedicines(): Flow<List<Medicine>> {
        val now = System.currentTimeMillis()
        val thirtyDays = 30 * 24 * 60 * 60 * 1000L
        return dao.getExpiringSoonMedicines(now, now + thirtyDays)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getExpiringSoonCount(): Flow<Int> {
        val now = System.currentTimeMillis()
        val thirtyDays = 30 * 24 * 60 * 60 * 1000L
        return dao.getExpiringSoonCount(now, now + thirtyDays)
    }

    override fun getExpiredMedicines(): Flow<List<Medicine>> {
        val now = System.currentTimeMillis()
        return dao.getExpiredMedicines(now).map { list -> list.map { it.toDomain() } }
    }

    override fun getTotalMedicineCount(): Flow<Int> = dao.getTotalMedicineCount()

    override fun getTotalStockValue(): Flow<Double?> = dao.getTotalStockValue()

    override fun searchMedicines(query: String): Flow<List<Medicine>> =
        dao.searchMedicines(query).map { list -> list.map { it.toDomain() } }

    override suspend fun addMedicine(medicine: Medicine): Long =
        dao.insertMedicine(medicine.toEntity())

    override suspend fun updateMedicine(medicine: Medicine) =
        dao.updateMedicine(medicine.toEntity())

    override suspend fun deleteMedicine(medicine: Medicine) {
        // Business rule: soft delete if has transactions, hard delete if none
        val txCount = dao.getTransactionCountForMedicine(medicine.id)
        if (txCount > 0) {
            dao.softDeleteMedicine(medicine.id)
        } else {
            dao.hardDeleteMedicine(medicine.toEntity())
        }
    }

    override suspend fun getMedicineByIdOnce(id: Long): Medicine? =
        dao.getMedicineByIdOnce(id)?.toDomain()
}
