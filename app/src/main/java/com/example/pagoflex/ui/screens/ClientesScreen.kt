package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.clientes
import com.example.pagoflex.model.compromisosDe
import com.example.pagoflex.model.deudaDe
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.ocultarRut
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.FilaEtiquetas
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

private val FILTROS = listOf("Todos", "Con deuda", "Inactivos")

@Composable
fun ClientesScreen(
    datos: BaseDatos,
    onVerCliente: (Int) -> Unit,
    onNuevoCliente: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf(FILTROS.first()) }
    val clientes = datos.clientes(busqueda).filter { cliente ->
        when (filtro) {
            "Con deuda" -> datos.deudaDe(cliente.idUsuario) > 0
            "Inactivos" -> !cliente.activo
            else -> true
        }
    }

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_clientes),
        menu = menu,
        snackbarHostState = snackbarHostState,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onNuevoCliente,
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_nuevo_cliente)) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.espacioPantalla, end = Dimens.espacioPantalla,
                top = Dimens.espacioPantalla, bottom = ESPACIO_BOTON_FLOTANTE
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            item {
                TarjetaSeccion(titulo = "Personas en PagoFlex", subtitulo = "Busca por nombre, RUT o correo") {
                    CampoTexto(
                        valor = busqueda,
                        onValorCambia = { busqueda = it },
                        etiqueta = stringResource(R.string.campo_buscar_cliente),
                        icono = R.drawable.ic_buscar
                    )
                    FilaEtiquetas(
                        opciones = FILTROS,
                        seleccionada = filtro,
                        onSeleccionar = { filtro = it },
                        textoDe = { it }
                    )
                }
            }
            item {
                TituloLista(
                    texto = if (clientes.size == 1) "1 cliente" else "${clientes.size} clientes",
                    subtitulo = "Toca uno para ver su ficha, editarlo o registrarle un compromiso."
                )
            }
            if (clientes.isEmpty()) {
                item { MensajeVacio("No encontramos clientes con esa búsqueda.") }
            }
            items(clientes, key = { it.idUsuario }) { cliente ->
                val deuda = datos.deudaDe(cliente.idUsuario)
                val porPagar = datos.compromisosDe(cliente.idUsuario).count {
                    it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO
                }
                TarjetaLista(
                    iniciales = inicialesDe(cliente),
                    colorAvatar = colorAvatarDe(cliente.idUsuario),
                    titulo = nombreCorto(cliente),
                    datoDerecho = if (deuda > 0) formatoPesos(deuda) else null,
                    linea2 = cliente.correo,
                    linea3 = "RUT ${ocultarRut(cliente.rut)}",
                    lineaFinal = when (porPagar) {
                        0 -> "Al día"
                        1 -> "1 compromiso por pagar"
                        else -> "$porPagar compromisos por pagar"
                    },
                    etiqueta = if (cliente.activo) "Activo" else "Inactivo",
                    colorEtiqueta = if (cliente.activo) MaterialTheme.coloresExtra.pagado else MaterialTheme.coloresExtra.anulado,
                    onClick = { onVerCliente(cliente.idUsuario) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ClientesPreview() {
    PagoFlexTheme {
        ClientesScreen(datos = BaseDatos(), onVerCliente = {}, onNuevoCliente = {})
    }
}
