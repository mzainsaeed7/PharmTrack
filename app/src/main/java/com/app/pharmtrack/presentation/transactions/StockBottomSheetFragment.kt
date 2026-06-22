package com.app.pharmtrack.presentation.transactions

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.FragmentStockBottomSheetBinding
import com.app.pharmtrack.domain.model.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StockBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentStockBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StockBottomSheetViewModel by viewModels()
    private val args: StockBottomSheetFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStockBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.setup(args.medicineId, args.transactionType)
        
        setupInputListeners()
        setupButtons()
        observeViewModel()
        
        // Auto focus quantity & open keyboard
        binding.etQuantity.requestFocus()
        binding.etQuantity.postDelayed({
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.etQuantity, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun setupInputListeners() {
        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.quantityStr.value = s?.toString() ?: ""
                binding.tilQuantity.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.notes.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupButtons() {
        binding.btnConfirm.setOnClickListener {
            viewModel.confirmTransaction()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.transactionType.collect { type ->
                        updateUiColors(type)
                    }
                }
                
                launch {
                    viewModel.medicine.collect { medicine ->
                        if (medicine != null) {
                            binding.tvDialogMedName.text = medicine.name
                            binding.tvPreviewCurrent.text = medicine.currentStock.toString()
                        }
                    }
                }
                
                launch {
                    viewModel.previewNewStock.collect { newStock ->
                        if (newStock != null) {
                            binding.tvPreviewNew.text = newStock.toString()
                        } else {
                            binding.tvPreviewNew.text = binding.tvPreviewCurrent.text
                        }
                    }
                }
                
                launch {
                    viewModel.isSaved.collect { isSaved ->
                        if (isSaved) {
                            findNavController().navigateUp()
                        }
                    }
                }
                
                launch {
                    viewModel.error.collect { errorMsg ->
                        if (errorMsg != null) {
                            binding.tilQuantity.error = errorMsg
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateUiColors(type: TransactionType) {
        val context = requireContext()
        if (type == TransactionType.IN) {
            binding.tvDialogTitle.text = "Stock IN"
            binding.tvDialogTitle.setTextColor(ContextCompat.getColor(context, R.color.status_safe))
            binding.btnConfirm.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.status_safe)
            )
            binding.btnConfirm.text = "Confirm Stock IN"
        } else {
            binding.tvDialogTitle.text = "Stock OUT"
            binding.tvDialogTitle.setTextColor(ContextCompat.getColor(context, R.color.status_danger))
            binding.btnConfirm.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.status_danger)
            )
            binding.btnConfirm.text = "Confirm Stock OUT"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
