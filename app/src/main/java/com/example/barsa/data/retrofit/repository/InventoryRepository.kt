package com.example.barsa.data.retrofit.repository

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.example.barsa.Stores.TokenManager
import com.example.barsa.data.retrofit.InventoryApiService
import com.example.barsa.data.retrofit.models.CreateMaterialErrorResponse
import com.example.barsa.data.retrofit.models.CreateMaterialResponse
import com.example.barsa.data.retrofit.models.InventoryErrorResponse
import com.example.barsa.data.retrofit.models.InventoryItem
import com.example.barsa.data.retrofit.models.InventoryPaginationResponse
import kotlinx.coroutines.flow.firstOrNull



import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.barsa.data.retrofit.models.CreateMovementDetail
import com.example.barsa.data.retrofit.models.CreateMovementErrorResponse
import com.example.barsa.data.retrofit.models.CreateMovementRequest
import com.example.barsa.data.retrofit.models.CreateMovementResponseWrapper
import com.example.barsa.data.retrofit.models.DeleteMaterialResponse
import com.example.barsa.data.retrofit.models.InventoryMovementHeader
import com.example.barsa.data.retrofit.models.InventoryMovementsErrorResponse
import com.example.barsa.data.retrofit.models.InventoryMovementsPaginationResponse


import com.example.barsa.data.retrofit.models.UpdateMaterialErrorResponse
import com.example.barsa.data.retrofit.models.UpdateMaterialResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryApiService: InventoryApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    // ==================== FUNCIONES AUXILIARES SEGURAS ====================
    private fun safeString(value: String?): String {
        return try {
            value?.trim()?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun safeOptionalString(value: String?): String? {
        return try {
            value?.trim()?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = "false"
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            Log.d("InventoryRepository", "Obteniendo items - Página: $page, Límite: $limit, Descripción: $descripcion")
            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = safeOptionalString(codigoMat),
                descripcion = safeOptionalString(descripcion),
                unidad = safeOptionalString(unidad),
                proceso = safeOptionalString(proceso),
                borrado = safeOptionalString(borrado)
            )
            // Validar y limpiar datos usando las propiedades seguras
            val validatedResponse = response.copy(
                data = response.data.map { item ->
                    // Log de validación usando propiedades seguras
                    Log.d("InventoryRepository", "Item validado: ${item.codigoMatSafe} - ${item.descripcionSafe}")
                    Log.d("InventoryRepository", "Estado stock: ${item.estadoStock}, Imágenes: ${item.tieneImagenes}")
                    item
                }
            )
            Log.d("InventoryRepository", "Items obtenidos exitosamente - Total: ${validatedResponse.totalItems}")
            Log.d("InventoryRepository", "Items con imágenes: ${validatedResponse.data.count { it.tieneImagenes }}")
            Log.d("InventoryRepository", "Items con stock bajo: ${validatedResponse.data.count { it.estadoStock == "Stock bajo" }}")
            Result.success(validatedResponse)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda específica por código
    suspend fun searchByCode(
        code: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Limpiar y validar el código de búsqueda de forma segura
            val cleanCode = safeOptionalString(code)
                ?: return Result.failure(Exception("Código de búsqueda vacío"))
            Log.d("InventoryRepository", "Buscando por código: '$cleanCode' - Página: $page")
            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = cleanCode,
                descripcion = null,
                unidad = null,
                proceso = null,
                borrado = "false"
            )
            // Log detallado de resultados usando propiedades seguras
            response.data.forEach { item ->
                Log.d("InventoryRepository", "Encontrado: ${item.codigoMatSafe} - ${item.descripcionSafe} (${item.estadoStock})")
            }
            Log.d("InventoryRepository", "Búsqueda por código exitosa - Total: ${response.totalItems}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP en búsqueda por código: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión en búsqueda por código", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado en búsqueda por código", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda específica por descripción
    suspend fun searchByDescription(
        description: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Limpiar y validar la descripción de búsqueda de forma segura
            val cleanDescription = safeOptionalString(description)
                ?: return Result.failure(Exception("Descripción de búsqueda vacía"))
            Log.d("InventoryRepository", "Buscando por descripción: '$cleanDescription' - Página: $page")
            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = null,
                descripcion = cleanDescription,
                unidad = null,
                proceso = null,
                borrado = "false"
            )
            // Log detallado usando propiedades seguras
            response.data.forEach { item ->
                Log.d("InventoryRepository", "Encontrado: ${item.codigoMatSafe} - ${item.descripcionSafe}")
                Log.d("InventoryRepository", "  Stock: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
            }
            Log.d("InventoryRepository", "Búsqueda por descripción exitosa - Total: ${response.totalItems}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP en búsqueda por descripción: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión en búsqueda por descripción", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado en búsqueda por descripción", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda mejorada con opciones flexibles
    suspend fun searchInventoryItems(
        query: String,
        page: Int = 1,
        limit: Int = 10,
        searchInCode: Boolean = true,
        searchInDescription: Boolean = true
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Validar query de forma segura
            val cleanQuery = safeOptionalString(query)
                ?: return Result.failure(Exception("Consulta de búsqueda vacía"))
            Log.d("InventoryRepository", "Búsqueda flexible: '$cleanQuery' - Código: $searchInCode, Descripción: $searchInDescription")
            // Si solo busca en código
            if (searchInCode && !searchInDescription) {
                return searchByCode(cleanQuery, page, limit)
            }
            // Si solo busca en descripción
            if (!searchInCode && searchInDescription) {
                return searchByDescription(cleanQuery, page, limit)
            }
            // Si busca en ambos, primero intenta por código exacto
            if (searchInCode && searchInDescription) {
                // Primero buscar por código exacto
                val codeResult = searchByCode(cleanQuery, page, limit)
                codeResult.fold(
                    onSuccess = { codeResponse ->
                        if (codeResponse.data.isNotEmpty()) {
                            Log.d("InventoryRepository", "Encontrados ${codeResponse.data.size} items por código")
                            // Log usando propiedades seguras
                            codeResponse.data.forEach { item ->
                                Log.d("InventoryRepository", "  ${item.codigoMatSafe}: ${item.descripcionSafe} (${item.estadoStock})")
                            }
                            return Result.success(codeResponse)
                        } else {
                            // Si no hay resultados por código, buscar por descripción
                            Log.d("InventoryRepository", "No hay resultados por código, buscando por descripción")
                            return searchByDescription(cleanQuery, page, limit)
                        }
                    },
                    onFailure = {
                        // Si falla la búsqueda por código, intentar por descripción
                        Log.d("InventoryRepository", "Falló búsqueda por código, intentando por descripción")
                        return searchByDescription(cleanQuery, page, limit)
                    }
                )
            }
            Result.failure(Exception("Parámetros de búsqueda inválidos"))
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP en búsqueda: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión en búsqueda", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado en búsqueda", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda avanzada con múltiples filtros
    suspend fun advancedSearch(
        query: String? = null,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = "false",
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Limpiar parámetros de búsqueda de forma segura
            val cleanQuery = safeOptionalString(query)
            val cleanCodigoMat = safeOptionalString(codigoMat)
            val cleanDescripcion = safeOptionalString(descripcion)
            val cleanUnidad = safeOptionalString(unidad)
            val cleanProceso = safeOptionalString(proceso)
            Log.d("InventoryRepository", "Búsqueda avanzada - Query: '$cleanQuery', Código: '$cleanCodigoMat', Descripción: '$cleanDescripcion'")
            // Si hay un query general, determinar si es código o descripción
            val finalCodigoMat = cleanCodigoMat ?: if (cleanQuery?.matches(Regex("^[A-Z0-9-]+$")) == true) cleanQuery else null
            val finalDescripcion = cleanDescripcion ?: if (cleanQuery != null && finalCodigoMat == null) cleanQuery else null
            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = finalCodigoMat,
                descripcion = finalDescripcion,
                unidad = cleanUnidad,
                proceso = cleanProceso,
                borrado = safeOptionalString(borrado)
            )
            // Log detallado de resultados usando propiedades seguras
            Log.d("InventoryRepository", "Búsqueda avanzada exitosa - Total: ${response.totalItems}")
            response.data.forEach { item ->
                Log.d("InventoryRepository", "  ${item.codigoMatSafe}: ${item.descripcionSafe}")
                Log.d("InventoryRepository", "    Stock: ${item.existenciaFormateada}, Precio: ${item.precioFormateado}")
                Log.d("InventoryRepository", "    Estado: ${item.estadoStock}, Proceso: ${item.procesoSafe}")
            }
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP en búsqueda avanzada: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión en búsqueda avanzada", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado en búsqueda avanzada", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Función para eliminar material
    suspend fun deleteMaterial(codigoMat: String): Result<DeleteMaterialResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Validar código de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
                ?: return Result.failure(Exception("Código de material vacío"))
            Log.d("InventoryRepository", "Eliminando material: $cleanCodigoMat")
            val response = inventoryApiService.deleteMaterial(
                token = "Bearer $token",
                codigoMat = cleanCodigoMat
            )
            if (response.isSuccessful) {
                response.body()?.let { deleteResponse ->
                    Log.d("InventoryRepository", "Material eliminado exitosamente: ${deleteResponse.body.message}")
                    Result.success(deleteResponse)
                } ?: Result.failure(Exception("Respuesta vacía del servidor"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("InventoryRepository", "Error al eliminar material: ${response.code()} - $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP al eliminar material: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error del servidor al eliminar material"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión al eliminar material", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado al eliminar material", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Función para crear material con archivos
    suspend fun createMaterial(
        context: Context,
        codigoMat: String,
        descripcion: String,
        unidad: String,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String,
        cantxunidad: Double,
        proceso: String,
        imageUris: List<Uri> = emptyList()
    ): Result<CreateMaterialResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Validar datos de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
                ?: return Result.failure(Exception("Código de material requerido"))
            val cleanDescripcion = safeOptionalString(descripcion)
                ?: return Result.failure(Exception("Descripción requerida"))
            val cleanUnidad = safeString(unidad).takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = safeString(unidadEntrada).takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = safeString(proceso).takeIf { it.isNotBlank() } ?: "Sin proceso"
            // Validar números usando la lógica de las propiedades seguras
            val safePcompra = maxOf(0.0, pcompra)
            val safeExistencia = maxOf(0.0, existencia)
            val safeMax = maxOf(1.0, max)
            val safeMin = maxOf(0.0, min)
            val safeInventarioInicial = maxOf(0.0, inventarioInicial)
            val safeCantxunidad = maxOf(1.0, cantxunidad)
            Log.d("InventoryRepository", "Creando material: $cleanCodigoMat - $cleanDescripcion")
            Log.d("InventoryRepository", "Datos validados - Precio: $safePcompra, Stock: $safeExistencia, Min: $safeMin, Max: $safeMax")
            Log.d("InventoryRepository", "Imágenes a enviar: ${imageUris.size}")
            // Crear RequestBody para cada campo
            val codigoMatBody = cleanCodigoMat.toRequestBody("text/plain".toMediaTypeOrNull())
            val descripcionBody = cleanDescripcion.toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadBody = cleanUnidad.toRequestBody("text/plain".toMediaTypeOrNull())
            val pcompraBody = safePcompra.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val existenciaBody = safeExistencia.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val maxBody = safeMax.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val minBody = safeMin.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val inventarioInicialBody = safeInventarioInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadEntradaBody = cleanUnidadEntrada.toRequestBody("text/plain".toMediaTypeOrNull())
            val cantxunidadBody = safeCantxunidad.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val procesoBody = cleanProceso.toRequestBody("text/plain".toMediaTypeOrNull())
            // Preparar archivos de imagen
            val imageParts = mutableListOf<MultipartBody.Part>()
            imageUris.forEachIndexed { index, uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        // Obtener el nombre real del archivo si es posible
                        val fileName = getFileName(context, uri) ?: "image_$index.jpg"
                        // Crear archivo temporal
                        val tempFile = File(context.cacheDir, "temp_create_$fileName")
                        val outputStream = FileOutputStream(tempFile)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()
                        // Determinar el tipo MIME
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        // Crear RequestBody y MultipartBody.Part
                        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("files", fileName, requestFile)
                        imageParts.add(imagePart)
                        Log.d("InventoryRepository", "Imagen $index preparada: $fileName")
                    }
                } catch (e: Exception) {
                    Log.e("InventoryRepository", "Error preparando imagen $index", e)
                }
            }
            Log.d("InventoryRepository", "Enviando request con ${imageParts.size} imágenes")
            val response = inventoryApiService.createMaterial(
                token = "Bearer $token",
                codigoMat = codigoMatBody,
                descripcion = descripcionBody,
                unidad = unidadBody,
                pcompra = pcompraBody,
                existencia = existenciaBody,
                max = maxBody,
                min = minBody,
                inventarioInicial = inventarioInicialBody,
                unidadEntrada = unidadEntradaBody,
                cantxunidad = cantxunidadBody,
                proceso = procesoBody,
                files = if (imageParts.isNotEmpty()) imageParts else null
            )
            // Limpiar archivos temporales
            imageUris.forEachIndexed { index, uri ->
                val fileName = getFileName(context, uri) ?: "image_$index.jpg"
                val tempFile = File(context.cacheDir, "temp_create_$fileName")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
            // Log del resultado usando propiedades seguras si hay data
            response.data?.let { item ->
                Log.d("InventoryRepository", "Material creado - Código: ${item.codigoMatSafe}, Descripción: ${item.descripcionSafe}")
                Log.d("InventoryRepository", "Stock inicial: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
            }
            Log.d("InventoryRepository", "Material creado exitosamente: ${response.message}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP al crear material: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("InventoryRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, CreateMaterialErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor al crear material"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión al crear material", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado al crear material", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Función para actualizar material
    suspend fun updateMaterial(
        context: Context,
        codigoMat: String,
        descripcion: String,
        unidad: String,
        pcompra: Double,
        existencia: Double,
        max: Double,
        min: Double,
        inventarioInicial: Double,
        unidadEntrada: String,
        cantxunidad: Double,
        proceso: String,
        borrado: Boolean = false,
        newImageUris: List<Uri> = emptyList()
    ): Result<UpdateMaterialResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            // Validar datos de forma segura
            val cleanCodigoMat = safeOptionalString(codigoMat)
                ?: return Result.failure(Exception("Código de material requerido"))
            val cleanDescripcion = safeOptionalString(descripcion)
                ?: return Result.failure(Exception("Descripción requerida"))
            val cleanUnidad = safeString(unidad).takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = safeString(unidadEntrada).takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = safeString(proceso).takeIf { it.isNotBlank() } ?: "Sin proceso"
            // Validar números usando la lógica de las propiedades seguras
            val safePcompra = maxOf(0.0, pcompra)
            val safeExistencia = maxOf(0.0, existencia)
            val safeMax = maxOf(1.0, max)
            val safeMin = maxOf(0.0, min)
            val safeInventarioInicial = maxOf(0.0, inventarioInicial)
            val safeCantxunidad = maxOf(1.0, cantxunidad)
            Log.d("InventoryRepository", "Actualizando material: $cleanCodigoMat - $cleanDescripcion")
            Log.d("InventoryRepository", "Datos validados - Precio: $safePcompra, Stock: $safeExistencia, Min: $safeMin, Max: $safeMax")
            Log.d("InventoryRepository", "Nuevas imágenes: ${newImageUris.size}")
            // Crear RequestBody para cada campo
            val descripcionBody = cleanDescripcion.toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadBody = cleanUnidad.toRequestBody("text/plain".toMediaTypeOrNull())
            val pcompraBody = safePcompra.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val existenciaBody = safeExistencia.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val maxBody = safeMax.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val minBody = safeMin.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val inventarioInicialBody = safeInventarioInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadEntradaBody = cleanUnidadEntrada.toRequestBody("text/plain".toMediaTypeOrNull())
            val cantxunidadBody = safeCantxunidad.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val procesoBody = cleanProceso.toRequestBody("text/plain".toMediaTypeOrNull())
            val borradoBody = borrado.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            // Preparar archivos de imagen - todos con el nombre "imagenes"
            val imageParts = mutableListOf<MultipartBody.Part>()
            newImageUris.forEachIndexed { index, uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        // Obtener el nombre real del archivo si es posible
                        val fileName = getFileName(context, uri) ?: "image_$index.jpg"
                        // Crear archivo temporal
                        val tempFile = File(context.cacheDir, "temp_update_$fileName")
                        val outputStream = FileOutputStream(tempFile)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()
                        // Determinar el tipo MIME
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        // Crear RequestBody y MultipartBody.Part
                        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                        // Todos los archivos usan el mismo nombre de campo "imagenes"
                        val imagePart = MultipartBody.Part.createFormData("imagenes", fileName, requestFile)
                        imageParts.add(imagePart)
                        Log.d("InventoryRepository", "Imagen $index preparada: $fileName")
                    }
                } catch (e: Exception) {
                    Log.e("InventoryRepository", "Error preparando imagen $index", e)
                }
            }
            Log.d("InventoryRepository", "Enviando actualización con ${imageParts.size} imágenes")
            val response = inventoryApiService.updateMaterial(
                token = "Bearer $token",
                codigoMat = cleanCodigoMat,
                descripcion = descripcionBody,
                unidad = unidadBody,
                pcompra = pcompraBody,
                existencia = existenciaBody,
                max = maxBody,
                min = minBody,
                inventarioInicial = inventarioInicialBody,
                unidadEntrada = unidadEntradaBody,
                cantxunidad = cantxunidadBody,
                proceso = procesoBody,
                borrado = borradoBody,
                imagenes = if (imageParts.isNotEmpty()) imageParts else null
            )
            // Limpiar archivos temporales
            newImageUris.forEachIndexed { index, uri ->
                val fileName = getFileName(context, uri) ?: "image_$index.jpg"
                val tempFile = File(context.cacheDir, "temp_update_$fileName")
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
            // Log del resultado usando propiedades seguras si hay data
            response.data?.let { item ->
                Log.d("InventoryRepository", "Material actualizado - Código: ${item.codigoMatSafe}, Descripción: ${item.descripcionSafe}")
                Log.d("InventoryRepository", "Stock actualizado: ${item.existenciaFormateada}, Estado: ${item.estadoStock}")
                Log.d("InventoryRepository", "Precio: ${item.precioFormateado}, Rango: ${item.rangoStockFormateado}")
            }
            Log.d("InventoryRepository", "Material actualizado exitosamente: ${response.message}")
            Result.success(response)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP al actualizar material: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("InventoryRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, UpdateMaterialErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor al actualizar material"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión al actualizar material", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado al actualizar material", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Función de conveniencia para obtener todos los items (sin paginación)
    suspend fun getAllInventoryItems(): Result<List<InventoryItem>> {
        return try {
            val response = getInventoryItems(page = 1, limit = 1000) // Límite alto para obtener todos
            response.map { paginationResponse ->
                // Log usando propiedades seguras
                Log.d("InventoryRepository", "Todos los items obtenidos: ${paginationResponse.data.size}")
                paginationResponse.data.forEach { item ->
                    Log.d("InventoryRepository", "  ${item.codigoMatSafe}: ${item.descripcionSafe} (${item.estadoStock})")
                }
                paginationResponse.data
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función auxiliar para obtener el nombre del archivo
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    suspend fun getInventoryMovements(
        page: Int = 1,
        limit: Int = 10,
        folio: String? = null,
        notes: String? = null,
        usuario: String? = null,
        codigoMat: String? = null,
        descripcion: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null
    ): Result<InventoryMovementsPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }
            Log.d("InventoryRepository", "Obteniendo movimientos - Página: $page, Límite: $limit")
            Log.d("InventoryRepository", "Filtros - Folio: $folio, Usuario: $usuario, Código: $codigoMat")
            Log.d("InventoryRepository", "Fechas - Inicio: $fechaInicio, Fin: $fechaFin")
            val response = inventoryApiService.getInventoryMovements(
                token = "Bearer $token",
                page = page,
                limit = limit,
                folio = safeOptionalString(folio),
                notes = safeOptionalString(notes),
                usuario = safeOptionalString(usuario),
                codigoMat = safeOptionalString(codigoMat),
                descripcion = safeOptionalString(descripcion),
                fechaInicio = safeOptionalString(fechaInicio),
                fechaFin = safeOptionalString(fechaFin)
            )
            // Validar y limpiar datos usando propiedades seguras
            val validatedResponse = response.copy(
                data = response.data.map { movement ->
                    Log.d("InventoryRepository", "Movimiento validado: ${movement.tipoMovimiento} - Consecutivo: ${movement.consecutivoSafe}")
                    Log.d("InventoryRepository", "Usuario: ${movement.usuarioSafe}, Fecha: ${movement.fechaFormateada}")
                    Log.d("InventoryRepository", "Detalles: ${movement.detallesSafe.size} items, Total: ${movement.valorTotalFormateado}")
                    movement
                }
            )
            // Estadísticas usando propiedades seguras
            val entradas = validatedResponse.data.count { it.isEntry }
            val salidas = validatedResponse.data.count { it.isExit }
            val procesados = validatedResponse.data.count { it.procesadaSafe }
            Log.d("InventoryRepository", "Movimientos obtenidos exitosamente - Total: ${validatedResponse.totalItems}")
            Log.d("InventoryRepository", "Estadísticas - Entradas: $entradas, Salidas: $salidas, Procesados: $procesados")
            Result.success(validatedResponse)
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "Error HTTP en movimientos: ${e.code()}")
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = errorBody?.let {
                try {
                    gson.fromJson(it, InventoryMovementsErrorResponse::class.java)
                } catch (ex: Exception) {
                    null
                }
            }
            Result.failure(Exception(errorResponse?.message ?: "Error del servidor"))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "Error de conexión en movimientos", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error inesperado en movimientos", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda específica de movimientos por usuario
    suspend fun searchMovementsByUser(
        usuario: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryMovementsPaginationResponse> {
        return try {
            val cleanUsuario = safeOptionalString(usuario)
                ?: return Result.failure(Exception("Usuario de búsqueda vacío"))
            Log.d("InventoryRepository", "Buscando movimientos por usuario: '$cleanUsuario'")
            getInventoryMovements(
                page = page,
                limit = limit,
                usuario = cleanUsuario
            )
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error en búsqueda por usuario", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Búsqueda por rango de fechas
    suspend fun searchMovementsByDateRange(
        fechaInicio: String,
        fechaFin: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryMovementsPaginationResponse> {
        return try {
            val cleanFechaInicio = safeOptionalString(fechaInicio)
            val cleanFechaFin = safeOptionalString(fechaFin)
            if (cleanFechaInicio == null && cleanFechaFin == null) {
                return Result.failure(Exception("Debe especificar al menos una fecha"))
            }
            Log.d("InventoryRepository", "Buscando movimientos por fechas: $cleanFechaInicio - $cleanFechaFin")
            getInventoryMovements(
                page = page,
                limit = limit,
                fechaInicio = cleanFechaInicio,
                fechaFin = cleanFechaFin
            )
        } catch (e: Exception) {
            Log.e("InventoryRepository", "Error en búsqueda por fechas", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // Obtener todos los movimientos (sin paginación)
    suspend fun getAllInventoryMovements(): Result<List<InventoryMovementHeader>> {
        return try {
            val response = getInventoryMovements(page = 1, limit = 1000)
            response.map { paginationResponse ->
                Log.d("InventoryRepository", "Todos los movimientos obtenidos: ${paginationResponse.data.size}")
                paginationResponse.data.forEach { movement ->
                    Log.d("InventoryRepository", "  ${movement.tipoMovimiento}: ${movement.consecutivoSafe} (${movement.usuarioSafe})")
                }
                paginationResponse.data
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== CREAR MOVIMIENTO CON MANEJO MEJORADO DE ERRORES ====================
    suspend fun createMovement(
        folio: Int,
        movId: Int,
        fecha: String,
        detalles: List<CreateMovementDetail>,
        observacion: String = "",
        autoriza: String = "",
        procesada: Boolean = false
    ): Result<CreateMovementResponseWrapper> {
        return try {
            Log.d("InventoryRepository", "=== INICIANDO CREACIÓN DE MOVIMIENTO ===")
            Log.d("InventoryRepository", "Folio: $folio, MovId: $movId, Fecha: $fecha")
            Log.d("InventoryRepository", "Detalles: ${detalles.size} items")
            Log.d("InventoryRepository", "Observación: '$observacion', Autoriza: '$autoriza'")
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.e("InventoryRepository", "Token no disponible para createMovement")
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            // Validar datos
            if (detalles.isEmpty()) {
                Log.e("InventoryRepository", "No se pueden crear movimientos sin detalles")
                return Result.failure(Exception("Debe incluir al menos un detalle en el movimiento"))
            }
            // Validar folio
            if (folio <= 0) {
                Log.e("InventoryRepository", "Folio inválido: $folio")
                return Result.failure(Exception("El folio debe ser un número mayor a 0"))
            }
            // Validar que todos los detalles tengan datos válidos usando propiedades seguras
            detalles.forEach { detalle ->
                if (!detalle.esValido) {
                    Log.e("InventoryRepository", "Detalle inválido: ${detalle.codigoMatSafe} - ${detalle.cantidadSafe}")
                    return Result.failure(Exception("Todos los detalles deben tener código de material válido y cantidad mayor a 0"))
                }
            }
            // Crear request con datos seguros
            val request = CreateMovementRequest(
                folio = folio,
                movId = movId,
                fecha = safeString(fecha).takeIf { it.isNotBlank() } ?: getCurrentDate(),
                procesada = procesada,
                detalles = detalles,
                observacion = safeString(observacion),
                autoriza = safeString(autoriza)
            )
            // Log del JSON exacto que se enviará
            Log.d("InventoryRepository", "📋 REQUEST JSON QUE SE ENVIARÁ:")
            try {
                val jsonRequest = gson.toJson(request)
                Log.d("InventoryRepository", jsonRequest)
            } catch (e: Exception) {
                Log.w("InventoryRepository", "No se pudo serializar request para log: ${e.message}")
            }
            Log.d("InventoryRepository", "📤 Enviando request al servidor...")
            val response = inventoryApiService.createMovement("Bearer $token", request)
            Log.d("InventoryRepository", "📥 Respuesta recibida:")
            Log.d("InventoryRepository", "HTTP Status: ${response.statusCodeValue}")
            Log.d("InventoryRepository", "Body Message: ${response.body.errorSafe}") // Ahora usa errorSafe del body
            Log.d("InventoryRepository", "Body Status Code Value: ${response.statusCodeValueSafe}") // Usa statusCodeValueSafe del wrapper

            // ✅ CAMBIO CLAVE: Verificar el statusCodeValue del wrapper
            if (response.statusCodeValueSafe == 400) { // Asumiendo 400 es el código para errores de validación/negocio
                Log.e("InventoryRepository", "🚫 LÓGICA DE NEGOCIO: Error detectado en el cuerpo de la respuesta (statusCodeValue: 400)")
                // 'response' es CreateMovementResponseWrapper, que ahora tiene getUserFriendlyMessage()
                Result.failure(Exception(response.getUserFriendlyMessage()))
            } else {
                Result.success(response)
            }
        } catch (e: HttpException) {
            Log.e("InventoryRepository", "❌ ERROR HTTP en createMovement: ${e.code()}")
            // ✅ MANEJO ESPECÍFICO DEL ERROR DE FOLIO NO EXISTENTE
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("InventoryRepository", "Error body: $errorBody")
            val errorResponse = errorBody?.let {
                try {
                    // Aquí se sigue usando CreateMovementErrorResponse para errores HTTP puros
                    gson.fromJson(it, CreateMovementErrorResponse::class.java)
                } catch (ex: Exception) {
                    Log.e("InventoryRepository", "Error parseando JSON de error", ex)
                    null
                }
            }
            // Determinar el tipo de error y devolver mensaje apropiado
            val errorMessage = when {
                errorResponse != null -> {
                    Log.d("InventoryRepository", "🔍 Analizando error específico:")
                    Log.d("InventoryRepository", "  Error: ${errorResponse.errorSafe}")
                    Log.d("InventoryRepository", "  Status: ${errorResponse.statusCodeValueSafe}")
                    Log.d("InventoryRepository", "  Es error de folio: ${errorResponse.isFolioNotExistError()}")
                    when {
                        errorResponse.isFolioNotExistError() -> {
                            Log.e("InventoryRepository", "🚫 ERROR: Folio de papeleta no existe")
                            "El folio de papeleta ${folio} no existe en el sistema. Por favor, verifica el número e intenta nuevamente."
                        }
                        errorResponse.isValidationError() -> {
                            Log.e("InventoryRepository", "⚠️ ERROR: Datos de validación")
                            errorResponse.getUserFriendlyMessage()
                        }
                        else -> {
                            Log.e("InventoryRepository", "❓ ERROR: Otro tipo de error")
                            errorResponse.getUserFriendlyMessage()
                        }
                    }
                }
                e.code() == 400 -> {
                    Log.e("InventoryRepository", "🚫 ERROR 400: Posible folio inexistente")
                    "Error en los datos enviados. Verifica que el folio de papeleta sea correcto."
                }
                e.code() == 401 -> "No tienes autorización para crear movimientos"
                e.code() == 403 -> "No tienes permisos para crear movimientos"
                e.code() == 500 -> "Error interno del servidor. Intenta más tarde"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Log.e("InventoryRepository", "❌ ERROR DE RED en createMovement", e)
            Result.failure(Exception("Fallo de conexión. Verifica tu red e intenta nuevamente"))
        } catch (e: Exception) {
            Log.e("InventoryRepository", "❌ ERROR INESPERADO en createMovement", e)
            Result.failure(Exception("Error inesperado: ${e.message}"))
        }
    }

    // ✅ FUNCIÓN CORREGIDA: createMovementFromInventoryItems
    suspend fun createMovementFromInventoryItems(
        folio: Int,
        movId: Int,
        fecha: String,
        selectedItems: List<Pair<InventoryItem, Double>>, // Item y cantidad
        observacion: String = "",
        autoriza: String = "",
        procesada: Boolean = false
    ): Result<CreateMovementResponseWrapper> {
        return try {
            Log.d("InventoryRepository", "=== CREANDO MOVIMIENTO DESDE ITEMS SELECCIONADOS ===")
            Log.d("InventoryRepository", "Items seleccionados: ${selectedItems.size}")
            if (selectedItems.isEmpty()) {
                return Result.failure(Exception("Debe seleccionar al menos un item"))
            }
            // ✅ CORREGIDO: Convertir items de inventario a detalles con procesada como String
            val detalles = selectedItems.mapNotNull { (item, cantidad) ->
                try {
                    val detalle = CreateMovementDetail(
                        codigoMat = item.codigoMatSafe,
                        cantidad = cantidad,
                        procesada = procesada.toString() // ✅ CAMBIO PRINCIPAL: Boolean a String
                    )
                    if (detalle.esValido) {
                        Log.d("InventoryRepository", "✅ Agregando: ${detalle.codigoMatSafe} - Cantidad: ${detalle.cantidadFormateada} - Procesada: ${detalle.procesadaSafe}")
                        detalle
                    } else {
                        Log.w("InventoryRepository", "❌ Detalle inválido omitido: ${item.codigoMatSafe}")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("InventoryRepository", "💥 Error procesando item: ${item.codigoMatSafe}", e)
                    null
                }
            }
            if (detalles.isEmpty()) {
                return Result.failure(Exception("No hay detalles válidos para crear el movimiento"))
            }
            Log.d("InventoryRepository", "✅ Detalles convertidos exitosamente: ${detalles.size} items")
            detalles.forEach { detalle ->
                Log.d("InventoryRepository", "  - ${detalle.codigoMatSafe}: ${detalle.cantidadFormateada} (procesada: '${detalle.procesadaSafe}')")
            }
            createMovement(
                folio = folio,
                movId = movId,
                fecha = fecha,
                detalles = detalles,
                observacion = observacion,
                autoriza = autoriza,
                procesada = procesada
            )
        } catch (e: Exception) {
            Log.e("InventoryRepository", "💥 Error en createMovementFromInventoryItems", e)
            Result.failure(e)
        }
    }

    // Función helper para obtener fecha actual
    private fun getCurrentDate(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.format(Date())
        } catch (e: Exception) {
            "2024-01-01" // Fecha por defecto en caso de error
        }
    }
}
