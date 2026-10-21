package com.example.pagoflex.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.ui.components.AvatarCircular
import com.example.pagoflex.ui.components.BarraSuperior
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra
import kotlinx.coroutines.launch

data class OpcionMenu(
    val ruta: String,
    @StringRes val titulo: Int,
    @DrawableRes val icono: Int
)

data class MenuSesion(
    val usuario: Usuario? = null,
    val detalle: String = "",
    val opciones: List<OpcionMenu> = emptyList(),
    val rutaActual: String = "",
    val onIrA: (String) -> Unit = {},
    val onCerrarSesion: () -> Unit = {},
    val modoOscuro: Boolean = true,
    val onCambiarModo: (Boolean) -> Unit = {}
)

@Composable
fun PantallaPrincipal(
    titulo: String,
    menu: MenuSesion,
    snackbarHostState: SnackbarHostState,
    botonFlotante: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
            title = { Text(stringResource(R.string.dialogo_titulo)) },
            text = { Text(stringResource(R.string.dialogo_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    menu.onCerrarSesion()
                }) { Text(stringResource(R.string.menu_cerrar_sesion)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
                EncabezadoMenu(menu)
                Spacer(Modifier.height(Dimens.espacioChico))

                menu.opciones.forEach { opcion ->
                    NavigationDrawerItem(
                        icon = { Icon(painterResource(opcion.icono), contentDescription = null) },
                        label = { Text(stringResource(opcion.titulo)) },
                        selected = opcion.ruta == menu.rutaActual,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (opcion.ruta != menu.rutaActual) menu.onIrA(opcion.ruta)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.espacioChico))
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.ic_luna), contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_modo_oscuro)) },
                    selected = false,
                    onClick = { menu.onCambiarModo(!menu.modoOscuro) },
                    badge = { Switch(checked = menu.modoOscuro, onCheckedChange = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
                    label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogo = true
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                BarraSuperior(
                    titulo = titulo,
                    icono = R.drawable.ic_menu,
                    descripcionIcono = stringResource(R.string.cd_menu),
                    onIconoClick = { scope.launch { drawerState.open() } },
                    acciones = { BotonModo(modoOscuro = menu.modoOscuro, onCambiarModo = menu.onCambiarModo) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = botonFlotante,
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            contenido(innerPadding)
        }
    }
}

@Composable
private fun EncabezadoMenu(menu: MenuSesion) {
    val colores = MaterialTheme.coloresExtra
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colores.encabezadoMenu))
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
                .padding(Dimens.espacioGrande)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_pagoflex),
                    contentDescription = stringResource(R.string.cd_logo),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = colores.textoEncabezado,
                    modifier = Modifier.padding(start = Dimens.espacioChico)
                )
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
            AvatarCircular(
                imagen = R.drawable.avatar_usuario,
                descripcion = stringResource(R.string.cd_avatar)
            )
            Spacer(Modifier.height(Dimens.espacioChico))
            Text(
                text = if (menu.usuario != null) stringResource(R.string.saludo, primerNombre(menu.usuario))
                else stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = colores.textoEncabezado
            )
            if (menu.detalle.isNotBlank()) {
                Text(
                    text = menu.detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.textoEncabezadoSuave
                )
            }
        }
    }
}

@Composable
fun BotonModo(
    modoOscuro: Boolean,
    onCambiarModo: (Boolean) -> Unit,
    color: Color = LocalContentColor.current
) {
    IconButton(onClick = { onCambiarModo(!modoOscuro) }) {
        Icon(
            painter = painterResource(if (modoOscuro) R.drawable.ic_sol else R.drawable.ic_luna),
            contentDescription = stringResource(if (modoOscuro) R.string.cd_modo_claro else R.string.cd_modo_oscuro),
            tint = color
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaPrincipalPreview() {
    PagoFlexTheme(modoOscuro = false) {
        PantallaPrincipal(
            titulo = "Hola, Camila",
            menu = MenuSesion(usuario = DatosAnexo9.usuarios.first(), detalle = "Cliente PagoFlex desde 2024", modoOscuro = false),
            snackbarHostState = remember { SnackbarHostState() }
        ) { innerPadding ->
            LazyColumn(contentPadding = innerPadding) {
                items(listOf("Así se ve una pantalla principal: barra con ☰ y el contenido debajo.")) {
                    TarjetaMensaje(texto = it, modifier = Modifier.padding(Dimens.espacioPantalla))
                }
            }
        }
    }
}
