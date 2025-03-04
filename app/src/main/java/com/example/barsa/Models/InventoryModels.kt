package com.example.barsa.Models



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
    val imagenUrl: String? = null
)