package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CambiosCompromiso
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EditarCompromisoUiState
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.EditarCompromisoViewModel
import java.time.LocalDate

@Composable
fun EditarCompromisoScreen(
    datos: BaseDatos,
    compromiso: Compromiso,
    onCambiosValidos: (CambiosCompromiso) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: EditarCompromisoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(compromiso.idCompromiso) { viewModel.onCargar(compromiso) }

    EditarCompromisoContent(
        uiState = uiState,
        datos = datos,
        compromiso = compromiso,
        onConceptoCambia = viewModel::onConceptoCambia,
        onMontoCambia = viewModel::onMontoCambia,
        onFechaVencimientoCambia = viewModel::onFechaVencimientoCambia,
        onNumeroCuotaCambia = viewModel::onNumeroCuotaCambia,
        onTotalCuotasCambia = viewModel::onTotalCuotasCambia,
        onGuardar = {
            viewModel.onGuardar(compromiso, datos)?.let { cambios ->
                onCambiosValidos(cambios)?.let { error -> viewModel.onErrorEdicion(error) }
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun EditarCompromisoContent(
    uiState: EditarCompromisoUiState,
    datos: BaseDatos,
    compromiso: Compromiso,
    onConceptoCambia: (String) -> Unit,
    onMontoCambia: (String) -> Unit,
    onFechaVencimientoCambia: (String) -> Unit,
    onNumeroCuotaCambia: (String) -> Unit,
    onTotalCuotasCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)
    val empresa = datos.empresaPorId(compromiso.idEmpresa)
    val montoEscrito = uiState.monto.toIntOrNull()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_editar_compromiso),
                subtitulo = compromiso.folio,
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
            TarjetaSeccion(titulo = "Quién debe y a quién") {
                FilaDato("Persona", persona?.let { nombreCompleto(it) } ?: "")
                FilaDato("Empresa", empresa?.nombreFantasia ?: "")
                FilaDato("Estado actual", compromiso.estado.etiqueta)
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato("Recargo actual", formatoPesos(compromiso.recargoAplicado))
                }
            }

            TarjetaSeccion(
                titulo = "Datos del compromiso",
                subtitulo = "Si mueves el vencimiento a hoy o después, vuelve a Pendiente y sin recargo."
            ) {
                CampoTexto(
                    valor = uiState.concepto,
                    onValorCambia = onConceptoCambia,
                    etiqueta = "Concepto *",
                    icono = R.drawable.ic_editar,
                    error = uiState.errorConcepto
                )
                CampoTexto(
                    valor = uiState.monto,
                    onValorCambia = onMontoCambia,
                    etiqueta = "Monto en pesos *",
                    icono = R.drawable.ic_dinero,
                    error = uiState.errorMonto,
                    ayuda = montoEscrito?.let { formatoPesos(it) } ?: "Solo números, sin puntos",
                    tipoTeclado = KeyboardType.Number
                )
                CampoTexto(
                    valor = uiState.fechaVencimiento,
                    onValorCambia = onFechaVencimientoCambia,
                    etiqueta = "Vencimiento (DD-MM-AAAA) *",
                    icono = R.drawable.ic_calendario,
                    error = uiState.errorFechaVencimiento,
                    ayuda = "Hoy es ${formatoFecha(datos.fechaHoy)}"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    CampoTexto(
                        valor = uiState.numeroCuota,
                        onValorCambia = onNumeroCuotaCambia,
                        etiqueta = "Cuota N°",
                        icono = R.drawable.ic_numero,
                        ayuda = "Opcional",
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        valor = uiState.totalCuotas,
                        onValorCambia = onTotalCuotasCambia,
                        etiqueta = "Total cuotas",
                        icono = R.drawable.ic_numero,
                        ayuda = "Opcional",
                        tipoTeclado = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                uiState.errorCuota?.let { TarjetaMensaje(texto = it) }
            }

            uiState.errorDuplicado?.let { TarjetaMensaje(texto = it) }
            uiState.errorEdicion?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_cambios),
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
private fun EditarCompromisoPreview() {
    val datos = BaseDatos()
    val compromiso = datos.compromisoPorId(106) ?: datos.compromisos.first()
    PagoFlexTheme {
        EditarCompromisoContent(
            uiState = EditarCompromisoUiState(
                concepto = compromiso.concepto, monto = "300000", fechaVencimiento = "30-10-2026",
                numeroCuota = "14", totalCuotas = "36", cargado = true
            ),
            datos = datos.copy(fechaHoy = LocalDate.of(2026, 10, 6)),
            compromiso = compromiso,
            onConceptoCambia = {}, onMontoCambia = {}, onFechaVencimientoCambia = {},
            onNumeroCuotaCambia = {}, onTotalCuotasCambia = {}, onGuardar = {}
        )
    }
}
