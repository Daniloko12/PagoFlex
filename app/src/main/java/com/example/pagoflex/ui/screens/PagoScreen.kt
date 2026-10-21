package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.PagoUiState
import com.example.pagoflex.model.SolicitudPago
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.formatoVencimiento
import com.example.pagoflex.model.tarjetasGuardadas
import com.example.pagoflex.model.textoTarjeta
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.CamposTarjeta
import com.example.pagoflex.ui.components.CasillaConTexto
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.GrupoRadio
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.PagoViewModel

@Composable
fun PagoScreen(
    datos: BaseDatos,
    compromiso: Compromiso,
    codigoCorrecto: String,
    motivoNoPagar: () -> String?,
    onPagoValido: (SolicitudPago) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: PagoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tarjetas = uiState.medioPago?.let { datos.tarjetasGuardadas(compromiso.idUsuarioFinal, it) } ?: emptyList()

    PagoContent(
        uiState = uiState,
        datos = datos,
        compromiso = compromiso,
        tarjetas = tarjetas,
        onMedioPagoSeleccionado = { medio ->
            viewModel.onMedioPagoSeleccionado(medio, datos.tarjetasGuardadas(compromiso.idUsuarioFinal, medio))
        },
        onTarjetaSeleccionada = viewModel::onTarjetaSeleccionada,
        onNumeroTarjetaCambia = viewModel::onNumeroTarjetaCambia,
        onTitularTarjetaCambia = viewModel::onTitularTarjetaCambia,
        onVencimientoTarjetaCambia = viewModel::onVencimientoTarjetaCambia,
        onCvvCambia = viewModel::onCvvCambia,
        onGuardarTarjetaCambia = viewModel::onGuardarTarjetaCambia,
        onCodigoCambia = viewModel::onCodigoCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onConfirmaTotalCambia = viewModel::onConfirmaTotalCambia,
        onPagar = {
            viewModel.onPagar(codigoCorrecto, motivoNoPagar(), tarjetas, datos.fechaHoy)
                ?.let { solicitud -> onPagoValido(solicitud) }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun PagoContent(
    uiState: PagoUiState,
    datos: BaseDatos,
    compromiso: Compromiso,
    tarjetas: List<Tarjeta>,
    onMedioPagoSeleccionado: (MedioPago) -> Unit,
    onTarjetaSeleccionada: (Int?) -> Unit,
    onNumeroTarjetaCambia: (String) -> Unit,
    onTitularTarjetaCambia: (String) -> Unit,
    onVencimientoTarjetaCambia: (String) -> Unit,
    onCvvCambia: (String) -> Unit,
    onGuardarTarjetaCambia: (Boolean) -> Unit,
    onCodigoCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onConfirmaTotalCambia: (Boolean) -> Unit,
    onPagar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val empresa = datos.empresaPorId(compromiso.idEmpresa)
    val total = formatoPesos(totalAPagar(compromiso))
    val conTarjeta = uiState.medioPago == MedioPago.DEBITO || uiState.medioPago == MedioPago.CREDITO
    val mostrarFormulario = conTarjeta && (uiState.usarTarjetaNueva || tarjetas.isEmpty())

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_pagar),
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
                titulo = "Confirma tu pago",
                subtitulo = "${empresa?.nombreFantasia ?: ""}\n${conceptoConCuota(compromiso)}"
            ) {
                FilaDato("Monto", formatoPesos(compromiso.monto))
                if (compromiso.estado == EstadoCompromiso.VENCIDO) {
                    FilaDato("Recargo por atraso", formatoPesos(compromiso.recargoAplicado))
                }
                HorizontalDivider()
                FilaDato("Total a pagar", total, destacado = true)
            }

            TarjetaSeccion(
                titulo = "¿Cómo quieres pagar?",
                subtitulo = "Pago simulado: no se cobra dinero real."
            ) {
                GrupoRadio(
                    opciones = MedioPago.entries.filter { it != MedioPago.SIMULADO },
                    seleccionada = uiState.medioPago,
                    onSeleccionar = onMedioPagoSeleccionado,
                    textoDe = { it.etiqueta },
                    error = uiState.errorMedioPago
                )
            }

            if (conTarjeta) {
                TarjetaSeccion(
                    titulo = if (uiState.medioPago == MedioPago.CREDITO) "Tarjeta de crédito" else "Tarjeta de débito",
                    subtitulo = "De tu tarjeta solo guardamos la marca y los 4 últimos números. Nunca el número completo ni el código de seguridad."
                ) {
                    if (tarjetas.isNotEmpty()) {
                        GrupoRadio(
                            opciones = tarjetas.map { it.idTarjeta } + listOf<Int?>(null),
                            seleccionada = if (uiState.usarTarjetaNueva) null else uiState.idTarjeta,
                            onSeleccionar = onTarjetaSeleccionada,
                            textoDe = { id ->
                                tarjetas.find { it.idTarjeta == id }
                                    ?.let { "${textoTarjeta(it)} · vence ${formatoVencimiento(it.vencimiento)}" }
                                    ?: "Usar otra tarjeta"
                            },
                            error = uiState.errorTarjeta
                        )
                    }
                    if (mostrarFormulario) {
                        if (tarjetas.isNotEmpty()) HorizontalDivider()
                        CamposTarjeta(
                            numero = uiState.numeroTarjeta,
                            onNumeroCambia = onNumeroTarjetaCambia,
                            titular = uiState.titularTarjeta,
                            onTitularCambia = onTitularTarjetaCambia,
                            vencimiento = uiState.vencimientoTarjeta,
                            onVencimientoCambia = onVencimientoTarjetaCambia,
                            cvv = uiState.cvv,
                            onCvvCambia = onCvvCambia,
                            errorNumero = uiState.errorNumeroTarjeta,
                            errorTitular = uiState.errorTitularTarjeta,
                            errorVencimiento = uiState.errorVencimientoTarjeta,
                            errorCvv = uiState.errorCvv
                        )
                        CasillaConTexto(
                            texto = "Guardar esta tarjeta en \"Mis tarjetas\"",
                            marcado = uiState.guardarTarjeta,
                            onCambio = onGuardarTarjetaCambia
                        )
                    }
                }
            }

            TarjetaSeccion(
                titulo = "Confirma que eres tú",
                subtitulo = "Así nadie más puede pagar desde tu teléfono."
            ) {
                CampoContrasena(
                    valor = uiState.codigo,
                    onValorCambia = onCodigoCambia,
                    etiqueta = "Código de confirmación",
                    visible = uiState.codigoVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorCodigo,
                    ayuda = "Son los 4 últimos números de tu RUT, antes del guion"
                )
                CasillaConTexto(
                    texto = "Revisé el total a pagar: $total",
                    marcado = uiState.confirmaTotal,
                    onCambio = onConfirmaTotalCambia,
                    error = uiState.errorConfirmacion
                )
            }

            uiState.errorPago?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = "Pagar $total",
                onClick = onPagar,
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
private fun PagoPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        PagoContent(
            uiState = PagoUiState(medioPago = MedioPago.DEBITO, idTarjeta = 1),
            datos = datos,
            compromiso = datos.compromisoPorId(101) ?: datos.compromisos.first(),
            tarjetas = datos.tarjetasGuardadas(1, MedioPago.DEBITO),
            onMedioPagoSeleccionado = {}, onTarjetaSeleccionada = {}, onNumeroTarjetaCambia = {},
            onTitularTarjetaCambia = {}, onVencimientoTarjetaCambia = {}, onCvvCambia = {},
            onGuardarTarjetaCambia = {}, onCodigoCambia = {}, onAlternarVisibilidad = {},
            onConfirmaTotalCambia = {}, onPagar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500, name = "Modo claro, tarjeta nueva con errores")
@Composable
private fun PagoConErroresPreview() {
    val datos = BaseDatos()
    PagoFlexTheme(modoOscuro = false) {
        PagoContent(
            uiState = PagoUiState(
                medioPago = MedioPago.CREDITO,
                usarTarjetaNueva = true,
                numeroTarjeta = "4111111111111112",
                vencimientoTarjeta = "0525",
                codigo = "1234",
                errorNumeroTarjeta = "Revisa el número: no corresponde a una tarjeta válida",
                errorTitularTarjeta = "Ingresa el nombre tal como aparece en la tarjeta",
                errorVencimientoTarjeta = "Esta tarjeta está vencida",
                errorCvv = "Ingresa el código de seguridad",
                errorCodigo = "El código no coincide. Son los 4 últimos números de tu RUT, antes del guion",
                errorConfirmacion = "Confirma que revisaste el total a pagar"
            ),
            datos = datos,
            compromiso = datos.compromisoPorId(101) ?: datos.compromisos.first(),
            tarjetas = datos.tarjetasGuardadas(1, MedioPago.CREDITO),
            onMedioPagoSeleccionado = {}, onTarjetaSeleccionada = {}, onNumeroTarjetaCambia = {},
            onTitularTarjetaCambia = {}, onVencimientoTarjetaCambia = {}, onCvvCambia = {},
            onGuardarTarjetaCambia = {}, onCodigoCambia = {}, onAlternarVisibilidad = {},
            onConfirmaTotalCambia = {}, onPagar = {}
        )
    }
}
