package com.app.pharmtrack.presentation.medicines.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.FragmentMedicineListBinding
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.utils.visibleIf
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicineListFragment : Fragment() {

    private var _binding: FragmentMedicineListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MedicineListViewModel by viewModels()
    private lateinit var adapter: MedicineAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicineListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
        setupFilterChips()
        setupFab()
        observeViewModel()
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = MedicineAdapter { medicine ->
            navigateToDetail(medicine)
        }
        binding.recyclerMedicines.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMedicines.adapter = adapter

        // Collapse FAB on scroll
        binding.recyclerMedicines.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.fabAddMedicine.shrink()
                else if (dy < 0) binding.fabAddMedicine.extend()
            }
        })

        // Swipe to delete
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val medicine = adapter.currentList[position]
                viewModel.deleteMedicine(medicine)
                showUndoSnackbar(medicine)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerMedicines)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // ── Filter Chips ──────────────────────────────────────────────────────────

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(R.id.chip_low_stock) -> MedicineFilter.LOW_STOCK
                checkedIds.contains(R.id.chip_expiring) -> MedicineFilter.EXPIRING_SOON
                checkedIds.contains(R.id.chip_expired) -> MedicineFilter.EXPIRED
                else -> MedicineFilter.ALL
            }
            viewModel.setFilter(filter)
        }
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabAddMedicine.setOnClickListener {
            findNavController().navigate(
                MedicineListFragmentDirections.actionListToAddMedicine()
            )
        }
    }

    // ── Observe ───────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.medicines)
                    val isEmpty = state.medicines.isEmpty() && !state.isLoading
                    binding.emptyState.visibleIf(isEmpty)
                    binding.recyclerMedicines.visibleIf(!isEmpty)
                }
            }
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun navigateToDetail(medicine: Medicine) {
        findNavController().navigate(
            MedicineListFragmentDirections.actionListToMedicineDetail(medicine.id)
        )
    }

    // ── Snackbar ──────────────────────────────────────────────────────────────

    private fun showUndoSnackbar(medicine: Medicine) {
        Snackbar.make(
            binding.root,
            "${medicine.name} deleted",
            Snackbar.LENGTH_LONG
        ).setAction("Undo") {
            // Phase 5: proper undo restore
            viewModel.undoDelete()
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
