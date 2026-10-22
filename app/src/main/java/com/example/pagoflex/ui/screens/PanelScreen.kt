package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.etiquetaMesCorta
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.formatoPesosCorto
import com.example.pagoflex.model.nombreMes
import com.example.pagoflex.model.primerNombre
import com.example.pagoflex.model.resumenPanel
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.components.BarraGrafico
import com.example.pagoflex.ui.components.BarraHorizontal
import com.example.pagoflex.ui.components.BotonPrincipal
import com.example.pagoflex.ui.components.BotonSecundario
import com.example.pagoflex.ui.components.GraficoBarras
import com.example.pagoflex.ui.components.GraficoDona
import com.example.pagoflex.ui.components.LeyendaGrafico
import com.example.pagoflex.ui.components.SegmentoGrafico
import com.example.pagoflex.ui.components.TarjetaIndicador
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra
import java.time.YearMonth

@Composable
fun PanelScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onVerCompromiso: (Int) -> Unit,
    onVerCompromisos: () -> Unit,
    onVerClientes: () -> Unit,
    onVerReportes: () -> Unit,
    onVerRecomendaciones: () -> Unit,
    onRegistrar: () -> Unit,
    onNuevoCliente: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val resumen = datos.resumenPanel()
    val colores = MaterialTheme.coloresExtra
    val mesActual = YearMonth.from(datos.fechaHoy)
    val segmentos = resumen.porEstado.map {
        SegmentoGrafico(tituloEstado(it.estado), it.cantidad.toFloat(), colorEstado(it.estado))
    }
    val mayorDeuda = resumen.deudaPorEmpresa.maxOfOrNull { it.monto }?.takeIf { it > 0 } ?: 1L

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_panel),
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
                Column {
                    Text(text = "Hola, ${primerNombre(usuario)}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Así va la cobranza al ${formatoFecha(datos.fechaHoy)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaIndicador(
                        titulo = "Por cobrar",
                        valor = formatoPesos(resumen.montoPorCobrar),
                        detalle = cantidadCompromisos(resumen.cantidadPorCobrar),
                        icono = R.drawable.ic_dinero,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onVerCompromisos,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaIndicador(
                        titulo = "Vencido",
                        valor = formatoPesos(resumen.montoVencido),
                        detalle = cantidadCompromisos(resumen.cantidadVencidos),
                        icono = R.drawable.ic_calendario,
                        color = colores.vencido,
                        onClick = onVerCompromisos,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaIndicador(
                        titulo = "Recaudado",
                        valor = formatoPesos(resumen.recaudadoMes),
                        detalle = (if (resumen.pagosMes == 1) "1 pago en " else "${resumen.pagosMes} pagos en ") +
                            nombreMes(mesActual.monthValue).lowercase(),
                        icono = R.drawable.ic_check_circulo,
                        color = colores.exito,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaIndicador(
                        titulo = "Clientes",
                        valor = "${resumen.clientesActivos} activos",
                        detalle = "${resumen.clientesConDeuda} con deuda",
                        icono = R.drawable.ic_clientes,
                        color = colores.informacion,
                        onClick = onVerClientes,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaIndicador(
                        titulo = "Reportes",
                        valor = "${resumen.reportesAbiertos} abiertos",
                        detalle = if (resumen.reportesAtrasados == 0) "Ninguno atrasado"
                        else "${resumen.reportesAtrasados} fuera de plazo",
                        icono = R.drawable.ic_reporte,
                        color = if (resumen.reportesAtrasados > 0) colores.vencido else colores.pendiente,
                        onClick = onVerReportes,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaIndicador(
                        titulo = "Recomendaciones",
                        valor = "${resumen.recomendacionesEnCurso} en curso",
                        detalle = "Toca para avanzarlas",
                        icono = R.drawable.ic_recomendar,
                        color = colores.enRevision,
                        onClick = onVerRecomendaciones,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TarjetaSeccion(titulo = "Compromisos por estado", subtitulo = "Todos los compromisos de todas las empresas") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GraficoDona(
                            segmentos = segmentos,
                            textoCentro = datos.compromisos.size.toString(),
                            subtextoCentro = "compromisos"
                        )
                        LeyendaGrafico(
                            segmentos = segmentos,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Dimens.espacioMedio)
                        )
                    }
                }
            }

            item {
                TarjetaSeccion(
                    titulo = "Recaudación de los últimos meses",
                    subtitulo = "Pagos recibidos en la app, el portal y el agente conversacional"
                ) {
                    GraficoBarras(
                        barras = resumen.recaudacionPorMes.map {
                            BarraGrafico(etiquetaMesCorta(it.mes), it.monto.toFloat(), formatoPesosCorto(it.monto))
                        },
                        modifier = Modifier.padding(top = Dimens.espacioChico)
                    )
                }
            }

            item {
                TarjetaSeccion(titulo = "Deuda por empresa", subtitulo = "Compromisos pendientes y vencidos") {
                    if (resumen.deudaPorEmpresa.isEmpty()) {
                        Text(text = "No hay deuda por cobrar", style = MaterialTheme.typography.bodyMedium)
                    }
                    resumen.deudaPorEmpresa.forEach { deuda ->
                        BarraHorizontal(
                            etiqueta = deuda.empresa.nombreFantasia,
                            valorTexto = formatoPesos(deuda.monto),
                            fraccion = deuda.monto.toFloat() / mayorDeuda,
                            color = colorAvatarDe(deuda.empresa.idEmpresa),
                            detalle = cantidadCompromisos(deuda.cantidad)
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonPrincipal(
                        texto = "Registrar",
                        onClick = onRegistrar,
                        icono = R.drawable.ic_agregar,
                        modifier = Modifier.weight(1f)
                    )
                    BotonSecundario(
                        texto = "Nuevo cliente",
                        onClick = onNuevoCliente,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (resumen.vencidosAntiguos.isNotEmpty()) {
                item { TituloLista(texto = "Requieren atención", subtitulo = "Los vencidos con más días de atraso") }
                items(resumen.vencidosAntiguos, key = { "vencido-${it.idCompromiso}" }) { compromiso ->
                    FilaCompromisoPanel(datos, compromiso) { onVerCompromiso(compromiso.idCompromiso) }
                }
            }
            if (resumen.proximosVencimientos.isNotEmpty()) {
                item { TituloLista(texto = "Vencen esta semana", subtitulo = "Pendientes de los próximos 7 días") }
                items(resumen.proximosVencimientos, key = { "proximo-${it.idCompromiso}" }) { compromiso ->
                    FilaCompromisoPanel(datos, compromiso) { onVerCompromiso(compromiso.idCompromiso) }
                }
            }
        }
    }
}

@Composable
private fun FilaCompromisoPanel(datos: BaseDatos, compromiso: Compromiso, onClick: () -> Unit) {
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
        onClick = onClick
    )
}

private fun cantidadCompromisos(cantidad: Int): String =
    if (cantidad == 1) "1 compromiso" else "$cantidad compromisos"

fun tituloEstado(estado: EstadoCompromiso): String = when (estado) {
    EstadoCompromiso.PENDIENTE -> "Pendientes"
    EstadoCompromiso.VENCIDO -> "Vencidos"
    EstadoCompromiso.PAGADO -> "Pagados"
    EstadoCompromiso.EN_REVISION -> "En revisión"
    EstadoCompromiso.ANULADO -> "Anulados"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PanelPreview() {
    val datos = BaseDatos()
    PagoFlexTheme {
        PanelScreen(
            datos = datos,
            usuario = datos.usuarioPorId(9) ?: datos.usuarios.last(),
            onVerCompromiso = {}, onVerCompromisos = {}, onVerClientes = {}, onVerReportes = {},
            onVerRecomendaciones = {}, onRegistrar = {}, onNuevoCliente = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Modo claro")
@Composable
private fun PanelClaroPreview() {
    val datos = BaseDatos()
    PagoFlexTheme(modoOscuro = false) {
        PanelScreen(
            datos = datos,
            usuario = datos.usuarioPorId(9) ?: datos.usuarios.last(),
            onVerCompromiso = {}, onVerCompromisos = {}, onVerClientes = {}, onVerReportes = {},
            onVerRecomendaciones = {}, onRegistrar = {}, onNuevoCliente = {}
        )
    }
}
