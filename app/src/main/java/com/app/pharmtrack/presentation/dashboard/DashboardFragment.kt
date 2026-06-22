package com.app.pharmtrack.presentation.dashboard

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.FragmentDashboardBinding
import com.app.pharmtrack.presentation.medicines.list.MedicineAdapter
import com.app.pharmtrack.presentation.transactions.TransactionAdapter
import com.app.pharmtrack.utils.DateUtils
import com.app.pharmtrack.utils.visibleIf
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
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
        setupPieChart()
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

    private fun setupPieChart() {
        binding.pieChart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            extraBottomOffset = 0f
            extraTopOffset = 0f
            extraLeftOffset = 0f
            extraRightOffset = 0f
            
            // Modern donut configuration
            setDrawHoleEnabled(true)
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 72f
            transparentCircleRadius = 76f
            setTransparentCircleColor(Color.parseColor("#0F000000")) // Soft shadow ring
            
            // Enable center text
            setDrawCenterText(true)
            
            // Clean look: disable labels inside slices
            setDrawEntryLabels(false)
            
            // Smooth quad easing animation
            animateY(1200, Easing.EaseInOutQuad)
            
            // Disable built-in legend (using premium custom layout instead)
            legend.isEnabled = false
        }
    }

    private fun getCenterText(totalCount: Int): SpannableString {
        val countStr = totalCount.toString()
        val labelStr = "Total Items"
        val fullText = "$countStr\n$labelStr"
        val s = SpannableString(fullText)
        val ctx = requireContext()
        
        // Count number styling (e.g. 24)
        s.setSpan(RelativeSizeSpan(1.7f), 0, countStr.length, 0)
        s.setSpan(StyleSpan(Typeface.BOLD), 0, countStr.length, 0)
        s.setSpan(ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.text_primary)), 0, countStr.length, 0)
        
        // Label styling (e.g. Total Items)
        s.setSpan(RelativeSizeSpan(0.8f), countStr.length + 1, fullText.length, 0)
        s.setSpan(ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.text_secondary)), countStr.length + 1, fullText.length, 0)
        return s
    }

    private fun updatePieChart(distribution: Map<String, Int>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        val labels = listOf("Safe", "Low", "Critical", "Out of Stock")
        val colorHexes = listOf(
            "#10B981", // Safe (Emerald Green)
            "#F59E0B", // Low (Amber Orange)
            "#EF4444", // Critical (Rose Red)
            "#94A3B8"  // Out of Stock (Slate Gray)
        )

        for (i in labels.indices) {
            val label = labels[i]
            val count = distribution[label] ?: 0
            if (count > 0) {
                entries.add(PieEntry(count.toFloat(), label))
                colors.add(Color.parseColor(colorHexes[i]))
            }
        }

        val totalMedicines = distribution.values.sum()

        // Bind counts to custom legend views
        binding.tvLegendSafeCount.text = "${distribution["Safe"] ?: 0} Meds"
        binding.tvLegendLowCount.text = "${distribution["Low"] ?: 0} Meds"
        binding.tvLegendCriticalCount.text = "${distribution["Critical"] ?: 0} Meds"
        binding.tvLegendEmptyCount.text = "${distribution["Out of Stock"] ?: 0} Meds"

        if (totalMedicines == 0) {
            binding.cvChartCard.visibility = View.GONE
            return
        } else {
            binding.cvChartCard.visibility = View.VISIBLE
        }

        // Set dynamic center text showing total items
        binding.pieChart.centerText = getCenterText(totalMedicines)

        val dataSet = PieDataSet(entries, "").apply {
            setColors(colors)
            sliceSpace = 4f // Elegant floating segment spacing
            setDrawValues(false) // Hide internal slice numbers for clean design
        }

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.invalidate() // Refresh chart view
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

                    // Pie Chart
                    updatePieChart(state.stockStatusDistribution)

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
