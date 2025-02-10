package com.example.barsa.Retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var token: String = ""

    val headerInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "token $token")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(headerInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(600, TimeUnit.SECONDS)
        .build()

    fun setToken(newToken: String) {
        token = newToken
    }
}