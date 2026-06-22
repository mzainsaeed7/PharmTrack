package com.app.pharmtrack.domain.model

data class Medicine(
    val id: Long = 0,
    val name: String,
    val genericName: String? = null,
    val category: String? = null,
    val manufacturer: String? = null,
    val unitType: String? = null,
    val purchasePrice: Double? = null,
    val sellingPrice: Double? = null,
    val currentStock: Int,
    val reorderLevel: Int = 10,
    val batchNumber: String? = null,
    val expiryDate: Long? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val stockStatus: StockStatus
        get() = when {
            currentStock == 0 -> StockStatus.OUT_OF_STOCK
            currentStock <= reorderLevel / 2 -> StockStatus.CRITICAL_LOW
            currentStock <= reorderLevel -> StockStatus.LOW
            else -> StockStatus.SAFE
        }

    val expiryStatus: ExpiryStatus
        get() {
            val now = System.currentTimeMillis()
            val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
            val thirtyDaysMs = 30 * 24 * 60 * 60 * 1000L
            return when {
                expiryDate == null -> ExpiryStatus.NO_DATE
                expiryDate < now -> ExpiryStatus.EXPIRED
                expiryDate <= now + sevenDaysMs -> ExpiryStatus.CRITICAL
                expiryDate <= now + thirtyDaysMs -> ExpiryStatus.WARNING
                else -> ExpiryStatus.SAFE
            }
        }

    val profitMargin: Double?
        get() {
            return if (purchasePrice != null && sellingPrice != null && purchasePrice > 0) {
                ((sellingPrice - purchasePrice) / purchasePrice) * 100
            } else null
        }
}

enum class StockStatus { OUT_OF_STOCK, CRITICAL_LOW, LOW, SAFE }
enum class ExpiryStatus { NO_DATE, EXPIRED, CRITICAL, WARNING, SAFE }
