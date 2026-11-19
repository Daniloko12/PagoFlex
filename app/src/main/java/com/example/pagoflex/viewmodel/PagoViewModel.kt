package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.PagoUiState
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.Tarjeta
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

class PagoViewModel : ViewModel() {
    private val _pago = MutableStateFlow(PagoUiState())
    val pago: StateFlow<PagoUiState> = _pago.asStateFlow()

    fun onMedioPagoSeleccionado(medio: MedioPago, tarjetasDelMedio: List<Tarjeta> = emptyList()) {
        _pago.update {
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
        _pago.update {
            it.copy(idTarjeta = idTarjeta, usarTarjetaNueva = idTarjeta == null, errorTarjeta = null)
        }
    }

    fun onNumeroTarjetaCambia(valor: String) {
        _pago.update { it.copy(numeroTarjeta = valor.filter { c -> c.isDigit() }.take(19), errorNumeroTarjeta = null) }
    }

    fun onTitularTarjetaCambia(valor: String) {
        _pago.update { it.copy(titularTarjeta = valor.take(40), errorTitularTarjeta = null) }
    }

    fun onVencimientoTarjetaCambia(valor: String) {
        _pago.update { it.copy(vencimientoTarjeta = valor.filter { c -> c.isDigit() }.take(4), errorVencimientoTarjeta = null) }
    }

    fun onCvvCambia(valor: String) {
        _pago.update {
            it.copy(cvv = valor.filter { c -> c.isDigit() }.take(largoCvv(it.numeroTarjeta)), errorCvv = null)
        }
    }

    fun onGuardarTarjetaCambia(valor: Boolean) {
        _pago.update { it.copy(guardarTarjeta = valor) }
    }

    fun onPagar(bloqueo: String?, tarjetasGuardadas: List<Tarjeta>, hoy: LocalDate): SolicitudPago? {
        val estado = _pago.value
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

        val hayErrores = listOf(errorMedioPago, errorTarjeta, bloqueo).any { it != null } ||
            erroresNueva?.hayErrores == true

        _pago.update {
            it.copy(
                errorMedioPago = errorMedioPago,
                errorTarjeta = errorTarjeta,
                errorNumeroTarjeta = erroresNueva?.numero,
                errorTitularTarjeta = erroresNueva?.titular,
                errorVencimientoTarjeta = erroresNueva?.vencimiento,
                errorCvv = erroresNueva?.cvv,
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

    private val _tarjeta = MutableStateFlow(TarjetaUiState())
    val tarjeta: StateFlow<TarjetaUiState> = _tarjeta.asStateFlow()

    fun onMedioTarjetaSeleccionado(medio: MedioPago) {
        _tarjeta.update { it.copy(medioPago = medio, errorTarjeta = null) }
    }

    fun onNumeroTarjetaNuevaCambia(valor: String) {
        _tarjeta.update { it.copy(numero = valor.filter { c -> c.isDigit() }.take(19), errorNumero = null, errorTarjeta = null) }
    }

    fun onTitularTarjetaNuevaCambia(valor: String) {
        _tarjeta.update { it.copy(titular = valor.take(40), errorTitular = null) }
    }

    fun onVencimientoTarjetaNuevaCambia(valor: String) {
        _tarjeta.update { it.copy(vencimiento = valor.filter { c -> c.isDigit() }.take(4), errorVencimiento = null) }
    }

    fun onCvvTarjetaNuevaCambia(valor: String) {
        _tarjeta.update { it.copy(cvv = valor.filter { c -> c.isDigit() }.take(largoCvv(it.numero)), errorCvv = null) }
    }

    fun onTarjetaRepetida() {
        _tarjeta.update { it.copy(errorTarjeta = "Ya tienes guardada esta tarjeta") }
    }

    fun onAgregarTarjeta(hoy: LocalDate): TarjetaNueva? {
        val estado = _tarjeta.value
        val errores = validarTarjeta(estado.numero, estado.titular, estado.vencimiento, estado.cvv, hoy)
        _tarjeta.update {
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
