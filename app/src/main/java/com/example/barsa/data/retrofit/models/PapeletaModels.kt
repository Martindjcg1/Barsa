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
    val isFinished: Boolean,
    val usuario: String
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

data class IniciarTiempoRequest(
    val folio: Int,
    val etapa: String,
    val fechaInicio: String
)

data class PausarTiempoRequest(
    val folio: Int,
    val etapa: String,
    val tiempo: Int
)

data class ReiniciarTiempoRequest(
    val folio: Int,
    val etapa: String
)

data class FinalizarTiempoRequest(
    val folio: Int,
    val etapa: String,
    val fechaFin: String,
    val tiempo: Int
)

data class DetencionTiempoRequest(
    val tiempo: Int,
    val etapa: String,
    val folio: Int,
    val fecha: String,
    val motivo: String
)

data class ApiSuccessResponse(
    val message: String?
)

data class ApiWrapperResponse(
    val body: ApiSuccessResponse?,
    val statusCodeValue: Int?,
    val statusCode: String?
)

data class ApiErrorResponse(
    val message: String?,
    val error: String?,
    val statusCode: Int?
)

data class ListadoTiemposResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<TiemposPeriodo>
)

data class TiemposPeriodo(
    val id: Int,
    val procesoFolio: Int,
    val etapa: String,
    val tiempo: Int,
    val fechaInicio: String?,
    val fechaFin: String?,
    val isRunning: Boolean,
    val isFinished: Boolean,
    val usuario: String,
    val detenciones: List<DetencionRemota>
)




