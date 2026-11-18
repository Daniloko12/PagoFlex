package com.example.pagoflex.model

const val SERVIDOR_EMULADOR = "10.0.2.2"

private val patronIp = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")
private val patronNombre = Regex("""^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*$""")

fun normalizarServidor(texto: String): String = texto.trim()
    .removePrefix("http://")
    .removePrefix("https://")
    .substringBefore("/")
    .substringBefore(":")
    .trim()

fun errorDireccionServidor(texto: String): String? {
    val direccion = normalizarServidor(texto)
    if (direccion.isEmpty()) return "Escribe la IP del PC donde corren los microservicios"
    val ip = patronIp.matchEntire(direccion)
    if (ip != null) {
        return if (ip.groupValues.drop(1).all { it.toInt() in 0..255 }) null
        else "Revisa la IP: cada número va de 0 a 255"
    }
    if (direccion.all { it.isDigit() || it == '.' }) return "Una IP tiene 4 números separados por puntos, por ejemplo 192.168.1.20"
    return if (patronNombre.matches(direccion)) null else "Escribe una IP como 192.168.1.20"
}
