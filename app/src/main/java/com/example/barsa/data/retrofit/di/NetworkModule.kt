package com.example.barsa.data.retrofit.di

import com.example.barsa.data.retrofit.InventoryApiService
import com.example.barsa.data.retrofit.NotificationApiService
import com.example.barsa.data.retrofit.PapeletaApiService
import com.example.barsa.data.retrofit.UserApiService
import com.example.barsa.data.retrofit.models.ImageInfo
import com.example.barsa.data.retrofit.models.ImageInfoDeserializer
import com.example.barsa.data.retrofit.models.ImagenesDeserializer
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                object : TypeToken<List<ImageInfo>>() {}.type,
                ImageInfoDeserializer()
            )
            .create()
    }


    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.54:3000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun ApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePapeletaApiService(retrofit: Retrofit): PapeletaApiService {
        return retrofit.create(PapeletaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideInventoryApiService(retrofit: Retrofit): InventoryApiService {
        return retrofit.create(InventoryApiService::class.java)
    }


    // NUEVO: Proveedor para NotificationApiService
    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }
}

// Como liberar los puertos de la base de datos