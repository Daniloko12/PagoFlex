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
import com.example.pagoflex.model.CambiarClaveUiState
import com.example.pagoflex.model.CambioClave
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.CambiarClaveViewModel

@Composable
fun CambiarClaveScreen(
    onCambioValido: (CambioClave) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: CambiarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CambiarClaveContent(
        uiState = uiState,
        onClaveActualCambia = viewModel::onClaveActualCambia,
        onClaveNuevaCambia = viewModel::onClaveNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onGuardar = {
            viewModel.onGuardar()?.let { cambio ->
                onCambioValido(cambio)?.let { error -> viewModel.onErrorCambio(error) }
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun CambiarClaveContent(
    uiState: CambiarClaveUiState,
    onClaveActualCambia: (String) -> Unit,
    onClaveNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_cambiar_clave),
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
            TarjetaSeccion(titulo = "Tu contraseña", subtitulo = "Primero escribe la actual y después la nueva") {
                CampoContrasena(
                    valor = uiState.claveActual,
                    onValorCambia = onClaveActualCambia,
                    etiqueta = "Contraseña actual",
                    visible = uiState.claveVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorClaveActual
                )
                CampoContrasena(
                    valor = uiState.claveNueva,
                    onValorCambia = onClaveNuevaCambia,
                    etiqueta = "Nueva contraseña",
                    visible = uiState.claveVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorClaveNueva,
                    ayuda = "${uiState.claveNueva.length}/5 dígitos"
                )
                CampoContrasena(
                    valor = uiState.confirmacionClave,
                    onValorCambia = onConfirmacionCambia,
                    etiqueta = "Repite la nueva contraseña",
                    visible = uiState.claveVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorConfirmacion
                )
            }

            uiState.errorCambio?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = "Guardar contraseña",
                onClick = onGuardar,
                icono = R.drawable.ic_candado,
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
private fun CambiarClavePreview() {
    PagoFlexTheme(modoOscuro = false) {
        CambiarClaveContent(
            uiState = CambiarClaveUiState(claveActual = "12345", errorClaveNueva = "Es muy fácil de adivinar. Elige otra combinación"),
            onClaveActualCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onGuardar = {}
        )
    }
}
