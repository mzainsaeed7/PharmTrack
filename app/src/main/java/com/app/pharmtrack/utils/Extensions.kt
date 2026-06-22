package com.app.pharmtrack.utils

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// ─── View Extensions ─────────────────────────────────────────────────────────

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// ─── TextView Extensions ─────────────────────────────────────────────────────

fun TextView.setTextColorRes(colorRes: Int) {
    setTextColor(ContextCompat.getColor(context, colorRes))
}

// ─── Fragment Extensions ──────────────────────────────────────────────────────

fun Fragment.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(requireView(), message, duration).show()
}

fun Fragment.showSnackbarWithAction(
    message: String,
    actionLabel: String,
    action: () -> Unit
) {
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        .setAction(actionLabel) { action() }
        .show()
}

// ─── Number Extensions ────────────────────────────────────────────────────────

fun Double.toPkrString(): String = "PKR %,.0f".format(this)

fun Int.toQuantityString(unit: String? = null): String {
    return if (unit != null) "$this $unit" else "$this units"
}
