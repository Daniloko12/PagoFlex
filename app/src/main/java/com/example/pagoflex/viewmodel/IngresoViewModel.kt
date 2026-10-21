package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.model.IngresoUiState
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.claveValida
import com.example.pagoflex.model.correoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class IngresoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(IngresoUiState())
    val uiState: StateFlow<IngresoUiState> = _uiState.asStateFlow()

    fun onRolSeleccionado(rol: Rol) {
        _uiState.update { it.copy(rol = rol, errorIngreso = null) }
    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null, errorIngreso = null) }
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor.filter { c -> c.isDigit() }.take(5), errorClave = null, errorIngreso = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun onUsarCuentaDePrueba(correo: String, rol: Rol) {
        _uiState.value = IngresoUiState(rol = rol, correo = correo, clave = CLAVE_DE_PRUEBA)
    }

    fun onErrorIngreso(mensaje: String) {
        _uiState.update { it.copy(errorIngreso = mensaje) }
    }

    fun onIngresar(): Credenciales? {
        val estado = _uiState.value

        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa tu correo"
            !correoValido(estado.correo) -> "Ingresa un correo válido, por ejemplo nombre@gmail.com"
            else -> null
        }
        val errorClave = when {
            estado.clave.isBlank() -> "Ingresa tu contraseña"
            !claveValida(estado.clave) -> "La contraseña tiene 5 dígitos"
            else -> null
        }
        val hayErrores = errorCorreo != null || errorClave != null

        _uiState.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave, errorIngreso = null) }
        return if (hayErrores) null else Credenciales(estado.correo, estado.clave, estado.rol)
    }

    fun onLimpiar() {
        _uiState.value = IngresoUiState()
    }
}
