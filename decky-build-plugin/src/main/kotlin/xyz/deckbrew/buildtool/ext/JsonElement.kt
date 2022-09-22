package xyz.deckbrew.buildtool.ext

import org.jetbrains.kotlin.com.google.gson.JsonArray
import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.JsonObject

inline fun <reified T : Any> JsonElement.get(key: String): T {
    val obj = this.asJsonObject

    return when (T::class) {
        String::class -> obj.get(key).asString as T
        Int::class -> obj.get(key).asInt as T
        Double::class -> obj.get(key).asDouble as T
        Boolean::class -> obj.get(key).asBoolean as T
        JsonObject::class -> obj.get(key).asJsonObject as T
        JsonArray::class -> obj.get(key).asJsonArray as T
        else -> throw IllegalArgumentException("Unsupported type ${T::class}")
    }
}

inline fun <reified T : Any> JsonElement.getOrNull(key: String): T? {
    val obj = this.asJsonObject
    if (!obj.has(key)) {
        return null
    }
    return get(key)
}
