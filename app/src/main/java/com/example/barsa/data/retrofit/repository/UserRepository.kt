package com.example.barsa.data.retrofit.repository

import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.UserApiService
import com.example.barsa.data.retrofit.models.BitacoraListadoInventario
import com.example.barsa.data.retrofit.models.BitacoraListadoProduccion
import com.example.barsa.data.retrofit.models.ChangePasswordRequest
import com.example.barsa.data.retrofit.models.ChangePasswordResponse
import com.example.barsa.data.retrofit.models.ErrorResponse
import com.example.barsa.data.retrofit.models.ErrorResponseInfo

import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.UsuarioInfoResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterRequest
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.ToggleUserStatusResponse
import com.example.barsa.data.retrofit.models.UpdatePersonalInfoRequest
import com.example.barsa.data.retrofit.models.UpdatePersonalInfoResponse
import com.example.barsa.data.retrofit.models.UpdateUserRequest
import com.example.barsa.data.retrofit.models.UpdateUserResponse
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

    suspend fun obtenerInfoUsuarioPersonal(): Result<UsuarioInfoResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = userApiService.obtenerInfoUsuarioPersonal("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponseInfo::class.java) }
                Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponseInfo::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al obtener información del usuario", e)
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

    // FUNCIÓN PARA ACTUALIZAR INFORMACIÓN PERSONAL DEL USUARIO LOGUEADO
    suspend fun updatePersonalInfo(nombreUsuario: String, email: String?): Result<UpdatePersonalInfoResponse> {
        return try {
            Log.d("UserRepository", "Iniciando updatePersonalInfo")
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                Log.e("UserRepository", "Token no encontrado")
                return Result.failure(Exception("No se encontró un token válido"))
            }

            val request = UpdatePersonalInfoRequest(
                nombreUsuario = nombreUsuario,
                email = email
            )

            Log.d("UserRepository", "Enviando request: $request")
            val response = userApiService.updatePersonalInfo("Bearer $token", request)
            Log.d("UserRepository", "Respuesta recibida exitosamente")

            // Crear una respuesta por defecto si el servidor no devuelve el formato esperado
            val safeResponse = UpdatePersonalInfoResponse(
                message = response.message ?: "Información actualizada correctamente",
                success = response.success ?: true
            )

            Result.success(safeResponse)
        } catch (e: HttpException) {
            Log.e("UserRepository", "HttpException en updatePersonalInfo", e)
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UserRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, ErrorResponse::class.java)
                } catch (ex: Exception) {
                    Log.e("UserRepository", "Error parsing error response", ex)
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Log.e("UserRepository", "IOException en updatePersonalInfo", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception general en updatePersonalInfo", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // FUNCIÓN PARA ACTUALIZAR INFORMACIÓN DE OTROS USUARIOS (FUNCIÓN ADMINISTRATIVA)
    suspend fun updateUserById(userId: String, updateData: UpdateUserRequest): Result<UpdateUserResponse> {
        return try {
            Log.d("UserRepository", "Iniciando updateUserById para ID: $userId")
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                Log.e("UserRepository", "Token no encontrado")
                return Result.failure(Exception("No se encontró un token válido"))
            }

            Log.d("UserRepository", "Enviando request de actualización: $updateData")
            val response = userApiService.updateUser("Bearer $token", userId, updateData)
            Log.d("UserRepository", "Respuesta de actualización recibida exitosamente")

            // Crear una respuesta por defecto si el servidor no devuelve el formato esperado
            val safeResponse = UpdateUserResponse(
                message = response.message ?: "Usuario actualizado correctamente",
                success = response.success ?: true
            )

            Result.success(safeResponse)
        } catch (e: HttpException) {
            Log.e("UserRepository", "HttpException en updateUserById", e)
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UserRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, ErrorResponse::class.java)
                } catch (ex: Exception) {
                    Log.e("UserRepository", "Error parsing error response", ex)
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Log.e("UserRepository", "IOException en updateUserById", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception general en updateUserById", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }
    // FUNCIÓN PARA ACTIVAR/DESACTIVAR USUARIO
    suspend fun toggleUserStatus(userId: String): Result<ToggleUserStatusResponse> {
        return try {
            Log.d("UserRepository", "Iniciando toggleUserStatus para ID: $userId")
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                Log.e("UserRepository", "Token no encontrado")
                return Result.failure(Exception("No se encontró un token válido"))
            }

            Log.d("UserRepository", "Enviando request para cambiar estado del usuario")
            val response = userApiService.toggleUserStatus("Bearer $token", userId)
            Log.d("UserRepository", "Respuesta de cambio de estado recibida exitosamente")

            // Crear una respuesta por defecto si el servidor no devuelve el formato esperado
            val safeResponse = ToggleUserStatusResponse(
                message = response.message ?: "Estado del usuario cambiado correctamente",
                success = response.success ?: true
            )

            Result.success(safeResponse)
        } catch (e: HttpException) {
            Log.e("UserRepository", "HttpException en toggleUserStatus", e)
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("UserRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, ErrorResponse::class.java)
                } catch (ex: Exception) {
                    Log.e("UserRepository", "Error parsing error response", ex)
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Log.e("UserRepository", "IOException en toggleUserStatus", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception general en toggleUserStatus", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    suspend fun getListadoBitacoraProduccion(
        fechaInicio: String? = null,
        fechaFin: String? = null,
        id: Int? = null,
        folio: Int? = null,
        etapa: String? = null,
        movimiento: String? = null,
        usuario: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Result<BitacoraListadoProduccion> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = userApiService.getListadoProduccion(
                token = "Bearer $token",
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                id = id,
                folio = folio,
                etapa = etapa,
                movimiento = movimiento,
                usuario = usuario,
                page = page,
                limit = limit
            )

            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("BitacoraViewModel", "Error al obtener listado producción", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getListadoBitacoraInventario(
        page: Int? = null,
        limit: Int? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        id: Int? = null,
        codigo: String? = null
    ): Result<BitacoraListadoInventario> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = userApiService.getListadoInventario(
                token = "Bearer $token",
                page = page,
                limit = limit,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                id = id,
                borrado = codigo
            )

            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("BitacoraViewModel", "Error al obtener listado inventario", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

}
