package com.example.barsa.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import com.example.barsa.data.local.TiempoDao
import com.example.barsa.data.local.TiempoDatabase
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideTiempoDatabase(@ApplicationContext context: Context): TiempoDatabase {
        return Room.databaseBuilder(context, TiempoDatabase::class.java, "tiempo_database")
            .build()
    }

    @Provides
    fun provideTiempoDao(database: TiempoDatabase): TiempoDao = database.tiempoDao()
}