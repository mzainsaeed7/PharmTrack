package com.app.pharmtrack.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatDate(timestampMs: Long?): String {
        if (timestampMs == null) return "—"
        return dateFormat.format(Date(timestampMs))
    }

    fun formatTime(timestampMs: Long): String = timeFormat.format(Date(timestampMs))

    fun formatDateTime(timestampMs: Long): String = dateTimeFormat.format(Date(timestampMs))

    fun daysUntilExpiry(expiryMs: Long): Long {
        val now = System.currentTimeMillis()
        return (expiryMs - now) / (24 * 60 * 60 * 1000L)
    }

    fun daysSinceExpiry(expiryMs: Long): Long {
        val now = System.currentTimeMillis()
        return (now - expiryMs) / (24 * 60 * 60 * 1000L)
    }

    fun startOfDay(timestampMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestampMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(timestampMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestampMs
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun startOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return startOfDay(cal.timeInMillis)
    }

    fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return startOfDay(cal.timeInMillis)
    }

    fun todayString(): String = dateFormat.format(Date())
}
