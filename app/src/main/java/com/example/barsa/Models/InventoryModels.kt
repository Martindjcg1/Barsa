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

// Modelo para entrada de inventario
data class InventoryEntry(
    val id: String,
    val date: Date,
    val supplier: Supplier,
    val items: List<InventoryTransactionItem>,
    val totalAmount: Double,
    val notes: String,
    val createdBy: String  // Nombre del usuario
)

// Modelo para salida de inventario
data class InventoryExit(
    val id: String,
    val date: Date,
    val reason: String,
    val items: List<InventoryTransactionItem>,
    val destination: String,
    val notes: String,
    val createdBy: String  // Nombre del usuario
)

data class InventoryEntryItem(
    val item: InventoryItem,
    val cantidad: Double,
    val precioUnitario: Double
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
