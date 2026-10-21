package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CambiosCompromiso
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EditarCompromisoUiState
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.motivoParaNoEditar
import com.example.pagoflex.model.parsearFecha
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditarCompromisoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditarCompromisoUiState())
    val uiState: StateFlow<EditarCompromisoUiState> = _uiState.asStateFlow()

    fun onCargar(compromiso: Compromiso) {
        if (_uiState.value.cargado) return
        _uiState.value = EditarCompromisoUiState(
            concepto = compromiso.concepto,
            monto = compromiso.monto.toString(),
            fechaVencimiento = formatoFecha(compromiso.fechaVencimiento),
            numeroCuota = compromiso.numeroCuota?.toString() ?: "",
            totalCuotas = compromiso.totalCuotas?.toString() ?: "",
            cargado = true
        )
    }

    fun onConceptoCambia(valor: String) {
        _uiState.update { it.copy(concepto = valor, errorConcepto = null, errorDuplicado = null) }
    }

    fun onMontoCambia(valor: String) {
        _uiState.update { it.copy(monto = valor.filter { c -> c.isDigit() }, errorMonto = null) }
    }

    fun onFechaVencimientoCambia(valor: String) {
        _uiState.update { it.copy(fechaVencimiento = valor, errorFechaVencimiento = null) }
    }

    fun onNumeroCuotaCambia(valor: String) {
        _uiState.update { it.copy(numeroCuota = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onTotalCuotasCambia(valor: String) {
        _uiState.update { it.copy(totalCuotas = valor.filter { c -> c.isDigit() }, errorCuota = null, errorDuplicado = null) }
    }

    fun onErrorEdicion(mensaje: String) {
        _uiState.update { it.copy(errorEdicion = mensaje) }
    }

    fun onGuardar(compromiso: Compromiso, datos: BaseDatos): CambiosCompromiso? {
        val estado = _uiState.value
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
        _uiState.update {
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
}
