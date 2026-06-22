package com.app.pharmtrack.presentation.medicines.list

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.ItemMedicineBinding
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockStatus
import com.app.pharmtrack.utils.StockStatusHelper

class MedicineAdapter(
    private val onItemClick: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicineViewHolder(
        private val binding: ItemMedicineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(medicine: Medicine) {
            val ctx = binding.root.context

            // Name
            binding.tvMedicineName.text = medicine.name

            // Generic name
            if (medicine.genericName.isNullOrBlank()) {
                binding.tvGenericName.visibility = View.GONE
            } else {
                binding.tvGenericName.visibility = View.VISIBLE
                binding.tvGenericName.text = medicine.genericName
            }

            // Category
            binding.chipCategory.text = medicine.category ?: "—"

            // ── Stock chip ───────────────────────────────────────────────────
            val stockColorRes = StockStatusHelper.getStockColorRes(medicine)
            val stockColor = ContextCompat.getColor(ctx, stockColorRes)
            val stockBgRes = when (medicine.stockStatus) {
                StockStatus.SAFE -> R.color.status_safe_bg
                StockStatus.LOW -> R.color.status_warning_bg
                StockStatus.CRITICAL_LOW -> R.color.status_danger_bg
                StockStatus.OUT_OF_STOCK -> R.color.status_danger_bg
            }
            binding.chipStockStatus.text = "${medicine.currentStock} units"
            binding.chipStockStatus.setTextColor(stockColor)
            binding.chipStockStatus.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, stockBgRes))

            // ── Expiry ───────────────────────────────────────────────────────
            val expiryColorRes = StockStatusHelper.getExpiryColorRes(medicine)
            binding.tvExpiry.text = StockStatusHelper.getExpiryLabel(medicine)
            binding.tvExpiry.setTextColor(ContextCompat.getColor(ctx, expiryColorRes))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem == newItem
    }
}
