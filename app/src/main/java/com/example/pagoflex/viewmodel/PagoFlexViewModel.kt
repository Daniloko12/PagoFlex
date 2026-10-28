package com.example.pagoflex.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.pagoflex.data.ConvertidorImagen
import com.example.pagoflex.data.PagoFlexDbHelper
import com.example.pagoflex.data.SesionPreferencias
import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.CambiosCompromiso
import com.example.pagoflex.model.Canal
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.ContactoRecomendacion
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.DatosCliente
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.DiasAnticipacion
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.HistorialRecomendacion
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.NuevaRecomendacion
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.ResultadoPago
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.TarjetaNueva
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import com.example.pagoflex.model.actualizarVencimientos
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.cuentaParaRecuperar
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.motivoParaNoAnular
import com.example.pagoflex.model.motivoParaNoCambiarEstado
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.motivoParaNoPagar
import com.example.pagoflex.model.motivoParaNoReportar
import com.example.pagoflex.model.motivoParaNoResolver
import com.example.pagoflex.model.motivoParaNoTomar
import com.example.pagoflex.model.normalizarRut
import com.example.pagoflex.model.reconocimientoPorId
import com.example.pagoflex.model.recomendacionPorId
import com.example.pagoflex.model.reportePorId
import com.example.pagoflex.model.siguienteCuota
import com.example.pagoflex.model.tarjetaPorId
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.LocalTime

class PagoFlexViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PagoFlexDbHelper(application)

    private val sesion = SesionPreferencias(application)

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

    private fun ahora(): LocalDateTime =
        LocalDateTime.of(_datos.value.fechaHoy, LocalTime.now().withSecond(0).withNano(0))

    private fun recargar() {
        _datos.value = db.cargarDatos()
        _usuarioActual.value = _usuarioActual.value?.let { _datos.value.usuarioPorId(it.idUsuario) }
        buscarCompromisos(_busqueda.value)
    }

    fun iniciarSesion(credenciales: Credenciales): String? {
        val usuario = db.buscarPorCredenciales(credenciales.correo, credenciales.clave)
        return when {
            usuario == null -> "Correo o contraseña incorrectos"
            !usuario.activo -> "Esta cuenta no está activa. Consulta con PagoFlex."
            else -> {
                abrirSesion(usuario)
                null
            }
        }
    }

    private fun abrirSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
        sesion.guardar(usuario.idUsuario)
        buscarCompromisos("")
    }

    fun crearCuenta(nueva: NuevaCuenta): String? {
        val datos = _datos.value
        val fecha = ahora()
        val usuario = Usuario(
            idUsuario = siguienteIdUsuario(datos),
            rol = Rol.USUARIO_FINAL,
            rut = normalizarRut(nueva.rut),
            nombres = nueva.nombres,
            apellidos = nueva.apellidos,
            correo = nueva.correo,
            telefono = nueva.telefono,
            fechaCreacion = fecha,
            fechaIncorporacion = datos.fechaHoy,
            avisosActivos = true,
            diasAnticipacion = 3
        )
        if (!db.registrarUsuario(usuario, nueva.clave)) return "No pudimos crear tu cuenta. Revisa que tu RUT y tu correo no estén registrados."
        recargar()
        abrirSesion(_datos.value.usuarioPorId(usuario.idUsuario) ?: usuario)
        return null
    }

    fun verificarCuenta(datosRecuperacion: DatosRecuperacion): String? {
        val cuenta = _datos.value.cuentaParaRecuperar(datosRecuperacion.correo, datosRecuperacion.rut)
        return when {
            cuenta == null -> "El correo y el RUT no coinciden con ninguna cuenta. Revisa que estén bien escritos."
            !cuenta.activo -> "Esta cuenta no está activa. Consulta con PagoFlex."
            else -> null
        }
    }

    fun recuperarClave(recuperada: ClaveRecuperada): String? {
        verificarCuenta(DatosRecuperacion(recuperada.correo, recuperada.rut))?.let { return it }
        val cuenta = _datos.value.cuentaParaRecuperar(recuperada.correo, recuperada.rut) ?: return "No encontramos la cuenta"
        if (db.buscarPorCredenciales(cuenta.correo, recuperada.claveNueva) != null) {
            return "Esa ya es tu contraseña. Puedes ingresar con ella o elegir otra."
        }
        return if (db.cambiarClave(cuenta, recuperada.claveNueva)) null else "No pudimos cambiar la contraseña. Inténtalo de nuevo."
    }

    fun actualizarMisDatos(misDatos: MisDatos): String? {
        val usuario = _usuarioActual.value ?: return "Inicia sesión"
        val editado = usuario.copy(
            nombres = misDatos.nombres,
            apellidos = misDatos.apellidos,
            correo = misDatos.correo,
            telefono = misDatos.telefono,
            idComuna = misDatos.idComuna ?: usuario.idComuna
        )
        if (editado == usuario) return "No cambiaste ningún dato"
        if (!db.actualizarUsuario(editado)) return "No pudimos guardar tus datos. Revisa que el correo no esté registrado."
        recargar()
        return null
    }

    fun cambiarMiClave(cambio: CambioClave): String? {
        val usuario = _usuarioActual.value ?: return "Inicia sesión"
        if (db.buscarPorCredenciales(usuario.correo, cambio.claveActual) == null) return "Tu contraseña actual no es correcta"
        return if (db.cambiarClave(usuario, cambio.claveNueva)) null else "No pudimos cambiar la contraseña. Inténtalo de nuevo."
    }

    private fun siguienteIdUsuario(datos: BaseDatos): Int = (datos.usuarios.maxOfOrNull { it.idUsuario } ?: 0) + 1

    fun cambiarAvatarDesdeCamara(foto: Bitmap): Boolean =
        guardarAvatar(ConvertidorImagen.aBytes(foto))

    fun cambiarAvatarDesdeGaleria(uri: Uri): Boolean {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return false
        return guardarAvatar(ConvertidorImagen.aBytes(imagen))
    }

    private fun guardarAvatar(bytes: ByteArray): Boolean {
        val usuario = _usuarioActual.value ?: return false
        if (!db.actualizarAvatar(usuario.idUsuario, bytes)) return false
        recargar()
        return true
    }

    fun cambiarModoOscuro(oscuro: Boolean) {
        _modoOscuro.value = oscuro
        sesion.guardarModoOscuro(oscuro)
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        sesion.borrar()
        _busqueda.value = ""
        _compromisosEncontrados.value = emptyList()
    }

    fun restablecerDatos() {
        db.restablecerDatos()
        _sinConexion.value = false
        sesion.guardarSinConexion(false)
        _ultimaActualizacion.value = DatosAnexo9.ULTIMA_ACTUALIZACION
        sesion.guardarUltimaActualizacion(DatosAnexo9.ULTIMA_ACTUALIZACION)
        recargar()
    }

    fun motivoParaNoPagar(idCompromiso: Int, revisarConexion: Boolean = true): String? =
        _datos.value.motivoParaNoPagar(idCompromiso, sinConexion = revisarConexion && _sinConexion.value)

    fun pagar(idCompromiso: Int, solicitud: SolicitudPago): ResultadoPago? {
        val usuario = _usuarioActual.value ?: return null
        if (motivoParaNoPagar(idCompromiso) != null) return null
        val datos = _datos.value
        val compromiso = datos.compromisoPorId(idCompromiso) ?: return null
        val fechaPago = ahora()

        val tarjetaGuardada = solicitud.idTarjeta?.let { id ->
            datos.tarjetaPorId(id)?.takeIf {
                it.idUsuario == usuario.idUsuario && it.guardada && it.medioPago == solicitud.medioPago
            } ?: return null
        }
        val tarjetaNueva = solicitud.tarjetaNueva?.let {
            crearTarjeta(datos, usuario.idUsuario, it, guardada = solicitud.guardarTarjeta, fecha = fechaPago)
        }
        val conTarjeta = solicitud.medioPago == MedioPago.DEBITO || solicitud.medioPago == MedioPago.CREDITO
        if (conTarjeta && tarjetaGuardada == null && tarjetaNueva == null) return null

        val pago = Pago(
            idPago = (datos.pagos.maxOfOrNull { it.idPago } ?: 0) + 1,
            numeroComprobante = siguienteComprobante(datos.pagos),
            idCompromiso = compromiso.idCompromiso,
            fechaHora = fechaPago,
            montoPagado = totalAPagar(compromiso),
            canal = Canal.APP,
            medioPago = solicitud.medioPago,
            idTarjeta = tarjetaGuardada?.idTarjeta ?: tarjetaNueva?.idTarjeta
        )
        val pagado = compromiso.copy(estado = EstadoCompromiso.PAGADO, fechaCambioEstado = fechaPago)
        val siguiente = datos.siguienteCuota(compromiso, fechaPago)

        val nuevos = mutableListOf<UsuarioReconocimiento>()
        val yaTiene = { idReconocimiento: Int ->
            datos.reconocimientosObtenidos.any {
                it.idUsuarioFinal == usuario.idUsuario && it.idReconocimiento == idReconocimiento
            }
        }
        val pagoAntesPorApp = datos.pagos.any { p ->
            p.canal == Canal.APP && datos.compromisoPorId(p.idCompromiso)?.idUsuarioFinal == usuario.idUsuario
        }
        if (!pagoAntesPorApp && !yaTiene(DatosAnexo9.RECONOCIMIENTO_PRIMER_PAGO_APP)) {
            nuevos += UsuarioReconocimiento(usuario.idUsuario, DatosAnexo9.RECONOCIMIENTO_PRIMER_PAGO_APP, fechaPago,
                "Pagaste \"${compromiso.concepto}\" desde la app")
        }
        val quedanVencidos = datos.compromisos.any {
            it.idUsuarioFinal == usuario.idUsuario && it.estado == EstadoCompromiso.VENCIDO && it.idCompromiso != idCompromiso
        }
        if (compromiso.estado == EstadoCompromiso.VENCIDO && !quedanVencidos && !yaTiene(DatosAnexo9.RECONOCIMIENTO_AL_DIA)) {
            nuevos += UsuarioReconocimiento(usuario.idUsuario, DatosAnexo9.RECONOCIMIENTO_AL_DIA, fechaPago,
                "Pagaste \"${compromiso.concepto}\" y quedaste sin compromisos vencidos")
        }

        if (!db.registrarPago(pago, pagado, nuevos, tarjetaNueva, siguiente)) return null
        _ultimaActualizacion.value = fechaPago
        sesion.guardarUltimaActualizacion(fechaPago)
        recargar()
        return ResultadoPago(
            pago = pago,
            reconocimientos = nuevos.mapNotNull { _datos.value.reconocimientoPorId(it.idReconocimiento)?.nombre },
            siguienteCuota = siguiente
        )
    }

    private fun crearTarjeta(datos: BaseDatos, idUsuario: Int, nueva: TarjetaNueva, guardada: Boolean, fecha: LocalDateTime) =
        Tarjeta(
            idTarjeta = (datos.tarjetas.maxOfOrNull { it.idTarjeta } ?: 0) + 1,
            idUsuario = idUsuario,
            medioPago = nueva.medioPago,
            marca = nueva.marca,
            ultimos4 = nueva.ultimos4,
            titular = nueva.titular,
            vencimiento = nueva.vencimiento,
            guardada = guardada,
            fechaRegistro = fecha
        )

    fun tarjetaRepetida(nueva: TarjetaNueva): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return _datos.value.tarjetasGuardadas(usuario.idUsuario).any {
            it.marca == nueva.marca && it.ultimos4 == nueva.ultimos4 && it.medioPago == nueva.medioPago
        }
    }

    fun agregarTarjeta(nueva: TarjetaNueva): Tarjeta? {
        val usuario = _usuarioActual.value?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return null
        if (tarjetaRepetida(nueva)) return null
        val tarjeta = crearTarjeta(_datos.value, usuario.idUsuario, nueva, guardada = true, fecha = ahora())
        if (!db.registrarTarjeta(tarjeta)) return null
        recargar()
        return tarjeta
    }

    fun quitarTarjeta(idTarjeta: Int): String? {
        val usuario = _usuarioActual.value ?: return "Inicia sesión"
        val tarjeta = _datos.value.tarjetaPorId(idTarjeta)
            ?.takeIf { it.idUsuario == usuario.idUsuario && it.guardada } ?: return "No encontramos esa tarjeta"
        if (!db.quitarTarjeta(tarjeta.idTarjeta)) return "No pudimos quitar la tarjeta. Inténtalo de nuevo."
        recargar()
        return null
    }

    private fun siguienteComprobante(pagos: List<Pago>): String {
        val ultimo = pagos.maxOfOrNull { it.numeroComprobante.removePrefix("CPR-").toIntOrNull() ?: 0 } ?: 0
        return "CPR-%06d".format(ultimo + 1)
    }

    fun motivoParaNoReportar(idCompromiso: Int): String? = _datos.value.motivoParaNoReportar(idCompromiso)

    fun reportar(idCompromiso: Int, solicitud: SolicitudReporte): Reporte? {
        if (motivoParaNoReportar(idCompromiso) != null) return null
        val datos = _datos.value
        val compromiso = datos.compromisoPorId(idCompromiso) ?: return null
        val fecha = ahora()
        val nuevoId = (datos.reportes.maxOfOrNull { it.idReporte } ?: 0) + 1
        val reporte = Reporte(
            idReporte = nuevoId,
            numero = "REP-%04d".format(nuevoId),
            idCompromiso = idCompromiso,
            motivo = solicitud.motivo,
            comentario = solicitud.comentario,
            fechaCreacion = fecha,
            estado = EstadoReporte.ABIERTO
        )
        val enRevision = compromiso.copy(estado = EstadoCompromiso.EN_REVISION, fechaCambioEstado = fecha)
        if (!db.registrarReporte(reporte, enRevision)) return null
        recargar()
        return reporte
    }

    fun registrarRecomendacion(nueva: NuevaRecomendacion): Recomendacion? {
        val usuario = _usuarioActual.value ?: return null
        val datos = _datos.value
        val nuevoId = (datos.recomendaciones.maxOfOrNull { it.idRecomendacion } ?: 0) + 1
        val recomendacion = Recomendacion(
            idRecomendacion = nuevoId,
            numero = "REC-%04d".format(nuevoId),
            idUsuarioFinal = usuario.idUsuario,
            nombreOrganizacion = nueva.nombreOrganizacion,
            rutOrganizacion = nueva.rutOrganizacion,
            idRubro = nueva.idRubro,
            idComuna = nueva.idComuna,
            fecha = ahora(),
            estado = EstadoRecomendacion.RECIBIDA
        )
        val contacto = nueva.contacto?.let {
            ContactoRecomendacion(nuevoId, it.nombre, it.correo, it.telefono, autorizaContacto = true)
        }
        if (!db.registrarRecomendacion(recomendacion, contacto)) return null
        recargar()
        return recomendacion
    }

    fun guardarAvisos(activos: Boolean, anticipacion: DiasAnticipacion): Boolean {
        val usuario = _usuarioActual.value ?: return false
        val ok = db.actualizarUsuario(usuario.copy(avisosActivos = activos, diasAnticipacion = anticipacion.dias))
        recargar()
        return ok
    }

    fun cambiarConexion(sinConexion: Boolean) {
        _sinConexion.value = sinConexion
        sesion.guardarSinConexion(sinConexion)
    }

    fun actualizar(): String {
        if (_sinConexion.value) {
            return "Sin conexión: te mostramos tu última situación conocida, del ${formatoFecha(_ultimaActualizacion.value.toLocalDate())}."
        }
        val datos = _datos.value
        val cambiados = actualizarVencimientos(datos.compromisos, datos.empresas, datos.fechaHoy)
            .filter { nuevo -> datos.compromisoPorId(nuevo.idCompromiso) != nuevo }
        if (cambiados.isNotEmpty()) db.actualizarCompromisos(cambiados)
        _ultimaActualizacion.value = ahora()
        sesion.guardarUltimaActualizacion(_ultimaActualizacion.value)
        recargar()
        return "Información actualizada"
    }

    fun tomarReporte(idReporte: Int): String? {
        val admin = _usuarioActual.value
        val datos = _datos.value
        datos.motivoParaNoTomar(idReporte, admin)?.let { return it }
        val reporte = datos.reportePorId(idReporte) ?: return "No encontramos el reporte"
        val tomado = reporte.copy(estado = EstadoReporte.EN_GESTION, idAdministrador = admin?.idUsuario, fechaToma = ahora())
        if (!db.actualizarReporte(tomado, null)) return "No pudimos guardar el cambio. Inténtalo de nuevo."
        recargar()
        return null
    }

    fun resolverReporte(idReporte: Int, respuesta: String): String? {
        val admin = _usuarioActual.value
        val datos = _datos.value
        datos.motivoParaNoResolver(idReporte, admin, respuesta)?.let { return it }
        val reporte = datos.reportePorId(idReporte) ?: return "No encontramos el reporte"
        val fecha = ahora()
        val compromiso = datos.compromisoPorId(reporte.idCompromiso)
            ?.takeIf { it.estado == EstadoCompromiso.EN_REVISION }
            ?.let { c ->
                val vencido = datos.fechaHoy.isAfter(c.fechaVencimiento)
                val recargo = if (vencido && c.recargoAplicado == 0)
                    datos.empresaPorId(c.idEmpresa)?.recargoAtraso ?: 0 else c.recargoAplicado
                c.copy(
                    estado = if (vencido) EstadoCompromiso.VENCIDO else EstadoCompromiso.PENDIENTE,
                    recargoAplicado = recargo,
                    fechaCambioEstado = fecha
                )
            }
        val resuelto = reporte.copy(estado = EstadoReporte.RESUELTO, respuesta = respuesta.trim(), fechaResolucion = fecha)
        if (!db.actualizarReporte(resuelto, compromiso)) return "No pudimos guardar el cambio. Inténtalo de nuevo."
        recargar()
        return null
    }

    fun cambiarEstadoRecomendacion(idRecomendacion: Int, nuevo: EstadoRecomendacion, motivo: String?): String? {
        val admin = _usuarioActual.value?.takeIf { it.rol == Rol.ADMINISTRADOR } ?: return "Inicia sesión como administrador"
        val datos = _datos.value
        datos.motivoParaNoCambiarEstado(idRecomendacion, nuevo, motivo)?.let { return it }
        val recomendacion = datos.recomendacionPorId(idRecomendacion) ?: return "No encontramos la recomendación"
        val fecha = ahora()
        val historial = HistorialRecomendacion(
            idHistorial = (datos.historialRecomendaciones.maxOfOrNull { it.idHistorial } ?: 0) + 1,
            idRecomendacion = idRecomendacion,
            estado = nuevo,
            idAdministrador = admin.idUsuario,
            fecha = fecha,
            motivo = if (nuevo == EstadoRecomendacion.DESCARTADA) motivo?.trim() else null
        )
        val yaEsEmbajador = datos.reconocimientosObtenidos.any {
            it.idUsuarioFinal == recomendacion.idUsuarioFinal && it.idReconocimiento == DatosAnexo9.RECONOCIMIENTO_EMBAJADOR
        }
        val reconocimiento = if (nuevo == EstadoRecomendacion.SE_UNIO && !yaEsEmbajador)
            UsuarioReconocimiento(recomendacion.idUsuarioFinal, DatosAnexo9.RECONOCIMIENTO_EMBAJADOR, fecha,
                "${recomendacion.nombreOrganizacion} se unió a PagoFlex gracias a tu recomendación")
        else null

        if (!db.cambiarEstadoRecomendacion(recomendacion.copy(estado = nuevo), historial, reconocimiento)) {
            return "No pudimos guardar el cambio. Inténtalo de nuevo."
        }
        recargar()
        return null
    }

    fun avanzarRecomendacion(idRecomendacion: Int): String? {
        val recomendacion = _datos.value.recomendacionPorId(idRecomendacion) ?: return "No encontramos la recomendación"
        val siguiente = EstadoRecomendacion.entries.firstOrNull {
            it != EstadoRecomendacion.DESCARTADA && it.orden == recomendacion.estado.orden + 1
        } ?: return "Esta recomendación ya terminó"
        return cambiarEstadoRecomendacion(idRecomendacion, siguiente, null)
    }

    fun buscarCompromisos(texto: String) {
        _busqueda.value = texto
        val esAdmin = _usuarioActual.value?.rol == Rol.ADMINISTRADOR
        _compromisosEncontrados.value = if (esAdmin) db.buscarCompromisos(texto) else emptyList()
    }

    fun anularCompromiso(idCompromiso: Int, motivo: MotivoAnulacion, comentario: String?): String? {
        val admin = _usuarioActual.value ?: return "Inicia sesión como administrador"
        val datos = _datos.value
        datos.motivoParaNoAnular(admin, idCompromiso, motivo, comentario)?.let { return it }
        val compromiso = datos.compromisoPorId(idCompromiso) ?: return "No encontramos el compromiso"
        val fecha = ahora()
        val anulacion = Anulacion(idCompromiso, admin.idUsuario, motivo, comentario?.trim()?.ifBlank { null }, fecha)
        val anulado = compromiso.copy(estado = EstadoCompromiso.ANULADO, fechaCambioEstado = fecha)
        if (!db.anularCompromiso(anulacion, anulado)) return "No pudimos guardar la anulación. Inténtalo de nuevo."
        recargar()
        return null
    }

    fun registrarCompromiso(nuevo: NuevoCompromiso): Compromiso? {
        val admin = _usuarioActual.value?.takeIf { it.rol == Rol.ADMINISTRADOR } ?: return null
        val datos = _datos.value
        val fecha = ahora()

        val personaNueva = nuevo.personaNueva?.let { p ->
            Usuario(
                idUsuario = siguienteIdUsuario(datos),
                rol = Rol.USUARIO_FINAL,
                rut = normalizarRut(p.rut),
                nombres = p.nombres,
                apellidos = p.apellidos,
                correo = p.correo,
                telefono = p.telefono,
                fechaCreacion = fecha,
                fechaIncorporacion = datos.fechaHoy,
                avisosActivos = true,
                diasAnticipacion = 3
            )
        }
        val idPersona = nuevo.idPersona ?: personaNueva?.idUsuario ?: return null

        val nuevoId = datos.compromisos.maxOf { it.idCompromiso } + 1
        val compromiso = Compromiso(
            idCompromiso = nuevoId,
            folio = "CP-%d-%04d".format(datos.fechaHoy.year, nuevoId),
            idUsuarioFinal = idPersona,
            idEmpresa = nuevo.idEmpresa,
            concepto = nuevo.concepto,
            numeroCuota = nuevo.numeroCuota,
            totalCuotas = nuevo.totalCuotas,
            monto = nuevo.monto,
            fechaVencimiento = nuevo.fechaVencimiento,
            estado = EstadoCompromiso.PENDIENTE,
            recargoAplicado = 0,
            fechaCambioEstado = fecha,
            idAdminRegistro = admin.idUsuario,
            fechaRegistro = fecha
        )
        if (!db.registrarCompromiso(compromiso, personaNueva)) return null
        recargar()
        return compromiso
    }

    fun editarCompromiso(idCompromiso: Int, cambios: CambiosCompromiso): String? {
        if (_usuarioActual.value?.rol != Rol.ADMINISTRADOR) return "Inicia sesión como administrador"
        val datos = _datos.value
        datos.motivoParaNoEditar(idCompromiso)?.let { return it }
        val actual = datos.compromisoPorId(idCompromiso) ?: return "No encontramos el compromiso"
        val vencido = datos.fechaHoy.isAfter(cambios.fechaVencimiento)
        val estado = if (vencido) EstadoCompromiso.VENCIDO else EstadoCompromiso.PENDIENTE
        val recargo = when {
            !vencido -> 0
            actual.recargoAplicado > 0 -> actual.recargoAplicado
            else -> datos.empresaPorId(actual.idEmpresa)?.recargoAtraso ?: 0
        }
        val editado = actual.copy(
            concepto = cambios.concepto,
            monto = cambios.monto,
            fechaVencimiento = cambios.fechaVencimiento,
            numeroCuota = cambios.numeroCuota,
            totalCuotas = cambios.totalCuotas,
            estado = estado,
            recargoAplicado = recargo,
            fechaCambioEstado = if (estado != actual.estado) ahora() else actual.fechaCambioEstado
        )
        if (editado == actual) return "No cambiaste ningún dato"
        if (!db.actualizarCompromisos(listOf(editado))) return "No pudimos guardar los cambios. Revisa que no esté repetido."
        recargar()
        return null
    }

    fun crearCliente(datosCliente: DatosCliente): Usuario? {
        if (_usuarioActual.value?.rol != Rol.ADMINISTRADOR) return null
        val datos = _datos.value
        val cliente = Usuario(
            idUsuario = siguienteIdUsuario(datos),
            rol = Rol.USUARIO_FINAL,
            rut = normalizarRut(datosCliente.rut),
            nombres = datosCliente.nombres,
            apellidos = datosCliente.apellidos,
            correo = datosCliente.correo,
            telefono = datosCliente.telefono,
            activo = datosCliente.activo,
            fechaCreacion = ahora(),
            idComuna = datosCliente.idComuna,
            fechaIncorporacion = datos.fechaHoy,
            avisosActivos = true,
            diasAnticipacion = 3
        )
        if (!db.registrarUsuario(cliente, CLAVE_DE_PRUEBA)) return null
        recargar()
        return cliente
    }

    fun editarCliente(idUsuario: Int, datosCliente: DatosCliente): String? {
        if (_usuarioActual.value?.rol != Rol.ADMINISTRADOR) return "Inicia sesión como administrador"
        val cliente = _datos.value.usuarioPorId(idUsuario)
            ?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return "No encontramos al cliente"
        val editado = cliente.copy(
            nombres = datosCliente.nombres,
            apellidos = datosCliente.apellidos,
            correo = datosCliente.correo,
            telefono = datosCliente.telefono,
            idComuna = datosCliente.idComuna,
            activo = datosCliente.activo
        )
        if (!db.actualizarUsuario(editado)) return "No pudimos guardar los cambios. Revisa que el correo no esté registrado."
        recargar()
        return null
    }

    fun restablecerClaveCliente(idUsuario: Int): String? {
        if (_usuarioActual.value?.rol != Rol.ADMINISTRADOR) return "Inicia sesión como administrador"
        val cliente = _datos.value.usuarioPorId(idUsuario)
            ?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return "No encontramos al cliente"
        return if (db.cambiarClave(cliente, CLAVE_DE_PRUEBA)) null else "No pudimos restablecer la contraseña"
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
