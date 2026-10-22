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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.ClaveRecuperada
import com.example.pagoflex.model.DatosRecuperacion
import com.example.pagoflex.model.RecuperarClaveUiState
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.RecuperarClaveViewModel

@Composable
fun RecuperarClaveScreen(
    onVerificar: (DatosRecuperacion) -> String?,
    onClaveValida: (ClaveRecuperada) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: RecuperarClaveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecuperarClaveContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onRutCambia = viewModel::onRutCambia,
        onClaveNuevaCambia = viewModel::onClaveNuevaCambia,
        onConfirmacionCambia = viewModel::onConfirmacionCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onVerificar = {
            viewModel.onVerificar()?.let { datos ->
                val error = onVerificar(datos)
                if (error == null) viewModel.onCuentaVerificada() else viewModel.onErrorRecuperacion(error)
            }
        },
        onGuardar = {
            viewModel.onGuardarClave()?.let { recuperada ->
                onClaveValida(recuperada)?.let { error -> viewModel.onErrorRecuperacion(error) }
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RecuperarClaveContent(
    uiState: RecuperarClaveUiState,
    onCorreoCambia: (String) -> Unit,
    onRutCambia: (String) -> Unit,
    onClaveNuevaCambia: (String) -> Unit,
    onConfirmacionCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onVerificar: () -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_recuperar_clave),
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
            TarjetaMensaje(
                texto = "Para cuidar tu cuenta primero confirmamos que eres tú con tu correo y tu RUT. Después eliges una contraseña nueva.",
                icono = R.drawable.ic_escudo
            )

            TarjetaSeccion(titulo = "1. Confirma que eres tú", subtitulo = "Usa el correo y el RUT con los que te registraste") {
                CampoTexto(
                    valor = uiState.correo,
                    onValorCambia = onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    ayuda = stringResource(R.string.ayuda_correo),
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.rut,
                    onValorCambia = onRutCambia,
                    etiqueta = "RUT",
                    icono = R.drawable.ic_persona,
                    error = uiState.errorRut,
                    ayuda = "Ej: 12.345.678-5"
                )
                if (uiState.cuentaVerificada) {
                    TarjetaMensaje(texto = "Listo, verificamos tu cuenta", exito = true)
                } else {
                    BotonPrincipal(
                        texto = "Verificar mis datos",
                        onClick = onVerificar,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState.cuentaVerificada) {
                TarjetaSeccion(titulo = "2. Crea tu nueva contraseña", subtitulo = "5 dígitos que no sean fáciles de adivinar") {
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
                    BotonPrincipal(
                        texto = "Guardar nueva contraseña",
                        onClick = onGuardar,
                        icono = R.drawable.ic_candado,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            uiState.errorRecuperacion?.let { TarjetaMensaje(texto = it) }

            BotonSecundario(
                texto = "Volver a iniciar sesión",
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarClavePreview() {
    PagoFlexTheme {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(correo = "crojas@correo.ejemplo"),
            onCorreoCambia = {}, onRutCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onVerificar = {}, onGuardar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cuenta verificada (modo claro)")
@Composable
private fun RecuperarClaveVerificadaPreview() {
    PagoFlexTheme(modoOscuro = false) {
        RecuperarClaveContent(
            uiState = RecuperarClaveUiState(
                correo = "crojas@correo.ejemplo", rut = "15.834.207-3", cuentaVerificada = true,
                claveNueva = "123", errorClaveNueva = "La contraseña debe tener 5 dígitos"
            ),
            onCorreoCambia = {}, onRutCambia = {}, onClaveNuevaCambia = {}, onConfirmacionCambia = {},
            onAlternarVisibilidad = {}, onVerificar = {}, onGuardar = {}
        )
    }
}
