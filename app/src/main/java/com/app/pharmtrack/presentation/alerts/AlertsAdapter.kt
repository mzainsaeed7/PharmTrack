package com.app.pharmtrack.presentation.alerts

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.ItemAlertBinding
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockStatus
import com.app.pharmtrack.utils.StockStatusHelper

enum class AlertTabType { LOW_STOCK, EXPIRING_SOON, EXPIRED }

class AlertsAdapter(
    private val onItemClick: (Medicine) -> Unit
) : ListAdapter<Medicine, AlertsAdapter.AlertViewHolder>(DiffCallback()) {

    private var currentTabType = AlertTabType.LOW_STOCK

    fun setTabType(tabType: AlertTabType) {
        currentTabType = tabType
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position), currentTabType)
    }

    inner class AlertViewHolder(
        private val binding: ItemAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(medicine: Medicine, tabType: AlertTabType) {
            val ctx = binding.root.context

            binding.tvAlertMedName.text = medicine.name
            
            if (medicine.genericName.isNullOrBlank()) {
                binding.tvAlertMedGeneric.visibility = View.GONE
            } else {
                binding.tvAlertMedGeneric.text = medicine.genericName
                binding.tvAlertMedGeneric.visibility = View.VISIBLE
            }

            when (tabType) {
                AlertTabType.LOW_STOCK -> {
                    // Reset card background to default
                    binding.cvAlertCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.surface)
                    )
                    
                    binding.chipAlertStatus.text = "${medicine.currentStock} / ${medicine.reorderLevel} units"
                    
                    val stockColor = ContextCompat.getColor(ctx, StockStatusHelper.getStockColorRes(medicine))
                    val stockBgRes = when (medicine.stockStatus) {
                        StockStatus.SAFE -> R.color.status_safe_bg
                        StockStatus.LOW -> R.color.status_warning_bg
                        StockStatus.CRITICAL_LOW, StockStatus.OUT_OF_STOCK -> R.color.status_danger_bg
                    }
                    binding.chipAlertStatus.setTextColor(stockColor)
                    binding.chipAlertStatus.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, stockBgRes)
                    )
                }
                AlertTabType.EXPIRING_SOON -> {
                    // Reset card background to default
                    binding.cvAlertCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.surface)
                    )

                    binding.chipAlertStatus.text = StockStatusHelper.getExpiryLabel(medicine)
                    
                    val expiryColor = ContextCompat.getColor(ctx, StockStatusHelper.getExpiryColorRes(medicine))
                    val expiryBgRes = R.color.status_warning_bg
                    binding.chipAlertStatus.setTextColor(expiryColor)
                    binding.chipAlertStatus.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, expiryBgRes)
                    )
                }
                AlertTabType.EXPIRED -> {
                    // Expired card has subtle red tint background
                    binding.cvAlertCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.status_danger_bg)
                    )

                    binding.chipAlertStatus.text = StockStatusHelper.getExpiryLabel(medicine)
                    
                    val expiryColor = ContextCompat.getColor(ctx, StockStatusHelper.getExpiryColorRes(medicine))
                    binding.chipAlertStatus.setTextColor(expiryColor)
                    binding.chipAlertStatus.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.status_danger_bg)
                    )
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine) =
            oldItem == newItem
    }
}
