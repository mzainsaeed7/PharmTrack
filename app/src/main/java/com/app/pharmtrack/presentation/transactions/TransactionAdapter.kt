package com.app.pharmtrack.presentation.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.ItemTransactionBinding
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.model.TransactionType
import com.app.pharmtrack.utils.DateUtils

class TransactionAdapter(
    private val showMedicineName: Boolean = false
) : ListAdapter<StockTransaction, TransactionAdapter.TransactionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: StockTransaction) {
            val ctx = binding.root.context

            // Icon background & source based on type
            if (transaction.type == TransactionType.IN) {
                binding.cvIconContainer.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.status_safe_bg)
                )
                binding.ivTransactionType.setImageResource(R.drawable.ic_arrow_upward)
                binding.ivTransactionType.setColorFilter(
                    ContextCompat.getColor(ctx, R.color.status_safe)
                )
                
                binding.tvQuantity.text = "+${transaction.quantity}"
                binding.tvQuantity.setTextColor(ContextCompat.getColor(ctx, R.color.status_safe))
            } else {
                binding.cvIconContainer.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, R.color.status_danger_bg)
                )
                binding.ivTransactionType.setImageResource(R.drawable.ic_arrow_downward)
                binding.ivTransactionType.setColorFilter(
                    ContextCompat.getColor(ctx, R.color.status_danger)
                )
                
                binding.tvQuantity.text = "-${transaction.quantity}"
                binding.tvQuantity.setTextColor(ContextCompat.getColor(ctx, R.color.status_danger))
            }

            // Notes and title formatting
            val titleText = if (showMedicineName) {
                if (!transaction.notes.isNullOrBlank()) {
                    "${transaction.medicineName} (${transaction.notes})"
                } else {
                    transaction.medicineName
                }
            } else {
                if (!transaction.notes.isNullOrBlank()) {
                    transaction.notes
                } else {
                    if (transaction.type == TransactionType.IN) "Stock Added" else "Stock Removed"
                }
            }
            binding.tvNotes.text = titleText

            // Timestamp formatting
            binding.tvTimestamp.text = DateUtils.formatDateTime(transaction.timestamp)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<StockTransaction>() {
        override fun areItemsTheSame(oldItem: StockTransaction, newItem: StockTransaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StockTransaction, newItem: StockTransaction) =
            oldItem == newItem
    }
}
