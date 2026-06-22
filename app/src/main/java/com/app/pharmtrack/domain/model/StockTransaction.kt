package com.app.pharmtrack.domain.model

data class StockTransaction(
    val id: Long = 0,
    val medicineId: Long,
    val medicineName: String = "",  // populated at repository level via join
    val type: TransactionType,
    val quantity: Int,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType { IN, OUT }
