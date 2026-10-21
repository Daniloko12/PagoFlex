package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.PagoUiState
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.largoCvv
import com.example.pagoflex.model.tarjetaNuevaDe
import com.example.pagoflex.model.validarTarjeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class PagoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PagoUiState())
    val uiState: StateFlow<PagoUiState> = _uiState.asStateFlow()

    fun onMedioPagoSeleccionado(medio: MedioPago, tarjetasDelMedio: List<Tarjeta> = emptyList()) {
        _uiState.update {
            it.copy(
                medioPago = medio,
                idTarjeta = tarjetasDelMedio.firstOrNull()?.idTarjeta,
                usarTarjetaNueva = tarjetasDelMedio.isEmpty(),
                errorMedioPago = null,
                errorTarjeta = null
            )
        }
    }

    fun onTarjetaSeleccionada(idTarjeta: Int?) {
        _uiState.update {
            it.copy(idTarjeta = idTarjeta, usarTarjetaNueva = idTarjeta == null, errorTarjeta = null)
        }
    }

    fun onNumeroTarjetaCambia(valor: String) {
        _uiState.update { it.copy(numeroTarjeta = valor.filter { c -> c.isDigit() }.take(19), errorNumeroTarjeta = null) }
    }

    fun onTitularTarjetaCambia(valor: String) {
        _uiState.update { it.copy(titularTarjeta = valor.take(40), errorTitularTarjeta = null) }
    }

    fun onVencimientoTarjetaCambia(valor: String) {
        _uiState.update { it.copy(vencimientoTarjeta = valor.filter { c -> c.isDigit() }.take(4), errorVencimientoTarjeta = null) }
    }

    fun onCvvCambia(valor: String) {
        _uiState.update {
            it.copy(cvv = valor.filter { c -> c.isDigit() }.take(largoCvv(it.numeroTarjeta)), errorCvv = null)
        }
    }

    fun onGuardarTarjetaCambia(valor: Boolean) {
        _uiState.update { it.copy(guardarTarjeta = valor) }
    }

    fun onCodigoCambia(valor: String) {
        val limpio = valor.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(codigo = limpio, errorCodigo = null) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(codigoVisible = !it.codigoVisible) }
    }

    fun onConfirmaTotalCambia(valor: Boolean) {
        _uiState.update { it.copy(confirmaTotal = valor, errorConfirmacion = null) }
    }

    fun onPagar(codigoCorrecto: String, bloqueo: String?, tarjetasGuardadas: List<Tarjeta>, hoy: LocalDate): SolicitudPago? {
        val estado = _uiState.value
        val medio = estado.medioPago
        val conTarjeta = medio == MedioPago.DEBITO || medio == MedioPago.CREDITO
        val usaNueva = conTarjeta && (estado.usarTarjetaNueva || tarjetasGuardadas.isEmpty())
        val usaGuardada = conTarjeta && !usaNueva

        val errorMedioPago = if (medio == null) "Elige cómo quieres pagar" else null
        val errorTarjeta = if (usaGuardada && tarjetasGuardadas.none { it.idTarjeta == estado.idTarjeta })
            "Elige una de tus tarjetas o usa otra" else null
        val erroresNueva = if (usaNueva)
            validarTarjeta(estado.numeroTarjeta, estado.titularTarjeta, estado.vencimientoTarjeta, estado.cvv, hoy)
        else null
        val errorCodigo = when {
            estado.codigo.isBlank() -> "Ingresa tu código de confirmación"
            estado.codigo != codigoCorrecto -> "El código no coincide. Son los 4 últimos números de tu RUT, antes del guion"
            else -> null
        }
        val errorConfirmacion = if (!estado.confirmaTotal) "Confirma que revisaste el total a pagar" else null

        val hayErrores = listOf(errorMedioPago, errorTarjeta, errorCodigo, errorConfirmacion, bloqueo).any { it != null } ||
            erroresNueva?.hayErrores == true

        _uiState.update {
            it.copy(
                errorMedioPago = errorMedioPago,
                errorTarjeta = errorTarjeta,
                errorNumeroTarjeta = erroresNueva?.numero,
                errorTitularTarjeta = erroresNueva?.titular,
                errorVencimientoTarjeta = erroresNueva?.vencimiento,
                errorCvv = erroresNueva?.cvv,
                errorCodigo = errorCodigo,
                errorConfirmacion = errorConfirmacion,
                errorPago = bloqueo,
                pagoExitoso = !hayErrores
            )
        }
        if (hayErrores || medio == null) return null

        return SolicitudPago(
            medioPago = medio,
            idTarjeta = if (usaGuardada) estado.idTarjeta else null,
            tarjetaNueva = if (usaNueva) tarjetaNuevaDe(medio, estado.numeroTarjeta, estado.titularTarjeta, estado.vencimientoTarjeta) else null,
            guardarTarjeta = usaNueva && estado.guardarTarjeta
        )
    }
}
