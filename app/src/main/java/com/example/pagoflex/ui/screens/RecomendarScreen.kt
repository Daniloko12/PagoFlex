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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.NuevaRecomendacion
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.RecomendacionUiState
import com.example.pagoflex.model.Rubro
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.CasillaConTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.RecomendacionViewModel
import java.time.LocalDate

@Composable
fun RecomendarScreen(
    rubros: List<Rubro>,
    comunas: List<Comuna>,
    propias: List<Recomendacion>,
    hoy: LocalDate,
    onRecomendacionValida: (NuevaRecomendacion) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: RecomendacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecomendarContent(
        uiState = uiState,
        rubros = rubros,
        comunas = comunas,
        onNombreOrganizacionCambia = viewModel::onNombreOrganizacionCambia,
        onRutOrganizacionCambia = viewModel::onRutOrganizacionCambia,
        onRubroSeleccionado = viewModel::onRubroSeleccionado,
        onComunaSeleccionada = viewModel::onComunaSeleccionada,
        onNombreContactoCambia = viewModel::onNombreContactoCambia,
        onCorreoContactoCambia = viewModel::onCorreoContactoCambia,
        onTelefonoContactoCambia = viewModel::onTelefonoContactoCambia,
        onAutorizaContactoCambia = viewModel::onAutorizaContactoCambia,
        onEnviar = { viewModel.onEnviar(propias, hoy)?.let { nueva -> onRecomendacionValida(nueva) } },
        onLimpiar = viewModel::onLimpiar,
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RecomendarContent(
    uiState: RecomendacionUiState,
    rubros: List<Rubro>,
    comunas: List<Comuna>,
    onNombreOrganizacionCambia: (String) -> Unit,
    onRutOrganizacionCambia: (String) -> Unit,
    onRubroSeleccionado: (Rubro) -> Unit,
    onComunaSeleccionada: (Comuna) -> Unit,
    onNombreContactoCambia: (String) -> Unit,
    onCorreoContactoCambia: (String) -> Unit,
    onTelefonoContactoCambia: (String) -> Unit,
    onAutorizaContactoCambia: (Boolean) -> Unit,
    onEnviar: () -> Unit,
    onLimpiar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_recomendar),
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
                titulo = "La organización",
                subtitulo = "¿Le pagas a un jardín, club o comunidad por transferencia? Cuéntanos cuál."
            ) {
                CampoTexto(
                    valor = uiState.nombreOrganizacion,
                    onValorCambia = onNombreOrganizacionCambia,
                    etiqueta = "Nombre de la organización *",
                    icono = R.drawable.ic_empresa,
                    error = uiState.errorNombreOrganizacion
                )
                ListaDesplegable(
                    etiqueta = "Rubro *",
                    opciones = rubros,
                    seleccionada = uiState.rubro,
                    onSeleccionar = onRubroSeleccionado,
                    textoDe = { it.nombre },
                    icono = R.drawable.ic_lista,
                    error = uiState.errorRubro
                )
                ListaDesplegable(
                    etiqueta = "Comuna *",
                    opciones = comunas,
                    seleccionada = uiState.comuna,
                    onSeleccionar = onComunaSeleccionada,
                    textoDe = { it.nombre },
                    icono = R.drawable.ic_ubicacion,
                    error = uiState.errorComuna
                )
                CampoTexto(
                    valor = uiState.rutOrganizacion,
                    onValorCambia = onRutOrganizacionCambia,
                    etiqueta = "RUT de la organización (opcional)",
                    icono = R.drawable.ic_numero,
                    error = uiState.errorRutOrganizacion,
                    ayuda = "Si lo sabes. Ej: 76.845.213-K"
                )
            }

            TarjetaSeccion(
                titulo = "Persona de contacto (opcional)",
                subtitulo = "Solo si esa persona aceptó que PagoFlex la contacte."
            ) {
                CampoTexto(
                    valor = uiState.nombreContacto,
                    onValorCambia = onNombreContactoCambia,
                    etiqueta = "Nombre del contacto",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorNombreContacto
                )
                CampoTexto(
                    valor = uiState.correoContacto,
                    onValorCambia = onCorreoContactoCambia,
                    etiqueta = "Correo del contacto",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreoContacto,
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.telefonoContacto,
                    onValorCambia = onTelefonoContactoCambia,
                    etiqueta = "Teléfono del contacto",
                    icono = R.drawable.ic_telefono,
                    tipoTeclado = KeyboardType.Phone
                )
                CasillaConTexto(
                    texto = "Esta persona autorizó que PagoFlex la contacte",
                    marcado = uiState.autorizaContacto,
                    onCambio = onAutorizaContactoCambia,
                    error = uiState.errorAutorizaContacto
                )
            }

            uiState.errorRecomendacion?.let { TarjetaMensaje(texto = it) }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)) {
                BotonSecundario(
                    texto = stringResource(R.string.btn_limpiar),
                    onClick = onLimpiar,
                    modifier = Modifier.weight(1f)
                )
                BotonPrincipal(
                    texto = stringResource(R.string.btn_recomendar),
                    onClick = onEnviar,
                    icono = R.drawable.ic_check,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecomendarVacioPreview() {
    PagoFlexTheme {
        RecomendarContent(
            uiState = RecomendacionUiState(),
            rubros = DatosAnexo9.rubros, comunas = DatosAnexo9.comunas,
            onNombreOrganizacionCambia = {}, onRutOrganizacionCambia = {}, onRubroSeleccionado = {},
            onComunaSeleccionada = {}, onNombreContactoCambia = {}, onCorreoContactoCambia = {},
            onTelefonoContactoCambia = {}, onAutorizaContactoCambia = {}, onEnviar = {}, onLimpiar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "Con errores")
@Composable
private fun RecomendarConErroresPreview() {
    PagoFlexTheme {
        RecomendarContent(
            uiState = RecomendacionUiState(
                nombreContacto = "Ana",
                errorNombreOrganizacion = "El nombre de la organización es obligatorio",
                errorRubro = "Elige el rubro",
                errorComuna = "Elige la comuna",
                errorAutorizaContacto = "Para guardar datos de contacto, confirma que esa persona autorizó que PagoFlex la contacte"
            ),
            rubros = DatosAnexo9.rubros, comunas = DatosAnexo9.comunas,
            onNombreOrganizacionCambia = {}, onRutOrganizacionCambia = {}, onRubroSeleccionado = {},
            onComunaSeleccionada = {}, onNombreContactoCambia = {}, onCorreoContactoCambia = {},
            onTelefonoContactoCambia = {}, onAutorizaContactoCambia = {}, onEnviar = {}, onLimpiar = {}
        )
    }
}
