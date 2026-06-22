package com.app.pharmtrack.utils

import com.app.pharmtrack.R
import com.app.pharmtrack.domain.model.ExpiryStatus
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockStatus

object StockStatusHelper {

    fun getStockColorRes(medicine: Medicine): Int = when (medicine.stockStatus) {
        StockStatus.SAFE -> R.color.status_safe
        StockStatus.LOW -> R.color.status_warning
        StockStatus.CRITICAL_LOW -> R.color.status_danger
        StockStatus.OUT_OF_STOCK -> R.color.status_danger
    }

    fun getStockLabel(medicine: Medicine): String = when (medicine.stockStatus) {
        StockStatus.SAFE -> "In Stock"
        StockStatus.LOW -> "Low Stock"
        StockStatus.CRITICAL_LOW -> "Critical"
        StockStatus.OUT_OF_STOCK -> "Out of Stock"
    }

    fun getExpiryColorRes(medicine: Medicine): Int = when (medicine.expiryStatus) {
        ExpiryStatus.SAFE -> R.color.status_safe
        ExpiryStatus.WARNING -> R.color.status_warning
        ExpiryStatus.CRITICAL -> R.color.status_danger
        ExpiryStatus.EXPIRED -> R.color.status_danger
        ExpiryStatus.NO_DATE -> R.color.text_secondary
    }

    fun getExpiryLabel(medicine: Medicine): String {
        return when (medicine.expiryStatus) {
            ExpiryStatus.NO_DATE -> "No expiry set"
            ExpiryStatus.SAFE -> DateUtils.formatDate(medicine.expiryDate)
            ExpiryStatus.WARNING -> {
                val days = DateUtils.daysUntilExpiry(medicine.expiryDate!!)
                "${days}d left"
            }
            ExpiryStatus.CRITICAL -> {
                val days = DateUtils.daysUntilExpiry(medicine.expiryDate!!)
                if (days <= 0) "Expires today!" else "${days}d left"
            }
            ExpiryStatus.EXPIRED -> {
                val days = DateUtils.daysSinceExpiry(medicine.expiryDate!!)
                "Expired ${days}d ago"
            }
        }
    }

    fun formatStockValue(value: Double?): String {
        if (value == null || value == 0.0) return "PKR 0"
        return "PKR %,.0f".format(value)
    }

    fun formatProfitMargin(margin: Double?): String {
        if (margin == null) return "—"
        return "%.1f%%".format(margin)
    }
}
