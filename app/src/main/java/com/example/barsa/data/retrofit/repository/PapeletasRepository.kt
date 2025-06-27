package com.example.barsa.data.retrofit.repository

import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.PapeletaApiService
import com.example.barsa.data.retrofit.models.ApiErrorResponse
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.DetencionTiempoRequest
import com.example.barsa.data.retrofit.models.ErrorResponse
import com.example.barsa.data.retrofit.models.FinalizarTiempoRequest
import com.example.barsa.data.retrofit.models.IniciarTiempoRequest
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.ListadoTiemposResponse
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.models.ReiniciarTiempoRequest
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.example.barsa.data.retrofit.models.TiemposPeriodo
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PapeletaRepository @Inject constructor(
    private val papeletaApiService: PapeletaApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun getListadoPapeletas(
        page: Int,
        tipoId: String? = null,
        folio: Int? = null,
        fecha: String? = null,
        status: String? = null,
        observacionGeneral: String? = null
    ): Result<ListadoPapeletasResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.getListadoPapeletas("Bearer $token", page, tipoId, folio, fecha, status, observacionGeneral)
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener listado", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getTiemposPorFolio(folio: Int): Result<List<TiempoRemoto>> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.getTiemposPorFolio("Bearer $token", folio)
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener tiempos", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getTiemposPorPeriodo(fechaInicio: String, fechaFin: String): Result<ListadoTiemposResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))
            val response = papeletaApiService.obtenerTiemposPorPeriodo("Bearer $token", fechaInicio, fechaFin)
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error HTTP inesperado"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener tiempos por periodo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


    suspend fun getUltimaDetencion(folio: Int): Result<DetencionRemota?> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No se encontró un token válido"))
            }

            val response = papeletaApiService.getUltimaDetencion("Bearer $token", folio)

            // Si la lista está vacía, significa que no hay detención activa
            val ultimaDetencion = response.firstOrNull()
            Result.success(ultimaDetencion)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener última detención", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun desactivarDetencionTiempo(folio: Int, etapa: String): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.desactivarDetencionTiempo("Bearer $token", folio, etapa)
            val responseBody = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && responseBody?.body?.message != null) {
                Result.success(responseBody.body.message)
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error en desactivar detención"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en desactivarDetencionTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun iniciarTiempo(folio: Int, etapa: String, fechaInicio: String): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val request = IniciarTiempoRequest(folio, etapa, fechaInicio)
            val response = papeletaApiService.iniciarTiempo("Bearer $token", request)
            val body = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && body?.body?.message != null) {
                Result.success(body.body.message)
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error al iniciar tiempo"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en iniciarTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun pausarTiempo(request: PausarTiempoRequest): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.pausarTiempo(request, "Bearer $token")
            val body = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && body?.body?.message != null) {
                Result.success(body.body.message)
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error al pausar tiempo"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en pausarTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getTiempoPorEtapa(folio: Int, etapa: String): Result<TiempoRemoto?> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No se encontró un token válido"))
            }

            val response = papeletaApiService.getTiempoPorEtapa("Bearer $token", folio, etapa)

            // Aunque el API siempre devuelve una lista (incluso en error), validamos el primero
            val tiempo = response.firstOrNull()
            Result.success(tiempo)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener tiempo por etapa", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun reiniciarTiempo(folio: Int, etapa: String): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val request = ReiniciarTiempoRequest(folio, etapa)
            val response = papeletaApiService.reiniciarTiempo("Bearer $token", request)
            val body = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && body?.body?.message != null) {
                Result.success(body.body.message)
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error al reiniciar tiempo"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en reiniciarTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun finalizarTiempo(folio: Int, etapa: String, fechaFin: String, tiempo: Int): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val request = FinalizarTiempoRequest(folio, etapa, fechaFin, tiempo)
            val response = papeletaApiService.finalizarTiempo("Bearer $token", request)
            val body = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && body?.body?.message != null) {
                Result.success(body.body.message)
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error al finalizar tiempo"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en finalizarTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


    suspend fun reportarDetencionTiempo(
        tiempo: Int,
        etapa: String,
        folio: Int,
        fecha: String,
        motivo: String
    ): Result<String> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No se encontró un token válido"))
            }

            val request = DetencionTiempoRequest(
                tiempo = tiempo,
                etapa = etapa,
                folio = folio,
                fecha = fecha,
                motivo = motivo
            )

            val response = papeletaApiService.reportarDetencionTiempo("Bearer $token", request)
            val body = response.body()
            val errorBody = response.errorBody()?.string()

            if (response.isSuccessful && body?.body?.message != null) {
                Result.success(body.body.message)
            } else {
                val parsedError = errorBody?.let {
                    gson.fromJson(it, ApiErrorResponse::class.java)
                }
                Result.failure(Exception(parsedError?.message ?: "Error al reportar detención de tiempo"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en reportarDetencionTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getDetencionesPorEtapa(folio: Int, etapa: String): Result<List<DetencionRemota>> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.getDetencionesPorEtapa("Bearer $token", folio, etapa)
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ApiErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener detenciones por etapa", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

    suspend fun getDetencionesPorFolio(folio: Int): Result<List<DetencionRemota>> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) return Result.failure(Exception("No se encontró un token válido"))

            val response = papeletaApiService.getDetencionesPorFolio("Bearer $token", folio)
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let { gson.fromJson(it, ErrorResponse::class.java) }
            Result.failure(Exception(errorResponse?.message ?: "Error de servidor"))
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error al obtener detenciones", e)
            Result.failure(Exception("Error inesperado"))
        }
    }


}