package com.example.barsa.Body.Inventory

import com.example.barsa.Models.InventoryItem

// Función para categorizar materiales basado en su descripción
fun categorizarMaterial(descripcion: String): String {
    val desc = descripcion.lowercase()

    return when {
        desc.contains("cubeta") -> "Cubetas"
        desc.contains("tela") -> "Telas"
        desc.contains("casco") -> "Cascos"
        desc.contains("llave") || desc.contains("allen") || desc.contains("hexagonal") -> "Herramientas"
        desc.contains("bisagra") || desc.contains("herrajes") -> "Bisagras y Herrajes"
        desc.contains("perno") || desc.contains("union") -> "Pernos y Sujetadores"
        desc.contains("cinta") || desc.contains("adhesivo") -> "Cintas y Adhesivos"
        desc.contains("separador") || desc.contains("cristal") -> "Separadores y Accesorios de Cristal"
        desc.contains("cubre canto") || desc.contains("nogal") -> "Cubrecantos y Acabados"
        desc.contains("tachuela") || desc.contains("tira") || desc.contains("banda") -> "Otros Materiales de Construcción"
        else -> "Otros"
    }
}

// Función para obtener todos los items del inventario
fun getAllInventoryItems(): List<InventoryItem> {
    // Aquí se cargarían los datos desde una base de datos o API
    // Por ahora, usamos datos de ejemplo
    return listOf(
        // Datos de ejemplo basados en la imagen del Excel
        InventoryItem(
            codigoMat = "001",
            descripcion = "TACHUELA",
            unidad = "PIEZAS",
            pCompra = 1.00,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZ",
            cantXUnidad = 10,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00101024",
            descripcion = "***CONTRAPESO",
            unidad = "PZAS",
            pCompra = 1.96,
            existencia = -1474.57,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00102014",
            descripcion = "TIRA TROQUELADA",
            unidad = "PZA",
            pCompra = 3.72,
            existencia = -1562.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00146123",
            descripcion = "LLAVE ALLEN",
            unidad = "PZA",
            pCompra = 0.71,
            existencia = 4896.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00150119",
            descripcion = "BISAGRA",
            unidad = "PZAS",
            pCompra = 3.00,
            existencia = 0.0,
            max = 1000,
            min = 200,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00150153",
            descripcion = "BISAGRA",
            unidad = "PZAS",
            pCompra = 5.88,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00176035",
            descripcion = "PERNO UNION",
            unidad = "PZA",
            pCompra = 0.52,
            existencia = 9592.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "E",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "00176040",
            descripcion = "PERNO UNION",
            unidad = "PZA",
            pCompra = 0.69,
            existencia = -118.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "E",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "00640013",
            descripcion = "CUBRE CANTO",
            unidad = "MTR",
            pCompra = 0.00,
            existencia = -48.3,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "MTR",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "01",
            descripcion = "TACHUELA",
            unidad = "PIEZAS",
            pCompra = 0.00,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "100",
            cantXUnidad = 10,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "010-0041",
            descripcion = "SEPARADOR",
            unidad = "PZAS",
            pCompra = 31.97,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "010-0042",
            descripcion = "SEPARADOR",
            unidad = "PZAS",
            pCompra = 38.36,
            existencia = 0.0,
            max = 0,
            min = 0,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "M",
            borrado = true
        ),
        InventoryItem(
            codigoMat = "01013011",
            descripcion = "CINTA NEGRA",
            unidad = "PZAS",
            pCompra = 28.50,
            existencia = 0.0,
            max = 100,
            min = 20,
            inventarioInicial = 0.0,
            unidadEntrada = "PZAS",
            cantXUnidad = 1,
            proceso = "E",
            borrado = true
        ),
        // Añadir más ejemplos para cubrir todas las categorías
        InventoryItem(
            codigoMat = "CUB001",
            descripcion = "CUBETA DE PINTURA 20L",
            unidad = "PZA",
            pCompra = 350.00,
            existencia = 5.0,
            max = 20,
            min = 5,
            inventarioInicial = 10.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "TEL001",
            descripcion = "TELA PARA TAPIZADO",
            unidad = "MTR",
            pCompra = 120.00,
            existencia = 45.0,
            max = 50,
            min = 10,
            inventarioInicial = 30.0,
            unidadEntrada = "MTR",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false
        ),
        InventoryItem(
            codigoMat = "CAS001",
            descripcion = "CASCO DE MUEBLE PINO",
            unidad = "PZA",
            pCompra = 85.00,
            existencia = 4.0,
            max = 15,
            min = 5,
            inventarioInicial = 10.0,
            unidadEntrada = "PZA",
            cantXUnidad = 1,
            proceso = "M",
            borrado = false,
            imagenUrl = "https://hebbkx1anhila5yf.public.blob.vercel-storage.com/1Silla.jpg-HB7s6P9KeDIbzEuTeUDENxjJXh11j0.jpeg",
            imagenesUrls = listOf(
                "https://hebbkx1anhila5yf.public.blob.vercel-storage.com/1Silla.jpg-HB7s6P9KeDIbzEuTeUDENxjJXh11j0.jpeg",
                "https://hebbkx1anhila5yf.public.blob.vercel-storage.com/2Mesa.jpg-h95PKhUXllCnuej4CMuW9Cn7qf2XJH.jpeg",
                "https://hebbkx1anhila5yf.public.blob.vercel-storage.com/Cubo-asiento.jpg-N6VvrhHaS2WP9IKmlfIEmiOafgxd1a.jpeg"
            )
        )
    )
}

