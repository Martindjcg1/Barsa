package com.example.barsa.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Pendiente modificar de acuerdo a la API
data class Produccion(
    val folio: String,
    val cantidad: Int,
    val fecha: String
)

data class TiempoDC (
    val tipoId: String,
    val folio: Int,
    val fecha: String,
    val status: String,
    val tiempo: Int
)

// Informacion DE Papeletas
data class PapeletaModels(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<Data>
)

// Información EN Papeletas
data class Data(
    val Tipold: String,
    val Folio: Int,
    val Fecha: String,
    val Status: String,
    val ObservacionGeneral: String
)

data class DetallePapeleta (
    // En una papeleta se producen "N" cantidad de un producto "X". Puede que se produzcan 10 para un cliente y 20 para otro cliente en la misma papeleta
    val codigo: String, // Codigo del producto "ABD110"
    val descripcion: String, // Nombre del producto "ARMARIO BARSA MOD. DAVOZ 110!
    val color: String, // "NEGRO", "NOGAL", "TABACO" ETC
    val Tipold: String, // Correspondiente a la papeleta (Relaciona la Papeleta con DetallePapeleta)
    val Folio: Int, // Correspondiente a la papeleta (Relaciona la Papeleta con DetallePapeleta)
    val Fecha: String, // Correspondiente a la papeleta
    val Status: String, // Correspondiente a la papeleta
    val cantidad: Int, // Cantidad
    val cliente: String
)

