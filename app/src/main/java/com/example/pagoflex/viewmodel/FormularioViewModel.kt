package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.FormularioUiState
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.PersonaNueva
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.correoValido
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.parsearFecha
import com.example.pagoflex.model.telefonoValido
import com.example.pagoflex.model.usuarioPorRut
import com.example.pagoflex.model.validarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FormularioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FormularioUiState())
    val uiState: StateFlow<FormularioUiState> = _uiState.asStateFlow()

    fun onEmpresaSeleccionada(empresa: EmpresaCliente) {
        _uiState.update { it.copy(empresa = empresa, errorEmpresa = null, errorDuplicado = null, registroExitoso = false) }
    }

    fun onRutPersonaCambia(valor: String, datos: BaseDatos) {
        val persona = if (validarRut(valor)) datos.usuarioPorRut(valor) else null
        _uiState.update {
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
        _uiState.update { it.copy(nombresPersona = valor, errorNombresPersona = null) }
    }

    fun onApellidosPersonaCambia(valor: String) {
        _uiState.update { it.copy(apellidosPersona = valor, errorApellidosPersona = null) }
    }

    fun onCorreoPersonaCambia(valor: String) {
        _uiState.update { it.copy(correoPersona = valor.trim(), errorCorreoPersona = null) }
    }

    fun onTelefonoPersonaCambia(valor: String) {
        _uiState.update { it.copy(telefonoPersona = valor, errorTelefonoPersona = null) }
    }

    fun onConceptoCambia(valor: String) {
        _uiState.update { it.copy(concepto = valor, errorConcepto = null, errorDuplicado = null, registroExitoso = false) }
    }

    fun onMontoCambia(valor: String) {
        _uiState.update { it.copy(monto = valor.filter { c -> c.isDigit() }, errorMonto = null, registroExitoso = false) }
    }

    fun onFechaVencimientoCambia(valor: String) {
        _uiState.update { it.copy(fechaVencimiento = valor, errorFechaVencimiento = null, registroExitoso = false) }
    }

    fun onNumeroCuotaCambia(valor: String) {
        _uiState.update { it.copy(numeroCuota = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onTotalCuotasCambia(valor: String) {
        _uiState.update { it.copy(totalCuotas = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onRegistrar(datos: BaseDatos): NuevoCompromiso? {
        val estado = _uiState.value
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

        _uiState.update {
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

    fun onUsarRut(rut: String, datos: BaseDatos) {
        if (_uiState.value.rutPersona.isBlank()) onRutPersonaCambia(rut, datos)
    }

    fun onLimpiar() {
        _uiState.value = FormularioUiState()
    }
}
