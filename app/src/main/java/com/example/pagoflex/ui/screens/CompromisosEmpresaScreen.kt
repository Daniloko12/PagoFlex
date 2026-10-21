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
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.etiquetaMes
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.mesesVencimiento
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.CampoTexto
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import java.time.YearMonth

@Composable
fun CompromisosEmpresaScreen(
    datos: BaseDatos,
    encontrados: List<Compromiso>,
    busqueda: String,
    onBuscar: (String) -> Unit,
    onVerCompromiso: (Int) -> Unit,
    onRegistrar: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var idEmpresa by rememberSaveable { mutableStateOf<Int?>(null) }
    var estado by rememberSaveable { mutableStateOf<EstadoCompromiso?>(null) }
    var mes by rememberSaveable { mutableStateOf<YearMonth?>(null) }
    val compromisos = encontrados
        .filter { idEmpresa == null || it.idEmpresa == idEmpresa }
        .filter { estado == null || it.estado == estado }
        .filter { mes == null || YearMonth.from(it.fechaVencimiento) == mes }

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_compromisos),
        menu = menu,
        snackbarHostState = snackbarHostState,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onRegistrar,
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_registrar)) },
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
                TarjetaSeccion(
                    titulo = "Compromisos de todas las empresas",
                    subtitulo = "Busca a una persona o filtra por empresa, estado y mes"
                ) {
                    CampoTexto(
                        valor = busqueda,
                        onValorCambia = onBuscar,
                        etiqueta = stringResource(R.string.campo_buscar),
                        icono = R.drawable.ic_buscar
                    )
                    ListaDesplegable(
                        etiqueta = "Empresa",
                        opciones = listOf<Int?>(null) + datos.empresas.map { it.idEmpresa },
                        seleccionada = idEmpresa,
                        onSeleccionar = { idEmpresa = it },
                        textoDe = { id -> if (id == null) "Todas" else datos.empresaPorId(id)?.nombreFantasia ?: "" },
                        icono = R.drawable.ic_empresa,
                        textoVacio = "Todas"
                    )
                    ListaDesplegable(
                        etiqueta = "Estado",
                        opciones = listOf<EstadoCompromiso?>(null) + EstadoCompromiso.entries,
                        seleccionada = estado,
                        onSeleccionar = { estado = it },
                        textoDe = { it?.etiqueta ?: "Todos" },
                        icono = R.drawable.ic_lista,
                        textoVacio = "Todos"
                    )
                    ListaDesplegable(
                        etiqueta = "Mes de vencimiento",
                        opciones = listOf<YearMonth?>(null) + datos.mesesVencimiento(idEmpresa),
                        seleccionada = mes,
                        onSeleccionar = { mes = it },
                        textoDe = { if (it == null) "Todos" else etiquetaMes(it) },
                        icono = R.drawable.ic_calendario,
                        textoVacio = "Todos"
                    )
                }
            }
            item {
                TituloLista(
                    texto = if (compromisos.size == 1) "1 compromiso" else "${compromisos.size} compromisos",
                    subtitulo = "Toca uno para ver el detalle, modificarlo o anularlo."
                )
            }
            if (compromisos.isEmpty()) {
                item {
                    MensajeVacio(
                        if (busqueda.isNotBlank()) "No encontramos compromisos para \"$busqueda\"."
                        else "No hay compromisos con estos filtros."
                    )
                }
            }
            items(compromisos, key = { it.idCompromiso }) { compromiso ->
                val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)
                TarjetaLista(
                    iniciales = persona?.let { inicialesDe(it) },
                    colorAvatar = colorAvatarDe(persona?.idUsuario ?: 0),
                    titulo = persona?.let { nombreCorto(it) } ?: "",
                    datoDerecho = formatoPesos(totalAPagar(compromiso)),
                    linea2 = conceptoConCuota(compromiso),
                    linea3 = datos.empresaPorId(compromiso.idEmpresa)?.nombreFantasia,
                    lineaFinal = textoVencimiento(compromiso, datos.fechaHoy),
                    etiqueta = compromiso.estado.etiqueta,
                    colorEtiqueta = colorEstado(compromiso.estado),
                    onClick = { onVerCompromiso(compromiso.idCompromiso) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CompromisosEmpresaPreview() {
    PagoFlexTheme {
        CompromisosEmpresaScreen(
            datos = BaseDatos(),
            encontrados = BaseDatos().compromisos,
            busqueda = "",
            onBuscar = {},
            onVerCompromiso = {},
            onRegistrar = {}
        )
    }
}
