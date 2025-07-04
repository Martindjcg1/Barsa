package com.example.barsa.data.retrofit.models

import com.google.gson.annotations.SerializedName

// Modelo para la información de imagen que devuelve el servidor
data class ImageInfo(
    val url: String? = null,
    val filename: String? = null,
    val originalName: String? = null,
    val size: Long? = null,
    val mimetype: String? = null,
    val path: String? = null
) {
    // Función para obtener la URL de la imagen
    fun getImageUrl(): String {
        return url ?: path ?: filename ?: ""
    }
}

// Requests
data class GetInventoryRequest(
    val page: Int = 1,
    val limit: Int = 10,
    val codigoMat: String? = null,
    val descripcion: String? = null,
    val unidad: String? = null,
    val proceso: String? = null,
    val borrado: String? = null
)

// Responses
data class InventoryPaginationResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<InventoryItem>
)

data class InventoryItem(
    val codigoMat: String,
    @SerializedName("imagenes") // El servidor envía "imagenes", pero lo mapeamos a imagenesInfo
    val imagenesInfo: List<ImageInfo> = emptyList(),
    val min: Double,
    val max: Double,
    val cantXUnidad: Double,
    val descripcion: String,
    val borrado: Boolean,
    val pcompra: Double,
    val proceso: String,
    val existencia: Double,
    val unidad: String,
    val inventarioInicial: Double,
    val unidadEntrada: String
) {
    // Propiedades computadas para compatibilidad con el código existente
    val imagenes: List<String>
        get() = imagenesInfo.map { it.getImageUrl() }

    val imagenUrl: String?
        get() = imagenes.firstOrNull()

    val imagenesUrls: List<String>
        get() = imagenes

    // Propiedades para convertir a Int cuando sea necesario para la UI
    val existenciaInt: Int
        get() = existencia.toInt()

    val minInt: Int
        get() = min.toInt()

    val maxInt: Int
        get() = max.toInt()
}

// Error response
data class InventoryErrorResponse(
    val message: String,
    val error: String? = null
)



// Request para crear material (sin imágenes, se envían por separado)
data class CreateMaterialRequest(
    val codigoMat: String,
    val descripcion: String,
    val unidad: String,
    val pcompra: Double,
    val existencia: Double,
    val max: Double,
    val min: Double,
    val inventarioInicial: Double,
    val unidadEntrada: String,
    val cantxunidad: Double,
    val proceso: String
    // Las imágenes se envían como archivos separados en multipart/form-data
)

// Response para crear material
data class CreateMaterialResponse(
    val success: Boolean,
    val message: String,
    val data: InventoryItem? = null
)

// Error response para crear material
data class CreateMaterialErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, List<String>>? = null
)



// Request para actualizar material
data class UpdateMaterialRequest(
    val descripcion: String,
    val unidad: String,
    val pcompra: Double,
    val existencia: Double,
    val max: Double,
    val min: Double,
    val inventarioInicial: Double,
    val unidadEntrada: String,
    val cantxunidad: Double,
    val proceso: String,
    val imagenesToDelete: List<String> = emptyList() // URLs de imágenes a eliminar
)

// Response para actualizar material
data class UpdateMaterialResponse(
    val success: Boolean,
    val message: String,
    val data: InventoryItem? = null
)

// Error response para actualizar material
data class UpdateMaterialErrorResponse(
    val success: Boolean = false,
    val message: String,
    val errors: Map<String, List<String>>? = null
)


data class DeleteMaterialResponse(
    val headers: Map<String, Any> = emptyMap(),
    val body: DeleteMaterialBody,
    val statusCode: String,
    val statusCodeValue: Int
)

data class DeleteMaterialBody(
    val message: String
)
