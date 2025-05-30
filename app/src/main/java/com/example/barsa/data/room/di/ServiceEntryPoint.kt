package com.example.barsa.data.room.di

import com.example.barsa.data.room.repository.TiemposRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint {
    fun tiemposRepository(): TiemposRepository
}
