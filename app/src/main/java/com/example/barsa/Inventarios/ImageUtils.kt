package com.example.barsa.Inventarios

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    // Configuración optimizada para evitar requests demasiado grandes
    private const val MAX_WIDTH = 800  // Reducido significativamente
    private const val MAX_HEIGHT = 600 // Reducido significativamente
    private const val COMPRESSION_QUALITY = 60 // Reducido para menor tamaño
    private const val MAX_IMAGES = 3 // Límite de imágenes

    /**
     * Convierte una URI de imagen a Base64 optimizado para APIs
     */
    fun uriToBase64(context: Context, uri: Uri, quality: Int = COMPRESSION_QUALITY): String? {
        return try {
            Log.d("ImageUtils", "Iniciando conversión de imagen a Base64")

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e("ImageUtils", "No se pudo decodificar la imagen")
                return null
            }

            Log.d("ImageUtils", "Imagen original: ${originalBitmap.width}x${originalBitmap.height}")

            // Redimensionar agresivamente para reducir tamaño
            val resizedBitmap = resizeBitmap(originalBitmap, MAX_WIDTH, MAX_HEIGHT)
            Log.d("ImageUtils", "Imagen redimensionada: ${resizedBitmap.width}x${resizedBitmap.height}")

            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            Log.d("ImageUtils", "Tamaño final del array de bytes: ${byteArray.size} bytes (${byteArray.size / 1024} KB)")

            // Formato simple para la API (sin el prefijo data:image)
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d("ImageUtils", "Base64 generado, longitud: ${base64String.length}")

            // Liberar memoria
            originalBitmap.recycle()
            resizedBitmap.recycle()

            base64String // Sin prefijo, solo el Base64 puro

        } catch (e: Exception) {
            Log.e("ImageUtils", "Error converting URI to Base64", e)
            null
        }
    }

    /**
     * Redimensiona un bitmap manteniendo la proporción
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Si la imagen ya es pequeña, no redimensionar
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Convierte múltiples URIs a Base64 con límite de imágenes
     */
    fun urisToBase64List(context: Context, uris: List<Uri>, quality: Int = COMPRESSION_QUALITY): List<String> {
        if (uris.isEmpty()) {
            Log.d("ImageUtils", "No hay imágenes para convertir")
            return emptyList()
        }

        // Limitar el número de imágenes
        val limitedUris = uris.take(MAX_IMAGES)
        if (uris.size > MAX_IMAGES) {
            Log.w("ImageUtils", "Se limitaron las imágenes de ${uris.size} a $MAX_IMAGES")
        }

        Log.d("ImageUtils", "Convirtiendo ${limitedUris.size} imagen(es) a Base64")

        val results = mutableListOf<String>()
        limitedUris.forEachIndexed { index, uri ->
            Log.d("ImageUtils", "Procesando imagen ${index + 1}/${limitedUris.size}")
            val base64 = uriToBase64(context, uri, quality)
            if (base64 != null) {
                results.add(base64)
                Log.d("ImageUtils", "Imagen ${index + 1} convertida exitosamente")
            } else {
                Log.e("ImageUtils", "Error al convertir imagen ${index + 1}: $uri")
            }
        }

        Log.d("ImageUtils", "Conversión completada: ${results.size}/${limitedUris.size} imágenes exitosas")
        return results
    }

    /**
     * Obtiene el tamaño estimado en KB de las imágenes después de la compresión
     */
    fun getEstimatedSizeKB(context: Context, uris: List<Uri>): Double {
        var totalSize = 0L
        val limitedUris = uris.take(MAX_IMAGES)

        limitedUris.forEach { uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val resizedBitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream)
                    totalSize += byteArrayOutputStream.size()

                    bitmap.recycle()
                    resizedBitmap.recycle()
                }
            } catch (e: Exception) {
                Log.e("ImageUtils", "Error calculating size for $uri", e)
            }
        }
        return totalSize / 1024.0 // Convertir a KB
    }

    fun getMaxImages(): Int = MAX_IMAGES
}
