package com.example.barsa.data.retrofit.models

import com.google.gson.*
import java.lang.reflect.Type

// Deserializador personalizado para ImageInfo que maneja tanto strings como objetos
class ImageInfoDeserializer : JsonDeserializer<List<ImageInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<ImageInfo> {
        if (json == null || json.isJsonNull) {
            return emptyList()
        }

        return when {
            json.isJsonArray -> {
                val jsonArray = json.asJsonArray
                jsonArray.mapNotNull { element ->
                    when {
                        // Si es un string simple (URLs antiguas)
                        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                            ImageInfo(url = element.asString)
                        }
                        // Si es un objeto (nuevas imágenes con metadatos)
                        element.isJsonObject -> {
                            val obj = element.asJsonObject
                            ImageInfo(
                                url = obj.get("url")?.takeIf { !it.isJsonNull }?.asString,
                                filename = obj.get("filename")?.takeIf { !it.isJsonNull }?.asString,
                                originalName = obj.get("originalName")?.takeIf { !it.isJsonNull }?.asString,
                                size = obj.get("size")?.takeIf { !it.isJsonNull }?.asLong,
                                mimetype = obj.get("mimetype")?.takeIf { !it.isJsonNull }?.asString,
                                path = obj.get("path")?.takeIf { !it.isJsonNull }?.asString
                            )
                        }
                        else -> null
                    }
                }
            }
            else -> emptyList()
        }
    }
}
