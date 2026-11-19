package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CambiosCompromiso
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.ContactoNuevo
import com.example.pagoflex.model.EditarCompromisoUiState
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.FormularioUiState
import com.example.pagoflex.model.LecturaBoleta
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.NuevaRecomendacion
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.PersonaNueva
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.RecomendacionUiState
import com.example.pagoflex.model.ReporteUiState
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Rubro
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.correoValido
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.mensajeLectura
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.normalizarRut
import com.example.pagoflex.model.parsearFecha
import com.example.pagoflex.model.telefonoValido
import com.example.pagoflex.model.usuarioPorRut
import com.example.pagoflex.model.validarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class CompromisoViewModel : ViewModel() {
    private val _formulario = MutableStateFlow(FormularioUiState())
    val formulario: StateFlow<FormularioUiState> = _formulario.asStateFlow()

    private val _imagenBoleta = MutableStateFlow<ByteArray?>(null)
    val imagenBoleta: StateFlow<ByteArray?> = _imagenBoleta.asStateFlow()

    fun onEmpresaSeleccionada(empresa: EmpresaCliente) {
        _formulario.update { it.copy(empresa = empresa, errorEmpresa = null, errorDuplicado = null, registroExitoso = false) }
    }

    fun onRutPersonaCambia(valor: String, datos: BaseDatos) {
        val persona = if (validarRut(valor)) datos.usuarioPorRut(valor) else null
        _formulario.update {
            it.copy(
                rutPersona = valor,
                personaNoExiste = validarRut(valor) && persona == null,
                personaEncontrada = persona?.takeIf { p -> p.rol == Rol.USUARIO_FINAL }?.let { p -> nombreCompleto(p) },
                errorRutPersona = null,
                errorDuplicado = null,
                registroExitoso = false
            )
        }
    }

    fun onNombresPersonaCambia(valor: String) {
        _formulario.update { it.copy(nombresPersona = valor, errorNombresPersona = null) }
    }

    fun onApellidosPersonaCambia(valor: String) {
        _formulario.update { it.copy(apellidosPersona = valor, errorApellidosPersona = null) }
    }

    fun onCorreoPersonaCambia(valor: String) {
        _formulario.update { it.copy(correoPersona = valor.trim(), errorCorreoPersona = null) }
    }

    fun onTelefonoPersonaCambia(valor: String) {
        _formulario.update { it.copy(telefonoPersona = valor, errorTelefonoPersona = null) }
    }

    fun onConceptoCambia(valor: String) {
        _formulario.update { it.copy(concepto = valor, errorConcepto = null, errorDuplicado = null, registroExitoso = false) }
    }

    fun onMontoCambia(valor: String) {
        _formulario.update { it.copy(monto = valor.filter { c -> c.isDigit() }, errorMonto = null, registroExitoso = false) }
    }

    fun onFechaVencimientoCambia(valor: String) {
        _formulario.update { it.copy(fechaVencimiento = valor, errorFechaVencimiento = null, registroExitoso = false) }
    }

    fun onNumeroCuotaCambia(valor: String) {
        _formulario.update { it.copy(numeroCuota = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onTotalCuotasCambia(valor: String) {
        _formulario.update { it.copy(totalCuotas = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onRegistrar(datos: BaseDatos): NuevoCompromiso? {
        val estado = _formulario.value
        val rut = estado.rutPersona.trim()
        val personaExistente = if (validarRut(rut)) datos.usuarioPorRut(rut) else null
        val idEmpresa = estado.empresa?.idEmpresa
        val errorEmpresa = if (idEmpresa == null) "Elige la empresa que cobra" else null

        val errorRut = when {
            rut.isBlank() -> "El RUT de la persona es obligatorio"
            !validarRut(rut) -> "El RUT no es válido: revisa el dígito verificador"
            personaExistente != null && personaExistente.rol != Rol.USUARIO_FINAL ->
                "Ese RUT pertenece a un administrador de PagoFlex"
            else -> null
        }
        val necesitaDatos = errorRut == null && personaExistente == null
        val errorNombres = if (necesitaDatos && estado.nombresPersona.isBlank()) "Ingresa los nombres de la persona" else null
        val errorApellidos = if (necesitaDatos && estado.apellidosPersona.isBlank()) "Ingresa los apellidos de la persona" else null
        val errorCorreo = when {
            !necesitaDatos -> null
            !correoValido(estado.correoPersona) -> "Ingresa un correo válido"
            datos.usuarios.any { it.correo.equals(estado.correoPersona.trim(), ignoreCase = true) } ->
                "Ese correo ya está registrado en PagoFlex"
            else -> null
        }
        val errorTelefono = if (necesitaDatos && !telefonoValido(estado.telefonoPersona))
            "Ingresa un teléfono de contacto (al menos 8 dígitos)" else null

        val concepto = estado.concepto.trim()
        val errorConcepto = when {
            concepto.isBlank() -> "El concepto es obligatorio"
            concepto.length > 120 -> "Máximo 120 caracteres"
            else -> null
        }
        val monto = estado.monto.toIntOrNull()
        val errorMonto = when {
            estado.monto.isBlank() -> "El monto es obligatorio"
            monto == null || monto <= 0 -> "El monto debe ser un número entero mayor que cero"
            else -> null
        }
        val fecha = parsearFecha(estado.fechaVencimiento)
        val errorFecha = when {
            estado.fechaVencimiento.isBlank() -> "La fecha de vencimiento es obligatoria"
            fecha == null -> "Usa el formato DD-MM-AAAA, por ejemplo 30-11-2026"
            fecha.isBefore(datos.fechaHoy) -> "La fecha no puede ser anterior a hoy (${formatoFecha(datos.fechaHoy)})"
            else -> null
        }
        val usaCuotas = estado.numeroCuota.isNotBlank() || estado.totalCuotas.isNotBlank()
        val numeroCuota = estado.numeroCuota.toIntOrNull()
        val totalCuotas = estado.totalCuotas.toIntOrNull()
        val errorCuota = when {
            !usaCuotas -> null
            numeroCuota == null || totalCuotas == null -> "Completa el número de cuota y el total de cuotas"
            numeroCuota <= 0 || totalCuotas <= 0 -> "Las cuotas deben ser mayores que cero"
            numeroCuota > totalCuotas -> "La cuota no puede ser mayor que el total de cuotas"
            else -> null
        }
        val errorDuplicado = if (idEmpresa != null && personaExistente != null && errorRut == null &&
            errorConcepto == null && errorCuota == null
        ) {
            val cuotaBuscada = if (usaCuotas) numeroCuota else null
            val existe = datos.compromisos.any {
                it.idUsuarioFinal == personaExistente.idUsuario && it.idEmpresa == idEmpresa &&
                    it.concepto.trim().equals(concepto, ignoreCase = true) && it.numeroCuota == cuotaBuscada &&
                    it.estado != EstadoCompromiso.ANULADO
            }
            if (existe) "Ya existe ese compromiso para esta persona (misma empresa, concepto y cuota)" else null
        } else null

        val hayErrores = listOf(
            errorEmpresa, errorRut, errorNombres, errorApellidos, errorCorreo, errorTelefono,
            errorConcepto, errorMonto, errorFecha, errorCuota, errorDuplicado
        ).any { it != null }

        _formulario.update {
            it.copy(
                errorEmpresa = errorEmpresa,
                errorRutPersona = errorRut,
                errorNombresPersona = errorNombres,
                errorApellidosPersona = errorApellidos,
                errorCorreoPersona = errorCorreo,
                errorTelefonoPersona = errorTelefono,
                errorConcepto = errorConcepto,
                errorMonto = errorMonto,
                errorFechaVencimiento = errorFecha,
                errorCuota = errorCuota,
                errorDuplicado = errorDuplicado,
                registroExitoso = !hayErrores
            )
        }
        if (hayErrores || idEmpresa == null || monto == null || fecha == null) return null

        return NuevoCompromiso(
            idEmpresa = idEmpresa,
            idPersona = personaExistente?.idUsuario,
            personaNueva = if (personaExistente == null) PersonaNueva(
                rut = rut,
                nombres = estado.nombresPersona.trim(),
                apellidos = estado.apellidosPersona.trim(),
                correo = estado.correoPersona.trim(),
                telefono = estado.telefonoPersona.trim()
            ) else null,
            concepto = concepto,
            monto = monto,
            fechaVencimiento = fecha,
            numeroCuota = if (usaCuotas) numeroCuota else null,
            totalCuotas = if (usaCuotas) totalCuotas else null
        )
    }

    fun onLeyendoBoleta() {
        _formulario.update { it.copy(leyendoBoleta = true, mensajeBoleta = null, boletaConDatos = false) }
    }

    fun onBoletaLeida(lectura: LecturaBoleta?, imagen: ByteArray?, datos: BaseDatos) {
        if (imagen != null) _imagenBoleta.value = imagen
        _formulario.update { estado ->
            val empresa = lectura?.idEmpresa?.let { datos.empresaPorId(it) }
            val concepto = estado.concepto.ifBlank { lectura?.concepto.orEmpty() }
            estado.copy(
                empresa = empresa ?: estado.empresa,
                concepto = concepto,
                monto = lectura?.monto?.toString() ?: estado.monto,
                fechaVencimiento = lectura?.vencimiento?.let { formatoFecha(it) } ?: estado.fechaVencimiento,
                errorEmpresa = if (empresa != null) null else estado.errorEmpresa,
                errorConcepto = if (concepto.isNotBlank()) null else estado.errorConcepto,
                errorMonto = if (lectura?.monto != null) null else estado.errorMonto,
                errorFechaVencimiento = if (lectura?.vencimiento != null) null else estado.errorFechaVencimiento,
                errorDuplicado = null,
                leyendoBoleta = false,
                mensajeBoleta = mensajeLectura(lectura, datos.empresas),
                boletaConDatos = lectura?.encontroAlgo == true
            )
        }
    }

    fun onUsarRut(rut: String, datos: BaseDatos) {
        if (_formulario.value.rutPersona.isBlank()) onRutPersonaCambia(rut, datos)
    }

    fun onLimpiarFormulario() {
        _formulario.value = FormularioUiState()
    }

    private val _edicion = MutableStateFlow(EditarCompromisoUiState())
    val edicion: StateFlow<EditarCompromisoUiState> = _edicion.asStateFlow()

    fun onCargarEdicion(compromiso: Compromiso) {
        if (_edicion.value.cargado) return
        _edicion.value = EditarCompromisoUiState(
            concepto = compromiso.concepto,
            monto = compromiso.monto.toString(),
            fechaVencimiento = formatoFecha(compromiso.fechaVencimiento),
            numeroCuota = compromiso.numeroCuota?.toString() ?: "",
            totalCuotas = compromiso.totalCuotas?.toString() ?: "",
            cargado = true
        )
    }

    fun onConceptoEdicionCambia(valor: String) {
        _edicion.update { it.copy(concepto = valor, errorConcepto = null, errorDuplicado = null) }
    }

    fun onMontoEdicionCambia(valor: String) {
        _edicion.update { it.copy(monto = valor.filter { c -> c.isDigit() }, errorMonto = null) }
    }

    fun onFechaEdicionCambia(valor: String) {
        _edicion.update { it.copy(fechaVencimiento = valor, errorFechaVencimiento = null) }
    }

    fun onNumeroCuotaEdicionCambia(valor: String) {
        _edicion.update { it.copy(numeroCuota = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onTotalCuotasEdicionCambia(valor: String) {
        _edicion.update { it.copy(totalCuotas = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onErrorEdicion(mensaje: String) {
        _edicion.update { it.copy(errorEdicion = mensaje) }
    }

    fun onGuardarEdicion(compromiso: Compromiso, datos: BaseDatos): CambiosCompromiso? {
        val estado = _edicion.value
        val errorEdicion = datos.motivoParaNoEditar(compromiso.idCompromiso)

        val concepto = estado.concepto.trim()
        val errorConcepto = when {
            concepto.isBlank() -> "El concepto es obligatorio"
            concepto.length > 120 -> "Máximo 120 caracteres"
            else -> null
        }
        val monto = estado.monto.toIntOrNull()
        val errorMonto = when {
            estado.monto.isBlank() -> "El monto es obligatorio"
            monto == null || monto <= 0 -> "El monto debe ser un número entero mayor que cero"
            else -> null
        }
        val fecha = parsearFecha(estado.fechaVencimiento)
        val errorFecha = when {
            estado.fechaVencimiento.isBlank() -> "La fecha de vencimiento es obligatoria"
            fecha == null -> "Usa el formato DD-MM-AAAA, por ejemplo 30-11-2026"
            fecha.isBefore(datos.fechaHoy) && fecha != compromiso.fechaVencimiento ->
                "La nueva fecha no puede ser anterior a hoy (${formatoFecha(datos.fechaHoy)})"
            else -> null
        }
        val usaCuotas = estado.numeroCuota.isNotBlank() || estado.totalCuotas.isNotBlank()
        val numeroCuota = estado.numeroCuota.toIntOrNull()
        val totalCuotas = estado.totalCuotas.toIntOrNull()
        val errorCuota = when {
            !usaCuotas -> null
            numeroCuota == null || totalCuotas == null -> "Completa el número de cuota y el total de cuotas"
            numeroCuota <= 0 || totalCuotas <= 0 -> "Las cuotas deben ser mayores que cero"
            numeroCuota > totalCuotas -> "La cuota no puede ser mayor que el total de cuotas"
            else -> null
        }
        val cuotaBuscada = if (usaCuotas) numeroCuota else null
        val errorDuplicado = if (errorConcepto == null && errorCuota == null && datos.compromisos.any {
                it.idCompromiso != compromiso.idCompromiso &&
                    it.idUsuarioFinal == compromiso.idUsuarioFinal && it.idEmpresa == compromiso.idEmpresa &&
                    it.concepto.trim().equals(concepto, ignoreCase = true) && it.numeroCuota == cuotaBuscada &&
                    it.estado != EstadoCompromiso.ANULADO
            }
        ) "Ya existe otro compromiso igual para esta persona (misma empresa, concepto y cuota)" else null

        val hayErrores = listOf(errorEdicion, errorConcepto, errorMonto, errorFecha, errorCuota, errorDuplicado).any { it != null }
        _edicion.update {
            it.copy(
                errorConcepto = errorConcepto,
                errorMonto = errorMonto,
                errorFechaVencimiento = errorFecha,
                errorCuota = errorCuota,
                errorDuplicado = errorDuplicado,
                errorEdicion = errorEdicion
            )
        }
        if (hayErrores || monto == null || fecha == null) return null

        return CambiosCompromiso(
            concepto = concepto,
            monto = monto,
            fechaVencimiento = fecha,
            numeroCuota = if (usaCuotas) numeroCuota else null,
            totalCuotas = if (usaCuotas) totalCuotas else null
        )
    }

    private val _reporte = MutableStateFlow(ReporteUiState())
    val reporte: StateFlow<ReporteUiState> = _reporte.asStateFlow()

    fun onMotivoReporteSeleccionado(motivo: MotivoReporte) {
        _reporte.update { it.copy(motivo = motivo, errorMotivo = null, errorComentario = null) }
    }

    fun onComentarioReporteCambia(valor: String) {
        _reporte.update { it.copy(comentario = valor, errorComentario = null) }
    }

    fun onEnviarReporte(bloqueo: String?): SolicitudReporte? {
        val estado = _reporte.value
        val motivo = estado.motivo

        val errorMotivo = if (motivo == null) "Elige el motivo del problema" else null
        val errorComentario = when {
            motivo == MotivoReporte.OTRO && estado.comentario.isBlank() ->
                "Cuéntanos brevemente qué pasó (obligatorio si eliges \"Otro\")"
            estado.comentario.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
        val hayErrores = listOf(errorMotivo, errorComentario, bloqueo).any { it != null }

        _reporte.update {
            it.copy(
                errorMotivo = errorMotivo,
                errorComentario = errorComentario,
                errorReporte = bloqueo,
                reporteEnviado = !hayErrores
            )
        }
        if (hayErrores || motivo == null) return null
        return SolicitudReporte(motivo, estado.comentario.trim().ifBlank { null })
    }

    private val _recomendacion = MutableStateFlow(RecomendacionUiState())
    val recomendacion: StateFlow<RecomendacionUiState> = _recomendacion.asStateFlow()

    fun onNombreOrganizacionCambia(valor: String) {
        _recomendacion.update { it.copy(nombreOrganizacion = valor, errorNombreOrganizacion = null, errorRecomendacion = null) }
    }

    fun onRutOrganizacionCambia(valor: String) {
        _recomendacion.update { it.copy(rutOrganizacion = valor, errorRutOrganizacion = null) }
    }

    fun onRubroSeleccionado(rubro: Rubro) {
        _recomendacion.update { it.copy(rubro = rubro, errorRubro = null) }
    }

    fun onComunaRecomendacionSeleccionada(comuna: Comuna) {
        _recomendacion.update { it.copy(comuna = comuna, errorComuna = null) }
    }

    fun onNombreContactoCambia(valor: String) {
        _recomendacion.update { it.copy(nombreContacto = valor, errorNombreContacto = null, errorAutorizaContacto = null) }
    }

    fun onCorreoContactoCambia(valor: String) {
        _recomendacion.update { it.copy(correoContacto = valor.trim(), errorCorreoContacto = null, errorAutorizaContacto = null) }
    }

    fun onTelefonoContactoCambia(valor: String) {
        _recomendacion.update { it.copy(telefonoContacto = valor, errorAutorizaContacto = null) }
    }

    fun onAutorizaContactoCambia(valor: Boolean) {
        _recomendacion.update { it.copy(autorizaContacto = valor, errorAutorizaContacto = null) }
    }

    fun onEnviarRecomendacion(propias: List<Recomendacion>, hoy: LocalDate): NuevaRecomendacion? {
        val estado = _recomendacion.value
        val nombre = estado.nombreOrganizacion.trim()
        val rubro = estado.rubro
        val comuna = estado.comuna
        val hayContacto = estado.nombreContacto.isNotBlank() || estado.correoContacto.isNotBlank() ||
            estado.telefonoContacto.isNotBlank()

        val errorNombre = when {
            nombre.isBlank() -> "El nombre de la organización es obligatorio"
            nombre.length > 120 -> "Máximo 120 caracteres"
            else -> null
        }
        val errorRut = if (estado.rutOrganizacion.isNotBlank() && !validarRut(estado.rutOrganizacion))
            "El RUT no es válido: revisa el dígito verificador" else null
        val errorRubro = if (rubro == null) "Elige el rubro" else null
        val errorComuna = if (comuna == null) "Elige la comuna" else null
        val errorAutoriza = if (hayContacto && !estado.autorizaContacto)
            "Para guardar datos de contacto, confirma que esa persona autorizó que PagoFlex la contacte" else null
        val errorNombreContacto = if (hayContacto && estado.autorizaContacto && estado.nombreContacto.isBlank())
            "Ingresa el nombre de la persona de contacto" else null
        val errorCorreoContacto = if (estado.correoContacto.isNotBlank() && !correoValido(estado.correoContacto))
            "Ingresa un correo válido" else null
        val existente = propias.find { it.nombreOrganizacion.trim().equals(nombre, ignoreCase = true) }
        val delMes = propias.count { YearMonth.from(it.fecha) == YearMonth.from(hoy) }
        val errorRecomendacion = when {
            existente != null -> "Ya recomendaste a ${existente.nombreOrganizacion}. Su estado es: ${existente.estado.etiqueta}."
            delMes >= 5 -> "Puedes registrar hasta 5 recomendaciones por mes. ¡Gracias por tu entusiasmo!"
            else -> null
        }

        val hayErrores = listOf(
            errorNombre, errorRut, errorRubro, errorComuna, errorAutoriza,
            errorNombreContacto, errorCorreoContacto, errorRecomendacion
        ).any { it != null }

        _recomendacion.update {
            it.copy(
                errorNombreOrganizacion = errorNombre,
                errorRutOrganizacion = errorRut,
                errorRubro = errorRubro,
                errorComuna = errorComuna,
                errorAutorizaContacto = errorAutoriza,
                errorNombreContacto = errorNombreContacto,
                errorCorreoContacto = errorCorreoContacto,
                errorRecomendacion = errorRecomendacion,
                recomendacionEnviada = !hayErrores
            )
        }
        if (hayErrores || rubro == null || comuna == null) return null

        return NuevaRecomendacion(
            nombreOrganizacion = nombre,
            rutOrganizacion = if (estado.rutOrganizacion.isBlank()) null else normalizarRut(estado.rutOrganizacion),
            idRubro = rubro.idRubro,
            idComuna = comuna.idComuna,
            contacto = if (hayContacto) ContactoNuevo(
                nombre = estado.nombreContacto.trim(),
                correo = estado.correoContacto.trim().ifBlank { null },
                telefono = estado.telefonoContacto.trim().ifBlank { null }
            ) else null
        )
    }

    fun onLimpiarRecomendacion() {
        _recomendacion.value = RecomendacionUiState()
    }
}
