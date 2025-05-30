package com.example.barsa.data.retrofit.models

data class ListadoPapeletasResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<Papeleta>
)

data class Papeleta(
    val detallepapeleta: List<DetallePapeleta>,
    val status: String,
    val folio: Int,
    val tipoId: String,
    val fecha: String,
    val observacionGeneral: String
)

data class DetallePapeleta(
    val id: Int,
    val nombreColor: String,
    val nombreCliente: String,
    val clienteId: Int,
    val surtida: Int,
    val backOrder: Int,
    val observacion: String,
    val colorId: Int,
    val folio: Int,
    val codigo: String,
    val cantidad: Int,
    val tipoId: String,
    val descripcionProducto: String
)

data class TiempoRemoto(
    val id: Int,
    val procesoFolio: Int,
    val etapa: String,
    val tiempo: Int,
    val fechaInicio: String?,
    val fechaFin: String?,
    val isRunning: Boolean,
    val isFinished: Boolean
)

data class DetencionRemota(
    val id: Int,
    val folio: Int,
    val tiempoId: Int,
    val etapa: String,
    val motivo: String,
    val fecha: String,
    val activa: Boolean
)

data class DesactivarDetencionResponse(
    val message: String? = null,
    val error: String? = null
)



