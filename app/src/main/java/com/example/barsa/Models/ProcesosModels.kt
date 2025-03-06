package com.example.barsa.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

// Pendiente modificar de acuerdo a la API
data class Produccion(
    val folio: String,
    val cantidad: Int,
    val fecha: String
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

data class TiempoDC (
    val tipoId: String,
    val folio: Int,
    val fecha: String,
    val status: String,
    val tiempo: Int
)

