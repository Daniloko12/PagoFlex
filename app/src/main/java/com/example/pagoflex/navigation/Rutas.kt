package com.example.pagoflex.navigation

sealed class Ruta(val ruta: String) {
    data object Ingreso : Ruta("ingreso")
    data object Registro : Ruta("registro")
    data object Perfil : Ruta("perfil")

    data object Inicio : Ruta("inicio")
    data object Historial : Ruta("historial")
    data object Avance : Ruta("avance")
    data object Tarjetas : Ruta("tarjetas")
    data object AgregarTarjeta : Ruta("agregar-tarjeta")
    data object Avisos : Ruta("avisos")
    data object Recomendar : Ruta("recomendar")

    data object DetalleCompromiso : Ruta("compromiso/{idCompromiso}") {
        fun crear(idCompromiso: Int) = "compromiso/$idCompromiso"
    }
    data object Pagar : Ruta("pagar/{idCompromiso}") {
        fun crear(idCompromiso: Int) = "pagar/$idCompromiso"
    }
    data object Reportar : Ruta("reportar/{idCompromiso}") {
        fun crear(idCompromiso: Int) = "reportar/$idCompromiso"
    }
    data object Comprobante : Ruta("comprobante/{numero}") {
        fun crear(numero: String) = "comprobante/$numero"
    }

    data object CompromisosEmpresa : Ruta("compromisos-empresa")
    data object DetalleCompromisoEmpresa : Ruta("compromiso-empresa/{idCompromiso}") {
        fun crear(idCompromiso: Int) = "compromiso-empresa/$idCompromiso"
    }
    data object RegistrarCompromiso : Ruta("registrar-compromiso?rut={rut}") {
        fun crear(rut: String? = null) = if (rut == null) "registrar-compromiso" else "registrar-compromiso?rut=$rut"
    }
    data object EditarCompromiso : Ruta("editar-compromiso/{idCompromiso}") {
        fun crear(idCompromiso: Int) = "editar-compromiso/$idCompromiso"
    }
    data object Cobranza : Ruta("cobranza")

    data object Clientes : Ruta("clientes")
    data object DetalleCliente : Ruta("cliente/{idUsuario}") {
        fun crear(idUsuario: Int) = "cliente/$idUsuario"
    }
    data object NuevoCliente : Ruta("nuevo-cliente")
    data object EditarCliente : Ruta("editar-cliente/{idUsuario}") {
        fun crear(idUsuario: Int) = "editar-cliente/$idUsuario"
    }

    data object Reportes : Ruta("reportes")
    data object DetalleReporte : Ruta("reporte/{idReporte}") {
        fun crear(idReporte: Int) = "reporte/$idReporte"
    }
    data object Recomendaciones : Ruta("recomendaciones")
    data object DetalleRecomendacion : Ruta("recomendacion/{idRecomendacion}") {
        fun crear(idRecomendacion: Int) = "recomendacion/$idRecomendacion"
    }
}
