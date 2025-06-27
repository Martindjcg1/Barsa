package com.example.barsa.Models

import com.example.barsa.data.retrofit.models.InventoryItem
import java.util.Date



// Modelo para los items de inventario
data class InventoryItemfake(
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

// Modelo para categorías de inventario
data class InventoryCategory(
    val id: Int,
    val name: String,
    val description: String,
    val iconResId: Int
)

// Nuevo modelo para tipos de movimientos de inventario
data class MovimientoInventario(
    val movId: Int,
    val descripcion: String,
    val aumenta: Boolean,
    val borrado: Boolean = false
)

// Nuevo modelo para cabecera de movimientos (entradas y salidas)
data class MovimientosMateria(
    val consecutivo: Int,
    val movId: Int, // Referencia a MovimientoInventario.movId
    val fecha: String,
    val folio: String,
    val usuario: String,
    val procesada: Boolean,
    val observacion: String,
    val autoriza: String = "" // Mayormente estará vacío
)

// Nuevo modelo para detalles de movimientos
data class DetalleMovimientoMateria(
    val id: Int,
    val consecutivo: Int, // Relacionado con MovimientosMateria
    val codigoMat: String,
    val cantidad: Double,
    val existenciaAnt: Double,
    val pCosto: Double,
    val procesada: Boolean
)

// Modelo para mostrar un item en la lista de selección
data class InventoryItemSelection(
    val item: InventoryItem,
    val cantidad: Double,
    val pCosto: Double
)

// Función para obtener los tipos de movimientos de inventario
fun getMovimientosInventario(): List<MovimientoInventario> {
    return listOf(
        MovimientoInventario(1, "DEVOLUCION DE CLIENTE", true),        // Aumenta stock
        MovimientoInventario(2, "DEVOLUCION A PROVEEDOR", false),      // Disminuye stock
        MovimientoInventario(3, "DEVOLUCION A ALMACEN", true),         // Aumenta stock
        MovimientoInventario(4, "ENTRADA A ALMACEN", true),            // Aumenta stock
        MovimientoInventario(5, "SALIDA DE ALMACEN", false)            // Disminuye stock
    )
}

// Función para verificar si un movimiento aumenta el stock
fun movimientoAumentaStock(movId: Int): Boolean {
    return getMovimientosInventario().find { it.movId == movId }?.aumenta ?: false
}

// Función para verificar si un movimiento disminuye el stock
fun movimientoDisminuyeStock(movId: Int): Boolean {
    return !movimientoAumentaStock(movId)
}

// Función para obtener la descripción de un movimiento
fun getDescripcionMovimiento(movId: Int): String {
    return getMovimientosInventario().find { it.movId == movId }?.descripcion ?: "Desconocido"
}

// Función para obtener el efecto en el stock de un movimiento
fun getEfectoEnStock(movId: Int): String {
    return if (movimientoAumentaStock(movId)) "Aumenta" else "Disminuye"
}
