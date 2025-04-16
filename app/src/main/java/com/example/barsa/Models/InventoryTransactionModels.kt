package com.example.barsa.Models

// Modelo para proveedores
data class Supplier(
    val id: String,
    val name: String,
    val contactInfo: String,
    val address: String
)



// Modelo para items en transacciones (tanto entradas como salidas)
data class InventoryTransactionItem(
    val inventoryItem: InventoryItem,
    val quantity: Double,
    val unitPrice: Double = 0.0 // Solo relevante para entradas
)

// Enumeración para razones de salida
enum class ExitReason(val displayName: String) {
    PRODUCTION("Producción"),
    DAMAGE("Daño/Deterioro"),
    RETURN("Devolución a proveedor"),
    TRANSFER("Transferencia a otra ubicación"),
    SALE("Venta"),
    OTHER("Otra razón")
}
