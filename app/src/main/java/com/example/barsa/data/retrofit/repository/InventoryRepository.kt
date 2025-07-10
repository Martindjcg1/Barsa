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
import com.example.barsa.data.retrofit.models.DeleteMaterialResponse
import com.example.barsa.data.retrofit.models.UpdateMaterialErrorResponse
import com.example.barsa.data.retrofit.models.UpdateMaterialResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream


@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryApiService: InventoryApiService,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()

    suspend fun getInventoryItems(
        page: Int = 1,
        limit: Int = 10,
        codigoMat: String? = null,
        descripcion: String? = null,
        unidad: String? = null,
        proceso: String? = null,
        borrado: String? = null
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
                codigoMat = codigoMat,
                descripcion = descripcion,
                unidad = unidad,
                proceso = proceso,
                borrado = borrado
            )

            // NUEVO: Validar y limpiar datos usando las propiedades seguras
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

            // NUEVO: Limpiar y validar el código de búsqueda
            val cleanCode = code.trim().takeIf { it.isNotBlank() } ?: return Result.failure(Exception("Código de búsqueda vacío"))

            Log.d("InventoryRepository", "Buscando por código: '$cleanCode' - Página: $page")

            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = cleanCode,
                descripcion = null,
                unidad = null,
                proceso = null,
                borrado = null
            )

            // NUEVO: Log detallado de resultados usando propiedades seguras
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

            // NUEVO: Limpiar y validar la descripción de búsqueda
            val cleanDescription = description.trim().takeIf { it.isNotBlank() }
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
                borrado = null
            )

            // NUEVO: Log detallado usando propiedades seguras
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

            // NUEVO: Validar query usando propiedades seguras
            val cleanQuery = query.trim().takeIf { it.isNotBlank() }
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
                            // NUEVO: Log usando propiedades seguras
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
        borrado: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<InventoryPaginationResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }

            // NUEVO: Limpiar parámetros de búsqueda
            val cleanQuery = query?.trim()?.takeIf { it.isNotBlank() }
            val cleanCodigoMat = codigoMat?.trim()?.takeIf { it.isNotBlank() }
            val cleanDescripcion = descripcion?.trim()?.takeIf { it.isNotBlank() }
            val cleanUnidad = unidad?.trim()?.takeIf { it.isNotBlank() }
            val cleanProceso = proceso?.trim()?.takeIf { it.isNotBlank() }

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
                borrado = borrado
            )

            // NUEVO: Log detallado de resultados usando propiedades seguras
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

    // FUNCIÓN CORREGIDA - Ahora incluye el token de autorización
    suspend fun deleteMaterial(codigoMat: String): Result<DeleteMaterialResponse> {
        return try {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token de acceso no disponible"))
            }

            // NUEVO: Validar código usando propiedades seguras
            val cleanCodigoMat = codigoMat.trim().takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Código de material vacío"))

            Log.d("InventoryRepository", "Eliminando material: $cleanCodigoMat")

            // AQUÍ ESTABA EL ERROR - Ahora se pasa el token
            val response = inventoryApiService.deleteMaterial(
                token = "Bearer $token",  // ← ESTA LÍNEA FALTABA
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

            // NUEVO: Validar datos usando las propiedades seguras del modelo
            val cleanCodigoMat = codigoMat.trim().takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Código de material requerido"))
            val cleanDescripcion = descripcion.trim().takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Descripción requerida"))
            val cleanUnidad = unidad.trim().takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = unidadEntrada.trim().takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = proceso.trim().takeIf { it.isNotBlank() } ?: "Sin proceso"

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

            // NUEVO: Log del resultado usando propiedades seguras si hay data
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

            // NUEVO: Validar datos usando las propiedades seguras del modelo
            val cleanCodigoMat = codigoMat.trim().takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Código de material requerido"))
            val cleanDescripcion = descripcion.trim().takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("Descripción requerida"))
            val cleanUnidad = unidad.trim().takeIf { it.isNotBlank() } ?: "UND"
            val cleanUnidadEntrada = unidadEntrada.trim().takeIf { it.isNotBlank() } ?: "UND"
            val cleanProceso = proceso.trim().takeIf { it.isNotBlank() } ?: "Sin proceso"

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

            // Crear RequestBody para cada campo (nombres exactos como en la imagen)
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
                inventarioInicial = inventarioInicialBody, // Nombre corregido
                unidadEntrada = unidadEntradaBody,
                cantxunidad = cantxunidadBody, // Nombre corregido
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

            // NUEVO: Log del resultado usando propiedades seguras si hay data
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
                // NUEVO: Log usando propiedades seguras
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
}
