package com.example.barsa.data.retrofit.di

import com.example.barsa.data.retrofit.repository.PapeletaRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPointA {
    fun papeletasRepository(): PapeletaRepository
}
