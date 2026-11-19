package com.example.pagoflex.model

import java.net.URI

const val PUERTO_GATEWAY = 8080
const val IP_EMULADOR = "10.0.2.2"
const val SERVIDOR_EMULADOR = "http://$IP_EMULADOR:$PUERTO_GATEWAY"

private val patronNombre = Regex("""^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*$""")

fun normalizarServidor(texto: String): String {
    val limpio = texto.trim().trimEnd('/')
    if (limpio.isEmpty()) return ""
    val conEsquema = if (limpio.startsWith("http://") || limpio.startsWith("https://")) limpio else "http://$limpio"
    val uri = runCatching { URI(conEsquema) }.getOrNull() ?: return conEsquema
    val host = uri.host ?: return conEsquema
    val puerto = when {
        uri.port != -1 -> ":${uri.port}"
        uri.scheme == "http" -> ":$PUERTO_GATEWAY"
        else -> ""
    }
    return "${uri.scheme}://$host$puerto"
}

fun errorDireccionServidor(texto: String): String? {
    val direccion = normalizarServidor(texto)
    if (direccion.isEmpty()) return "Escribe la IP del PC con los microservicios o la URL de VS Code"
    val host = runCatching { URI(direccion).host }.getOrNull()
        ?: return "Escribe una IP como 192.168.1.20 o una URL como https://…devtunnels.ms"
    if (host.all { it.isDigit() || it == '.' }) {
        val partes = host.split(".")
        if (partes.size != 4 || partes.any { it.isEmpty() }) return "Una IP tiene 4 números separados por puntos, por ejemplo 192.168.1.20"
        if (partes.any { it.toInt() > 255 }) return "Revisa la IP: cada número va de 0 a 255"
        return null
    }
    return if (patronNombre.matches(host)) null else "Escribe una IP como 192.168.1.20 o una URL como https://…devtunnels.ms"
}
