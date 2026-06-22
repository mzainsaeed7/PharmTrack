package com.app.pharmtrack.domain.usecase.medicine

import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import javax.inject.Inject

class AddMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(medicine: Medicine): Long {
        return repository.addMedicine(medicine)
    }
}
