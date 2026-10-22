package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.CambiarClaveUiState
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.model.claveValida
import com.example.pagoflex.model.errorClaveNueva
import com.example.pagoflex.model.errorConfirmacionClave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CambiarClaveViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CambiarClaveUiState())
    val uiState: StateFlow<CambiarClaveUiState> = _uiState.asStateFlow()

    private fun soloDigitos(valor: String) = valor.filter { it.isDigit() }.take(5)

    fun onClaveActualCambia(valor: String) {
        _uiState.update { it.copy(claveActual = soloDigitos(valor), errorClaveActual = null, errorCambio = null) }
    }

    fun onClaveNuevaCambia(valor: String) {
        _uiState.update { it.copy(claveNueva = soloDigitos(valor), errorClaveNueva = null, errorCambio = null) }
    }

    fun onConfirmacionCambia(valor: String) {
        _uiState.update { it.copy(confirmacionClave = soloDigitos(valor), errorConfirmacion = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun onErrorCambio(mensaje: String) {
        _uiState.update { it.copy(errorCambio = mensaje) }
    }

    fun onGuardar(): CambioClave? {
        val estado = _uiState.value
        val errorActual = when {
            estado.claveActual.isBlank() -> "Ingresa tu contraseña actual"
            !claveValida(estado.claveActual) -> "La contraseña tiene 5 dígitos"
            else -> null
        }
        val errorNueva = errorClaveNueva(estado.claveNueva)
            ?: if (estado.claveNueva == estado.claveActual) "La nueva contraseña debe ser distinta a la actual" else null
        val errorConfirmacion = errorConfirmacionClave(estado.claveNueva, estado.confirmacionClave)
        _uiState.update {
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

    fun onLimpiar() {
        _uiState.value = CambiarClaveUiState()
    }
}
