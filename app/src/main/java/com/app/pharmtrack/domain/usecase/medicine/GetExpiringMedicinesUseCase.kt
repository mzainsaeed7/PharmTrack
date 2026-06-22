package com.app.pharmtrack.domain.usecase.medicine

import com.app.pharmtrack.domain.model.Medicine
import com.app.pharmtrack.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpiringMedicinesUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    fun getExpiringSoon(): Flow<List<Medicine>> = repository.getExpiringSoonMedicines()
    fun getExpired(): Flow<List<Medicine>> = repository.getExpiredMedicines()
}
