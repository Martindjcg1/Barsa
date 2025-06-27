package com.example.barsa.data.retrofit.models

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// Deserializador personalizado para manejar tanto strings como objetos en imagenes
class ImagenesDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String> {
        if (json == null || json.isJsonNull) {
            return emptyList()
        }

        return when {
            json.isJsonArray -> {
                val jsonArray = json.asJsonArray
                jsonArray.mapNotNull { element ->
                    when {
                        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                            element.asString
                        }
                        element.isJsonObject -> {
                            val obj = element.asJsonObject
                            // Intentar obtener la URL de diferentes campos posibles
                            obj.get("url")?.takeIf { !it.isJsonNull }?.asString
                                ?: obj.get("path")?.takeIf { !it.isJsonNull }?.asString
                                ?: obj.get("filename")?.takeIf { !it.isJsonNull }?.asString
                                ?: ""
                        }
                        else -> null
                    }
                }.filter { it.isNotEmpty() }
            }
            else -> emptyList()
        }
    }
}

// Modelo alternativo usando el deserializador personalizado
data class InventoryItemAlternative(
    val codigoMat: String,
    @JsonAdapter(ImagenesDeserializer::class)
    val imagenes: List<String> = emptyList(), // Usar deserializador personalizado
    val min: Double,
    val max: Double,
    val cantXUnidad: Double,
    val descripcion: String,
    val borrado: Boolean,
    val pcompra: Double,
    val proceso: String,
    val existencia: Double,
    val unidad: String,
    val inventarioInicial: Double,
    val unidadEntrada: String
) {
    // Propiedades computadas para compatibilidad con el código existente
    val imagenUrl: String?
        get() = imagenes.firstOrNull()

    val imagenesUrls: List<String>
        get() = imagenes

    // Propiedades para convertir a Int cuando sea necesario para la UI
    val existenciaInt: Int
        get() = existencia.toInt()

    val minInt: Int
        get() = min.toInt()

    val maxInt: Int
        get() = max.toInt()
}
