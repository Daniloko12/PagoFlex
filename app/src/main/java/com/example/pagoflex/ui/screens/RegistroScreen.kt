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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.NuevaCuenta
import com.example.pagoflex.model.RegistroUiState
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.CasillaConTexto
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    datos: BaseDatos,
    onCuentaValida: (NuevaCuenta) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: RegistroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RegistroContent(
        uiState = uiState,
        onRutCambia = viewModel::onRutCambia,
        onNombresCambia = viewModel::onNombresCambia,
        onApellidosCambia = viewModel::onApellidosCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onClaveCambia = viewModel::onClaveCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onAceptaTerminosCambia = viewModel::onAceptaTerminosCambia,
        onCrearCuenta = {
            viewModel.onCrearCuenta(datos)?.let { nueva ->
                onCuentaValida(nueva)?.let { error -> viewModel.onErrorRegistro(error) }
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegistroContent(
    uiState: RegistroUiState,
    onRutCambia: (String) -> Unit,
    onNombresCambia: (String) -> Unit,
    onApellidosCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onAceptaTerminosCambia: (Boolean) -> Unit,
    onCrearCuenta: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_registro),
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
            Text(
                text = "Crea tu cuenta para ver y pagar todos tus compromisos en un solo lugar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TarjetaSeccion(titulo = "Tus datos") {
                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = onRutCambia,
                    etiqueta = "RUT *",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorRut,
                    ayuda = "Ej: 12.345.678-5"
                )
                CampoTexto(
                    valor = uiState.nombres,
                    onValorCambia = onNombresCambia,
                    etiqueta = "Nombres *",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorNombres
                )
                CampoTexto(
                    valor = uiState.apellidos,
                    onValorCambia = onApellidosCambia,
                    etiqueta = "Apellidos *",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorApellidos
                )
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = "Correo electrónico *",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    ayuda = "Con este correo vas a iniciar sesión",
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.telefono,
                    onValorCambia = onTelefonoCambia,
                    etiqueta = "Teléfono *",
                    icono = R.drawable.ic_telefono,
                    error = uiState.errorTelefono,
                    ayuda = "Ej: +56 9 1234 5678",
                    tipoTeclado = KeyboardType.Phone
                )
            }

            TarjetaSeccion(
                titulo = "Tu contraseña",
                subtitulo = "5 números que solo tú conozcas. La guardamos cifrada: ni PagoFlex puede leerla."
            ) {
                CampoContrasena(
                    valor = uiState.clave,
                    onValorCambia = onClaveCambia,
                    etiqueta = "Contraseña *",
                    visible = uiState.claveVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorClave,
                    ayuda = "${uiState.clave.length}/5 dígitos"
                )
                CampoContrasena(
                    valor = uiState.confirmacionClave,
                    onValorCambia = onConfirmacionCambia,
                    etiqueta = "Repite la contraseña *",
                    visible = uiState.claveVisible,
                    onAlternarVisible = onAlternarVisibilidad,
                    error = uiState.errorConfirmacion
                )
            }

            CasillaConTexto(
                texto = "Acepto los términos de uso y que PagoFlex me envíe avisos de mis vencimientos",
                marcado = uiState.aceptaTerminos,
                onCambio = onAceptaTerminosCambia,
                error = uiState.errorTerminos
            )

            uiState.errorRegistro?.let { TarjetaMensaje(texto = it) }

            BotonPrincipal(
                texto = stringResource(R.string.btn_crear_cuenta),
                onClick = onCrearCuenta,
                icono = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.btn_ya_tengo_cuenta))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegistroPreview() {
    PagoFlexTheme {
        RegistroContent(
            uiState = RegistroUiState(),
            onRutCambia = {}, onNombresCambia = {}, onApellidosCambia = {}, onCorreoCambia = {},
            onTelefonoCambia = {}, onClaveCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onAceptaTerminosCambia = {}, onCrearCuenta = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "Modo claro con errores")
@Composable
private fun RegistroConErroresPreview() {
    PagoFlexTheme(modoOscuro = false) {
        RegistroContent(
            uiState = RegistroUiState(
                rut = "15.834.207-3",
                correo = "camila@",
                clave = "11111",
                confirmacionClave = "1111",
                errorRut = "Ya existe una cuenta con ese RUT",
                errorNombres = "Ingresa los nombres",
                errorCorreo = "Ingresa un correo válido, por ejemplo nombre@gmail.com",
                errorClave = "Es muy fácil de adivinar. Elige otra combinación",
                errorConfirmacion = "Las contraseñas no coinciden",
                errorTerminos = "Debes aceptar los términos para crear tu cuenta"
            ),
            onRutCambia = {}, onNombresCambia = {}, onApellidosCambia = {}, onCorreoCambia = {},
            onTelefonoCambia = {}, onClaveCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onAceptaTerminosCambia = {}, onCrearCuenta = {}
        )
    }
}
