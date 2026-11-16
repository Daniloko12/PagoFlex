package com.example.pagoflex.data

import android.content.Context
import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CodigoRecuperacion
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
import com.example.pagoflex.model.usuarioPorId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class PagoFlexRepositorio(context: Context) {
    private val db = PagoFlexDbHelper(context)

    private val sesion = SesionPreferencias(context)

    private val _datos = MutableStateFlow(db.cargarDatos())
    val datos: StateFlow<BaseDatos> = _datos.asStateFlow()

    private val _usuarioActual = MutableStateFlow(db.buscarUsuarioPorId(sesion.leer()))
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

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

    init {
        buscarCompromisos("")
    }

    private fun refrescar() {
        val nuevos = db.cargarDatos()
        _datos.value = nuevos
        _usuarioActual.value = _usuarioActual.value?.let { nuevos.usuarioPorId(it.idUsuario) }
        buscarCompromisos(_busqueda.value)
    }

    private fun guardar(cambio: Boolean): Boolean {
        if (cambio) refrescar()
        return cambio
    }

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
        _compromisosEncontrados.value = if (esAdmin) db.buscarCompromisos(texto) else emptyList()
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

    fun restablecerDatos() {
        db.restablecerDatos()
        cambiarConexion(false)
        registrarActualizacion(DatosAnexo9.ULTIMA_ACTUALIZACION)
        refrescar()
    }

    fun buscarPorCredenciales(correo: String, clave: String): Usuario? = db.buscarPorCredenciales(correo, clave)

    fun codigoVigenteDe(idUsuario: Int): CodigoRecuperacion? = db.codigoVigenteDe(idUsuario)

    fun guardarCodigoRecuperacion(codigo: CodigoRecuperacion): Boolean = db.guardarCodigoRecuperacion(codigo)

    fun sumarIntentoCodigo(codigo: CodigoRecuperacion): Boolean = db.sumarIntentoCodigo(codigo)

    fun cambiarClaveConCodigo(usuario: Usuario, codigo: CodigoRecuperacion, claveNueva: String, movimiento: Movimiento): Boolean =
        guardar(db.cambiarClaveConCodigo(usuario, codigo, claveNueva, listOf(movimiento)))

    fun cambiarClave(usuario: Usuario, claveNueva: String, movimiento: Movimiento): Boolean =
        guardar(db.cambiarClave(usuario, claveNueva, listOf(movimiento)))

    fun registrarUsuario(usuario: Usuario, clave: String, movimiento: Movimiento): Boolean =
        guardar(db.registrarUsuario(usuario, clave, listOf(movimiento)))

    fun actualizarUsuario(usuario: Usuario, movimientos: List<Movimiento> = emptyList()): Boolean =
        guardar(db.actualizarUsuario(usuario, movimientos))

    fun actualizarAvatar(idUsuario: Int, avatar: ByteArray): Boolean = guardar(db.actualizarAvatar(idUsuario, avatar))

    fun registrarPago(
        pago: Pago,
        compromiso: Compromiso,
        reconocimientos: List<UsuarioReconocimiento>,
        tarjetaNueva: Tarjeta?,
        siguienteCuota: Compromiso?,
        movimientos: List<Movimiento>
    ): Boolean = guardar(db.registrarPago(pago, compromiso, reconocimientos, tarjetaNueva, siguienteCuota, movimientos))

    fun registrarTarjeta(tarjeta: Tarjeta): Boolean = guardar(db.registrarTarjeta(tarjeta))

    fun quitarTarjeta(idTarjeta: Int): Boolean = guardar(db.quitarTarjeta(idTarjeta))

    fun registrarReporte(reporte: Reporte, compromiso: Compromiso, movimiento: Movimiento): Boolean =
        guardar(db.registrarReporte(reporte, compromiso, listOf(movimiento)))

    fun actualizarReporte(reporte: Reporte, compromiso: Compromiso?, movimiento: Movimiento): Boolean =
        guardar(db.actualizarReporte(reporte, compromiso, listOf(movimiento)))

    fun registrarRecomendacion(recomendacion: Recomendacion, contacto: ContactoRecomendacion?): Boolean =
        guardar(db.registrarRecomendacion(recomendacion, contacto))

    fun cambiarEstadoRecomendacion(
        recomendacion: Recomendacion,
        historial: HistorialRecomendacion,
        reconocimiento: UsuarioReconocimiento?
    ): Boolean = guardar(db.cambiarEstadoRecomendacion(recomendacion, historial, reconocimiento))

    fun anularCompromiso(anulacion: Anulacion, compromiso: Compromiso, movimiento: Movimiento): Boolean =
        guardar(db.anularCompromiso(anulacion, compromiso, listOf(movimiento)))

    fun registrarCompromiso(
        compromiso: Compromiso,
        personaNueva: Usuario?,
        movimientos: List<Movimiento>,
        fotoBoleta: ByteArray? = null
    ): Boolean = guardar(db.registrarCompromiso(compromiso, personaNueva, movimientos, fotoBoleta))

    fun boletaDe(idCompromiso: Int): ByteArray? = db.boletaDe(idCompromiso)

    fun actualizarCompromisos(compromisos: List<Compromiso>, movimientos: List<Movimiento>): Boolean =
        guardar(db.actualizarCompromisos(compromisos, movimientos))

    fun cerrar() {
        db.close()
    }
}
