package com.app.pharmtrack.presentation.medicines.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.FragmentMedicineDetailBinding
import com.app.pharmtrack.domain.model.ExpiryStatus
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockStatus
import com.app.pharmtrack.presentation.transactions.TransactionAdapter
import com.app.pharmtrack.utils.DateUtils
import com.app.pharmtrack.utils.StockStatusHelper
import com.app.pharmtrack.utils.gone
import com.app.pharmtrack.utils.showSnackbar
import com.app.pharmtrack.utils.toPkrString
import com.app.pharmtrack.utils.visible
import com.app.pharmtrack.utils.visibleIf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicineDetailFragment : Fragment() {

    private var _binding: FragmentMedicineDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MedicineDetailViewModel by viewModels()
    private val args: MedicineDetailFragmentArgs by navArgs()
    
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.setMedicineId(args.medicineId)
        
        setupToolbar()
        setupButtons()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    findNavController().navigate(
                        MedicineDetailFragmentDirections.actionDetailToEditMedicine(args.medicineId)
                    )
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmationDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.btnStockIn.setOnClickListener {
            findNavController().navigate(
                MedicineDetailFragmentDirections.actionDetailToStockBottomSheet(args.medicineId, "IN")
            )
        }
        
        binding.btnStockOut.setOnClickListener {
            findNavController().navigate(
                MedicineDetailFragmentDirections.actionDetailToStockBottomSheet(args.medicineId, "OUT")
            )
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(showMedicineName = false)
        binding.rvTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.medicine?.let { bindMedicine(it) }
                    
                    transactionAdapter.submitList(state.transactions)
                    val showEmpty = state.transactions.isEmpty() && !state.isLoading
                    binding.llEmptyTransactions.visibleIf(showEmpty)
                    binding.rvTransactions.visibleIf(!showEmpty)
                    
                    if (state.isDeleted) {
                        findNavController().navigateUp()
                    }
                    
                    state.error?.let {
                        showSnackbar(it)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun bindMedicine(medicine: Medicine) {
        val context = requireContext()
        
        // Toolbar Title
        binding.toolbar.title = medicine.name
        
        // Header Card
        binding.tvDetailName.text = medicine.name
        if (medicine.genericName.isNullOrBlank()) {
            binding.tvDetailGeneric.gone()
        } else {
            binding.tvDetailGeneric.text = medicine.genericName
            binding.tvDetailGeneric.visible()
        }
        
        if (medicine.category.isNullOrBlank()) {
            binding.chipDetailCategory.gone()
        } else {
            binding.chipDetailCategory.text = medicine.category
            binding.chipDetailCategory.visible()
        }
        
        binding.tvDetailManufacturer.text = medicine.manufacturer ?: "Unknown Manufacturer"
        
        // Stock Status Card
        binding.tvDetailStockQty.text = medicine.currentStock.toString()
        binding.tvDetailUnitType.text = if (medicine.unitType != null) "units (${medicine.unitType})" else "units"
        
        // Stock Status Chip
        val stockColor = ContextCompat.getColor(context, StockStatusHelper.getStockColorRes(medicine))
        val stockBgRes = when (medicine.stockStatus) {
            StockStatus.SAFE -> R.color.status_safe_bg
            StockStatus.LOW -> R.color.status_warning_bg
            StockStatus.CRITICAL_LOW, StockStatus.OUT_OF_STOCK -> R.color.status_danger_bg
        }
        binding.chipDetailStockStatus.text = StockStatusHelper.getStockLabel(medicine)
        binding.chipDetailStockStatus.setTextColor(stockColor)
        binding.chipDetailStockStatus.chipBackgroundColor = ColorStateList.valueOf(
            ContextCompat.getColor(context, stockBgRes)
        )
        
        // Progress Bar
        val maxProgress = maxOf(medicine.currentStock, medicine.reorderLevel * 2)
        binding.pbStock.max = if (maxProgress > 0) maxProgress else 100
        binding.pbStock.progress = medicine.currentStock
        
        val progressTintRes = when (medicine.stockStatus) {
            StockStatus.SAFE -> R.color.status_safe
            StockStatus.LOW -> R.color.status_warning
            StockStatus.CRITICAL_LOW, StockStatus.OUT_OF_STOCK -> R.color.status_danger
        }
        binding.pbStock.progressTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, progressTintRes)
        )
        
        binding.tvReorderLevel.text = "Reorder Level: ${medicine.reorderLevel} units"
        
        // Financial Card
        binding.tvDetailPurchasePrice.text = medicine.purchasePrice?.toPkrString() ?: "—"
        binding.tvDetailSellingPrice.text = medicine.sellingPrice?.toPkrString() ?: "—"
        
        binding.tvDetailMargin.text = StockStatusHelper.formatProfitMargin(medicine.profitMargin)
        val margin = medicine.profitMargin
        if (margin != null && margin > 0) {
            binding.tvDetailMargin.setTextColor(ContextCompat.getColor(context, R.color.status_safe))
        } else {
            binding.tvDetailMargin.setTextColor(ContextCompat.getColor(context, R.color.status_danger))
        }
        
        // Batch & Expiry Card
        binding.tvDetailBatch.text = medicine.batchNumber ?: "—"
        binding.tvDetailExpiry.text = DateUtils.formatDate(medicine.expiryDate)
        
        // Expiry Status Chip
        val expiryBgRes = when (medicine.expiryStatus) {
            ExpiryStatus.SAFE -> R.color.status_safe_bg
            ExpiryStatus.WARNING -> R.color.status_warning_bg
            ExpiryStatus.CRITICAL, ExpiryStatus.EXPIRED -> R.color.status_danger_bg
            ExpiryStatus.NO_DATE -> R.color.divider
        }
        binding.chipDetailExpiryStatus.text = StockStatusHelper.getExpiryLabel(medicine)
        binding.chipDetailExpiryStatus.setTextColor(
            ContextCompat.getColor(context, StockStatusHelper.getExpiryColorRes(medicine))
        )
        binding.chipDetailExpiryStatus.chipBackgroundColor = ColorStateList.valueOf(
            ContextCompat.getColor(context, expiryBgRes)
        )
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Medicine?")
            .setMessage("Are you sure you want to delete this medicine? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMedicine()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
