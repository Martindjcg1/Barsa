package com.example.barsa.data.room.di

import com.example.barsa.data.room.local.DetencionDao
import com.example.barsa.data.room.local.ProcesoDao
import com.example.barsa.data.room.local.TiempoDao
import com.example.barsa.data.room.repository.OfflineTiemposRepository
import com.example.barsa.data.room.repository.TiemposRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideTiemposRepository(
        procesoDao: ProcesoDao,
        tiempoDao: TiempoDao,
        detencionDao: DetencionDao
    ): TiemposRepository = OfflineTiemposRepository(procesoDao, tiempoDao, detencionDao)
}