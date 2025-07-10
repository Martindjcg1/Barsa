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

// CORREGIDO: Todos los campos ahora son nullable para manejar datos del servidor
data class InventoryItem(
    val codigoMat: String?, // CAMBIADO: ahora nullable
    @SerializedName("imagenes")
    val imagenesInfo: List<ImageInfo>? = null, // CAMBIADO: ahora nullable
    val min: Double? = null, // CAMBIADO: ahora nullable
    val max: Double? = null, // CAMBIADO: ahora nullable
    val cantXUnidad: Double? = null, // CAMBIADO: ahora nullable
    val descripcion: String? = null, // CAMBIADO: ahora nullable
    val borrado: Boolean? = null, // CAMBIADO: ahora nullable
    val pcompra: Double? = null, // CAMBIADO: ahora nullable
    val proceso: String? = null, // CAMBIADO: ahora nullable
    val existencia: Double? = null, // CAMBIADO: ahora nullable
    val unidad: String? = null, // CAMBIADO: ahora nullable
    val inventarioInicial: Double? = null, // CAMBIADO: ahora nullable
    val unidadEntrada: String? = null // CAMBIADO: ahora nullable
) {
    // CORREGIDO: Propiedades computadas que manejan nulls correctamente
    val imagenes: List<String>
        get() = try {
            imagenesInfo?.takeIf { it.isNotEmpty() }?.map { it.getImageUrl() }?.filter { it.isNotEmpty() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    val imagenUrl: String
        get() = try {
            imagenes.firstOrNull() ?: "/placeholder.svg?height=100&width=100"
        } catch (e: Exception) {
            "/placeholder.svg?height=100&width=100"
        }

    val imagenesUrls: List<String>
        get() = try {
            if (imagenes.isNotEmpty()) imagenes else listOf("/placeholder.svg?height=100&width=100")
        } catch (e: Exception) {
            listOf("/placeholder.svg?height=100&width=100")
        }

    // CORREGIDO: Propiedades seguras para strings que manejan nulls
    val codigoMatSafe: String
        get() = try {
            codigoMat?.takeIf { it.isNotBlank() } ?: "SIN-CODIGO"
        } catch (e: Exception) {
            "SIN-CODIGO"
        }

    val descripcionSafe: String
        get() = try {
            descripcion?.takeIf { it.isNotBlank() } ?: "Sin descripción"
        } catch (e: Exception) {
            "Sin descripción"
        }

    val unidadSafe: String
        get() = try {
            unidad?.takeIf { it.isNotBlank() } ?: "UND"
        } catch (e: Exception) {
            "UND"
        }

    val procesoSafe: String
        get() = try {
            proceso?.takeIf { it.isNotBlank() } ?: "Sin proceso"
        } catch (e: Exception) {
            "Sin proceso"
        }

    val unidadEntradaSafe: String
        get() = try {
            unidadEntrada?.takeIf { it.isNotBlank() } ?: "UND"
        } catch (e: Exception) {
            "UND"
        }

    // CORREGIDO: Propiedades para convertir a Int que manejan nulls
    val existenciaInt: Int
        get() = try {
            maxOf(0, existencia?.toInt() ?: 0)
        } catch (e: Exception) {
            0
        }

    val minInt: Int
        get() = try {
            maxOf(0, min?.toInt() ?: 0)
        } catch (e: Exception) {
            0
        }

    val maxInt: Int
        get() = try {
            maxOf(1, max?.toInt() ?: 1)
        } catch (e: Exception) {
            1
        }

    // CORREGIDO: Propiedades adicionales útiles que manejan nulls
    val pcompraSafe: Double
        get() = try {
            maxOf(0.0, pcompra ?: 0.0)
        } catch (e: Exception) {
            0.0
        }

    val cantXUnidadSafe: Double
        get() = try {
            maxOf(1.0, cantXUnidad ?: 1.0)
        } catch (e: Exception) {
            1.0
        }

    val inventarioInicialSafe: Double
        get() = try {
            maxOf(0.0, inventarioInicial ?: 0.0)
        } catch (e: Exception) {
            0.0
        }

    val existenciaSafe: Double
        get() = try {
            maxOf(0.0, existencia ?: 0.0)
        } catch (e: Exception) {
            0.0
        }

    val minSafe: Double
        get() = try {
            maxOf(0.0, min ?: 0.0)
        } catch (e: Exception) {
            0.0
        }

    val maxSafe: Double
        get() = try {
            maxOf(1.0, max ?: 1.0)
        } catch (e: Exception) {
            1.0
        }

    val borradoSafe: Boolean
        get() = try {
            borrado ?: false
        } catch (e: Exception) {
            false
        }

    // CORREGIDO: Propiedades de estado que manejan nulls
    val tieneImagenes: Boolean
        get() = try {
            imagenes.isNotEmpty()
        } catch (e: Exception) {
            false
        }

    val estadoStock: String
        get() = try {
            val existenciaActual = existenciaSafe
            val minimo = minSafe
            val maximo = maxSafe
            when {
                existenciaActual <= 0 -> "Sin stock"
                existenciaActual <= minimo -> "Stock bajo"
                existenciaActual >= maximo -> "Stock alto"
                else -> "Stock normal"
            }
        } catch (e: Exception) {
            "Estado desconocido"
        }

    val estadoStockColor: String
        get() = try {
            when (estadoStock) {
                "Sin stock" -> "#F44336" // Rojo
                "Stock bajo" -> "#FF9800" // Naranja
                "Stock alto" -> "#2196F3" // Azul
                "Stock normal" -> "#4CAF50" // Verde
                else -> "#9E9E9E" // Gris
            }
        } catch (e: Exception) {
            "#9E9E9E" // Gris por defecto
        }

    // CORREGIDO: Propiedades formateadas para mostrar en UI que manejan nulls
    val existenciaFormateada: String
        get() = try {
            "${existenciaInt} ${unidadSafe}"
        } catch (e: Exception) {
            "0 UND"
        }

    val precioFormateado: String
        get() = try {
            "$${String.format("%.2f", pcompraSafe)}"
        } catch (e: Exception) {
            "$0.00"
        }

    val rangoStockFormateado: String
        get() = try {
            "Min: ${minInt} - Max: ${maxInt}"
        } catch (e: Exception) {
            "Min: 0 - Max: 1"
        }

    // NUEVO: Función para validar si el item tiene datos mínimos válidos
    fun isValid(): Boolean {
        return try {
            codigoMatSafe.isNotBlank() && descripcionSafe.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    // NUEVO: Función para obtener un resumen del item
    fun getSummary(): String {
        return try {
            "$codigoMatSafe - $descripcionSafe (${existenciaFormateada})"
        } catch (e: Exception) {
            "Item inválido"
        }
    }
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
    val imagenesToDelete: List<String> = emptyList()
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
