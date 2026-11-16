package com.example.pagoflex.model

enum class Accion(val descripcion: String) {
    VER_MIS_COMPROMISOS("Ver sus compromisos, su avance y su historial"),
    AGREGAR_MIS_COMPROMISOS("Agregar sus propias cuentas o boletas por pagar"),
    PAGAR_COMPROMISO("Pagar sus propios compromisos"),
    REPORTAR_COBRO("Reportar un cobro que no reconoce"),
    RECOMENDAR_ORGANIZACION("Recomendar una organización"),
    VER_TODOS_LOS_COMPROMISOS("Buscar y ver los compromisos de todos los clientes"),
    CREAR_COMPROMISO("Registrar compromisos de pago para cualquier cliente"),
    EDITAR_COMPROMISO("Modificar concepto, monto, vencimiento y cuotas"),
    ANULAR_COMPROMISO("Anular compromisos indicando el motivo"),
    GESTIONAR_REPORTES("Tomar y resolver los reportes de los clientes"),
    GESTIONAR_CLIENTES("Crear y editar clientes y restablecer su contraseña"),
    GESTIONAR_RECOMENDACIONES("Avanzar o descartar recomendaciones"),
    VER_MOVIMIENTOS("Ver el historial de movimientos de toda la app")
}

private val accionesUsuario = setOf(
    Accion.VER_MIS_COMPROMISOS,
    Accion.AGREGAR_MIS_COMPROMISOS,
    Accion.PAGAR_COMPROMISO,
    Accion.REPORTAR_COBRO,
    Accion.RECOMENDAR_ORGANIZACION
)

private val accionesAdministrador = setOf(
    Accion.VER_TODOS_LOS_COMPROMISOS,
    Accion.CREAR_COMPROMISO,
    Accion.EDITAR_COMPROMISO,
    Accion.ANULAR_COMPROMISO,
    Accion.GESTIONAR_REPORTES,
    Accion.GESTIONAR_CLIENTES,
    Accion.GESTIONAR_RECOMENDACIONES,
    Accion.VER_MOVIMIENTOS
)

fun accionesDe(rol: Rol): Set<Accion> = when (rol) {
    Rol.USUARIO_FINAL -> accionesUsuario
    Rol.ADMINISTRADOR -> accionesAdministrador
}

fun puede(usuario: Usuario?, accion: Accion): Boolean =
    usuario != null && usuario.activo && accion in accionesDe(usuario.rol)

fun motivoSinPermiso(usuario: Usuario?, accion: Accion): String? = when {
    usuario == null -> "Inicia sesión para continuar"
    !usuario.activo -> "Esta cuenta no está activa. Consulta con PagoFlex."
    !puede(usuario, accion) -> "Tu rol (${usuario.rol.etiqueta}) no permite: ${accion.descripcion.lowercase()}"
    else -> null
}

fun puedeVerCompromiso(usuario: Usuario?, compromiso: Compromiso): Boolean = when {
    puede(usuario, Accion.VER_TODOS_LOS_COMPROMISOS) -> true
    puede(usuario, Accion.VER_MIS_COMPROMISOS) -> compromiso.idUsuarioFinal == usuario?.idUsuario
    else -> false
}
