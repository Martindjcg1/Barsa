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

/*
// Informacion DE Papeletas
data class PapeletaModels(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val data: List<Data>
)

Información EN Papeletas
data class Data(
    val Tipold: String,
    val Folio: Int,
    val Fecha: String,
    val Status: String,
    val ObservacionGeneral: String,
    val detallePapeleta: List<DetallePapeleta>
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
*/

data class PapeletaModels(
    val totalItems: Int, // Total de ítems
    val totalPages: Int, // Total de páginas
    val currentPage: Int, // Página actual
    val data: List<Papeleta> // Lista de papeletas
)

data class Papeleta(
    val Tipold: String, // Identificador de tipo de papeleta
    val Folio: Int, // Folio único de la papeleta
    val Fecha: String, // Fecha de la papeleta
    val Status: String, // Estado de la papeleta
    val ObservacionGeneral: String, // Observación general asociada a la papeleta
    val detalles: List<DetallePapeleta> // Lista de detalles asociados a la papeleta
)

data class DetallePapeleta(
    val productos: List<ProductoDetalle>, // Lista de productos con detalles específicos
    val clientes: List<ClienteDetalle>, // Lista de detalles por cliente
)

data class ProductoDetalle(
    val producto: Producto, // Información del producto
    val color: color, // Color asociado
    val observaciones: List<String> // Observaciones específicas del producto (Tela que se utilizara o tipo de madera o material con el que se fabricara)
)

data class Producto(
    val codigo: String, // Código del producto (ej. "ABD110")
    val descripcion: String // Descripción o nombre del producto
)

data class color(
    val id: Int, // Id único del color
    val nombre: String // Nombre del color (ej. "NEGRO", "NOGAL")
)

data class ClienteDetalle(
    val cliente: Cliente, // Información del cliente
    val cantidades: List<CantidadDetalle>, // Lista de cantidades específicas por cliente-producto-color
)

data class Cliente(
    val id: Int, // Id único del cliente
    val nombre: String // Nombre del cliente
)

data class CantidadDetalle(
    val cantidad: Int, // Cantidad asociada a un producto y cliente
    val surtida: Int, // 0 o 1
    val backOrder: Int // Número de 4 digitos por lo regular
)

