package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.avisosProgramados
import com.example.pagoflex.model.compromisosVigentes
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
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.ImagenEncabezado
import com.example.pagoflex.ui.components.TarjetaEstadistica
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
    onActualizar: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val resumen = datos.resumenSituacion(usuario.idUsuario)
    val vigentes = datos.compromisosVigentes(usuario.idUsuario)
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
                    texto = "Tus compromisos",
                    subtitulo = "De todas tus empresas. Toca uno para ver el detalle o pagarlo."
                )
            }
            if (vigentes.isEmpty()) {
                item { MensajeVacio("¡Estás al día! No tienes compromisos por pagar.") }
            }
            items(vigentes, key = { it.idCompromiso }) { compromiso ->
                val empresa = datos.empresaPorId(compromiso.idEmpresa)
                TarjetaLista(
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
            onVerCompromiso = {},
            onActualizar = {}
        )
    }
}
