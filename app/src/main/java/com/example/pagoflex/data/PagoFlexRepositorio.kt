package com.example.pagoflex.data

import android.content.Context
import com.example.pagoflex.data.remoto.ActualizacionCompromisos
import com.example.pagoflex.data.remoto.ActualizacionReporte
import com.example.pagoflex.data.remoto.ActualizacionUsuario
import com.example.pagoflex.data.remoto.CambioEstadoRecomendacion
import com.example.pagoflex.data.remoto.ErrorServidor
import com.example.pagoflex.data.remoto.Ingreso
import com.example.pagoflex.data.remoto.NuevaClave
import com.example.pagoflex.data.remoto.NuevaFoto
import com.example.pagoflex.data.remoto.RecuperacionClave
import com.example.pagoflex.data.remoto.RegistroAnulacion
import com.example.pagoflex.data.remoto.RegistroCompromiso
import com.example.pagoflex.data.remoto.RegistroPago
import com.example.pagoflex.data.remoto.RegistroRecomendacion
import com.example.pagoflex.data.remoto.RegistroReporte
import com.example.pagoflex.data.remoto.RegistroUsuario
import com.example.pagoflex.data.remoto.ResultadoVerificacion
import com.example.pagoflex.data.remoto.ServidorPagoFlex
import com.example.pagoflex.data.remoto.SolicitudCodigo
import com.example.pagoflex.data.remoto.VerificacionCodigo
import com.example.pagoflex.data.remoto.cargarDatosDelServidor
import com.example.pagoflex.data.remoto.mensajeDeError
import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.ContactoRecomendacion
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.HistorialRecomendacion
import com.example.pagoflex.model.Movimiento
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import com.example.pagoflex.model.baseDatosVacia
import com.example.pagoflex.model.compromisosQueCoinciden
import com.example.pagoflex.model.usuarioPorId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

enum class EstadoCarga { CARGANDO, LISTA, FALLIDA }

class PagoFlexRepositorio(context: Context) {
    private val usuariosApi = ServidorPagoFlex.usuarios
    private val compromisosApi = ServidorPagoFlex.compromisos
    private val pagosApi = ServidorPagoFlex.pagos
    private val movimientosApi = ServidorPagoFlex.movimientos

    private val sesion = SesionPreferencias(context)

    private val _datos = MutableStateFlow(baseDatosVacia())
    val datos: StateFlow<BaseDatos> = _datos.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _cargaInicial = MutableStateFlow(EstadoCarga.CARGANDO)
    val cargaInicial: StateFlow<EstadoCarga> = _cargaInicial.asStateFlow()

    private val _servidorDisponible = MutableStateFlow(true)
    val servidorDisponible: StateFlow<Boolean> = _servidorDisponible.asStateFlow()

    private val _sinConexion = MutableStateFlow(sesion.leerSinConexion())
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    private val _ultimaActualizacion = MutableStateFlow(sesion.leerUltimaActualizacion() ?: DatosAnexo9.ULTIMA_ACTUALIZACION)
    val ultimaActualizacion: StateFlow<LocalDateTime> = _ultimaActualizacion.asStateFlow()

    private val _modoOscuro = MutableStateFlow(sesion.leerModoOscuro())
    val modoOscuro: StateFlow<Boolean> = _modoOscuro.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _compromisosEncontrados = MutableStateFlow<List<Compromiso>>(emptyList())
    val compromisosEncontrados: StateFlow<List<Compromiso>> = _compromisosEncontrados.asStateFlow()

    suspend fun refrescar(): Boolean = try {
        val nuevos = cargarDatosDelServidor(DatosAnexo9.FECHA_HOY)
        _datos.value = nuevos
        _usuarioActual.value = nuevos.usuarioPorId(sesion.leer())
        buscarCompromisos(_busqueda.value)
        _servidorDisponible.value = true
        _cargaInicial.value = EstadoCarga.LISTA
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _servidorDisponible.value = false
        if (_cargaInicial.value != EstadoCarga.LISTA) _cargaInicial.value = EstadoCarga.FALLIDA
        false
    }

    suspend fun reintentarConexion(): Boolean {
        _cargaInicial.value = EstadoCarga.CARGANDO
        return refrescar()
    }

    private suspend fun <T> pedir(operacion: suspend () -> T): Result<T> = try {
        Result.success(operacion())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(ErrorServidor(mensajeDeError(e)))
    }

    private suspend fun enviar(operacion: suspend () -> Unit): String? {
        val resultado = pedir(operacion)
        if (resultado.isSuccess) refrescar()
        return resultado.exceptionOrNull()?.message
    }

    suspend fun validarCredenciales(correo: String, clave: String): Result<Usuario> =
        pedir { usuariosApi.iniciarSesion(Ingreso(correo.trim(), clave)) }

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.idUsuario)
        buscarCompromisos("")
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
        _busqueda.value = ""
        _compromisosEncontrados.value = emptyList()
    }

    fun buscarCompromisos(texto: String) {
        _busqueda.value = texto
        val esAdmin = _usuarioActual.value?.rol == Rol.ADMINISTRADOR
        _compromisosEncontrados.value = if (esAdmin) _datos.value.compromisosQueCoinciden(texto) else emptyList()
    }

    fun cambiarModoOscuro(oscuro: Boolean) {
        _modoOscuro.value = oscuro
        sesion.guardarModoOscuro(oscuro)
    }

    fun cambiarConexion(sinConexion: Boolean) {
        _sinConexion.value = sinConexion
        sesion.guardarSinConexion(sinConexion)
    }

    fun registrarActualizacion(fechaHora: LocalDateTime) {
        _ultimaActualizacion.value = fechaHora
        sesion.guardarUltimaActualizacion(fechaHora)
    }

    suspend fun restablecerDatos(): String? {
        val error = enviar {
            movimientosApi.restablecer()
            usuariosApi.restablecer()
            compromisosApi.restablecer()
            pagosApi.restablecer()
        }
        if (error == null) {
            cambiarConexion(false)
            registrarActualizacion(DatosAnexo9.ULTIMA_ACTUALIZACION)
        }
        return error
    }

    suspend fun solicitarCodigo(idUsuario: Int, codigo: String, ahora: LocalDateTime): String? =
        pedir { usuariosApi.solicitarCodigo(SolicitudCodigo(idUsuario, codigo, ahora)) }.exceptionOrNull()?.message

    suspend fun verificarCodigo(idUsuario: Int, codigo: String, ahora: LocalDateTime): Result<ResultadoVerificacion> =
        pedir { usuariosApi.verificarCodigo(VerificacionCodigo(idUsuario, codigo, ahora)) }

    suspend fun recuperarClave(
        idUsuario: Int,
        codigo: String,
        claveNueva: String,
        ahora: LocalDateTime,
        movimiento: Movimiento
    ): Result<ResultadoVerificacion> =
        pedir { usuariosApi.recuperarClave(RecuperacionClave(idUsuario, codigo, claveNueva, ahora, listOf(movimiento))) }

    suspend fun cambiarClave(usuario: Usuario, claveActual: String?, claveNueva: String, movimiento: Movimiento): String? =
        enviar { usuariosApi.cambiarClave(usuario.idUsuario, NuevaClave(claveActual, claveNueva, listOf(movimiento))) }

    suspend fun registrarUsuario(usuario: Usuario, clave: String, movimiento: Movimiento): String? =
        enviar { usuariosApi.registrar(RegistroUsuario(usuario, clave, listOf(movimiento))) }

    suspend fun actualizarUsuario(usuario: Usuario, movimientos: List<Movimiento> = emptyList()): String? =
        enviar { usuariosApi.actualizar(usuario.idUsuario, ActualizacionUsuario(usuario, movimientos)) }

    suspend fun actualizarAvatar(idUsuario: Int, avatar: ByteArray): String? =
        enviar { usuariosApi.cambiarAvatar(idUsuario, NuevaFoto(avatar)) }

    suspend fun registrarPago(
        pago: Pago,
        compromiso: Compromiso,
        reconocimientos: List<UsuarioReconocimiento>,
        tarjetaNueva: Tarjeta?,
        siguienteCuota: Compromiso?,
        movimientos: List<Movimiento>
    ): String? = enviar {
        pagosApi.registrarPago(RegistroPago(pago, tarjetaNueva, compromiso, siguienteCuota, reconocimientos, movimientos))
    }

    suspend fun registrarTarjeta(tarjeta: Tarjeta): String? = enviar { pagosApi.registrarTarjeta(tarjeta) }

    suspend fun quitarTarjeta(idTarjeta: Int): String? = enviar { pagosApi.quitarTarjeta(idTarjeta) }

    suspend fun registrarReporte(reporte: Reporte, compromiso: Compromiso, movimiento: Movimiento): String? =
        enviar { compromisosApi.registrarReporte(RegistroReporte(reporte, compromiso, listOf(movimiento))) }

    suspend fun actualizarReporte(reporte: Reporte, compromiso: Compromiso?, movimiento: Movimiento): String? =
        enviar { compromisosApi.actualizarReporte(reporte.idReporte, ActualizacionReporte(reporte, compromiso, listOf(movimiento))) }

    suspend fun registrarRecomendacion(recomendacion: Recomendacion, contacto: ContactoRecomendacion?): String? =
        enviar { compromisosApi.registrarRecomendacion(RegistroRecomendacion(recomendacion, contacto)) }

    suspend fun cambiarEstadoRecomendacion(
        recomendacion: Recomendacion,
        historial: HistorialRecomendacion,
        reconocimiento: UsuarioReconocimiento?
    ): String? = enviar {
        compromisosApi.cambiarEstadoRecomendacion(
            recomendacion.idRecomendacion,
            CambioEstadoRecomendacion(recomendacion, historial, reconocimiento)
        )
    }

    suspend fun anularCompromiso(anulacion: Anulacion, compromiso: Compromiso, movimiento: Movimiento): String? =
        enviar { compromisosApi.anular(RegistroAnulacion(anulacion, compromiso, listOf(movimiento))) }

    suspend fun registrarCompromiso(
        compromiso: Compromiso,
        personaNueva: Usuario?,
        movimientoPersona: Movimiento?,
        movimientoCompromiso: Movimiento,
        fotoBoleta: ByteArray? = null
    ): String? = enviar {
        if (personaNueva != null) {
            usuariosApi.registrar(RegistroUsuario(personaNueva, CLAVE_DE_PRUEBA, listOfNotNull(movimientoPersona)))
        }
        compromisosApi.registrar(RegistroCompromiso(compromiso, fotoBoleta, listOf(movimientoCompromiso)))
    }

    suspend fun boletaDe(idCompromiso: Int): ByteArray? = try {
        compromisosApi.boleta(idCompromiso).imagen
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    suspend fun actualizarCompromisos(compromisos: List<Compromiso>, movimientos: List<Movimiento>): String? =
        enviar { compromisosApi.actualizar(ActualizacionCompromisos(compromisos, movimientos)) }
}
