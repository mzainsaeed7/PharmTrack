package com.app.pharmtrack.data.local.dao

import androidx.room.*
import com.app.pharmtrack.data.local.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    // ─── Insert / Update / Delete ───────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET is_deleted = 1, updated_at = :now WHERE id = :id")
    suspend fun softDeleteMedicine(id: Long, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun hardDeleteMedicine(medicine: MedicineEntity)

    @Query("UPDATE medicines SET current_stock = :newStock, updated_at = :now WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, now: Long = System.currentTimeMillis())

    // ─── Single Reads ────────────────────────────────────────────────────────

    @Query("SELECT * FROM medicines WHERE id = :id AND is_deleted = 0")
    fun getMedicineById(id: Long): Flow<MedicineEntity?>

    @Query("SELECT * FROM medicines WHERE id = :id AND is_deleted = 0")
    suspend fun getMedicineByIdOnce(id: Long): MedicineEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE medicine_id = :id")
    suspend fun getTransactionCountForMedicine(id: Long): Int

    // ─── All Medicines ────────────────────────────────────────────────────────

    @Query("SELECT * FROM medicines WHERE is_deleted = 0 ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT COUNT(*) FROM medicines WHERE is_deleted = 0")
    fun getTotalMedicineCount(): Flow<Int>

    // ─── Low Stock ───────────────────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM medicines 
        WHERE is_deleted = 0 AND current_stock <= reorder_level 
        ORDER BY current_stock ASC
        """
    )
    fun getLowStockMedicines(): Flow<List<MedicineEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM medicines 
        WHERE is_deleted = 0 AND current_stock <= reorder_level
        """
    )
    fun getLowStockCount(): Flow<Int>

    // ─── Expiry Queries ───────────────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM medicines 
        WHERE is_deleted = 0 
          AND expiry_date IS NOT NULL 
          AND expiry_date <= :thirtyDaysFromNow 
          AND expiry_date > :now 
        ORDER BY expiry_date ASC
        """
    )
    fun getExpiringSoonMedicines(now: Long, thirtyDaysFromNow: Long): Flow<List<MedicineEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM medicines 
        WHERE is_deleted = 0 
          AND expiry_date IS NOT NULL 
          AND expiry_date <= :thirtyDaysFromNow 
          AND expiry_date > :now
        """
    )
    fun getExpiringSoonCount(now: Long, thirtyDaysFromNow: Long): Flow<Int>

    @Query(
        """
        SELECT * FROM medicines 
        WHERE is_deleted = 0 
          AND expiry_date IS NOT NULL 
          AND expiry_date < :now 
        ORDER BY expiry_date ASC
        """
    )
    fun getExpiredMedicines(now: Long): Flow<List<MedicineEntity>>

    // ─── Dashboard ────────────────────────────────────────────────────────────

    @Query(
        """
        SELECT SUM(current_stock * purchase_price) FROM medicines 
        WHERE is_deleted = 0 AND purchase_price IS NOT NULL
        """
    )
    fun getTotalStockValue(): Flow<Double?>

    // ─── Search ───────────────────────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM medicines 
        WHERE is_deleted = 0 
          AND (name LIKE '%' || :query || '%' OR generic_name LIKE '%' || :query || '%')
        ORDER BY name ASC
        """
    )
    fun searchMedicines(query: String): Flow<List<MedicineEntity>>
}
