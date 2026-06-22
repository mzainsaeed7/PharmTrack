package com.app.pharmtrack.di

import com.app.pharmtrack.data.repository.MedicineRepositoryImpl
import com.app.pharmtrack.data.repository.TransactionRepositoryImpl
import com.app.pharmtrack.domain.repository.MedicineRepository
import com.app.pharmtrack.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicineRepository(
        impl: MedicineRepositoryImpl
    ): MedicineRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}
