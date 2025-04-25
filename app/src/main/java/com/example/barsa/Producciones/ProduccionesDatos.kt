package com.example.barsa.Producciones

import com.example.barsa.Models.CantidadDetalle
import com.example.barsa.Models.Cliente
import com.example.barsa.Models.ClienteDetalle
import com.example.barsa.Models.DetallePapeleta
import com.example.barsa.Models.Papeleta
import com.example.barsa.Models.Producto
import com.example.barsa.Models.ProductoDetalle
import com.example.barsa.Models.color

val statusValues = listOf("S", "P", "A", "B")

val productos = listOf(
    Producto(codigo = "ABD110", descripcion = "ARMARIO BARSA MOD. DAVOZ 110"),
    Producto(codigo = "BCD220", descripcion = "ALACENA BARSA MOD. DAVOZ 60CM 4/P"),
    Producto(codigo = "CDE330", descripcion = "ANTECOMEDOR BARSA VENECIA 4/SILLAS"),
    Producto(codigo = "DEF440", descripcion = "ARMARIO BARSA MOD. SMART 2020")
)

val colores = listOf(
    color(id = 1, nombre = "NEGRO"),
    color(id = 2, nombre = "NOGAL"),
    color(id = 3, nombre = "TABACO"),
    color(id = 4, nombre = "Avellana")
)

val clientes = listOf(
    Cliente(id = 1, nombre = "Cliente A"),
    Cliente(id = 2, nombre = "Cliente B"),
    Cliente(id = 3, nombre = "Cliente C"),
    Cliente(id = 4, nombre = "Cliente D")
)

val observaciones = listOf(
    "TELA DUBLIN BEIGE",
    "TELA TERCIOPELO",
    "MELAMINA MONARCA",
    "TELA OSLO"
)

fun generarPapeletas(): List<Papeleta> {
    return List(150) { index ->
        val tipoId = when {
            index < 50 -> "A"
            index < 100 -> "B"
            else -> "C"
        }

        val status = statusValues[index % statusValues.size]

        val detalles = List((1..5).random()) {
            val detalleProductos = productos.shuffled().mapIndexed { idx, producto ->
                ProductoDetalle(
                    producto = producto,
                    color = colores.shuffled().first(),
                    observaciones = listOf(observaciones[idx % observaciones.size])
                )
            }
            val detalleClientes = clientes.shuffled().map { cliente ->
                ClienteDetalle(
                    cliente = cliente,
                    cantidades = List((1..3).random()) {
                        CantidadDetalle(
                            cantidad = (1..100).random(),
                            surtida = (0..1).random(),
                            backOrder = (1000..9999).random()
                        )
                    }
                )
            }

            DetallePapeleta(
                productos = detalleProductos,
                clientes = detalleClientes
            )
        }

        Papeleta(
            Tipold = tipoId,
            Folio = index + 1000,
            Fecha = "${(index % 28 + 1).toString().padStart(2, '0')}-${(index % 12 + 1).toString().padStart(2, '0')}-2025",
            Status = status,
            ObservacionGeneral = "Observación General ${index + 1}",
            detalles = detalles
        )
    }
}
