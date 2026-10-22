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
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.EditarPerfilUiState
import com.example.pagoflex.model.MisDatos
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.EditarPerfilViewModel

@Composable
fun EditarPerfilScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onDatosValidos: (MisDatos) -> String?,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: EditarPerfilViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuario.idUsuario) { viewModel.onCargar(usuario, datos) }

    EditarPerfilContent(
        uiState = uiState,
        usuario = usuario,
        comunas = datos.comunas,
        onNombresCambia = viewModel::onNombresCambia,
        onApellidosCambia = viewModel::onApellidosCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onComunaSeleccionada = viewModel::onComunaSeleccionada,
        onGuardar = {
            viewModel.onGuardar(usuario, datos)?.let { misDatos ->
                onDatosValidos(misDatos)?.let { error -> viewModel.onErrorGuardar(error) }
            }
        },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun EditarPerfilContent(
    uiState: EditarPerfilUiState,
    usuario: Usuario,
    comunas: List<Comuna>,
    onNombresCambia: (String) -> Unit,
    onApellidosCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onComunaSeleccionada: (Comuna) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.titulo_editar_perfil),
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
            TarjetaSeccion(titulo = "Mis datos", subtitulo = "Mantén tus datos al día para recibir tus avisos") {
                TarjetaMensaje(texto = "RUT ${usuario.rut} (no se puede cambiar)", icono = R.drawable.ic_persona)
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
                    etiqueta = "Correo *",
                    icono = R.drawable.ic_correo,
                    error = uiState.errorCorreo,
                    ayuda = "Con este correo inicias sesión",
                    tipoTeclado = KeyboardType.Email
                )
                CampoTexto(
                    valor = uiState.telefono,
                    onValorCambia = onTelefonoCambia,
                    etiqueta = "Teléfono *",
                    icono = R.drawable.ic_telefono,
                    error = uiState.errorTelefono,
                    tipoTeclado = KeyboardType.Phone
                )
                if (usuario.rol == Rol.USUARIO_FINAL) {
                    ListaDesplegable(
                        etiqueta = "Comuna *",
                        opciones = comunas,
                        seleccionada = uiState.comuna,
                        onSeleccionar = onComunaSeleccionada,
                        textoDe = { it.nombre },
                        icono = R.drawable.ic_ubicacion,
                        error = uiState.errorComuna
                    )
                }
            }

            uiState.errorGuardar?.let { TarjetaMensaje(texto = it) }

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
private fun EditarPerfilPreview() {
    val datos = BaseDatos()
    val usuario = datos.usuarioPorId(1) ?: datos.usuarios.first()
    PagoFlexTheme {
        EditarPerfilContent(
            uiState = EditarPerfilUiState(
                nombres = usuario.nombres, apellidos = usuario.apellidos, correo = usuario.correo,
                telefono = usuario.telefono ?: "", comuna = datos.comunas.first(), idCargado = usuario.idUsuario
            ),
            usuario = usuario,
            comunas = datos.comunas,
            onNombresCambia = {}, onApellidosCambia = {}, onCorreoCambia = {}, onTelefonoCambia = {},
            onComunaSeleccionada = {}, onGuardar = {}
        )
    }
}
