package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.ClienteUiState
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.DatosCliente
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.validarPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClienteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    fun onCargar(cliente: Usuario, datos: BaseDatos) {
        if (_uiState.value.idEditando == cliente.idUsuario) return
        _uiState.value = ClienteUiState(
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

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null) }
    }

    fun onNombresCambia(valor: String) {
        _uiState.update { it.copy(nombres = valor, errorNombres = null) }
    }

    fun onApellidosCambia(valor: String) {
        _uiState.update { it.copy(apellidos = valor, errorApellidos = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onTelefonoCambia(valor: String) {
        _uiState.update { it.copy(telefono = valor, errorTelefono = null) }
    }

    fun onComunaSeleccionada(comuna: Comuna) {
        _uiState.update { it.copy(comuna = comuna, errorComuna = null) }
    }

    fun onActivoCambia(valor: Boolean) {
        _uiState.update { it.copy(activo = valor) }
    }

    fun onGuardar(datos: BaseDatos): DatosCliente? {
        val estado = _uiState.value
        val errores = datos.validarPersona(
            estado.rut, estado.nombres, estado.apellidos, estado.correo, estado.telefono,
            idPropio = estado.idEditando
        )
        val errorComuna = if (estado.comuna == null) "Elige la comuna" else null

        _uiState.update {
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
