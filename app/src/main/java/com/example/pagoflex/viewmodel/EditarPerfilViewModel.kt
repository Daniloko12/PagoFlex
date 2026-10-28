package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.EditarPerfilUiState
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.validarPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditarPerfilViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState.asStateFlow()

    fun onCargar(usuario: Usuario, datos: BaseDatos) {
        if (_uiState.value.idCargado == usuario.idUsuario) return
        _uiState.value = EditarPerfilUiState(
            nombres = usuario.nombres,
            apellidos = usuario.apellidos,
            correo = usuario.correo,
            telefono = usuario.telefono ?: "",
            comuna = datos.comunaPorId(usuario.idComuna),
            idCargado = usuario.idUsuario
        )
    }

    fun onNombresCambia(valor: String) {
        _uiState.update { it.copy(nombres = valor, errorNombres = null, errorGuardar = null) }
    }

    fun onApellidosCambia(valor: String) {
        _uiState.update { it.copy(apellidos = valor, errorApellidos = null, errorGuardar = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorGuardar = null) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null, errorGuardar = null) }
    }

    fun onComunaSeleccionada(comuna: Comuna) {
        _uiState.update { it.copy(comuna = comuna, errorComuna = null, errorGuardar = null) }
    }

    fun onErrorGuardar(mensaje: String) {
        _uiState.update { it.copy(errorGuardar = mensaje) }
    }

    fun onGuardar(usuario: Usuario, datos: BaseDatos): MisDatos? {
        val estado = _uiState.value
        val errores = datos.validarPersona(
            usuario.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono,
            idPropio = usuario.idUsuario
        )
        val errorComuna = if (usuario.rol == Rol.USUARIO_FINAL && estado.comuna == null) "Elige tu comuna" else null
        _uiState.update {
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
}
