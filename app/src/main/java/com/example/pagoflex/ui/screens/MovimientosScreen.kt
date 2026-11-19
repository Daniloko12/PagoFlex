package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.GrupoMovimiento
import com.example.pagoflex.model.movimientosFiltrados
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.FilaMovimiento
import com.example.pagoflex.ui.components.FiltroGrupos
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun MovimientosScreen(
    datos: BaseDatos,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var texto by rememberSaveable { mutableStateOf("") }
    var grupo by rememberSaveable { mutableStateOf<GrupoMovimiento?>(null) }
    val movimientos = datos.movimientosFiltrados(grupo, texto)

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_movimientos),
        menu = menu,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item {
                CampoTexto(
                    valor = texto,
                    onValorCambia = { texto = it },
                    etiqueta = "Buscar por cliente o detalle",
                    icono = R.drawable.ic_buscar
                )
            }
            item { FiltroGrupos(seleccionado = grupo, onSeleccionar = { grupo = it }) }
            item {
                TituloLista(
                    texto = "${movimientos.size} movimientos",
                    subtitulo = "Registro de auditoría: qué cambió, cuándo y quién lo hizo. No se puede editar ni borrar."
                )
            }
            if (movimientos.isEmpty()) {
                item { MensajeVacio("No hay movimientos con estos filtros.") }
            }
            items(movimientos, key = { it.idMovimiento }) { movimiento ->
                FilaMovimiento(movimiento = movimiento, datos = datos, mostrarTitular = true, modifier = Modifier.animateItem())
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MovimientosPreview() {
    PagoFlexTheme {
        MovimientosScreen(datos = BaseDatos())
    }
}
