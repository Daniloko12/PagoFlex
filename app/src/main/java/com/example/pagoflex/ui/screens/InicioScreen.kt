package com.example.pagoflex.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.FiltroEstado
import com.example.pagoflex.model.FiltroVencimiento
import com.example.pagoflex.model.FiltrosCompromiso
import com.example.pagoflex.model.ResumenSituacion
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.avisosProgramados
import com.example.pagoflex.model.compromisosFiltrados
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.empresasDelUsuario
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.model.resumenSituacion
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun InicioScreen(
    datos: BaseDatos,
    usuario: Usuario,
    sinConexion: Boolean,
    ultimaActualizacion: LocalDateTime,
    onVerCompromiso: (Int) -> Unit,
    onVerAvance: () -> Unit = {},
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var estado by rememberSaveable { mutableStateOf(FiltroEstado.POR_PAGAR) }
    var idEmpresa by rememberSaveable { mutableStateOf<Int?>(null) }
    var vencimiento by rememberSaveable { mutableStateOf(FiltroVencimiento.CUALQUIERA) }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }
    val filtros = FiltrosCompromiso(estado, idEmpresa, vencimiento)

    val resumen = datos.resumenSituacion(usuario.idUsuario)
    val compromisos = datos.compromisosFiltrados(usuario.idUsuario, filtros)
    val avisoDeHoy = datos.avisosProgramados(usuario, usuario.avisosActivos ?: true, anticipacionDe(usuario), LocalTime.now())
        .firstOrNull { it.fechaEntrega.toLocalDate() == datos.fechaHoy }

    PantallaPrincipal(
        titulo = stringResource(R.string.saludo, primerNombre(usuario)),
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
            item { EstadoConexion(sinConexion, ultimaActualizacion) }
            if (avisoDeHoy != null) {
                item { TarjetaMensaje(texto = avisoDeHoy.mensaje, icono = R.drawable.ic_campana) }
            }
            item { ResumenDelMes(datos = datos, resumen = resumen) }

            item {
                TarjetaSeccion(
                    titulo = "Filtrar compromisos",
                    subtitulo = if (filtros.cantidadActivos == 0) "Mostrando lo que tienes por pagar"
                    else "${filtros.cantidadActivos} filtro(s) activo(s)"
                ) {
                    val giro by animateFloatAsState(targetValue = if (mostrarFiltros) 180f else 0f, label = "giroFiltros")
                    TextButton(onClick = { mostrarFiltros = !mostrarFiltros }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_buscar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(giro)
                        )
                        Text(
                            text = if (mostrarFiltros) "Ocultar filtros" else "Mostrar filtros",
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                    AnimatedVisibility(visible = mostrarFiltros, label = "filtros") {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                            Text(text = "Estado", style = MaterialTheme.typography.labelLarge)
                            FilaChips(
                                opciones = FiltroEstado.entries,
                                seleccionada = estado,
                                textoDe = { it.etiqueta },
                                onSeleccionar = { estado = it }
                            )
                            ListaDesplegable(
                                etiqueta = "Organización",
                                opciones = listOf<Int?>(null) + datos.empresasDelUsuario(usuario.idUsuario).map { it.idEmpresa },
                                seleccionada = idEmpresa,
                                onSeleccionar = { idEmpresa = it },
                                textoDe = { id -> if (id == null) "Todas" else datos.empresaPorId(id)?.nombreFantasia ?: "" },
                                icono = R.drawable.ic_empresa,
                                textoVacio = "Todas"
                            )
                            Text(text = "Vencimiento", style = MaterialTheme.typography.labelLarge)
                            FilaChips(
                                opciones = FiltroVencimiento.entries,
                                seleccionada = vencimiento,
                                textoDe = { it.etiqueta },
                                onSeleccionar = { vencimiento = it }
                            )
                        }
                    }
                    if (filtros.cantidadActivos > 0) {
                        TextButton(
                            onClick = {
                                estado = FiltroEstado.POR_PAGAR
                                idEmpresa = null
                                vencimiento = FiltroVencimiento.CUALQUIERA
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Limpiar filtros") }
                    }
                }
            }

            item {
                TituloLista(
                    texto = "Tus compromisos (${compromisos.size})",
                    subtitulo = "Toca uno para ver el detalle o pagarlo."
                )
            }
            if (compromisos.isEmpty()) {
                item {
                    if (filtros.cantidadActivos == 0) {
                        TarjetaMensaje(texto = "¡Estás al día! No tienes compromisos por pagar.", exito = true)
                    } else {
                        MensajeVacio("No hay compromisos con estos filtros.")
                    }
                }
            }
            items(compromisos, key = { it.idCompromiso }) { compromiso ->
                val empresa = datos.empresaPorId(compromiso.idEmpresa)
                TarjetaLista(
                    modifier = Modifier.animateItem(),
                    iniciales = empresa?.let { inicialesEmpresa(it.nombreFantasia) },
                    colorAvatar = colorAvatarDe(compromiso.idEmpresa),
                    titulo = empresa?.nombreFantasia ?: "",
                    datoDerecho = formatoPesos(totalAPagar(compromiso)),
                    linea2 = conceptoConCuota(compromiso),
                    lineaFinal = textoVencimiento(compromiso, datos.fechaHoy),
                    etiqueta = compromiso.estado.etiqueta,
                    colorEtiqueta = colorEstado(compromiso.estado),
                    onClick = { onVerCompromiso(compromiso.idCompromiso) }
                )
            }
            item {
                TextButton(onClick = onVerAvance, modifier = Modifier.fillMaxWidth()) {
                    Text("Ver el avance de tus cuotas y tu cumplimiento")
                }
            }
        }
    }
}

@Composable
private fun ResumenDelMes(datos: BaseDatos, resumen: ResumenSituacion) {
    val proximo = resumen.proximoVencimiento
    val total by animateIntAsState(
        targetValue = resumen.totalPorPagar,
        animationSpec = tween(durationMillis = 700),
        label = "totalPorPagar"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.coloresDestacada()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
        ) {
            Text(text = "Total por pagar este mes", style = MaterialTheme.typography.labelLarge)
            Text(
                text = formatoPesos(total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = listOfNotNull(
                    proximo?.let { "Próximo: ${formatoFecha(it.fechaVencimiento)} · ${datos.empresaPorId(it.idEmpresa)?.nombreFantasia ?: ""}" },
                    if (resumen.cantidadVencidos > 0) "${resumen.cantidadVencidos} vencido(s)" else null
                ).joinToString("  ·  ").ifEmpty { "Sin vencimientos próximos" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun <T> FilaChips(
    opciones: List<T>,
    seleccionada: T,
    textoDe: (T) -> String,
    onSeleccionar: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        opciones.forEach { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                label = { Text(textoDe(opcion)) }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioPreview() {
    PagoFlexTheme {
        InicioScreen(
            datos = BaseDatos(),
            usuario = DatosAnexo9.usuarios.first(),
            sinConexion = false,
            ultimaActualizacion = DatosAnexo9.ULTIMA_ACTUALIZACION,
            onVerCompromiso = {}
        )
    }
}
