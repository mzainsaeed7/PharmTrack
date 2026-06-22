package com.app.pharmtrack.domain.usecase.medicine

import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllMedicinesUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    operator fun invoke(): Flow<List<Medicine>> = repository.getAllMedicines()
}
