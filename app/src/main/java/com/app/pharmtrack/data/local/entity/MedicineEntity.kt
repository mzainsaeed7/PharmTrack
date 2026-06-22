package com.app.pharmtrack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "generic_name")
    val genericName: String? = null,

    @ColumnInfo(name = "category")
    val category: String? = null,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String? = null,

    @ColumnInfo(name = "unit_type")
    val unitType: String? = null,

    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Double? = null,

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double? = null,

    @ColumnInfo(name = "current_stock")
    val currentStock: Int,

    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Int = 10,

    @ColumnInfo(name = "batch_number")
    val batchNumber: String? = null,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: Long? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
