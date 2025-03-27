package com.example.barsa.Models

import java.util.Date

data class InventoryCategory(
    val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int
)

data class InventoryItem(
    val codigoMat: String,
    val descripcion: String,
    val unidad: String,
    val pCompra: Double,
    val existencia: Double,
    val max: Int,
    val min: Int,
    val inventarioInicial: Double,
    val unidadEntrada: String,
    val cantXUnidad: Int,
    val proceso: String,
    val borrado: Boolean,
    val imagenUrl: String? = null,
    val imagenesUrls: List<String> = emptyList()
)

data class InventoryEntry(
    val id: String,
    val fecha: Date,
    val proveedor: String,
    val items: List<InventoryEntryItem>,
    val observaciones: String,
    val registradoPor: String
)

data class InventoryEntryItem(
    val item: InventoryItem,
    val cantidad: Double,
    val precioUnitario: Double
)

data class InventoryExit(
    val id: String,
    val fecha: Date,
    val motivo: String,
    val items: List<InventoryExitItem>,
    val observaciones: String,
    val registradoPor: String
)

data class InventoryExitItem(
    val item: InventoryItem,
    val cantidad: Double
)

data class InventoryChange(
    val id: String,
    val fecha: Date,
    val tipoOperacion: TipoOperacion,
    val descripcion: String,
    val itemsAfectados: List<String>, // Códigos de los items afectados
    val registradoPor: String
)

enum class TipoOperacion {
    ENTRADA,
    SALIDA,
    MODIFICACION,
    ELIMINACION
}
