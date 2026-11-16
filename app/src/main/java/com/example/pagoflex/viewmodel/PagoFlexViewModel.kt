package com.example.pagoflex.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pagoflex.data.ConvertidorImagen
import com.example.pagoflex.data.PagoFlexRepositorio
import com.example.pagoflex.model.Accion
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
import com.example.pagoflex.model.INTENTOS_MAXIMOS_CODIGO
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.NuevaRecomendacion
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.ResultadoCodigo
import com.example.pagoflex.model.ResultadoPago
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.TarjetaNueva
import com.example.pagoflex.model.TipoMovimiento
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import com.example.pagoflex.model.actualizarVencimientos
import com.example.pagoflex.model.cambiosDeUsuario
import com.example.pagoflex.model.claveValida
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.cuentaParaRecuperar
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.generarCodigoRecuperacion
import com.example.pagoflex.model.mensajeCodigo
import com.example.pagoflex.model.motivoParaNoAnular
import com.example.pagoflex.model.motivoParaNoCambiarEstado
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.motivoParaNoPagar
import com.example.pagoflex.model.motivoParaNoReportar
import com.example.pagoflex.model.motivoParaNoResolver
import com.example.pagoflex.model.motivoParaNoTomar
import com.example.pagoflex.model.motivoSinPermiso
import com.example.pagoflex.model.movimientoAnulacion
import com.example.pagoflex.model.movimientoCuenta
import com.example.pagoflex.model.movimientoCuotaGenerada
import com.example.pagoflex.model.movimientoModificacion
import com.example.pagoflex.model.movimientoPago
import com.example.pagoflex.model.movimientoRegistro
import com.example.pagoflex.model.movimientoReporte
import com.example.pagoflex.model.movimientoVencimiento
import com.example.pagoflex.model.normalizarRut
import com.example.pagoflex.model.nuevoCodigoRecuperacion
import com.example.pagoflex.model.puede
import com.example.pagoflex.model.puedeVerCompromiso
import com.example.pagoflex.model.recomendacionPorId
import com.example.pagoflex.model.reconocimientoPorId
import com.example.pagoflex.model.reportePorId
import com.example.pagoflex.model.revisarCodigo
import com.example.pagoflex.model.siguienteCuota
import com.example.pagoflex.model.tarjetaPorId
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.LocalTime

class PagoFlexViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val MENSAJE_CLAVE_INSEGURA = "La contraseña no cumple los 5 requisitos de seguridad"
        const val INTERVALO_SINCRONIZACION_MS = 60_000L
    }

    private val repositorio = PagoFlexRepositorio(application)

    val datos: StateFlow<BaseDatos> = repositorio.datos

    val usuarioActual: StateFlow<Usuario?> = repositorio.usuarioActual

    val sinConexion: StateFlow<Boolean> = repositorio.sinConexion

    private val sincronizacionAutomatica: Flow<Unit> = flow {
        while (true) {
            sincronizar()
            emit(Unit)
            delay(INTERVALO_SINCRONIZACION_MS)
        }
    }

    val ultimaActualizacion: StateFlow<LocalDateTime> =
        combine(repositorio.ultimaActualizacion, sincronizacionAutomatica) { fecha, _ -> fecha }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repositorio.ultimaActualizacion.value)

    val modoOscuro: StateFlow<Boolean> = repositorio.modoOscuro

    val busqueda: StateFlow<String> = repositorio.busqueda

    val compromisosEncontrados: StateFlow<List<Compromiso>> = repositorio.compromisosEncontrados

    private fun sinPermiso(accion: Accion): String? = motivoSinPermiso(usuarioActual.value, accion)

    private fun conPermiso(accion: Accion): Usuario? = usuarioActual.value?.takeIf { puede(it, accion) }

    private fun ahora(): LocalDateTime =
        LocalDateTime.of(datos.value.fechaHoy, LocalTime.now().withSecond(0).withNano(0))

    fun iniciarSesion(credenciales: Credenciales): String? {
        val usuario = repositorio.buscarPorCredenciales(credenciales.correo, credenciales.clave)
        return when {
            usuario == null -> "Correo o contraseña incorrectos"
            !usuario.activo -> "Esta cuenta no está activa. Consulta con PagoFlex."
            else -> {
                repositorio.iniciarSesion(usuario)
                null
            }
        }
    }

    fun crearCuenta(nueva: NuevaCuenta): String? {
        if (!claveValida(nueva.clave)) return MENSAJE_CLAVE_INSEGURA
        val datos = datos.value
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
        val registro = movimientoCuenta(TipoMovimiento.CUENTA_CREADA, usuario.idUsuario, usuario.idUsuario, fecha, "Creaste tu cuenta desde la app")
        if (!repositorio.registrarUsuario(usuario, nueva.clave, registro)) return "No pudimos crear tu cuenta. Revisa que tu RUT y tu correo no estén registrados."
        repositorio.iniciarSesion(this.datos.value.usuarioPorId(usuario.idUsuario) ?: usuario)
        return null
    }

    private fun cuentaActivaPara(datosRecuperacion: DatosRecuperacion): Usuario? =
        datos.value.cuentaParaRecuperar(datosRecuperacion.correo)

    fun solicitarCodigo(datosRecuperacion: DatosRecuperacion): String? {
        val cuenta = cuentaActivaPara(datosRecuperacion) ?: return null
        val codigo = generarCodigoRecuperacion()
        return if (repositorio.guardarCodigoRecuperacion(nuevoCodigoRecuperacion(cuenta.idUsuario, codigo, ahora()))) codigo else null
    }

    private fun validarCodigo(cuenta: Usuario?, codigo: String): String? {
        val vigente = cuenta?.let { repositorio.codigoVigenteDe(it.idUsuario) }
        val resultado = revisarCodigo(vigente, codigo, ahora())
        if (resultado == ResultadoCodigo.INCORRECTO && vigente != null) {
            repositorio.sumarIntentoCodigo(vigente)
            return mensajeCodigo(resultado, INTENTOS_MAXIMOS_CODIGO - vigente.intentos - 1)
        }
        return mensajeCodigo(resultado)
    }

    fun verificarCodigo(datosRecuperacion: DatosRecuperacion, codigo: String): String? =
        validarCodigo(cuentaActivaPara(datosRecuperacion), codigo)

    fun recuperarClave(recuperada: ClaveRecuperada): String? {
        val cuenta = cuentaActivaPara(DatosRecuperacion(recuperada.correo))
        validarCodigo(cuenta, recuperada.codigo)?.let { return it }
        val vigente = cuenta?.let { repositorio.codigoVigenteDe(it.idUsuario) } ?: return mensajeCodigo(ResultadoCodigo.SIN_CODIGO)
        if (!claveValida(recuperada.claveNueva)) return MENSAJE_CLAVE_INSEGURA
        if (repositorio.buscarPorCredenciales(cuenta.correo, recuperada.claveNueva) != null) {
            return "Esa ya es tu contraseña. Puedes ingresar con ella o elegir otra."
        }
        val registro = movimientoCuenta(
            TipoMovimiento.CLAVE_RECUPERADA, cuenta.idUsuario, cuenta.idUsuario, ahora(),
            "Verificaste tu identidad con un código de un solo uso"
        )
        return if (repositorio.cambiarClaveConCodigo(cuenta, vigente, recuperada.claveNueva, registro)) null
        else "No pudimos cambiar la contraseña. Inténtalo de nuevo."
    }

    fun actualizarMisDatos(misDatos: MisDatos): String? {
        val usuario = usuarioActual.value ?: return "Inicia sesión"
        val editado = usuario.copy(
            nombres = misDatos.nombres,
            apellidos = misDatos.apellidos,
            correo = misDatos.correo,
            telefono = misDatos.telefono,
            idComuna = misDatos.idComuna ?: usuario.idComuna
        )
        if (editado == usuario) return "No cambiaste ningún dato"
        val registro = movimientoCuenta(TipoMovimiento.DATOS_ACTUALIZADOS, usuario.idUsuario, usuario.idUsuario, ahora(), cambiosDeUsuario(usuario, editado))
        if (!repositorio.actualizarUsuario(editado, listOf(registro))) return "No pudimos guardar tus datos. Revisa que el correo no esté registrado."
        return null
    }

    fun cambiarMiClave(cambio: CambioClave): String? {
        val usuario = usuarioActual.value ?: return "Inicia sesión"
        if (repositorio.buscarPorCredenciales(usuario.correo, cambio.claveActual) == null) return "Tu contraseña actual no es correcta"
        if (!claveValida(cambio.claveNueva)) return MENSAJE_CLAVE_INSEGURA
        val registro = movimientoCuenta(TipoMovimiento.CLAVE_CAMBIADA, usuario.idUsuario, usuario.idUsuario, ahora(), "Cambiaste tu contraseña desde Mi perfil")
        return if (repositorio.cambiarClave(usuario, cambio.claveNueva, registro)) null else "No pudimos cambiar la contraseña. Inténtalo de nuevo."
    }

    private fun siguienteIdUsuario(datos: BaseDatos): Int = (datos.usuarios.maxOfOrNull { it.idUsuario } ?: 0) + 1

    fun cambiarAvatarDesdeCamara(foto: Bitmap): Boolean =
        guardarAvatar(ConvertidorImagen.aBytes(foto))

    fun cambiarAvatarDesdeGaleria(uri: Uri): Boolean {
        val imagen = ConvertidorImagen.desdeUri(getApplication(), uri) ?: return false
        return guardarAvatar(ConvertidorImagen.aBytes(imagen))
    }

    private fun guardarAvatar(bytes: ByteArray): Boolean {
        val usuario = usuarioActual.value ?: return false
        if (!repositorio.actualizarAvatar(usuario.idUsuario, bytes)) return false
        return true
    }

    fun cambiarModoOscuro(oscuro: Boolean) {
        repositorio.cambiarModoOscuro(oscuro)
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
    }

    fun restablecerDatos() {
        repositorio.restablecerDatos()
    }

    private fun motivoCompromisoAjeno(idCompromiso: Int): String? {
        val compromiso = datos.value.compromisoPorId(idCompromiso) ?: return null
        return if (puedeVerCompromiso(usuarioActual.value, compromiso)) null else "Este compromiso no está a tu nombre"
    }

    fun motivoParaNoPagar(idCompromiso: Int, revisarConexion: Boolean = true): String? =
        sinPermiso(Accion.PAGAR_COMPROMISO)
            ?: motivoCompromisoAjeno(idCompromiso)
            ?: datos.value.motivoParaNoPagar(idCompromiso, sinConexion = revisarConexion && sinConexion.value)

    fun pagar(idCompromiso: Int, solicitud: SolicitudPago): ResultadoPago? {
        val usuario = conPermiso(Accion.PAGAR_COMPROMISO) ?: return null
        if (motivoParaNoPagar(idCompromiso) != null) return null
        val datos = datos.value
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

        val registros = listOfNotNull(movimientoPago(pago, compromiso), siguiente?.let { movimientoCuotaGenerada(it, fechaPago) })
        if (!repositorio.registrarPago(pago, pagado, nuevos, tarjetaNueva, siguiente, registros)) return null
        repositorio.registrarActualizacion(fechaPago)
        return ResultadoPago(
            pago = pago,
            reconocimientos = nuevos.mapNotNull { this.datos.value.reconocimientoPorId(it.idReconocimiento)?.nombre },
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
        val usuario = usuarioActual.value ?: return false
        return datos.value.tarjetasGuardadas(usuario.idUsuario).any {
            it.marca == nueva.marca && it.ultimos4 == nueva.ultimos4 && it.medioPago == nueva.medioPago
        }
    }

    fun agregarTarjeta(nueva: TarjetaNueva): Tarjeta? {
        val usuario = conPermiso(Accion.PAGAR_COMPROMISO) ?: return null
        if (tarjetaRepetida(nueva)) return null
        val tarjeta = crearTarjeta(datos.value, usuario.idUsuario, nueva, guardada = true, fecha = ahora())
        if (!repositorio.registrarTarjeta(tarjeta)) return null
        return tarjeta
    }

    fun quitarTarjeta(idTarjeta: Int): String? {
        val usuario = usuarioActual.value ?: return "Inicia sesión"
        val tarjeta = datos.value.tarjetaPorId(idTarjeta)
            ?.takeIf { it.idUsuario == usuario.idUsuario && it.guardada } ?: return "No encontramos esa tarjeta"
        if (!repositorio.quitarTarjeta(tarjeta.idTarjeta)) return "No pudimos quitar la tarjeta. Inténtalo de nuevo."
        return null
    }

    private fun siguienteComprobante(pagos: List<Pago>): String {
        val ultimo = pagos.maxOfOrNull { it.numeroComprobante.removePrefix("CPR-").toIntOrNull() ?: 0 } ?: 0
        return "CPR-%06d".format(ultimo + 1)
    }

    fun motivoParaNoReportar(idCompromiso: Int): String? =
        sinPermiso(Accion.REPORTAR_COBRO)
            ?: motivoCompromisoAjeno(idCompromiso)
            ?: datos.value.motivoParaNoReportar(idCompromiso)

    fun reportar(idCompromiso: Int, solicitud: SolicitudReporte): Reporte? {
        if (motivoParaNoReportar(idCompromiso) != null) return null
        val datos = datos.value
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
        val registro = movimientoReporte(TipoMovimiento.REPORTE_ENVIADO, reporte, compromiso, compromiso.idUsuarioFinal, fecha)
        if (!repositorio.registrarReporte(reporte, enRevision, registro)) return null
        return reporte
    }

    fun registrarRecomendacion(nueva: NuevaRecomendacion): Recomendacion? {
        val usuario = conPermiso(Accion.RECOMENDAR_ORGANIZACION) ?: return null
        val datos = datos.value
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
        if (!repositorio.registrarRecomendacion(recomendacion, contacto)) return null
        return recomendacion
    }

    fun guardarAvisos(activos: Boolean, anticipacion: DiasAnticipacion): Boolean {
        val usuario = usuarioActual.value ?: return false
        val ok = repositorio.actualizarUsuario(usuario.copy(avisosActivos = activos, diasAnticipacion = anticipacion.dias))
        return ok
    }

    fun cambiarConexion(sinConexion: Boolean) {
        repositorio.cambiarConexion(sinConexion)
        if (!sinConexion) sincronizar()
    }

    fun sincronizar(): Boolean {
        if (sinConexion.value) return false
        val datos = datos.value
        val cambiados = actualizarVencimientos(datos.compromisos, datos.empresas, datos.fechaHoy)
            .filter { nuevo -> datos.compromisoPorId(nuevo.idCompromiso) != nuevo }
        val vencidos = cambiados.filter { it.estado == EstadoCompromiso.VENCIDO }.map { movimientoVencimiento(it) }
        if (cambiados.isNotEmpty()) repositorio.actualizarCompromisos(cambiados, vencidos)
        repositorio.registrarActualizacion(ahora())
        return true
    }

    fun tomarReporte(idReporte: Int): String? {
        val admin = conPermiso(Accion.GESTIONAR_REPORTES) ?: return sinPermiso(Accion.GESTIONAR_REPORTES)
        val datos = datos.value
        datos.motivoParaNoTomar(idReporte, admin)?.let { return it }
        val reporte = datos.reportePorId(idReporte) ?: return "No encontramos el reporte"
        val compromiso = datos.compromisoPorId(reporte.idCompromiso) ?: return "No encontramos el compromiso del reporte"
        val fecha = ahora()
        val tomado = reporte.copy(estado = EstadoReporte.EN_GESTION, idAdministrador = admin.idUsuario, fechaToma = fecha)
        val registro = movimientoReporte(TipoMovimiento.REPORTE_EN_GESTION, tomado, compromiso, admin.idUsuario, fecha)
        if (!repositorio.actualizarReporte(tomado, null, registro)) return "No pudimos guardar el cambio. Inténtalo de nuevo."
        return null
    }

    fun resolverReporte(idReporte: Int, respuesta: String): String? {
        val admin = conPermiso(Accion.GESTIONAR_REPORTES) ?: return sinPermiso(Accion.GESTIONAR_REPORTES)
        val datos = datos.value
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
        val delReporte = compromiso ?: datos.compromisoPorId(reporte.idCompromiso) ?: return "No encontramos el compromiso del reporte"
        val registro = movimientoReporte(TipoMovimiento.REPORTE_RESUELTO, resuelto, delReporte, admin.idUsuario, fecha)
        if (!repositorio.actualizarReporte(resuelto, compromiso, registro)) return "No pudimos guardar el cambio. Inténtalo de nuevo."
        return null
    }

    fun cambiarEstadoRecomendacion(idRecomendacion: Int, nuevo: EstadoRecomendacion, motivo: String?): String? {
        val admin = conPermiso(Accion.GESTIONAR_RECOMENDACIONES) ?: return sinPermiso(Accion.GESTIONAR_RECOMENDACIONES)
        val datos = datos.value
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

        if (!repositorio.cambiarEstadoRecomendacion(recomendacion.copy(estado = nuevo), historial, reconocimiento)) {
            return "No pudimos guardar el cambio. Inténtalo de nuevo."
        }
        return null
    }

    fun avanzarRecomendacion(idRecomendacion: Int): String? {
        val recomendacion = datos.value.recomendacionPorId(idRecomendacion) ?: return "No encontramos la recomendación"
        val siguiente = EstadoRecomendacion.entries.firstOrNull {
            it != EstadoRecomendacion.DESCARTADA && it.orden == recomendacion.estado.orden + 1
        } ?: return "Esta recomendación ya terminó"
        return cambiarEstadoRecomendacion(idRecomendacion, siguiente, null)
    }

    fun buscarCompromisos(texto: String) {
        repositorio.buscarCompromisos(texto)
    }

    fun anularCompromiso(idCompromiso: Int, motivo: MotivoAnulacion, comentario: String?): String? {
        val admin = conPermiso(Accion.ANULAR_COMPROMISO) ?: return sinPermiso(Accion.ANULAR_COMPROMISO)
        val datos = datos.value
        datos.motivoParaNoAnular(admin, idCompromiso, motivo, comentario)?.let { return it }
        val compromiso = datos.compromisoPorId(idCompromiso) ?: return "No encontramos el compromiso"
        val fecha = ahora()
        val anulacion = Anulacion(idCompromiso, admin.idUsuario, motivo, comentario?.trim()?.ifBlank { null }, fecha)
        val anulado = compromiso.copy(estado = EstadoCompromiso.ANULADO, fechaCambioEstado = fecha)
        if (!repositorio.anularCompromiso(anulacion, anulado, movimientoAnulacion(anulacion, anulado))) return "No pudimos guardar la anulación. Inténtalo de nuevo."
        return null
    }

    fun registrarMiCompromiso(nuevo: NuevoCompromiso, fotoBoleta: ByteArray? = null): Compromiso? {
        val usuario = conPermiso(Accion.AGREGAR_MIS_COMPROMISOS) ?: return null
        if (nuevo.idPersona != usuario.idUsuario || nuevo.personaNueva != null) return null
        val datos = datos.value
        val fecha = ahora()
        val nuevoId = datos.compromisos.maxOf { it.idCompromiso } + 1
        val compromiso = Compromiso(
            idCompromiso = nuevoId,
            folio = "CP-%d-%04d".format(datos.fechaHoy.year, nuevoId),
            idUsuarioFinal = usuario.idUsuario,
            idEmpresa = nuevo.idEmpresa,
            concepto = nuevo.concepto,
            numeroCuota = nuevo.numeroCuota,
            totalCuotas = nuevo.totalCuotas,
            monto = nuevo.monto,
            fechaVencimiento = nuevo.fechaVencimiento,
            estado = EstadoCompromiso.PENDIENTE,
            recargoAplicado = 0,
            fechaCambioEstado = fecha,
            idAdminRegistro = null,
            fechaRegistro = fecha
        )
        val registro = movimientoRegistro(compromiso, usuario.idUsuario, fecha)
        if (!repositorio.registrarCompromiso(compromiso, null, listOf(registro), fotoBoleta)) return null
        return compromiso
    }

    fun boletaDe(idCompromiso: Int): ByteArray? {
        val compromiso = datos.value.compromisoPorId(idCompromiso) ?: return null
        if (!puedeVerCompromiso(usuarioActual.value, compromiso)) return null
        return repositorio.boletaDe(idCompromiso)
    }

    fun registrarCompromiso(nuevo: NuevoCompromiso): Compromiso? {
        val admin = conPermiso(Accion.CREAR_COMPROMISO) ?: return null
        val datos = datos.value
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
        val registros = listOfNotNull(
            personaNueva?.let {
                movimientoCuenta(TipoMovimiento.CUENTA_CREADA, it.idUsuario, admin.idUsuario, fecha, "Un administrador creó la cuenta al registrar un compromiso")
            },
            movimientoRegistro(compromiso, admin.idUsuario, fecha)
        )
        if (!repositorio.registrarCompromiso(compromiso, personaNueva, registros)) return null
        return compromiso
    }

    fun editarCompromiso(idCompromiso: Int, cambios: CambiosCompromiso): String? {
        val admin = conPermiso(Accion.EDITAR_COMPROMISO) ?: return sinPermiso(Accion.EDITAR_COMPROMISO)
        val datos = datos.value
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
        val registro = movimientoModificacion(actual, editado, admin.idUsuario, ahora())
        if (!repositorio.actualizarCompromisos(listOf(editado), listOf(registro))) return "No pudimos guardar los cambios. Revisa que no esté repetido."
        return null
    }

    fun crearCliente(datosCliente: DatosCliente): Usuario? {
        val admin = conPermiso(Accion.GESTIONAR_CLIENTES) ?: return null
        val datos = datos.value
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
        val registro = movimientoCuenta(TipoMovimiento.CUENTA_CREADA, cliente.idUsuario, admin.idUsuario, cliente.fechaCreacion, "Cuenta creada por un administrador")
        if (!repositorio.registrarUsuario(cliente, CLAVE_DE_PRUEBA, registro)) return null
        return cliente
    }

    fun editarCliente(idUsuario: Int, datosCliente: DatosCliente): String? {
        val admin = conPermiso(Accion.GESTIONAR_CLIENTES) ?: return sinPermiso(Accion.GESTIONAR_CLIENTES)
        val cliente = datos.value.usuarioPorId(idUsuario)
            ?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return "No encontramos al cliente"
        val editado = cliente.copy(
            nombres = datosCliente.nombres,
            apellidos = datosCliente.apellidos,
            correo = datosCliente.correo,
            telefono = datosCliente.telefono,
            idComuna = datosCliente.idComuna,
            activo = datosCliente.activo
        )
        val registro = movimientoCuenta(TipoMovimiento.DATOS_ACTUALIZADOS, cliente.idUsuario, admin.idUsuario, ahora(), cambiosDeUsuario(cliente, editado))
        if (!repositorio.actualizarUsuario(editado, listOf(registro))) return "No pudimos guardar los cambios. Revisa que el correo no esté registrado."
        return null
    }

    fun restablecerClaveCliente(idUsuario: Int): String? {
        val admin = conPermiso(Accion.GESTIONAR_CLIENTES) ?: return sinPermiso(Accion.GESTIONAR_CLIENTES)
        val cliente = datos.value.usuarioPorId(idUsuario)
            ?.takeIf { it.rol == Rol.USUARIO_FINAL } ?: return "No encontramos al cliente"
        val registro = movimientoCuenta(TipoMovimiento.CLAVE_RESTABLECIDA, cliente.idUsuario, admin.idUsuario, ahora(), "Se asignó la contraseña de prueba")
        return if (repositorio.cambiarClave(cliente, CLAVE_DE_PRUEBA, registro)) null else "No pudimos restablecer la contraseña"
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
