package com.example.pagoflex.data.remoto

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.Base64

class AdaptadorTexto<T : Any>(private val leer: (String) -> T) : TypeAdapter<T>() {
    override fun write(salida: JsonWriter, valor: T?) {
        if (valor == null) salida.nullValue() else salida.value(valor.toString())
    }

    override fun read(entrada: JsonReader): T? {
        if (entrada.peek() == JsonToken.NULL) {
            entrada.nextNull()
            return null
        }
        return leer(entrada.nextString())
    }
}

class AdaptadorBase64 : TypeAdapter<ByteArray>() {
    override fun write(salida: JsonWriter, valor: ByteArray?) {
        if (valor == null) salida.nullValue() else salida.value(Base64.getEncoder().encodeToString(valor))
    }

    override fun read(entrada: JsonReader): ByteArray? {
        if (entrada.peek() == JsonToken.NULL) {
            entrada.nextNull()
            return null
        }
        return Base64.getDecoder().decode(entrada.nextString())
    }
}
