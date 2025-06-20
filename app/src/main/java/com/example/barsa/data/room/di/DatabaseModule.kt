package com.example.barsa.data.room.di

import android.app.Application
import androidx.room.Room
import com.example.barsa.data.room.local.DetencionDao
//import com.example.barsa.data.room.local.ProcesoDao
import dagger.Module
import com.example.barsa.data.room.local.TiempoDao
import com.example.barsa.data.room.local.TiempoDatabase
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideTiempoDatabase(application: Application): TiempoDatabase {
        return Room.databaseBuilder(application, TiempoDatabase::class.java, "tiempo_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    //@Provides
    //fun provideProcesoDao(database: TiempoDatabase): ProcesoDao = database.procesoDao()

    @Provides
    fun provideTiempoDao(database: TiempoDatabase): TiempoDao = database.tiempoDao()

    @Provides
    fun provideDetencionDao(database: TiempoDatabase): DetencionDao = database.detencionDao()
}