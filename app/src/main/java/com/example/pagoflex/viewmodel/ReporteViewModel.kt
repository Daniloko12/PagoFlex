package com.example.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.ReporteUiState
import com.example.pagoflex.model.SolicitudReporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReporteViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReporteUiState())
    val uiState: StateFlow<ReporteUiState> = _uiState.asStateFlow()

    fun onMotivoSeleccionado(motivo: MotivoReporte) {
        _uiState.update { it.copy(motivo = motivo, errorMotivo = null, errorComentario = null) }
    }

    fun onComentarioCambia(valor: String) {
        _uiState.update { it.copy(comentario = valor, errorComentario = null) }
    }

    fun onEnviar(bloqueo: String?): SolicitudReporte? {
        val estado = _uiState.value
        val motivo = estado.motivo

        val errorMotivo = if (motivo == null) "Elige el motivo del problema" else null
        val errorComentario = when {
            motivo == MotivoReporte.OTRO && estado.comentario.isBlank() ->
                "Cuéntanos brevemente qué pasó (obligatorio si eliges \"Otro\")"
            estado.comentario.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
        val hayErrores = listOf(errorMotivo, errorComentario, bloqueo).any { it != null }

        _uiState.update {
            it.copy(
                errorMotivo = errorMotivo,
                errorComentario = errorComentario,
                errorReporte = bloqueo,
                reporteEnviado = !hayErrores
            )
        }
        if (hayErrores || motivo == null) return null
        return SolicitudReporte(motivo, estado.comentario.trim().ifBlank { null })
    }
}
