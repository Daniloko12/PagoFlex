package com.example.pagoflex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.Credenciales
import com.example.pagoflex.model.IngresoUiState
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.usuariosDelRol
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.CampoContrasena
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.OpcionCuenta
import com.example.pagoflex.ui.components.SelectorSegmentado
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra
import com.example.pagoflex.viewmodel.IngresoViewModel

@Composable
fun IngresoScreen(
    datos: BaseDatos,
    onIngresar: (Credenciales) -> String?,
    onRestablecer: () -> Unit,
    onCrearCuenta: () -> Unit,
    modoOscuro: Boolean,
    onCambiarModo: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: IngresoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    IngresoContent(
        uiState = uiState,
        cuentasDePrueba = datos.usuariosDelRol(uiState.rol),
        onRolSeleccionado = viewModel::onRolSeleccionado,
        onCorreoCambia = viewModel::onCorreoCambia,
        onClaveCambia = viewModel::onClaveCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = {
            viewModel.onIngresar()?.let { credenciales ->
                onIngresar(credenciales)?.let { error -> viewModel.onErrorIngreso(error) }
            }
        },
        onUsarCuentaDePrueba = { usuario -> viewModel.onUsarCuentaDePrueba(usuario.correo, usuario.rol) },
        onRestablecer = onRestablecer,
        onCrearCuenta = onCrearCuenta,
        modoOscuro = modoOscuro,
        onCambiarModo = onCambiarModo,
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun IngresoContent(
    uiState: IngresoUiState,
    cuentasDePrueba: List<Usuario>,
    onRolSeleccionado: (Rol) -> Unit,
    onCorreoCambia: (String) -> Unit,
    onClaveCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onUsarCuentaDePrueba: (Usuario) -> Unit = {},
    onRestablecer: () -> Unit = {},
    onCrearCuenta: () -> Unit = {},
    modoOscuro: Boolean = true,
    onCambiarModo: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var verCuentas by remember { mutableStateOf(false) }
    var confirmarRestablecer by remember { mutableStateOf(false) }

    if (verCuentas) {
        AlertDialog(
            onDismissRequest = { verCuentas = false },
            title = { Text(stringResource(R.string.titulo_cuentas_prueba)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(R.string.texto_cuentas_prueba, CLAVE_DE_PRUEBA),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Dimens.espacioChico)
                    )
                    cuentasDePrueba.forEachIndexed { indice, usuario ->
                        if (indice > 0) HorizontalDivider()
                        OpcionCuenta(
                            nombre = nombreCorto(usuario),
                            detalle = usuario.correo,
                            iniciales = inicialesDe(usuario),
                            seleccionada = usuario.correo == uiState.correo,
                            onClick = {
                                onUsarCuentaDePrueba(usuario)
                                verCuentas = false
                            },
                            colorAvatar = colorAvatarDe(usuario.idUsuario)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { verCuentas = false }) { Text(stringResource(R.string.btn_cerrar)) }
            }
        )
    }

    if (confirmarRestablecer) {
        AlertDialog(
            onDismissRequest = { confirmarRestablecer = false },
            icon = { Icon(painterResource(R.drawable.ic_actualizar), contentDescription = null) },
            title = { Text(stringResource(R.string.dialogo_restablecer_titulo)) },
            text = { Text(stringResource(R.string.dialogo_restablecer_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRestablecer = false
                    onRestablecer()
                }) { Text(stringResource(R.string.btn_restablecer)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestablecer = false }) { Text(stringResource(R.string.dialogo_cancelar)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            EncabezadoMarca(modoOscuro = modoOscuro, onCambiarModo = onCambiarModo)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioGrande),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    Text(text = stringResource(R.string.titulo_ingreso), style = MaterialTheme.typography.titleLarge)

                    SelectorSegmentado(
                        opciones = Rol.entries,
                        seleccionada = uiState.rol,
                        onSeleccionar = onRolSeleccionado,
                        textoDe = { nombreAcceso(it) }
                    )
                    Column {
                        Text(
                            text = uiState.rol.etiqueta,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.rol.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.radioTarjeta),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(Dimens.bordeEtiqueta, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.espacioMedio),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                        ) {
                            CampoTexto(
                                valor = uiState.correo,
                                onValorCambia = onCorreoCambia,
                                etiqueta = stringResource(R.string.campo_correo),
                                icono = R.drawable.ic_correo,
                                error = uiState.errorCorreo,
                                ayuda = stringResource(R.string.ayuda_correo),
                                tipoTeclado = KeyboardType.Email
                            )
                            CampoContrasena(
                                valor = uiState.clave,
                                onValorCambia = onClaveCambia,
                                etiqueta = stringResource(R.string.campo_clave),
                                visible = uiState.claveVisible,
                                onAlternarVisible = onAlternarVisibilidad,
                                error = uiState.errorClave,
                                ayuda = stringResource(R.string.ayuda_clave)
                            )
                        }
                    }

                    uiState.errorIngreso?.let { TarjetaMensaje(texto = it) }

                    BotonPrincipal(
                        texto = stringResource(R.string.btn_ingresar),
                        onClick = onIngresar,
                        icono = R.drawable.ic_candado,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.rol == Rol.USUARIO_FINAL) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.texto_sin_cuenta),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onCrearCuenta) {
                                Text(stringResource(R.string.btn_crear_cuenta), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    TextButton(
                        onClick = { verCuentas = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(painterResource(R.drawable.ic_persona), contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.btn_cuentas_prueba),
                            modifier = Modifier.padding(start = Dimens.espacioMini)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_escudo),
                            contentDescription = null,
                            tint = MaterialTheme.coloresExtra.exito,
                            modifier = Modifier.size(Dimens.tamanoIcono)
                        )
                        Text(
                            text = stringResource(R.string.aviso_confianza),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.aviso_demo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { confirmarRestablecer = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { Text(stringResource(R.string.btn_restablecer_datos)) }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoMarca(modoOscuro: Boolean, onCambiarModo: (Boolean) -> Unit) {
    val colores = MaterialTheme.coloresExtra
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colores.encabezado))
    ) {
        Image(
            painter = painterResource(R.drawable.fondo_marca),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = Dimens.espacioGrande, end = Dimens.espacioGrande, top = Dimens.espacioGrande, bottom = 64.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_pagoflex),
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = colores.textoEncabezado,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                BotonModo(modoOscuro = modoOscuro, onCambiarModo = onCambiarModo, color = colores.textoEncabezado)
            }
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.marca_titular),
                style = MaterialTheme.typography.headlineMedium,
                color = colores.textoEncabezado
            )
            Spacer(Modifier.height(Dimens.espacioChico))
            Text(
                text = stringResource(R.string.marca_bajada),
                style = MaterialTheme.typography.bodyLarge,
                color = colores.textoEncabezadoSuave
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun IngresoPreview() {
    PagoFlexTheme {
        IngresoContent(
            uiState = IngresoUiState(),
            cuentasDePrueba = BaseDatos().usuariosDelRol(Rol.USUARIO_FINAL),
            onRolSeleccionado = {}, onCorreoCambia = {}, onClaveCambia = {},
            onAlternarVisibilidad = {}, onIngresar = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "Modo claro con errores")
@Composable
private fun IngresoConErroresPreview() {
    PagoFlexTheme(modoOscuro = false) {
        IngresoContent(
            uiState = IngresoUiState(
                rol = Rol.ADMINISTRADOR,
                correo = "paula@",
                clave = "123",
                errorCorreo = "Ingresa un correo válido, por ejemplo nombre@gmail.com",
                errorClave = "La contraseña tiene 5 dígitos"
            ),
            cuentasDePrueba = BaseDatos().usuariosDelRol(Rol.ADMINISTRADOR),
            onRolSeleccionado = {}, onCorreoCambia = {}, onClaveCambia = {},
            onAlternarVisibilidad = {}, onIngresar = {}, modoOscuro = false
        )
    }
}
