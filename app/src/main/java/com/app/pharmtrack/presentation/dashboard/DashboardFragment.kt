package com.app.pharmtrack.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.pharmtrack.databinding.FragmentDashboardBinding
import com.app.pharmtrack.presentation.medicines.list.MedicineAdapter
import com.app.pharmtrack.presentation.transactions.TransactionAdapter
import com.app.pharmtrack.utils.DateUtils
import com.app.pharmtrack.utils.visibleIf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var alertsAdapter: MedicineAdapter
    private lateinit var activityAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        alertsAdapter = MedicineAdapter { medicine ->
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardToMedicineDetail(medicine.id)
            )
        }
        binding.rvUrgentAlerts.apply {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        activityAdapter = TransactionAdapter(showMedicineName = true)
        binding.rvRecentTransactions.apply {
            adapter = activityAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        // Set date header immediately
        binding.tvDate.text = DateUtils.todayString()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Stat Cards
                    binding.tvTotalMedicines.text = state.totalMedicines.toString()
                    binding.tvLowStock.text = state.lowStockCount.toString()
                    binding.tvExpiringSoon.text = state.expiringSoonCount.toString()
                    binding.tvStockValue.text = state.totalStockValueFormatted

                    // Urgent Alerts
                    alertsAdapter.submitList(state.urgentAlerts)
                    val noAlerts = state.urgentAlerts.isEmpty()
                    binding.tvNoAlerts.visibleIf(noAlerts)
                    binding.rvUrgentAlerts.visibleIf(!noAlerts)

                    // Recent Activity
                    activityAdapter.submitList(state.recentTransactions)
                    val noActivity = state.recentTransactions.isEmpty()
                    binding.tvNoActivity.visibleIf(noActivity)
                    binding.rvRecentTransactions.visibleIf(!noActivity)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
