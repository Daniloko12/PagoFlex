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
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.ClienteUiState
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.DatosCliente
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.InterruptorConTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.viewmodel.ClienteViewModel

@Composable
fun ClienteFormScreen(
    datos: BaseDatos,
    cliente: Usuario?,
    onClienteValido: (DatosCliente) -> Unit,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: ClienteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cliente?.idUsuario) {
        if (cliente != null) viewModel.onCargar(cliente, datos)
    }

    ClienteFormContent(
        uiState = uiState,
        comunas = datos.comunas,
        onRutCambia = viewModel::onRutCambia,
        onNombresCambia = viewModel::onNombresCambia,
        onApellidosCambia = viewModel::onApellidosCambia,
        onCorreoCambia = viewModel::onCorreoCambia,
        onTelefonoCambia = viewModel::onTelefonoCambia,
        onComunaSeleccionada = viewModel::onComunaSeleccionada,
        onActivoCambia = viewModel::onActivoCambia,
        onGuardar = { viewModel.onGuardar(datos)?.let { datosCliente -> onClienteValido(datosCliente) } },
        onVolver = onVolver,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ClienteFormContent(
    uiState: ClienteUiState,
    comunas: List<Comuna>,
    onRutCambia: (String) -> Unit,
    onNombresCambia: (String) -> Unit,
    onApellidosCambia: (String) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onTelefonoCambia: (String) -> Unit,
    onComunaSeleccionada: (Comuna) -> Unit,
    onActivoCambia: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val editando = uiState.idEditando != null

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(if (editando) R.string.titulo_editar_cliente else R.string.titulo_nuevo_cliente),
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
                titulo = "Datos del cliente",
                subtitulo = if (editando) "El RUT no se puede cambiar." else "Con su correo y la contraseña $CLAVE_DE_PRUEBA podrá ingresar a la app."
            ) {
                if (editando) {
                    TarjetaMensaje(texto = "RUT ${uiState.rut}", icono = R.drawable.ic_persona)
                } else {
                    CampoTexto(
                        valor = uiState.rut,
                        onValorCambia = onRutCambia,
                        etiqueta = "RUT *",
                        icono = R.drawable.ic_persona,
                        error = uiState.errorRut,
                        ayuda = "Ej: 12.345.678-5"
                    )
                }
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
            if (editando) {
                TarjetaSeccion(titulo = "Cuenta") {
                    InterruptorConTexto(
                        texto = "Cuenta activa",
                        descripcion = "Si la desactivas, no podrá iniciar sesión. Sus compromisos no cambian.",
                        activado = uiState.activo,
                        onCambio = onActivoCambia
                    )
                }
            }

            BotonPrincipal(
                texto = stringResource(if (editando) R.string.btn_guardar_cambios else R.string.btn_crear_cliente),
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
private fun ClienteNuevoPreview() {
    PagoFlexTheme {
        ClienteFormContent(
            uiState = ClienteUiState(errorRut = "El RUT no es válido: revisa el dígito verificador"),
            comunas = BaseDatos().comunas,
            onRutCambia = {}, onNombresCambia = {}, onApellidosCambia = {}, onCorreoCambia = {},
            onTelefonoCambia = {}, onComunaSeleccionada = {}, onActivoCambia = {}, onGuardar = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Editar (modo claro)")
@Composable
private fun ClienteEditarPreview() {
    PagoFlexTheme(modoOscuro = false) {
        ClienteFormContent(
            uiState = ClienteUiState(
                rut = "15.834.207-3", nombres = "Camila Andrea", apellidos = "Rojas Peña",
                correo = "crojas@correo.ejemplo", telefono = "+56 9 0000 0101",
                comuna = Comuna(1, "Maipú"), idEditando = 1
            ),
            comunas = BaseDatos().comunas,
            onRutCambia = {}, onNombresCambia = {}, onApellidosCambia = {}, onCorreoCambia = {},
            onTelefonoCambia = {}, onComunaSeleccionada = {}, onActivoCambia = {}, onGuardar = {}
        )
    }
}
