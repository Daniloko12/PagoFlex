package com.example.pagoflex.data.remoto

import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.ContactoRecomendacion
import com.example.pagoflex.model.HistorialRecomendacion
import com.example.pagoflex.model.Movimiento
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.ResultadoCodigo
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import java.time.LocalDateTime

data class Ingreso(val correo: String, val clave: String)

data class RegistroUsuario(val usuario: Usuario, val clave: String, val movimientos: List<Movimiento>)

data class ActualizacionUsuario(val usuario: Usuario, val movimientos: List<Movimiento>)

data class NuevaClave(val claveActual: String?, val claveNueva: String, val movimientos: List<Movimiento>)

class NuevaFoto(val avatar: ByteArray)

data class SolicitudCodigo(val idUsuario: Int, val codigo: String, val ahora: LocalDateTime)

data class VerificacionCodigo(val idUsuario: Int, val codigo: String, val ahora: LocalDateTime)

data class RecuperacionClave(
    val idUsuario: Int,
    val codigo: String,
    val claveNueva: String,
    val ahora: LocalDateTime,
    val movimientos: List<Movimiento>
)

data class CodigoCreado(val idCodigo: Int, val fechaExpiracion: LocalDateTime)

data class ResultadoVerificacion(val resultado: ResultadoCodigo, val intentosRestantes: Int)

class RegistroCompromiso(val compromiso: Compromiso, val boleta: ByteArray?, val movimientos: List<Movimiento>)

data class ActualizacionCompromisos(val compromisos: List<Compromiso>, val movimientos: List<Movimiento>)

class BoletaGuardada(val idCompromiso: Int, val imagen: ByteArray, val fechaRegistro: LocalDateTime)

data class RegistroAnulacion(val anulacion: Anulacion, val compromiso: Compromiso, val movimientos: List<Movimiento>)

data class RegistroReporte(val reporte: Reporte, val compromiso: Compromiso, val movimientos: List<Movimiento>)

data class ActualizacionReporte(val reporte: Reporte, val compromiso: Compromiso?, val movimientos: List<Movimiento>)

data class RegistroRecomendacion(val recomendacion: Recomendacion, val contacto: ContactoRecomendacion?)

data class CambioEstadoRecomendacion(
    val recomendacion: Recomendacion,
    val historial: HistorialRecomendacion,
    val reconocimiento: UsuarioReconocimiento?
)

data class RegistroPago(
    val pago: Pago,
    val tarjetaNueva: Tarjeta?,
    val compromiso: Compromiso,
    val siguienteCuota: Compromiso?,
    val reconocimientos: List<UsuarioReconocimiento>,
    val movimientos: List<Movimiento>
)

data class MensajeError(val error: String?)
