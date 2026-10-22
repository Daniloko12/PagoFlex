package com.example.pagoflex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import java.time.YearMonth

private val ORDEN_ESTADOS = listOf(
    EstadoCompromiso.VENCIDO,
    EstadoCompromiso.PENDIENTE,
    EstadoCompromiso.EN_REVISION,
    EstadoCompromiso.PAGADO,
    EstadoCompromiso.ANULADO
)

@OptIn(ExperimentalLayoutApi::class)
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

    val filtrados = encontrados
        .filter { idEmpresa == null || it.idEmpresa == idEmpresa }
        .filter { mes == null || YearMonth.from(it.fechaVencimiento) == mes }
    val visibles = filtrados.filter { estado == null || it.estado == estado }
    val grupos = ORDEN_ESTADOS.mapNotNull { e ->
        visibles.filter { it.estado == e }.takeIf { it.isNotEmpty() }?.let { e to ordenarGrupo(e, it) }
    }
    val porCobrar = visibles
        .filter { it.estado == EstadoCompromiso.PENDIENTE || it.estado == EstadoCompromiso.VENCIDO }
        .sumOf { totalAPagar(it).toLong() }
    val hayFiltros = idEmpresa != null || estado != null || mes != null || busqueda.isNotBlank()

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
                CampoTexto(
                    valor = busqueda,
                    onValorCambia = onBuscar,
                    etiqueta = stringResource(R.string.campo_buscar),
                    icono = R.drawable.ic_buscar
                )
            }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    EtiquetaInteres(
                        texto = "Todos ${filtrados.size}",
                        color = MaterialTheme.colorScheme.primary,
                        seleccionada = estado == null,
                        onClick = { estado = null }
                    )
                    ORDEN_ESTADOS.forEach { opcion ->
                        val cantidad = filtrados.count { it.estado == opcion }
                        EtiquetaInteres(
                            texto = "${tituloEstado(opcion)} $cantidad",
                            color = colorEstado(opcion),
                            seleccionada = estado == opcion,
                            onClick = { estado = if (estado == opcion) null else opcion }
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    ListaDesplegable(
                        etiqueta = "Empresa",
                        opciones = listOf<Int?>(null) + datos.empresas.map { it.idEmpresa },
                        seleccionada = idEmpresa,
                        onSeleccionar = { idEmpresa = it },
                        textoDe = { id -> if (id == null) "Todas" else datos.empresaPorId(id)?.nombreFantasia ?: "" },
                        icono = R.drawable.ic_empresa,
                        textoVacio = "Todas",
                        modifier = Modifier.weight(1f)
                    )
                    ListaDesplegable(
                        etiqueta = "Vence en",
                        opciones = listOf<YearMonth?>(null) + datos.mesesVencimiento(idEmpresa),
                        seleccionada = mes,
                        onSeleccionar = { mes = it },
                        textoDe = { if (it == null) "Todos" else etiquetaMes(it) },
                        icono = R.drawable.ic_calendario,
                        textoVacio = "Todos",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (visibles.size == 1) "1 compromiso" else "${visibles.size} compromisos") +
                            if (porCobrar > 0) " · ${formatoPesos(porCobrar)} por cobrar" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (hayFiltros) {
                        TextButton(onClick = {
                            idEmpresa = null
                            estado = null
                            mes = null
                            onBuscar("")
                        }) { Text(stringResource(R.string.btn_limpiar)) }
                    }
                }
            }
            if (visibles.isEmpty()) {
                item {
                    MensajeVacio(
                        if (busqueda.isNotBlank()) "No encontramos compromisos para \"$busqueda\"."
                        else "No hay compromisos con estos filtros."
                    )
                }
            }
            grupos.forEach { (estadoGrupo, lista) ->
                item(key = "grupo-${estadoGrupo.name}") { EncabezadoGrupo(estadoGrupo, lista) }
                items(lista, key = { it.idCompromiso }) { compromiso ->
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
}

@Composable
private fun EncabezadoGrupo(estado: EstadoCompromiso, lista: List<Compromiso>) {
    val color = colorEstado(estado)
    val monto = lista.sumOf { totalAPagar(it).toLong() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.espacioMedio, bottom = Dimens.espacioMini),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "${tituloEstado(estado)} · ${lista.size}",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.espacioChico)
        )
        Text(
            text = formatoPesos(monto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ordenarGrupo(estado: EstadoCompromiso, lista: List<Compromiso>): List<Compromiso> = when (estado) {
    EstadoCompromiso.PAGADO, EstadoCompromiso.ANULADO -> lista.sortedByDescending { it.fechaCambioEstado }
    else -> lista.sortedBy { it.fechaVencimiento }
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

@Preview(showBackground = true, showSystemUi = true, name = "Modo claro")
@Composable
private fun CompromisosEmpresaClaroPreview() {
    PagoFlexTheme(modoOscuro = false) {
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
