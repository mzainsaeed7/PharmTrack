package com.app.pharmtrack.presentation.alerts

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
import com.app.pharmtrack.databinding.FragmentAlertsBinding
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.utils.visibleIf
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlertsViewModel by viewModels()
    private lateinit var alertsAdapter: AlertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupTabLayout()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        alertsAdapter = AlertsAdapter { medicine ->
            findNavController().navigate(
                AlertsFragmentDirections.actionAlertsToMedicineDetail(medicine.id)
            )
        }
        binding.rvAlerts.apply {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                refreshCurrentTab()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.lowStockMedicines.collect {
                        if (binding.tabLayout.selectedTabPosition == 0) {
                            refreshCurrentTab()
                        }
                    }
                }
                launch {
                    viewModel.expiringSoonMedicines.collect {
                        if (binding.tabLayout.selectedTabPosition == 1) {
                            refreshCurrentTab()
                        }
                    }
                }
                launch {
                    viewModel.expiredMedicines.collect {
                        if (binding.tabLayout.selectedTabPosition == 2) {
                            refreshCurrentTab()
                        }
                    }
                }
            }
        }
    }

    private fun refreshCurrentTab() {
        when (binding.tabLayout.selectedTabPosition) {
            0 -> updateList(viewModel.lowStockMedicines.value, AlertTabType.LOW_STOCK)
            1 -> updateList(viewModel.expiringSoonMedicines.value, AlertTabType.EXPIRING_SOON)
            2 -> updateList(viewModel.expiredMedicines.value, AlertTabType.EXPIRED)
        }
    }

    private fun updateList(list: List<Medicine>, tabType: AlertTabType) {
        alertsAdapter.setTabType(tabType)
        alertsAdapter.submitList(list)
        binding.llEmptyState.visibleIf(list.isEmpty())
        binding.rvAlerts.visibleIf(list.isNotEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
