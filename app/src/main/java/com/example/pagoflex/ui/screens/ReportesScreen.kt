package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.cantidadReportesAtrasados
import com.example.pagoflex.model.compromisoPorId
import com.example.pagoflex.model.diasHabilesEntre
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.reporteAtrasado
import com.example.pagoflex.model.reportesOrdenados
import com.example.pagoflex.model.usuarioDeReporte
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.FilaEtiquetas
import com.example.pagoflex.ui.components.TarjetaEstadistica
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun ReportesScreen(
    datos: BaseDatos,
    onVerReporte: (Int) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var filtro by rememberSaveable { mutableStateOf<EstadoReporte?>(null) }
    val reportes = datos.reportesOrdenados(filtro)

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_reportes),
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
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaEstadistica(
                        valor = datos.reportes.count { it.estado == EstadoReporte.ABIERTO }.toString(),
                        etiqueta = "Abiertos",
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        valor = datos.reportes.count { it.estado == EstadoReporte.EN_GESTION }.toString(),
                        etiqueta = "En gestión",
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaEstadistica(
                        valor = datos.cantidadReportesAtrasados().toString(),
                        etiqueta = "Atrasados",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                TituloLista(
                    texto = "Reportes de problemas",
                    subtitulo = "Primero los que llevan más tiempo esperando. Toca uno para atenderlo."
                )
            }
            item {
                FilaEtiquetas(
                    opciones = listOf<EstadoReporte?>(null) + EstadoReporte.entries,
                    seleccionada = filtro,
                    onSeleccionar = { filtro = it },
                    textoDe = { it?.etiqueta ?: "Todos" }
                )
            }
            if (reportes.isEmpty()) {
                item { MensajeVacio("No hay reportes con este filtro.") }
            }
            items(reportes, key = { it.idReporte }) { reporte ->
                ItemReporte(datos, reporte, onClick = { onVerReporte(reporte.idReporte) })
            }
        }
    }
}

@Composable
private fun ItemReporte(datos: BaseDatos, reporte: Reporte, onClick: () -> Unit) {
    val compromiso = datos.compromisoPorId(reporte.idCompromiso)
    val usuario = datos.usuarioDeReporte(reporte)
    val empresa = datos.empresaPorId(compromiso?.idEmpresa)
    val atrasado = reporteAtrasado(reporte, datos.fechaHoy)
    val responsable = datos.usuarioPorId(reporte.idAdministrador)
    val situacion = when {
        atrasado -> "Esperando hace ${diasHabilesEntre(reporte.fechaCreacion.toLocalDate(), datos.fechaHoy)} días hábiles"
        reporte.estado == EstadoReporte.ABIERTO -> "Recibido el ${formatoFecha(reporte.fechaCreacion.toLocalDate())}"
        reporte.estado == EstadoReporte.EN_GESTION -> "Lo atiende ${responsable?.let { nombreCorto(it) } ?: "otro administrador"}"
        else -> "Resuelto el ${reporte.fechaResolucion?.let { formatoFecha(it.toLocalDate()) } ?: ""}"
    }
    TarjetaLista(
        iniciales = usuario?.let { inicialesDe(it) },
        colorAvatar = colorAvatarDe(usuario?.idUsuario ?: 0),
        titulo = usuario?.let { nombreCorto(it) } ?: "",
        datoDerecho = compromiso?.let { formatoPesos(it.monto) },
        linea2 = tituloMotivo(reporte.motivo),
        linea3 = empresa?.nombreFantasia,
        lineaFinal = situacion,
        colorLineaFinal = if (atrasado) MaterialTheme.coloresExtra.advertencia else Color.Unspecified,
        destacarLineaFinal = atrasado,
        etiqueta = reporte.estado.etiqueta,
        colorEtiqueta = colorReporte(reporte.estado),
        onClick = onClick
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ReportesPreview() {
    PagoFlexTheme {
        ReportesScreen(datos = BaseDatos(), onVerReporte = {})
    }
}
