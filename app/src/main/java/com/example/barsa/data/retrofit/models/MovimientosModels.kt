package com.example.barsa.data.retrofit.models

import java.text.SimpleDateFormat
import java.util.Locale


// ==================== MODELOS PARA MOVIMIENTOS DE INVENTARIO CORREGIDOS ====================

// Request para obtener movimientos
data class GetInventoryMovementsRequest(
    val page: Int = 1,
    val limit: Int = 10,
    val folio: String? = null,
    val notes: String? = null,
    val usuario: String? = null,
    val codigoMat: String? = null,
    val descripcion: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null
)

// Response de paginación para movimientos
data class InventoryMovementsPaginationResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<InventoryMovementHeader>
)

// Header del movimiento (información principal)
data class InventoryMovementHeader(
    val consecutivo: Int?,
    val movId: Int?,
    val descripcionInventario: String?,
    val fecha: String?,
    val folio: Int?,
    val usuario: String?,
    val procesada: Boolean?,
    val observacion: String?,
    val detalles: List<InventoryMovementDetail>?
) {
    // CORREGIDO: Propiedades seguras para manejo de nulls con try-catch
    val consecutivoSafe: Int
        get() = try {
            maxOf(0, consecutivo ?: 0)
        } catch (e: Exception) {
            0
        }

    val movIdSafe: Int
        get() = try {
            maxOf(0, movId ?: 0)
        } catch (e: Exception) {
            0
        }

    val descripcionInventarioSafe: String
        get() = try {
            descripcionInventario?.trim()?.takeIf { it.isNotBlank() } ?: "Sin descripción"
        } catch (e: Exception) {
            "Sin descripción"
        }

    val fechaSafe: String
        get() = try {
            fecha?.trim()?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }

    val folioSafe: Int
        get() = try {
            maxOf(0, folio ?: 0)
        } catch (e: Exception) {
            0
        }

    val usuarioSafe: String
        get() = try {
            usuario?.trim()?.takeIf { it.isNotBlank() } ?: "Usuario desconocido"
        } catch (e: Exception) {
            "Usuario desconocido"
        }

    val procesadaSafe: Boolean
        get() = try {
            procesada ?: false
        } catch (e: Exception) {
            false
        }

    val observacionSafe: String
        get() = try {
            observacion?.trim()?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }

    val detallesSafe: List<InventoryMovementDetail>
        get() = try {
            detalles?.filterNotNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    // Propiedades computadas
    val tipoMovimiento: String
        get() = try {
            when (movIdSafe) {
                1 -> "DEVOLUCIÓN DE CLIENTE"
                2 -> "DEVOLUCIÓN A PROVEEDOR"
                3 -> "DEVOLUCIÓN A ALMACÉN"
                4 -> "ENTRADA A ALMACÉN"
                5 -> "SALIDA DE ALMACÉN"
                else -> "MOVIMIENTO DESCONOCIDO"
            }
        } catch (e: Exception) {
            "MOVIMIENTO DESCONOCIDO"
        }

    val iconoMovimiento: String
        get() = try {
            when (movIdSafe) {
                1 -> "↩️" // Devolución cliente
                2 -> "📤" // Devolución proveedor
                3 -> "🔄" // Devolución almacén
                4 -> "📥" // Entrada
                5 -> "📤" // Salida
                else -> "❓"
            }
        } catch (e: Exception) {
            "❓"
        }

    val isEntry: Boolean
        get() = try {
            movIdSafe in listOf(1, 3, 4)
        } catch (e: Exception) {
            false
        }

    val isExit: Boolean
        get() = try {
            movIdSafe in listOf(2, 5)
        } catch (e: Exception) {
            false
        }

    val tieneObservacion: Boolean
        get() = try {
            observacionSafe.isNotBlank()
        } catch (e: Exception) {
            false
        }

    val fechaFormateada: String
        get() {
            return try {
                if (fechaSafe.isBlank()) {
                    "Sin fecha"
                } else {
                    val inputFormats = listOf(
                        "yyyy-MM-dd HH:mm:ss.SSSSSS",
                        "yyyy-MM-dd HH:mm:ss",
                        "yyyy-MM-dd",
                        "dd/MM/yyyy HH:mm:ss",
                        "dd/MM/yyyy"
                    )
                    for (format in inputFormats) {
                        try {
                            val inputFormat = SimpleDateFormat(format, Locale.getDefault())
                            val date = inputFormat.parse(fechaSafe)
                            if (date != null) {
                                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                return outputFormat.format(date)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                    fechaSafe
                }
            } catch (e: Exception) {
                "Sin fecha"
            }
        }

    // Cálculos de totales
    val cantidadTotal: Double
        get() = try {
            detallesSafe.sumOf { it.cantidadSafe }
        } catch (e: Exception) {
            0.0
        }

    val valorTotal: Double
        get() = try {
            detallesSafe.sumOf { it.valorTotal }
        } catch (e: Exception) {
            0.0
        }

    val valorTotalFormateado: String
        get() = try {
            "$${String.format("%.2f", valorTotal)}"
        } catch (e: Exception) {
            "$0.00"
        }

    // Función para validar si el movimiento es válido
    fun isValid(): Boolean {
        return try {
            consecutivoSafe > 0 && movIdSafe > 0 && descripcionInventarioSafe.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    // Resumen del movimiento
    fun getSummary(): String {
        return try {
            "$tipoMovimiento - Consecutivo: $consecutivoSafe (${detallesSafe.size} items)"
        } catch (e: Exception) {
            "Movimiento inválido"
        }
    }
}

data class InventoryMovementDetail(
    val id: Int?,
    val consecutivo: Int?,
    val codigoMat: String?,
    val descripcion: String?,
    val cantidad: Double?,
    val existenciaAnterior: Double?,
    val pCosto: Double?,
    val procesada: Boolean?
) {
    // CORREGIDO: Propiedades computadas seguras con try-catch
    val idSafe: Int
        get() = try {
            id ?: 0
        } catch (e: Exception) {
            0
        }

    val consecutivoSafe: Int
        get() = try {
            consecutivo ?: 0
        } catch (e: Exception) {
            0
        }

    val codigoMatSafe: String
        get() = try {
            codigoMat?.trim()?.takeIf { it.isNotBlank() } ?: "Sin código"
        } catch (e: Exception) {
            "Sin código"
        }

    val descripcionSafe: String
        get() = try {
            descripcion?.trim()?.takeIf { it.isNotBlank() } ?: "Sin descripción"
        } catch (e: Exception) {
            "Sin descripción"
        }

    val cantidadSafe: Double
        get() = try {
            cantidad ?: 0.0
        } catch (e: Exception) {
            0.0
        }

    val existenciaAnteriorSafe: Double
        get() = try {
            existenciaAnterior ?: 0.0
        } catch (e: Exception) {
            0.0
        }

    val pcostoSafe: Double
        get() = try {
            pCosto ?: 0.0
        } catch (e: Exception) {
            0.0
        }

    val procesadaSafe: Boolean
        get() = try {
            procesada ?: false
        } catch (e: Exception) {
            false
        }

    // CORREGIDO: Propiedades formateadas con try-catch
    val cantidadFormateada: String
        get() = try {
            String.format("%.2f", cantidadSafe)
        } catch (e: Exception) {
            "0.00"
        }

    val existenciaAnteriorFormateada: String
        get() = try {
            String.format("%.2f", existenciaAnteriorSafe)
        } catch (e: Exception) {
            "0.00"
        }

    val pcostoFormateado: String
        get() = try {
            "$ ${String.format("%.2f", pcostoSafe)}"
        } catch (e: Exception) {
            "$ 0.00"
        }

    // CORREGIDO: Valor total calculado con try-catch
    val valorTotal: Double
        get() = try {
            cantidadSafe * pcostoSafe
        } catch (e: Exception) {
            0.0
        }

    val valorTotalFormateado: String
        get() = try {
            "$ ${String.format("%.2f", valorTotal)}"
        } catch (e: Exception) {
            "$ 0.00"
        }
}

// Error response para movimientos
data class InventoryMovementsErrorResponse(
    val message: String,
    val error: String? = null
) {
    // NUEVO: Propiedades seguras
    val messageSafe: String
        get() = try {
            message.trim().takeIf { it.isNotBlank() } ?: "Error desconocido"
        } catch (e: Exception) {
            "Error desconocido"
        }

    val errorSafe: String
        get() = try {
            error?.trim()?.takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }
}

// ==================== MODELOS PARA CREAR MOVIMIENTO CORREGIDOS ====================

// CORREGIDO: Detalle del movimiento simplificado según tu JSON
data class CreateMovementDetail(
    val codigoMat: String,
    val cantidad: Double,
    val procesada: String = "false"  // ✅ CAMBIADO: String en lugar de Boolean
) {
    // CORREGIDO: Propiedades seguras mejoradas
    val codigoMatSafe: String
        get() = try {
            codigoMat.trim().takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }

    val cantidadSafe: Double
        get() = try {
            maxOf(0.0, cantidad)
        } catch (e: Exception) {
            0.0
        }

    val procesadaSafe: String
        get() = try {
            procesada.takeIf { it in listOf("true", "false") } ?: "false"
        } catch (e: Exception) {
            "false"
        }

    // CORREGIDO: Propiedades formateadas seguras
    val cantidadFormateada: String
        get() = try {
            String.format("%.2f", cantidadSafe)
        } catch (e: Exception) {
            "0.00"
        }

    // CORREGIDO: Validaciones mejoradas
    val esValido: Boolean
        get() = try {
            codigoMatSafe.isNotBlank() && cantidadSafe > 0
        } catch (e: Exception) {
            false
        }
}

// CORREGIDO: Request para crear movimiento con validaciones
data class CreateMovementRequest(
    val folio: Int,
    val movId: Int,
    val fecha: String,
    val procesada: Boolean = false,
    val detalles: List<CreateMovementDetail>,
    val observacion: String = "",
    val autoriza: String = ""
) {
    // NUEVO: Propiedades seguras para evitar nulls
    val folioSafe: Int
        get() = try {
            maxOf(0, folio)
        } catch (e: Exception) {
            0
        }

    val movIdSafe: Int
        get() = try {
            maxOf(1, movId)
        } catch (e: Exception) {
            1
        }

    val fechaSafe: String
        get() = try {
            fecha.trim().takeIf { it.isNotBlank() } ?: ""
        } catch (e: Exception) {
            ""
        }

    val observacionSafe: String
        get() = try {
            observacion.trim()
        } catch (e: Exception) {
            ""
        }

    val autorizaSafe: String
        get() = try {
            autoriza.trim()
        } catch (e: Exception) {
            ""
        }

    val detallesSafe: List<CreateMovementDetail>
        get() = try {
            detalles.filter { it.esValido }
        } catch (e: Exception) {
            emptyList()
        }

    val procesadaSafe: Boolean
        get() = try {
            procesada
        } catch (e: Exception) {
            false
        }
}

// CORREGIDO: Response para crear movimiento
data class CreateMovementResponse(
    val message: String,
    val error: String? = null,
    val statusCode: String,
    val statusCodeValue: Int
) {




    // CORREGIDO: Propiedades computadas seguras con try-catch
    val messageSafe: String
        get() = try {
            message.trim().takeIf { it.isNotBlank() } ?: "Movimiento creado"
        } catch (e: Exception) {
            "Movimiento creado"
        }



    val statusCodeSafe: String
        get() = try {
            statusCode.trim().takeIf { it.isNotBlank() } ?: "OK"
        } catch (e: Exception) {
            "OK"
        }

    val statusCodeValueSafe: Int
        get() = try {
            statusCodeValue // Directamente usa el valor parseado del JSON
        } catch (e: Exception) {
            // En caso de un error de deserialización inesperado, asume un error 400
            400
        }

    // Propiedades de estado
    val isSuccess: Boolean
        get() = try {
            statusCodeValueSafe in 200..299
        } catch (e: Exception) {
            false
        }

    val isCreated: Boolean
        get() = try {
            statusCodeValueSafe == 201
        } catch (e: Exception) {
            false
        }

    val errorSafe: String
        get() = try {
            error?.trim()?.takeIf { it.isNotBlank() } ?: "Error desconocido"
        } catch (e: Exception) {
            "Error desconocido"
        }

    // Función para detectar errores específicos
    fun isFolioNotExistError(): Boolean {
        return try {
            errorSafe.contains("folio de papeleta no existe", ignoreCase = true) ||
                    messageSafe.contains("folio de papeleta no existe", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    fun isValidationError(): Boolean {
        return try {
            statusCodeValueSafe == 400
        } catch (e: Exception) {
            false
        }
    }

    // Obtener mensaje de error amigable para el usuario
    fun getUserFriendlyMessage(): String {
        return try {
            when {
                isFolioNotExistError() -> "El folio de papeleta ingresado no existe. Por favor, verifica el número e intenta nuevamente."
                statusCodeValueSafe == 400 -> "Los datos ingresados no son válidos. ${messageSafe}"
                statusCodeValueSafe == 401 -> "No tienes autorización para realizar esta operación."
                statusCodeValueSafe == 403 -> "No tienes permisos para crear movimientos."
                statusCodeValueSafe == 500 -> "Error interno del servidor. Intenta más tarde."
                else -> messageSafe
            }
        } catch (e: Exception) {
            "Error desconocido al procesar la solicitud"
        }
    }
}

// CORREGIDO: Wrapper para el response
data class CreateMovementResponseWrapper(
    val headers: Map<String, Any> = emptyMap(),
    val body: CreateMovementResponse, // Este es el objeto interno que ahora solo tiene 'error'
    val statusCode: String,
    val statusCodeValue: Int
) {
    // CORREGIDO: Propiedades computadas con try-catch
    // isSuccess ahora verifica el statusCodeValue del wrapper
    val isSuccess: Boolean
        get() = try {
            statusCodeValue in 200..299
        } catch (e: Exception) {
            false
        }

    // message ahora toma el error del body interno, o un mensaje por defecto
    val message: String
        get() = try {
            body.errorSafe.takeIf { it.isNotBlank() } ?: "Respuesta inválida"
        } catch (e: Exception) {
            "Respuesta inválida"
        }

    val statusCodeSafe: String
        get() = try {
            statusCode.trim().takeIf { it.isNotBlank() } ?: "UNKNOWN"
        } catch (e: Exception) {
            "UNKNOWN"
        }

    val statusCodeValueSafe: Int
        get() = try {
            statusCodeValue // Usa directamente el valor del wrapper
        } catch (e: Exception) {
            0
        }

    // NUEVO/CORREGIDO: Función para detectar errores específicos de folio
    fun isFolioNotExistError(): Boolean {
        return try {
            statusCodeValueSafe == 400 && body.errorSafe.contains("folio de papeleta no existe", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    // NUEVO/CORREGIDO: Función para detectar errores de validación
    fun isValidationError(): Boolean {
        return try {
            statusCodeValueSafe == 400
        } catch (e: Exception) {
            false
        }
    }

    // NUEVO/CORREGIDO: Obtener mensaje de error amigable para el usuario
    fun getUserFriendlyMessage(): String {
        return try {
            when {
                isFolioNotExistError() -> "El folio de papeleta ingresado no existe. Por favor, verifica el número e intenta nuevamente."
                statusCodeValueSafe == 400 -> "Los datos ingresados no son válidos. ${body.errorSafe}"
                statusCodeValueSafe == 401 -> "No tienes autorización para realizar esta operación."
                statusCodeValueSafe == 403 -> "No tienes permisos para crear movimientos."
                statusCodeValueSafe == 500 -> "Error interno del servidor. Intenta más tarde."
                else -> message // Fallback al mensaje derivado de body.errorSafe
            }
        } catch (e: Exception) {
            "Error desconocido al procesar la solicitud"
        }
    }
}


// ==================== EXTENSIONES PARA COMPATIBILIDAD CORREGIDAS ====================

// CORREGIDO: Extensión para convertir Pair a CreateMovementDetail simplificado
fun List<Pair<InventoryItem, Double>>.toCreateMovementDetails(procesada: String = "false"): List<CreateMovementDetail> {
    return try {
        this.mapNotNull { (item, cantidad) ->
            try {
                val codigoMat = item.codigoMatSafe.takeIf { it.isNotBlank() }
                if (codigoMat != null && cantidad > 0) {
                    CreateMovementDetail(
                        codigoMat = codigoMat,
                        cantidad = cantidad,
                        procesada = procesada
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}


private fun String?.safeString(): String {
    return try {
        when {
            this == null -> ""
            this.isBlank() -> ""
            else -> this.trim()
        }
    } catch (e: Exception) {
        ""
    }
}


private fun String?.safeOptionalString(): String? {
    return try {
        this?.trim()?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        null
    }
}



// Modelo específico para errores de creación de movimiento
data class CreateMovementErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val statusCode: String? = null,
    val statusCodeValue: Int? = null,
    val details: String? = null
) {
    val errorSafe: String
        get() = try {
            error?.trim()?.takeIf { it.isNotBlank() } ?: "Error desconocido"
        } catch (e: Exception) {
            "Error desconocido"
        }

    val messageSafe: String
        get() = try {
            message?.trim()?.takeIf { it.isNotBlank() } ?: errorSafe
        } catch (e: Exception) {
            "Error desconocido"
        }

    val statusCodeSafe: String
        get() = try {
            statusCode?.trim()?.takeIf { it.isNotBlank() } ?: "UNKNOWN"
        } catch (e: Exception) {
            "UNKNOWN"
        }

    val statusCodeValueSafe: Int
        get() = try {
            statusCodeValue ?: 400
        } catch (e: Exception) {
            400
        }

    // Función para detectar errores específicos
    fun isFolioNotExistError(): Boolean {
        return try {
            errorSafe.contains("folio de papeleta no existe", ignoreCase = true) ||
                    messageSafe.contains("folio de papeleta no existe", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    fun isValidationError(): Boolean {
        return try {
            statusCodeValueSafe == 400
        } catch (e: Exception) {
            false
        }
    }

    // Obtener mensaje de error amigable para el usuario
    fun getUserFriendlyMessage(): String {
        return try {
            when {
                isFolioNotExistError() -> "El folio de papeleta ingresado no existe. Por favor, verifica el número e intenta nuevamente."
                statusCodeValueSafe == 400 -> "Los datos ingresados no son válidos. ${messageSafe}"
                statusCodeValueSafe == 401 -> "No tienes autorización para realizar esta operación."
                statusCodeValueSafe == 403 -> "No tienes permisos para crear movimientos."
                statusCodeValueSafe == 500 -> "Error interno del servidor. Intenta más tarde."
                else -> messageSafe
            }
        } catch (e: Exception) {
            "Error desconocido al procesar la solicitud"
        }
    }
}