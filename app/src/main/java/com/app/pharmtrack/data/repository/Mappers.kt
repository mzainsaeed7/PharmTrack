package com.app.pharmtrack.data.repository

import com.app.pharmtrack.data.local.entity.MedicineEntity
import com.app.pharmtrack.data.local.entity.TransactionEntity
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.model.TransactionType

// ─── Medicine Mappers ────────────────────────────────────────────────────────

fun MedicineEntity.toDomain(): Medicine = Medicine(
    id = id,
    name = name,
    genericName = genericName,
    category = category,
    manufacturer = manufacturer,
    unitType = unitType,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock,
    reorderLevel = reorderLevel,
    batchNumber = batchNumber,
    expiryDate = expiryDate,
    isDeleted = isDeleted,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Medicine.toEntity(): MedicineEntity = MedicineEntity(
    id = id,
    name = name,
    genericName = genericName,
    category = category,
    manufacturer = manufacturer,
    unitType = unitType,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    currentStock = currentStock,
    reorderLevel = reorderLevel,
    batchNumber = batchNumber,
    expiryDate = expiryDate,
    isDeleted = isDeleted,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)

// ─── Transaction Mappers ──────────────────────────────────────────────────────

fun TransactionEntity.toDomain(medicineName: String = ""): StockTransaction = StockTransaction(
    id = id,
    medicineId = medicineId,
    medicineName = medicineName,
    type = if (type == "IN") TransactionType.IN else TransactionType.OUT,
    quantity = quantity,
    notes = notes,
    timestamp = timestamp
)

fun StockTransaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    medicineId = medicineId,
    type = type.name,
    quantity = quantity,
    notes = notes,
    timestamp = timestamp
)
