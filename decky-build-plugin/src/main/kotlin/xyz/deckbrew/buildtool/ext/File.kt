package xyz.deckbrew.buildtool.ext

import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.JsonParser
import java.io.File

fun File.readJson(): JsonElement {
    return JsonParser.parseReader(this.reader())
}

inline fun <reified T: Any> File.readJson(): T {
    return GsonBuilder()
        .setLenient()
        .create()
        .fromJson(this.reader(), T::class.java)
}

fun File.exists(path: String) = resolve(path).exists()
