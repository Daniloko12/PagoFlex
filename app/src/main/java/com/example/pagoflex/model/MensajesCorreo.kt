package com.example.pagoflex.model

const val CORREO_SOPORTE = "soporte@pagoflex.ejemplo"

data class CorreoArmado(val para: String, val asunto: String, val mensaje: String)

private fun detalleDelCobro(compromiso: Compromiso): String = listOf(
    "- Concepto: ${compromiso.concepto}",
    "- Folio: ${compromiso.folio}",
    "- Monto: ${formatoPesos(compromiso.monto)}",
    "- Vencimiento: ${formatoFecha(compromiso.fechaVencimiento)}",
    "- Estado: ${compromiso.estado.etiqueta}"
).joinToString("\n")

fun correoParaEmpresa(empresa: EmpresaCliente, compromiso: Compromiso, usuario: Usuario) = CorreoArmado(
    para = empresa.correoAtencion,
    asunto = "Consulta por ${compromiso.concepto} (${compromiso.folio})",
    mensaje = "Hola, equipo de ${empresa.nombreFantasia}:\n\n" +
        "Les escribo por este cobro que veo en PagoFlex:\n${detalleDelCobro(compromiso)}\n\n" +
        "Mi consulta es:\n\n\n" +
        "Saludos,\n${nombreCompleto(usuario)}\n${usuario.correo}"
)

fun correoParaCliente(persona: Usuario, compromiso: Compromiso, empresa: EmpresaCliente?) = CorreoArmado(
    para = persona.correo,
    asunto = "PagoFlex: ${compromiso.concepto} (${compromiso.folio})",
    mensaje = "Hola, ${primerNombre(persona)}:\n\n" +
        "Te escribimos por este pago${empresa?.let { " de ${it.nombreFantasia}" }.orEmpty()}:\n" +
        "${detalleDelCobro(compromiso)}\n\n\n\n" +
        "Saludos,\nEquipo PagoFlex"
)

fun correoParaSoporte(usuario: Usuario?, correoDeLaCuenta: String? = usuario?.correo): CorreoArmado {
    val misDatos = listOfNotNull(
        usuario?.let { "- Nombre: ${nombreCompleto(it)}" },
        correoDeLaCuenta?.trim()?.takeIf { it.isNotEmpty() }?.let { "- Correo de la cuenta: $it" },
        usuario?.let { "- Rol: ${it.rol.etiqueta}" }
    )
    return CorreoArmado(
        para = CORREO_SOPORTE,
        asunto = if (usuario == null) "No puedo entrar a PagoFlex" else "Ayuda con PagoFlex",
        mensaje = "Hola, equipo PagoFlex:\n\nLes cuento qué pasó:\n\n\n" +
            if (misDatos.isEmpty()) "" else "Mis datos:\n${misDatos.joinToString("\n")}"
    )
}
