package com.example.barsa.di

import com.example.barsa.data.local.TiempoDao
import com.example.barsa.data.repository.OfflineTiemposRepository
import com.example.barsa.data.repository.TiemposRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideTiemposRepository(tiempoDao: TiempoDao): TiemposRepository =
        OfflineTiemposRepository(tiempoDao)
}