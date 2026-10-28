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
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.TarjetaNueva
import com.example.pagoflex.model.TarjetaUiState
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CamposTarjeta
import com.example.pagoflex.ui.components.SelectorSegmentado
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.TarjetaViewModel
import java.time.LocalDate

@Composable
fun AgregarTarjetaScreen(
    hoy: LocalDate,
    onTarjetaValida: (TarjetaNueva) -> Boolean,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: TarjetaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AgregarTarjetaContent(
        uiState = uiState,
        onMedioPagoSeleccionado = viewModel::onMedioPagoSeleccionado,
        onNumeroCambia = viewModel::onNumeroCambia,
        onTitularCambia = viewModel::onTitularCambia,
        onVencimientoCambia = viewModel::onVencimientoCambia,
        onCvvCambia = viewModel::onCvvCambia,
        onGuardar = {
            viewModel.onAgregar(hoy)?.let { nueva ->
                if (!onTarjetaValida(nueva)) viewModel.onTarjetaRepetida()
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AgregarTarjetaContent(
    uiState: TarjetaUiState,
    onMedioPagoSeleccionado: (MedioPago) -> Unit,
    onNumeroCambia: (String) -> Unit,
    onTitularCambia: (String) -> Unit,
    onVencimientoCambia: (String) -> Unit,
    onCvvCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_agregar_tarjeta),
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
                titulo = "Datos de la tarjeta",
                subtitulo = "Solo guardamos la marca y los 4 últimos números. Nunca el número completo ni el código de seguridad."
            ) {
                SelectorSegmentado(
                    opciones = listOf(MedioPago.DEBITO, MedioPago.CREDITO),
                    seleccionada = uiState.medioPago,
                    onSeleccionar = onMedioPagoSeleccionado,
                    textoDe = { if (it == MedioPago.CREDITO) "Crédito" else "Débito" }
                )
                CamposTarjeta(
                    numero = uiState.numero,
                    onNumeroCambia = onNumeroCambia,
                    titular = uiState.titular,
                    onTitularCambia = onTitularCambia,
                    vencimiento = uiState.vencimiento,
                    onVencimientoCambia = onVencimientoCambia,
                    cvv = uiState.cvv,
                    onCvvCambia = onCvvCambia,
                    errorNumero = uiState.errorNumero,
                    errorTitular = uiState.errorTitular,
                    errorVencimiento = uiState.errorVencimiento,
                    errorCvv = uiState.errorCvv
                )
            }
            uiState.errorTarjeta?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_tarjeta),
                onClick = onGuardar,
                icono = R.drawable.ic_check,
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
private fun AgregarTarjetaPreview() {
    PagoFlexTheme(modoOscuro = false) {
        AgregarTarjetaContent(
            uiState = TarjetaUiState(numero = "5555555555554444", titular = "Camila Rojas", vencimiento = "1129"),
            onMedioPagoSeleccionado = {}, onNumeroCambia = {}, onTitularCambia = {},
            onVencimientoCambia = {}, onCvvCambia = {}, onGuardar = {}
        )
    }
}
