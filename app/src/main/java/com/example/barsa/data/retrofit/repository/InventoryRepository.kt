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

            Log.d("InventoryRepository", "Items obtenidos exitosamente - Total: ${response.totalItems}")
            Result.success(response)

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

            Log.d("InventoryRepository", "Buscando por código: '$code' - Página: $page")

            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = code,
                descripcion = null,
                unidad = null,
                proceso = null,
                borrado = null
            )

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

            Log.d("InventoryRepository", "Buscando por descripción: '$description' - Página: $page")

            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = null,
                descripcion = description,
                unidad = null,
                proceso = null,
                borrado = null
            )

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

            Log.d("InventoryRepository", "Búsqueda flexible: '$query' - Código: $searchInCode, Descripción: $searchInDescription")

            // Si solo busca en código
            if (searchInCode && !searchInDescription) {
                return searchByCode(query, page, limit)
            }

            // Si solo busca en descripción
            if (!searchInCode && searchInDescription) {
                return searchByDescription(query, page, limit)
            }

            // Si busca en ambos, primero intenta por código exacto
            if (searchInCode && searchInDescription) {
                // Primero buscar por código exacto
                val codeResult = searchByCode(query, page, limit)

                codeResult.fold(
                    onSuccess = { codeResponse ->
                        if (codeResponse.data.isNotEmpty()) {
                            Log.d("InventoryRepository", "Encontrados ${codeResponse.data.size} items por código")
                            return Result.success(codeResponse)
                        } else {
                            // Si no hay resultados por código, buscar por descripción
                            Log.d("InventoryRepository", "No hay resultados por código, buscando por descripción")
                            return searchByDescription(query, page, limit)
                        }
                    },
                    onFailure = {
                        // Si falla la búsqueda por código, intentar por descripción
                        Log.d("InventoryRepository", "Falló búsqueda por código, intentando por descripción")
                        return searchByDescription(query, page, limit)
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

            Log.d("InventoryRepository", "Búsqueda avanzada - Query: '$query', Código: '$codigoMat', Descripción: '$descripcion'")

            // Si hay un query general, determinar si es código o descripción
            val finalCodigoMat = codigoMat ?: if (query?.matches(Regex("^[A-Z0-9-]+$")) == true) query else null
            val finalDescripcion = descripcion ?: if (query != null && finalCodigoMat == null) query else null

            val response = inventoryApiService.getInventoryItems(
                token = "Bearer $token",
                page = page,
                limit = limit,
                codigoMat = finalCodigoMat,
                descripcion = finalDescripcion,
                unidad = unidad,
                proceso = proceso,
                borrado = borrado
            )

            Log.d("InventoryRepository", "Búsqueda avanzada exitosa - Total: ${response.totalItems}")
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

            Log.d("InventoryRepository", "Eliminando material: $codigoMat")

            // AQUÍ ESTABA EL ERROR - Ahora se pasa el token
            val response = inventoryApiService.deleteMaterial(
                token = "Bearer $token",  // ← ESTA LÍNEA FALTABA
                codigoMat = codigoMat
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

            Log.d("InventoryRepository", "Creando material: $codigoMat - $descripcion")
            Log.d("InventoryRepository", "Imágenes a enviar: ${imageUris.size}")

            // Crear RequestBody para cada campo
            val codigoMatBody = codigoMat.toRequestBody("text/plain".toMediaTypeOrNull())
            val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadBody = unidad.toRequestBody("text/plain".toMediaTypeOrNull())
            val pcompraBody = pcompra.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val existenciaBody = existencia.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val maxBody = max.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val minBody = min.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val inventarioInicialBody = inventarioInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadEntradaBody = unidadEntrada.toRequestBody("text/plain".toMediaTypeOrNull())
            val cantxunidadBody = cantxunidad.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val procesoBody = proceso.toRequestBody("text/plain".toMediaTypeOrNull())

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

            Log.d("InventoryRepository", "Actualizando material: $codigoMat - $descripcion")
            Log.d("InventoryRepository", "Nuevas imágenes: ${newImageUris.size}")

            // Crear RequestBody para cada campo (nombres exactos como en la imagen)
            val descripcionBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadBody = unidad.toRequestBody("text/plain".toMediaTypeOrNull())
            val pcompraBody = pcompra.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val existenciaBody = existencia.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val maxBody = max.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val minBody = min.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val inventarioInicialBody = inventarioInicial.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val unidadEntradaBody = unidadEntrada.toRequestBody("text/plain".toMediaTypeOrNull())
            val cantxunidadBody = cantxunidad.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val procesoBody = proceso.toRequestBody("text/plain".toMediaTypeOrNull())
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
                codigoMat = codigoMat,
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
            response.map { it.data }
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