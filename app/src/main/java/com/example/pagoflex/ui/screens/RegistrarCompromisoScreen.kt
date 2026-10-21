package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.FormularioUiState
import com.example.pagoflex.model.NuevoCompromiso
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.FormularioViewModel
import java.time.LocalDate

@Composable
fun RegistrarCompromisoScreen(
    datos: BaseDatos,
    onRegistroExitoso: (NuevoCompromiso) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    rutInicial: String? = null,
    viewModel: FormularioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(rutInicial) {
        if (rutInicial != null) viewModel.onUsarRut(rutInicial, datos)
    }

    RegistrarCompromisoContent(
        uiState = uiState,
        empresas = datos.empresas.filter { it.activa },
        hoy = datos.fechaHoy,
        onEmpresaSeleccionada = viewModel::onEmpresaSeleccionada,
        onRutPersonaCambia = { viewModel.onRutPersonaCambia(it, datos) },
        onNombresPersonaCambia = viewModel::onNombresPersonaCambia,
        onApellidosPersonaCambia = viewModel::onApellidosPersonaCambia,
        onCorreoPersonaCambia = viewModel::onCorreoPersonaCambia,
        onTelefonoPersonaCambia = viewModel::onTelefonoPersonaCambia,
        onConceptoCambia = viewModel::onConceptoCambia,
        onMontoCambia = viewModel::onMontoCambia,
        onFechaVencimientoCambia = viewModel::onFechaVencimientoCambia,
        onNumeroCuotaCambia = viewModel::onNumeroCuotaCambia,
        onTotalCuotasCambia = viewModel::onTotalCuotasCambia,
        onRegistrar = { viewModel.onRegistrar(datos)?.let { nuevo -> onRegistroExitoso(nuevo) } },
        onLimpiar = viewModel::onLimpiar,
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegistrarCompromisoContent(
    uiState: FormularioUiState,
    empresas: List<EmpresaCliente>,
    hoy: LocalDate,
    onEmpresaSeleccionada: (EmpresaCliente) -> Unit,
    onRutPersonaCambia: (String) -> Unit,
    onNombresPersonaCambia: (String) -> Unit,
    onApellidosPersonaCambia: (String) -> Unit,
    onCorreoPersonaCambia: (String) -> Unit,
    onTelefonoPersonaCambia: (String) -> Unit,
    onConceptoCambia: (String) -> Unit,
    onMontoCambia: (String) -> Unit,
    onFechaVencimientoCambia: (String) -> Unit,
    onNumeroCuotaCambia: (String) -> Unit,
    onTotalCuotasCambia: (String) -> Unit,
    onRegistrar: () -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val montoEscrito = uiState.monto.toIntOrNull()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_registrar),
                subtitulo = uiState.empresa?.nombreFantasia,
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
                titulo = "Empresa que cobra",
                subtitulo = "El compromiso aparecerá con el nombre de esta empresa."
            ) {
                ListaDesplegable(
                    etiqueta = "Empresa *",
                    opciones = empresas,
                    seleccionada = uiState.empresa,
                    onSeleccionar = onEmpresaSeleccionada,
                    textoDe = { it.nombreFantasia },
                    icono = R.drawable.ic_empresa,
                    error = uiState.errorEmpresa
                )
            }

            TarjetaSeccion(
                titulo = "Persona que debe pagar",
                subtitulo = "Búscala por RUT. Si aún no está en PagoFlex, ingresa sus datos básicos."
            ) {
                CampoTexto(
                    valor = uiState.rutPersona,
                    onValorCambia = onRutPersonaCambia,
                    etiqueta = "RUT de la persona *",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorRutPersona,
                    ayuda = uiState.personaEncontrada?.let { "Persona encontrada: $it" } ?: "Ej: 12.476.318-5"
                )
                if (uiState.personaNoExiste) {
                    TarjetaMensaje(texto = "Esta persona aún no está en PagoFlex. Ingresa sus datos básicos.")
                    CampoTexto(
                        valor = uiState.nombresPersona,
                        onValorCambia = onNombresPersonaCambia,
                        etiqueta = "Nombres *",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorNombresPersona
                    )
                    CampoTexto(
                        valor = uiState.apellidosPersona,
                        onValorCambia = onApellidosPersonaCambia,
                        etiqueta = "Apellidos *",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorApellidosPersona
                    )
                    CampoTexto(
                        valor = uiState.correoPersona,
                        onValorCambia = onCorreoPersonaCambia,
                        etiqueta = "Correo *",
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreoPersona,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoTexto(
                        valor = uiState.telefonoPersona,
                        onValorCambia = onTelefonoPersonaCambia,
                        etiqueta = "Teléfono *",
                        icono = R.drawable.ic_telefono,
                        error = uiState.errorTelefonoPersona,
                        tipoTeclado = KeyboardType.Phone
                    )
                }
            }

            TarjetaSeccion(
                titulo = "Compromiso",
                subtitulo = "Queda Pendiente y la persona lo verá en su app."
            ) {
                CampoTexto(
                    valor = uiState.concepto,
                    onValorCambia = onConceptoCambia,
                    etiqueta = "Concepto *",
                    icono = R.drawable.ic_editar,
                    error = uiState.errorConcepto,
                    ayuda = "Ej: Cuota pie departamento"
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
                    ayuda = "No puede ser anterior a hoy (${formatoFecha(hoy)})"
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

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_registrar),
                    onClick = onRegistrar,
                    icono = R.drawable.ic_agregar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistrarVacioPreview() {
    PagoFlexTheme {
        RegistrarCompromisoContent(
            uiState = FormularioUiState(),
            empresas = BaseDatos().empresas,
            hoy = LocalDate.of(2026, 10, 6),
            onEmpresaSeleccionada = {}, onRutPersonaCambia = {}, onNombresPersonaCambia = {}, onApellidosPersonaCambia = {},
            onCorreoPersonaCambia = {}, onTelefonoPersonaCambia = {}, onConceptoCambia = {},
            onMontoCambia = {}, onFechaVencimientoCambia = {}, onNumeroCuotaCambia = {},
            onTotalCuotasCambia = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500, name = "Persona nueva con errores")
@Composable
private fun RegistrarConErroresPreview() {
    PagoFlexTheme(modoOscuro = false) {
        RegistrarCompromisoContent(
            uiState = FormularioUiState(
                rutPersona = "11.111.111-1",
                personaNoExiste = true,
                monto = "abc",
                errorNombresPersona = "Ingresa los nombres de la persona",
                errorCorreoPersona = "Ingresa un correo válido",
                errorConcepto = "El concepto es obligatorio",
                errorMonto = "El monto es obligatorio",
                errorFechaVencimiento = "Usa el formato DD-MM-AAAA, por ejemplo 30-11-2026",
                errorCuota = "La cuota no puede ser mayor que el total de cuotas"
            ),
            empresas = BaseDatos().empresas,
            hoy = LocalDate.of(2026, 10, 6),
            onEmpresaSeleccionada = {}, onRutPersonaCambia = {}, onNombresPersonaCambia = {}, onApellidosPersonaCambia = {},
            onCorreoPersonaCambia = {}, onTelefonoPersonaCambia = {}, onConceptoCambia = {},
            onMontoCambia = {}, onFechaVencimientoCambia = {}, onNumeroCuotaCambia = {},
            onTotalCuotasCambia = {}, onRegistrar = {}, onLimpiar = {}
        )
    }
}
