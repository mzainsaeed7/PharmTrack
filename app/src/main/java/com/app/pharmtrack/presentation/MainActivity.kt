package com.app.pharmtrack.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.ActivityMainBinding
import com.app.pharmtrack.domain.repository.MedicineRepository
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var medicineRepository: MedicineRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        setupAlertsBadge()
    }

    private fun setupAlertsBadge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    medicineRepository.getLowStockCount(),
                    medicineRepository.getExpiringSoonCount(),
                    medicineRepository.getExpiredMedicines()
                ) { lowStock, expiringSoon, expiredList ->
                    lowStock + expiringSoon + expiredList.size
                }.collect { totalAlerts ->
                    val badge = binding.bottomNav.getOrCreateBadge(R.id.alertsFragment)
                    if (totalAlerts > 0) {
                        badge.isVisible = true
                        badge.number = totalAlerts
                    } else {
                        badge.isVisible = false
                    }
                }
            }
        }
    }
}
