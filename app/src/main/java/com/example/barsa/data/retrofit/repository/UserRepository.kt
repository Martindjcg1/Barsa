package com.example.barsa.data.retrofit.repository

import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.UserApiService
import com.example.barsa.data.retrofit.models.ChangePasswordRequest
import com.example.barsa.data.retrofit.models.ChangePasswordResponse
import com.example.barsa.data.retrofit.models.ErrorResponse

import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterRequest
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.UserDetailResponse
import com.example.barsa.data.retrofit.models.UserProfile
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
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

    suspend fun register(
        nombre: String,
        apellidos: String?,
        nombreUsuario: String,
        email: String?,
        password: String,
        rol: String
    ): Result<RegisterResponse> {
        return try {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token de acceso disponible"))
            }

            val response = userApiService.register(
                token = "Bearer $accessToken",
                registerData = RegisterRequest(nombre, apellidos, nombreUsuario, email, password, rol)
            )
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Register error", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


    suspend fun logout(): Result<LogoutResponse> {
        return try {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token de acceso disponible"))
            }

            val response = userApiService.logout("Bearer $accessToken")
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Logout error", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


    suspend fun refreshToken(): Result<RefreshResponse> {
        return try {
            val refreshToken = tokenManager.refreshTokenFlow.firstOrNull()
            if (refreshToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay refresh token disponible"))
            }

            val response = userApiService.refreshToken("Bearer $refreshToken")
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Refresh token error", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


    suspend fun changePassword(oldPassword: String, newPassword: String): Result<ChangePasswordResponse> {
        return try {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token de acceso disponible"))
            }

            val response = userApiService.changePassword(
                token = "Bearer $accessToken",
                changePasswordData = ChangePasswordRequest(oldPassword, newPassword)
            )
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Change password error", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    // ACTUALIZADO: Ahora maneja la respuesta como lista directa
    suspend fun getUsers(
        nombre: String? = null,
        nombreUsuario: String? = null,
        email: String? = null,
        rol: String? = null,
        estado: String? = null
    ): Result<List<UserProfile>> {
        return try {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            Log.d("UserRepository", "Access token: ${if (accessToken.isNullOrEmpty()) "EMPTY" else "EXISTS"}")

            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token de acceso disponible"))
            }

            Log.d("UserRepository", "Calling getUsers with params - nombre: $nombre, nombreUsuario: $nombreUsuario, email: $email, rol: $rol, estado: $estado")

            val response = userApiService.getUsers(
                token = "Bearer $accessToken",
                nombre = nombre,
                nombreUsuario = nombreUsuario,
                email = email,
                rol = rol,
                estado = estado
            )

            Log.d("UserRepository", "GetUsers success - Total users: ${response.size}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Exception - Code: ${e.code()}, Message: ${e.message()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UserRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor (${e.code()})"))
        } catch (e: IOException) {
            Log.e("UserRepository", "IO Exception", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Unexpected exception in getUsers", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun getUserDetail(userId: String): Result<UserDetailResponse> {
        return try {
            val accessToken = tokenManager.accessTokenFlow.firstOrNull()
            Log.d("UserRepository", "Getting user detail for ID: $userId")

            if (accessToken.isNullOrEmpty()) {
                return Result.failure(Exception("No hay token de acceso disponible"))
            }

            val response = userApiService.getUserDetail(
                token = "Bearer $accessToken",
                userId = userId
            )

            Log.d("UserRepository", "GetUserDetail success for user: ${response.nombreUsuario}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("UserRepository", "HTTP Exception in getUserDetail - Code: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UserRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor (${e.code()})"))
        } catch (e: IOException) {
            Log.e("UserRepository", "IO Exception in getUserDetail", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Unexpected exception in getUserDetail", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }
}
