package com.app.pharmtrack.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.app.pharmtrack.R
import com.app.pharmtrack.databinding.ActivityMainBinding
import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.model.StockTransaction
import com.app.pharmtrack.domain.model.TransactionType
import com.app.pharmtrack.domain.repository.MedicineRepository
import com.app.pharmtrack.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var medicineRepository: MedicineRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

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
        checkAndPrepopulateDummyData()
    }

    private fun checkAndPrepopulateDummyData() {
        lifecycleScope.launch {
            try {
                val totalMeds = medicineRepository.getTotalMedicineCount().first()
                if (totalMeds == 0) {
                    prepopulateDummyData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun prepopulateDummyData() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L

        val list = listOf(
            Medicine(
                name = "Panadol 500mg",
                genericName = "Paracetamol",
                category = "Tablet",
                manufacturer = "GSK Pakistan",
                unitType = "Per Tablet",
                purchasePrice = 1.8,
                sellingPrice = 2.5,
                currentStock = 150,
                reorderLevel = 50,
                batchNumber = "P-8849",
                expiryDate = now + 365 * oneDay
            ),
            Medicine(
                name = "Amoxil 250mg",
                genericName = "Amoxicillin",
                category = "Capsule",
                manufacturer = "GSK Pakistan",
                unitType = "Per Strip",
                purchasePrice = 25.0,
                sellingPrice = 35.0,
                currentStock = 8,
                reorderLevel = 30,
                batchNumber = "AM-1120",
                expiryDate = now + 180 * oneDay
            ),
            Medicine(
                name = "Cac-1000 Plus",
                genericName = "Calcium + Vit C",
                category = "Tablet",
                manufacturer = "Sandoz",
                unitType = "Per Tube",
                purchasePrice = 120.0,
                sellingPrice = 150.0,
                currentStock = 4,
                reorderLevel = 10,
                batchNumber = "CAC-908",
                expiryDate = now + 5 * oneDay
            ),
            Medicine(
                name = "Augmentin 625mg",
                genericName = "Co-Amoxiclav",
                category = "Tablet",
                manufacturer = "GSK Pakistan",
                unitType = "Per Tablet",
                purchasePrice = 12.0,
                sellingPrice = 16.0,
                currentStock = 45,
                reorderLevel = 20,
                batchNumber = "AU-774",
                expiryDate = now + 20 * oneDay
            ),
            Medicine(
                name = "Brufen Syrup",
                genericName = "Ibuprofen",
                category = "Syrup",
                manufacturer = "Abbott Pakistan",
                unitType = "Per Bottle",
                purchasePrice = 45.0,
                sellingPrice = 55.0,
                currentStock = 0,
                reorderLevel = 15,
                batchNumber = "BR-341",
                expiryDate = now + 240 * oneDay
            ),
            Medicine(
                name = "Flygy Syrup",
                genericName = "Metronidazole",
                category = "Syrup",
                manufacturer = "Searle",
                unitType = "Per Bottle",
                purchasePrice = 38.0,
                sellingPrice = 48.0,
                currentStock = 60,
                reorderLevel = 20,
                batchNumber = "FL-556",
                expiryDate = now - 10 * oneDay
            ),
            Medicine(
                name = "Arinac Forte",
                genericName = "Ibuprofen + Pseudoephedrine",
                category = "Tablet",
                manufacturer = "Abbott Pakistan",
                unitType = "Per Tablet",
                purchasePrice = 3.5,
                sellingPrice = 4.5,
                currentStock = 100,
                reorderLevel = 30,
                batchNumber = "AR-202",
                expiryDate = now + 500 * oneDay
            ),
            Medicine(
                name = "Gravinate 50mg",
                genericName = "Dimenhydrinate",
                category = "Tablet",
                manufacturer = "Searle",
                unitType = "Per Tablet",
                purchasePrice = 1.0,
                sellingPrice = 1.5,
                currentStock = 3,
                reorderLevel = 15,
                batchNumber = "GR-445",
                expiryDate = now - 15 * oneDay
            ),
            Medicine(
                name = "Surbex-Z",
                genericName = "Multivitamins + Zinc",
                category = "Tablet",
                manufacturer = "Abbott Pakistan",
                unitType = "Per Strip",
                purchasePrice = 80.0,
                sellingPrice = 95.0,
                currentStock = 80,
                reorderLevel = 20,
                batchNumber = "SZ-889",
                expiryDate = now + 600 * oneDay
            ),
            Medicine(
                name = "Loprin 75mg",
                genericName = "Aspirin",
                category = "Tablet",
                manufacturer = "Getz Pharma",
                unitType = "Per Strip",
                purchasePrice = 15.0,
                sellingPrice = 20.0,
                currentStock = 200,
                reorderLevel = 40,
                batchNumber = "LP-332",
                expiryDate = now + 400 * oneDay
            )
        )

        for (med in list) {
            val id = medicineRepository.addMedicine(med)

            if (med.name == "Panadol 500mg") {
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.IN,
                        quantity = 150,
                        notes = "Initial Batch",
                        timestamp = now - 3 * oneDay
                    )
                )
            }
            if (med.name == "Amoxil 250mg") {
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.IN,
                        quantity = 50,
                        notes = "Supplier delivery",
                        timestamp = now - 5 * oneDay
                    )
                )
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.OUT,
                        quantity = 42,
                        notes = "Dispensed to OPD patients",
                        timestamp = now - 2 * oneDay
                    )
                )
            }
            if (med.name == "Brufen Syrup") {
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.IN,
                        quantity = 15,
                        notes = "Supplier delivery",
                        timestamp = now - 4 * oneDay
                    )
                )
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.OUT,
                        quantity = 15,
                        notes = "Dispensed to ward",
                        timestamp = now - 1 * oneDay
                    )
                )
            }
            if (med.name == "Flygy Syrup") {
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.IN,
                        quantity = 60,
                        notes = "Opening Stock",
                        timestamp = now - 15 * oneDay
                    )
                )
            }
            if (med.name == "Gravinate 50mg") {
                transactionRepository.addTransaction(
                    StockTransaction(
                        medicineId = id,
                        medicineName = med.name,
                        type = TransactionType.IN,
                        quantity = 3,
                        notes = "Opening Stock",
                        timestamp = now - 20 * oneDay
                    )
                )
            }
        }
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
