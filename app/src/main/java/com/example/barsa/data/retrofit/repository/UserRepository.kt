package com.example.barsa.data.retrofit.repository

import android.util.Log
import com.example.barsa.data.retrofit.UserApiService
import com.example.barsa.data.retrofit.models.ErrorResponse
import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    private val gson = Gson()

    suspend fun login(nombreUsuario: String, password: String): Result<LoginResponse> {
        return try {
            val response = userApiService.login(LoginRequest(nombreUsuario, password))
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error", e)
            Result.failure(Exception("Error inesperado"))
        }
    }
}