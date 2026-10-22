package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.AvanceEmpresa
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.avancePorEmpresa
import com.example.pagoflex.model.avisosProgramados
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.formatoPorcentaje
import com.example.pagoflex.model.indicadorDe
import com.example.pagoflex.model.mensajeCategoria
import com.example.pagoflex.model.nombreMes
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.model.resumenSituacion
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.ui.components.AvatarIniciales
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.ImagenEncabezado
import com.example.pagoflex.ui.components.TarjetaEstadistica
import com.example.pagoflex.ui.components.TarjetaMensaje
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun InicioScreen(
    datos: BaseDatos,
    usuario: Usuario,
    sinConexion: Boolean,
    ultimaActualizacion: LocalDateTime,
    onVerCompromiso: (Int) -> Unit,
    onActualizar: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val resumen = datos.resumenSituacion(usuario.idUsuario)
    val avances = datos.avancePorEmpresa(usuario.idUsuario)
    val indicador = indicadorDe(usuario.idUsuario)
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
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                ImagenEncabezado(
                    imagen = R.drawable.banner_pagoflex,
                    descripcion = stringResource(R.string.cd_banner),
                    titulo = "Tu mes de ${nombreMes(datos.fechaHoy.monthValue).lowercase()}",
                    subtitulo = "Todos tus compromisos, en un solo lugar",
                    modifier = Modifier.clip(EstilosTarjeta.forma)
                )
            }
            item { EstadoConexion(sinConexion, ultimaActualizacion, onActualizar) }
            if (avisoDeHoy != null) {
                item { TarjetaMensaje(texto = avisoDeHoy.mensaje, icono = R.drawable.ic_campana) }
            }

            item {
                TarjetaEstadistica(
                    valor = formatoPesos(resumen.totalPorPagar),
                    etiqueta = "Total por pagar este mes",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    val proximo = resumen.proximoVencimiento
                    TarjetaEstadistica(
                        valor = if (proximo != null) formatoFecha(proximo.fechaVencimiento).take(5) else "—",
                        etiqueta = if (proximo != null)
                            "Próximo vencimiento\n${datos.empresaPorId(proximo.idEmpresa)?.nombreFantasia ?: ""}"
                        else "Sin próximos vencimientos",
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        valor = resumen.cantidadVencidos.toString(),
                        etiqueta = if (resumen.cantidadVencidos == 1) "Compromiso vencido" else "Compromisos vencidos",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TarjetaSeccion(titulo = "Tu cumplimiento") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = indicador.porcentaje?.let { formatoPorcentaje(it) } ?: "—",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(end = Dimens.espacioChico)
                        )
                        EtiquetaInteres(texto = indicador.categoria.etiqueta, color = colorCategoria(indicador.categoria))
                    }
                    Text(text = mensajeCategoria(indicador.categoria), style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                TituloLista(
                    texto = "Tus empresas",
                    subtitulo = "La cuota que te toca pagar en cada una. Toca una para ver el detalle o pagarla."
                )
            }
            if (avances.isEmpty()) {
                item { MensajeVacio("Todavía no tienes compromisos registrados.") }
            } else if (avances.all { it.alDia }) {
                item { TarjetaMensaje(texto = "¡Estás al día! No tienes compromisos por pagar.", exito = true) }
            }
            items(avances, key = { it.empresa.idEmpresa }) { avance ->
                TarjetaAvanceEmpresa(
                    avance = avance,
                    hoy = datos.fechaHoy,
                    onClick = avance.actual?.let { actual -> { onVerCompromiso(actual.idCompromiso) } }
                )
            }
        }
    }
}

@Composable
private fun TarjetaAvanceEmpresa(
    avance: AvanceEmpresa,
    hoy: LocalDate,
    onClick: (() -> Unit)?
) {
    val actual = avance.actual
    val referencia = actual ?: avance.ultimoPagado
    val colores = MaterialTheme.coloresExtra
    val contenido: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarIniciales(
                    iniciales = inicialesEmpresa(avance.empresa.nombreFantasia),
                    color = colorAvatarDe(avance.empresa.idEmpresa)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = Dimens.espacioChico)
                ) {
                    Text(text = avance.empresa.nombreFantasia, style = MaterialTheme.typography.titleMedium)
                    referencia?.let {
                        Text(
                            text = it.concepto,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (actual != null) {
                    Text(
                        text = formatoPesos(totalAPagar(actual)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    EtiquetaInteres(texto = "Al día", color = colores.exito, seleccionada = true)
                }
            }

            val numero = referencia?.numeroCuota
            val total = referencia?.totalCuotas
            if (numero != null && total != null) {
                val pagadas = if (actual != null) numero - 1 else numero
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (actual != null) "Cuota $numero de $total" else "Cuotas pagadas",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$pagadas de $total pagadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { pagadas / total.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (actual == null) colores.exito else MaterialTheme.colorScheme.primary
                )
            }

            if (actual != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = textoVencimiento(actual, hoy),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (actual.estado == EstadoCompromiso.VENCIDO) colores.vencido
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    EtiquetaInteres(texto = actual.estado.etiqueta, color = colorEstado(actual.estado))
                }
                if (avance.porPagar > 1) {
                    val otras = avance.porPagar - 1
                    Text(
                        text = if (otras == 1) "Después tienes 1 cuota más por pagar" else "Después tienes $otras cuotas más por pagar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = if (numero != null && numero == total) "¡Pagaste todas tus cuotas con esta empresa!"
                    else "No tienes pagos pendientes con esta empresa",
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.exito
                )
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            shape = EstilosTarjeta.forma,
            colors = EstilosTarjeta.colores(),
            elevation = EstilosTarjeta.elevacion(),
            border = EstilosTarjeta.borde(),
            content = contenido
        )
    } else {
        Card(
            shape = EstilosTarjeta.forma,
            colors = EstilosTarjeta.colores(),
            elevation = EstilosTarjeta.elevacion(),
            border = EstilosTarjeta.borde(),
            content = contenido
        )
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
            onVerCompromiso = {},
            onActualizar = {}
        )
    }
}
