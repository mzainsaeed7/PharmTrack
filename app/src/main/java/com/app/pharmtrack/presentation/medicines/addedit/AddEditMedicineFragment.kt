package com.app.pharmtrack.presentation.medicines.addedit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.FragmentAddEditMedicineBinding
import com.app.pharmtrack.utils.DateUtils
import com.app.pharmtrack.utils.gone
import com.app.pharmtrack.utils.visible
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddEditMedicineFragment : Fragment() {

    private var _binding: FragmentAddEditMedicineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditMedicineViewModel by viewModels()
    private val args: AddEditMedicineFragmentArgs by navArgs()

    private var selectedExpiryDate: Long? = null
    private var formPopulated = false

    private val categories = listOf(
        "Tablet", "Syrup", "Capsule", "Injection",
        "Drops", "Cream", "Powder", "Other"
    )
    private val unitTypes = listOf(
        "Per Tablet", "Per Strip", "Per Bottle",
        "Per Vial", "Per Sachet", "Per Tube"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditMedicineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDropdowns()
        setupExpiryDatePicker()
        observeViewModel()
        viewModel.loadMedicine(args.medicineId)
    }

    private fun setupToolbar() {
        val isEditMode = args.medicineId != -1L
        binding.toolbar.title = if (isEditMode)
            getString(R.string.edit_medicine_title)
        else
            getString(R.string.add_medicine_title)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveMedicine()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDropdowns() {
        val categoryAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, categories
        )
        binding.etCategory.setAdapter(categoryAdapter)

        val unitAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, unitTypes
        )
        binding.etUnitType.setAdapter(unitAdapter)
    }

    private fun setupExpiryDatePicker() {
        binding.etExpiryDate.setOnClickListener { showDatePicker() }
        binding.tilExpiryDate.setEndIconOnClickListener {
            selectedExpiryDate = null
            binding.etExpiryDate.setText("")
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        selectedExpiryDate?.let { calendar.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 23, 59, 59)
                selectedExpiryDate = cal.timeInMillis
                binding.etExpiryDate.setText(DateUtils.formatDate(selectedExpiryDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveMedicine() {
        viewModel.saveMedicine(
            name = binding.etName.text?.toString() ?: "",
            genericName = binding.etGenericName.text?.toString() ?: "",
            category = binding.etCategory.text?.toString() ?: "",
            manufacturer = binding.etManufacturer.text?.toString() ?: "",
            unitType = binding.etUnitType.text?.toString() ?: "",
            purchasePriceStr = binding.etPurchasePrice.text?.toString() ?: "",
            sellingPriceStr = binding.etSellingPrice.text?.toString() ?: "",
            currentStockStr = binding.etCurrentStock.text?.toString() ?: "",
            reorderLevelStr = binding.etReorderLevel.text?.toString() ?: "",
            batchNumber = binding.etBatchNumber.text?.toString() ?: "",
            expiryDate = selectedExpiryDate,
            existingId = args.medicineId
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Pre-populate in edit mode
                    if (state.isEditMode && state.medicine != null && !state.isSaved) {
                        populateForm(state)
                    }

                    // Validation errors
                    binding.tilName.error = state.nameError
                    binding.tilCurrentStock.error = state.stockError
                    binding.tilReorderLevel.error = state.reorderError

                    // Warnings
                    if (state.pricingWarning != null) binding.chipPricingWarning.visible()
                    else binding.chipPricingWarning.gone()

                    if (state.expiryWarning != null) binding.chipExpiryWarning.visible()
                    else binding.chipExpiryWarning.gone()

                    // Navigate back on save
                    if (state.isSaved) {
                        findNavController().navigateUp()
                    }

                    // Error snackbar
                    state.error?.let { msg ->
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun populateForm(state: AddEditUiState) {
        if (formPopulated) return
        formPopulated = true
        val medicine = state.medicine ?: return

        binding.etName.setText(medicine.name)
        binding.etGenericName.setText(medicine.genericName ?: "")
        binding.etCategory.setText(medicine.category ?: "", false)
        binding.etManufacturer.setText(medicine.manufacturer ?: "")
        binding.etUnitType.setText(medicine.unitType ?: "", false)
        binding.etPurchasePrice.setText(medicine.purchasePrice?.toString() ?: "")
        binding.etSellingPrice.setText(medicine.sellingPrice?.toString() ?: "")
        binding.etCurrentStock.setText(medicine.currentStock.toString())
        binding.etReorderLevel.setText(medicine.reorderLevel.toString())
        binding.etBatchNumber.setText(medicine.batchNumber ?: "")
        medicine.expiryDate?.let { ts ->
            selectedExpiryDate = ts
            binding.etExpiryDate.setText(DateUtils.formatDate(ts))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
