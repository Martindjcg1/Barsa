package com.example.barsa.data.retrofit.repository

import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.PapeletaApiService
import com.example.barsa.data.retrofit.models.DesactivarDetencionResponse
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.ErrorResponse
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.TiempoRemoto
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

            if (response.isSuccessful && responseBody != null) {
                Result.success(responseBody.message ?: "Operación exitosa")
            } else {
                val parsedError = errorBody?.let { gson.fromJson(it, DesactivarDetencionResponse::class.java) }
                Result.failure(Exception(parsedError?.error ?: "Error en desactivar detención"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("PapeletaRepository", "Error en desactivarDetencionTiempo", e)
            Result.failure(Exception("Error inesperado"))
        }
    }

}