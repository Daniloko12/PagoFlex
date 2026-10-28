package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.ReporteUiState
import com.example.pagoflex.model.SolicitudReporte
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.GrupoRadio
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.ReporteViewModel

@Composable
fun ReporteScreen(
    datos: BaseDatos,
    compromiso: Compromiso,
    motivoNoReportar: () -> String?,
    onReporteValido: (SolicitudReporte) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: ReporteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReporteContent(
        uiState = uiState,
        datos = datos,
        compromiso = compromiso,
        onMotivoSeleccionado = viewModel::onMotivoSeleccionado,
        onComentarioCambia = viewModel::onComentarioCambia,
        onEnviar = { viewModel.onEnviar(motivoNoReportar())?.let { solicitud -> onReporteValido(solicitud) } },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ReporteContent(
    uiState: ReporteUiState,
    datos: BaseDatos,
    compromiso: Compromiso,
    onMotivoSeleccionado: (MotivoReporte) -> Unit,
    onComentarioCambia: (String) -> Unit,
    onEnviar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val empresa = datos.empresaPorId(compromiso.idEmpresa)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_reportar),
                subtitulo = empresa?.nombreFantasia,
                icono = R.drawable.ic_volver,
                descripcionIcono = stringResource(R.string.cd_volver),
                onIconoClick = onVolver
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            TarjetaSeccion(
                titulo = "¿Qué pasó?",
                subtitulo = "${empresa?.nombreFantasia ?: ""}\n${conceptoConCuota(compromiso)}"
            ) {
                GrupoRadio(
                    opciones = MotivoReporte.entries,
                    seleccionada = uiState.motivo,
                    onSeleccionar = onMotivoSeleccionado,
                    textoDe = { it.etiqueta },
                    error = uiState.errorMotivo
                )
                CampoTexto(
                    valor = uiState.comentario,
                    onValorCambia = onComentarioCambia,
                    etiqueta = if (uiState.motivo == MotivoReporte.OTRO) "Comentario (obligatorio)" else "Comentario (opcional)",
                    icono = R.drawable.ic_editar,
                    error = uiState.errorComentario,
                    ayuda = "${uiState.comentario.length}/500",
                    unaLinea = false
                )
            }
            TarjetaMensaje(texto = "Mientras revisamos tu reporte, el compromiso queda \"En revisión\": no se puede pagar y no suma días de atraso.")
            uiState.errorReporte?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = stringResource(R.string.btn_enviar_reporte),
                onClick = onEnviar,
                icono = R.drawable.ic_reporte,
                modifier = Modifier.fillMaxWidth()
            )
            BotonSecundario(
                texto = stringResource(R.string.btn_cancelar),
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReportePreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        ReporteContent(
            uiState = ReporteUiState(motivo = MotivoReporte.OTRO, errorComentario = "Cuéntanos brevemente qué pasó (obligatorio si eliges \"Otro\")"),
            datos = datos,
            compromiso = datos.compromisoPorId(101) ?: datos.compromisos.first(),
            onMotivoSeleccionado = {}, onComentarioCambia = {}, onEnviar = {}
        )
    }
}
