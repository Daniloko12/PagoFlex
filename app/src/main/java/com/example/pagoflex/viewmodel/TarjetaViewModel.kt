package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.TarjetaNueva
import com.example.pagoflex.model.TarjetaUiState
import com.example.pagoflex.model.largoCvv
import com.example.pagoflex.model.tarjetaNuevaDe
import com.example.pagoflex.model.validarTarjeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class TarjetaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TarjetaUiState())
    val uiState: StateFlow<TarjetaUiState> = _uiState.asStateFlow()

    fun onMedioPagoSeleccionado(medio: MedioPago) {
        _uiState.update { it.copy(medioPago = medio, errorTarjeta = null) }
    }

    fun onNumeroCambia(valor: String) {
        _uiState.update { it.copy(numero = valor.filter { c -> c.isDigit() }.take(19), errorNumero = null, errorTarjeta = null) }
    }

    fun onTitularCambia(valor: String) {
        _uiState.update { it.copy(titular = valor.take(40), errorTitular = null) }
    }

    fun onVencimientoCambia(valor: String) {
        _uiState.update { it.copy(vencimiento = valor.filter { c -> c.isDigit() }.take(4), errorVencimiento = null) }
    }

    fun onCvvCambia(valor: String) {
        _uiState.update { it.copy(cvv = valor.filter { c -> c.isDigit() }.take(largoCvv(it.numero)), errorCvv = null) }
    }

    fun onTarjetaRepetida() {
        _uiState.update { it.copy(errorTarjeta = "Ya tienes guardada esta tarjeta") }
    }

    fun onAgregar(hoy: LocalDate): TarjetaNueva? {
        val estado = _uiState.value
        val errores = validarTarjeta(estado.numero, estado.titular, estado.vencimiento, estado.cvv, hoy)
        _uiState.update {
            it.copy(
                errorNumero = errores.numero,
                errorTitular = errores.titular,
                errorVencimiento = errores.vencimiento,
                errorCvv = errores.cvv,
                errorTarjeta = null
            )
        }
        if (errores.hayErrores) return null
        return tarjetaNuevaDe(estado.medioPago, estado.numero, estado.titular, estado.vencimiento)
    }
}
