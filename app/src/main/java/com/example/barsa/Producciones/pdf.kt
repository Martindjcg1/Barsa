package com.example.barsa.Producciones

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.TiempoRemoto
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun generarPDF(
    context: Context,
    folio: Int,
    tiempos: List<TiempoRemoto>,
    detenciones: List<DetencionRemota>
) {
    val pdfDocument = PdfDocument()
    val paint = Paint()
    val titlePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 16f
        color = Color.BLACK
    }

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    var yPos = 40

    fun nuevaPagina() {
        pdfDocument.finishPage(page)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPos = 40
    }

    canvas.drawText("Informe de Producción - Folio $folio", 40f, yPos.toFloat(), titlePaint)
    yPos += 30

    tiempos.forEach { tiempo ->
        val contenido = listOf(
            "Etapa: ${tiempo.etapa}",
            " - Inicio: ${tiempo.fechaInicio}",
            " - Fin: ${tiempo.fechaFin ?: "En curso"}",
            " - Tiempo total: ${formatTiempoSmart(tiempo.tiempo)}"
        )

        contenido.forEach { texto ->
            if (yPos > 800) nuevaPagina()
            canvas.drawText(texto, if (texto.startsWith("Etapa")) 40f else 60f, yPos.toFloat(), paint)
            yPos += 20
        }

        val dets = detenciones.filter { it.etapa == tiempo.etapa }
        if (dets.isNotEmpty()) {
            if (yPos > 800) nuevaPagina()
            canvas.drawText("   Detenciones:", 60f, yPos.toFloat(), paint)
            yPos += 20

            dets.forEach {
                if (yPos > 800) nuevaPagina()
                canvas.drawText("    - ${it.motivo} (${formatearFechaHoraLinda(it.fecha)})", 80f, yPos.toFloat(), paint)
                yPos += 20
            }
        }

        yPos += 10
    }

    pdfDocument.finishPage(page)

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "InformeFolio_${folio}_$timeStamp.pdf"
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "PDF guardado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}