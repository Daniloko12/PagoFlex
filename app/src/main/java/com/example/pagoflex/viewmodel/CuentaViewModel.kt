package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CambiarClaveUiState
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.ClienteUiState
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.model.DatosCliente
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.EditarPerfilUiState
import com.example.pagoflex.model.IngresoUiState
import com.example.pagoflex.model.LARGO_CODIGO_RECUPERACION
import com.example.pagoflex.model.LARGO_MAXIMO_CLAVE
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.PasoRecuperacion
import com.example.pagoflex.model.RecuperarClaveUiState
import com.example.pagoflex.model.RegistroUiState
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.correoValido
import com.example.pagoflex.model.errorClaveNueva
import com.example.pagoflex.model.errorConfirmacionClave
import com.example.pagoflex.model.errorFormatoCodigo
import com.example.pagoflex.model.validarPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CuentaViewModel : ViewModel() {
    private val _ingreso = MutableStateFlow(IngresoUiState())
    val ingreso: StateFlow<IngresoUiState> = _ingreso.asStateFlow()

    fun onCorreoIngresoCambia(valor: String) {
        _ingreso.update { it.copy(correo = valor.trim(), errorCorreo = null, errorIngreso = null) }
    }

    fun onClaveIngresoCambia(valor: String) {
        _ingreso.update { it.copy(clave = valor.take(LARGO_MAXIMO_CLAVE), errorClave = null, errorIngreso = null) }
    }

    fun onErrorIngreso(mensaje: String) {
        _ingreso.update { it.copy(errorIngreso = mensaje) }
    }

    fun onIngresar(): Credenciales? {
        val estado = _ingreso.value

        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa tu correo"
            !correoValido(estado.correo) -> "Ingresa un correo válido, por ejemplo nombre@gmail.com"
            else -> null
        }
        val errorClave = if (estado.clave.isBlank()) "Ingresa tu contraseña" else null
        val hayErrores = errorCorreo != null || errorClave != null

        _ingreso.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave, errorIngreso = null) }
        return if (hayErrores) null else Credenciales(estado.correo, estado.clave)
    }

    fun onLimpiarIngreso() {
        _ingreso.value = IngresoUiState()
    }

    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro.asStateFlow()

    fun onRutRegistroCambia(valor: String) {
        _registro.update { it.copy(rut = valor, errorRut = null, errorRegistro = null) }
    }

    fun onNombresRegistroCambia(valor: String) {
        _registro.update { it.copy(nombres = valor, errorNombres = null) }
    }

    fun onApellidosRegistroCambia(valor: String) {
        _registro.update { it.copy(apellidos = valor, errorApellidos = null) }
    }

    fun onCorreoRegistroCambia(valor: String) {
        _registro.update { it.copy(correo = valor.trim(), errorCorreo = null, errorRegistro = null) }
    }

    fun onTelefonoRegistroCambia(valor: String) {
        _registro.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onClaveRegistroCambia(valor: String) {
        _registro.update { it.copy(clave = valor.take(LARGO_MAXIMO_CLAVE), errorClave = null) }
    }

    fun onConfirmacionRegistroCambia(valor: String) {
        _registro.update { it.copy(confirmacionClave = valor.take(LARGO_MAXIMO_CLAVE), errorConfirmacion = null) }
    }

    fun onAceptaTerminosCambia(valor: Boolean) {
        _registro.update { it.copy(aceptaTerminos = valor, errorTerminos = null) }
    }

    fun onErrorRegistro(mensaje: String) {
        _registro.update { it.copy(errorRegistro = mensaje) }
    }

    fun onCrearCuenta(datos: BaseDatos): NuevaCuenta? {
        val estado = _registro.value
        val errores = datos.validarPersona(estado.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono)
        val errorClave = errorClaveNueva(estado.clave)
        val errorConfirmacion = errorConfirmacionClave(estado.clave, estado.confirmacionClave)
        val errorTerminos = if (!estado.aceptaTerminos) "Debes aceptar los términos para crear tu cuenta" else null
        val hayErrores = errores.hayErrores || listOf(errorClave, errorConfirmacion, errorTerminos).any { it != null }

        _registro.update {
            it.copy(
                errorRut = errores.rut,
                errorNombres = errores.nombres,
                errorApellidos = errores.apellidos,
                errorCorreo = errores.correo,
                errorTelefono = errores.telefono,
                errorClave = errorClave,
                errorConfirmacion = errorConfirmacion,
                errorTerminos = errorTerminos,
                errorRegistro = null
            )
        }
        if (hayErrores) return null

        return NuevaCuenta(
            rut = estado.rut.trim(),
            nombres = estado.nombres.trim(),
            apellidos = estado.apellidos.trim(),
            correo = estado.correo.trim(),
            telefono = estado.telefono.trim(),
            clave = estado.clave
        )
    }

    fun onLimpiarRegistro() {
        _registro.value = RegistroUiState()
    }

    private val _cambioClave = MutableStateFlow(CambiarClaveUiState())
    val cambioClave: StateFlow<CambiarClaveUiState> = _cambioClave.asStateFlow()

    private fun limitar(valor: String) = valor.take(LARGO_MAXIMO_CLAVE)

    fun onClaveActualCambia(valor: String) {
        _cambioClave.update { it.copy(claveActual = limitar(valor), errorClaveActual = null, errorCambio = null) }
    }

    fun onClaveNuevaCambia(valor: String) {
        _cambioClave.update { it.copy(claveNueva = limitar(valor), errorClaveNueva = null, errorCambio = null) }
    }

    fun onConfirmacionClaveNuevaCambia(valor: String) {
        _cambioClave.update { it.copy(confirmacionClave = limitar(valor), errorConfirmacion = null) }
    }

    fun onErrorCambioClave(mensaje: String) {
        _cambioClave.update { it.copy(errorCambio = mensaje) }
    }

    fun onGuardarCambioClave(): CambioClave? {
        val estado = _cambioClave.value
        val errorActual = if (estado.claveActual.isBlank()) "Ingresa tu contraseña actual" else null
        val errorNueva = errorClaveNueva(estado.claveNueva)
            ?: if (estado.claveNueva == estado.claveActual) "La nueva contraseña debe ser distinta a la actual" else null
        val errorConfirmacion = errorConfirmacionClave(estado.claveNueva, estado.confirmacionClave)
        _cambioClave.update {
            it.copy(
                errorClaveActual = errorActual,
                errorClaveNueva = errorNueva,
                errorConfirmacion = errorConfirmacion,
                errorCambio = null
            )
        }
        if (errorActual != null || errorNueva != null || errorConfirmacion != null) return null
        return CambioClave(estado.claveActual, estado.claveNueva)
    }

    fun onLimpiarCambioClave() {
        _cambioClave.value = CambiarClaveUiState()
    }

    private val _recuperacion = MutableStateFlow(RecuperarClaveUiState())
    val recuperacion: StateFlow<RecuperarClaveUiState> = _recuperacion.asStateFlow()

    fun onCorreoRecuperacionCambia(valor: String) {
        _recuperacion.update { it.copy(correo = valor.trim(), errorCorreo = null, errorRecuperacion = null) }
    }

    fun onCodigoCambia(valor: String) {
        _recuperacion.update {
            it.copy(codigo = valor.filter { c -> c.isDigit() }.take(LARGO_CODIGO_RECUPERACION), errorCodigo = null, errorRecuperacion = null)
        }
    }

    fun onClaveRecuperacionCambia(valor: String) {
        _recuperacion.update { it.copy(claveNueva = valor.take(LARGO_MAXIMO_CLAVE), errorClaveNueva = null, errorRecuperacion = null) }
    }

    fun onConfirmacionRecuperacionCambia(valor: String) {
        _recuperacion.update { it.copy(confirmacionClave = valor.take(LARGO_MAXIMO_CLAVE), errorConfirmacion = null) }
    }

    fun onErrorRecuperacion(mensaje: String) {
        _recuperacion.update { it.copy(errorRecuperacion = mensaje) }
    }

    fun onErrorCodigo(mensaje: String) {
        _recuperacion.update { it.copy(errorCodigo = mensaje) }
    }

    fun onSolicitarCodigo(): DatosRecuperacion? {
        val estado = _recuperacion.value
        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa el correo de tu cuenta"
            !correoValido(estado.correo) -> "Ingresa un correo válido, por ejemplo nombre@gmail.com"
            else -> null
        }
        _recuperacion.update { it.copy(errorCorreo = errorCorreo, errorRecuperacion = null) }
        return if (errorCorreo != null) null else DatosRecuperacion(estado.correo)
    }

    fun onCodigoEnviado() {
        _recuperacion.update { it.copy(paso = PasoRecuperacion.CODIGO, codigo = "", errorCodigo = null, errorRecuperacion = null) }
    }

    fun onVerificarCodigo(): String? {
        val estado = _recuperacion.value
        val error = errorFormatoCodigo(estado.codigo)
        _recuperacion.update { it.copy(errorCodigo = error, errorRecuperacion = null) }
        return if (estado.paso == PasoRecuperacion.CODIGO && error == null) estado.codigo else null
    }

    fun onCodigoCorrecto() {
        _recuperacion.update { it.copy(paso = PasoRecuperacion.CLAVE, errorCodigo = null, errorRecuperacion = null) }
    }

    fun onCambiarDatosRecuperacion() {
        _recuperacion.update {
            it.copy(paso = PasoRecuperacion.DATOS, codigo = "", claveNueva = "", confirmacionClave = "", errorCodigo = null, errorRecuperacion = null)
        }
    }

    fun onGuardarClaveRecuperada(): ClaveRecuperada? {
        val estado = _recuperacion.value
        val errorClave = errorClaveNueva(estado.claveNueva)
        val errorConfirmacion = errorConfirmacionClave(estado.claveNueva, estado.confirmacionClave)
        _recuperacion.update { it.copy(errorClaveNueva = errorClave, errorConfirmacion = errorConfirmacion, errorRecuperacion = null) }
        if (estado.paso != PasoRecuperacion.CLAVE || errorClave != null || errorConfirmacion != null) return null
        return ClaveRecuperada(estado.correo, estado.codigo, estado.claveNueva)
    }

    fun onLimpiarRecuperacion() {
        _recuperacion.value = RecuperarClaveUiState()
    }

    private val _perfil = MutableStateFlow(EditarPerfilUiState())
    val perfil: StateFlow<EditarPerfilUiState> = _perfil.asStateFlow()

    fun onCargarPerfil(usuario: Usuario, datos: BaseDatos) {
        if (_perfil.value.idCargado == usuario.idUsuario) return
        _perfil.value = EditarPerfilUiState(
            nombres = usuario.nombres,
            apellidos = usuario.apellidos,
            correo = usuario.correo,
            telefono = usuario.telefono ?: "",
            comuna = datos.comunaPorId(usuario.idComuna),
            idCargado = usuario.idUsuario
        )
    }

    fun onNombresPerfilCambia(valor: String) {
        _perfil.update { it.copy(nombres = valor, errorNombres = null, errorGuardar = null) }
    }

    fun onApellidosPerfilCambia(valor: String) {
        _perfil.update { it.copy(apellidos = valor, errorApellidos = null, errorGuardar = null) }
    }

    fun onCorreoPerfilCambia(valor: String) {
        _perfil.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGuardar = null) }
    }

    fun onTelefonoPerfilCambia(valor: String) {
        _perfil.update { it.copy(telefono = valor, errorTelefono = null, errorGuardar = null) }
    }

    fun onComunaPerfilSeleccionada(comuna: Comuna) {
        _perfil.update { it.copy(comuna = comuna, errorComuna = null, errorGuardar = null) }
    }

    fun onErrorPerfil(mensaje: String) {
        _perfil.update { it.copy(errorGuardar = mensaje) }
    }

    fun onGuardarPerfil(usuario: Usuario, datos: BaseDatos): MisDatos? {
        val estado = _perfil.value
        val errores = datos.validarPersona(
            usuario.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono,
            idPropio = usuario.idUsuario
        )
        val errorComuna = if (usuario.rol == Rol.USUARIO_FINAL && estado.comuna == null) "Elige tu comuna" else null
        _perfil.update {
            it.copy(
                errorNombres = errores.nombres,
                errorApellidos = errores.apellidos,
                errorCorreo = errores.correo,
                errorTelefono = errores.telefono,
                errorComuna = errorComuna,
                errorGuardar = null
            )
        }
        if (errores.hayErrores || errorComuna != null) return null

        return MisDatos(
            nombres = estado.nombres.trim(),
            apellidos = estado.apellidos.trim(),
            correo = estado.correo.trim(),
            telefono = estado.telefono.trim(),
            idComuna = estado.comuna?.idComuna
        )
    }

    private val _cliente = MutableStateFlow(ClienteUiState())
    val cliente: StateFlow<ClienteUiState> = _cliente.asStateFlow()

    fun onCargarCliente(cliente: Usuario, datos: BaseDatos) {
        if (_cliente.value.idEditando == cliente.idUsuario) return
        _cliente.value = ClienteUiState(
            rut = cliente.rut,
            nombres = cliente.nombres,
            apellidos = cliente.apellidos,
            correo = cliente.correo,
            telefono = cliente.telefono ?: "",
            comuna = datos.comunaPorId(cliente.idComuna),
            activo = cliente.activo,
            idEditando = cliente.idUsuario
        )
    }

    fun onRutClienteCambia(valor: String) {
        _cliente.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onNombresClienteCambia(valor: String) {
        _cliente.update { it.copy(nombres = valor, errorNombres = null) }
    }

    fun onApellidosClienteCambia(valor: String) {
        _cliente.update { it.copy(apellidos = valor, errorApellidos = null) }
    }

    fun onCorreoClienteCambia(valor: String) {
        _cliente.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onTelefonoClienteCambia(valor: String) {
        _cliente.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onComunaClienteSeleccionada(comuna: Comuna) {
        _cliente.update { it.copy(comuna = comuna, errorComuna = null) }
    }

    fun onActivoClienteCambia(valor: Boolean) {
        _cliente.update { it.copy(activo = valor) }
    }

    fun onGuardarCliente(datos: BaseDatos): DatosCliente? {
        val estado = _cliente.value
        val errores = datos.validarPersona(
            estado.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono,
            idPropio = estado.idEditando
        )
        val errorComuna = if (estado.comuna == null) "Elige la comuna" else null

        _cliente.update {
            it.copy(
                errorRut = errores.rut,
                errorNombres = errores.nombres,
                errorApellidos = errores.apellidos,
                errorCorreo = errores.correo,
                errorTelefono = errores.telefono,
                errorComuna = errorComuna
            )
        }
        val comuna = estado.comuna
        if (errores.hayErrores || comuna == null) return null

        return DatosCliente(
            rut = estado.rut.trim(),
            nombres = estado.nombres.trim(),
            apellidos = estado.apellidos.trim(),
            correo = estado.correo.trim(),
            telefono = estado.telefono.trim(),
            idComuna = comuna.idComuna,
            activo = estado.activo
        )
    }
}
