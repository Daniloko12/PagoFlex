package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.ContactoNuevo
import com.example.pagoflex.model.NuevaRecomendacion
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.RecomendacionUiState
import com.example.pagoflex.model.Rubro
import com.example.pagoflex.model.correoValido
import com.example.pagoflex.model.normalizarRut
import com.example.pagoflex.model.validarRut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class RecomendacionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecomendacionUiState())
    val uiState: StateFlow<RecomendacionUiState> = _uiState.asStateFlow()

    fun onNombreOrganizacionCambia(valor: String) {
        _uiState.update { it.copy(nombreOrganizacion = valor, errorNombreOrganizacion = null, errorRecomendacion = null) }
    }

    fun onRutOrganizacionCambia(valor: String) {
        _uiState.update { it.copy(rutOrganizacion = valor, errorRutOrganizacion = null) }
    }

    fun onRubroSeleccionado(rubro: Rubro) {
        _uiState.update { it.copy(rubro = rubro, errorRubro = null) }
    }

    fun onComunaSeleccionada(comuna: Comuna) {
        _uiState.update { it.copy(comuna = comuna, errorComuna = null) }
    }

    fun onNombreContactoCambia(valor: String) {
        _uiState.update { it.copy(nombreContacto = valor, errorNombreContacto = null, errorAutorizaContacto = null) }
    }

    fun onCorreoContactoCambia(valor: String) {
        _uiState.update { it.copy(correoContacto = valor.trim(), errorCorreoContacto = null, errorAutorizaContacto = null) }
    }

    fun onTelefonoContactoCambia(valor: String) {
        _uiState.update { it.copy(telefonoContacto = valor, errorAutorizaContacto = null) }
    }

    fun onAutorizaContactoCambia(valor: Boolean) {
        _uiState.update { it.copy(autorizaContacto = valor, errorAutorizaContacto = null) }
    }

    fun onEnviar(propias: List<Recomendacion>, hoy: LocalDate): NuevaRecomendacion? {
        val estado = _uiState.value
        val nombre = estado.nombreOrganizacion.trim()
        val rubro = estado.rubro
        val comuna = estado.comuna
        val hayContacto = estado.nombreContacto.isNotBlank() || estado.correoContacto.isNotBlank() ||
            estado.telefonoContacto.isNotBlank()

        val errorNombre = when {
            nombre.isBlank() -> "El nombre de la organización es obligatorio"
            nombre.length > 120 -> "Máximo 120 caracteres"
            else -> null
        }
        val errorRut = if (estado.rutOrganizacion.isNotBlank() && !validarRut(estado.rutOrganizacion))
            "El RUT no es válido: revisa el dígito verificador" else null
        val errorRubro = if (rubro == null) "Elige el rubro" else null
        val errorComuna = if (comuna == null) "Elige la comuna" else null
        val errorAutoriza = if (hayContacto && !estado.autorizaContacto)
            "Para guardar datos de contacto, confirma que esa persona autorizó que PagoFlex la contacte" else null
        val errorNombreContacto = if (hayContacto && estado.autorizaContacto && estado.nombreContacto.isBlank())
            "Ingresa el nombre de la persona de contacto" else null
        val errorCorreoContacto = if (estado.correoContacto.isNotBlank() && !correoValido(estado.correoContacto))
            "Ingresa un correo válido" else null
        val existente = propias.find { it.nombreOrganizacion.trim().equals(nombre, ignoreCase = true) }
        val delMes = propias.count { YearMonth.from(it.fecha) == YearMonth.from(hoy) }
        val errorRecomendacion = when {
            existente != null -> "Ya recomendaste a ${existente.nombreOrganizacion}. Su estado es: ${existente.estado.etiqueta}."
            delMes >= 5 -> "Puedes registrar hasta 5 recomendaciones por mes. ¡Gracias por tu entusiasmo!"
            else -> null
        }

        val hayErrores = listOf(
            errorNombre, errorRut, errorRubro, errorComuna, errorAutoriza,
            errorNombreContacto, errorCorreoContacto, errorRecomendacion
        ).any { it != null }

        _uiState.update {
            it.copy(
                errorNombreOrganizacion = errorNombre,
                errorRutOrganizacion = errorRut,
                errorRubro = errorRubro,
                errorComuna = errorComuna,
                errorAutorizaContacto = errorAutoriza,
                errorNombreContacto = errorNombreContacto,
                errorCorreoContacto = errorCorreoContacto,
                errorRecomendacion = errorRecomendacion,
                recomendacionEnviada = !hayErrores
            )
        }
        if (hayErrores || rubro == null || comuna == null) return null

        return NuevaRecomendacion(
            nombreOrganizacion = nombre,
            rutOrganizacion = if (estado.rutOrganizacion.isBlank()) null else normalizarRut(estado.rutOrganizacion),
            idRubro = rubro.idRubro,
            idComuna = comuna.idComuna,
            contacto = if (hayContacto) ContactoNuevo(
                nombre = estado.nombreContacto.trim(),
                correo = estado.correoContacto.trim().ifBlank { null },
                telefono = estado.telefonoContacto.trim().ifBlank { null }
            ) else null
        )
    }

    fun onLimpiar() {
        _uiState.value = RecomendacionUiState()
    }
}
