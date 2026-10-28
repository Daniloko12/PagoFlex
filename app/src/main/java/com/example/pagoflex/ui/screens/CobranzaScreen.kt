package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.diasAtraso
import com.example.pagoflex.model.empresaPorId
import com.example.pagoflex.model.etiquetaMes
import com.example.pagoflex.model.formatoPesos
import com.example.pagoflex.model.formatoPorcentaje
import com.example.pagoflex.model.mesesCobranza
import com.example.pagoflex.model.resumenCobranza
import com.example.pagoflex.model.totalAPagar
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.model.vencidosEmpresa
import com.example.pagoflex.ui.components.FilaDato
import com.example.pagoflex.ui.components.FilaEtiquetas
import com.example.pagoflex.ui.components.ListaDesplegable
import com.example.pagoflex.ui.components.TarjetaEstadistica
import com.example.pagoflex.ui.components.TarjetaLista
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun CobranzaScreen(
    datos: BaseDatos,
    onVerCompromiso: (Int) -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var idEmpresa by rememberSaveable { mutableStateOf(datos.empresas.first().idEmpresa) }
    val empresa = datos.empresaPorId(idEmpresa)
    val meses = datos.mesesCobranza(idEmpresa)
    var mes by rememberSaveable { mutableStateOf(DatosAnexo9.MES_RESUMEN_ANEXO) }
    val resumen = datos.resumenCobranza(idEmpresa, mes)
    val vencidos = datos.vencidosEmpresa(idEmpresa)

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_cobranza),
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
                TarjetaSeccion(
                    titulo = empresa?.nombreFantasia ?: "",
                    subtitulo = "Resumen de ${etiquetaMes(resumen.mes).lowercase()}. Elige otra empresa u otro mes para comparar."
                ) {
                    ListaDesplegable(
                        etiqueta = "Empresa",
                        opciones = datos.empresas,
                        seleccionada = empresa,
                        onSeleccionar = { idEmpresa = it.idEmpresa },
                        textoDe = { it.nombreFantasia },
                        icono = R.drawable.ic_empresa
                    )
                    FilaEtiquetas(
                        opciones = meses,
                        seleccionada = mes,
                        onSeleccionar = { mes = it },
                        textoDe = { etiquetaMes(it) }
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    TarjetaEstadistica(resumen.pagados.toString(), "Pagados", Modifier.weight(1f))
                    TarjetaEstadistica(resumen.pendientes.toString(), "Pendientes", Modifier.weight(1f))
                    TarjetaEstadistica(resumen.vencidos.toString(), "Vencidos", Modifier.weight(1f))
                }
            }
            item {
                TarjetaEstadistica(
                    valor = formatoPesos(resumen.montoRecaudado),
                    etiqueta = "Monto recaudado (incluye recargos)",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                TarjetaSeccion(titulo = "Tasa de cobranza", subtitulo = "Pagados ÷ (compromisos del mes − anulados) × 100") {
                    val tasa = resumen.tasa
                    Text(
                        text = if (tasa != null) formatoPorcentaje(tasa) else "Sin compromisos este mes",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (tasa != null) {
                        LinearProgressIndicator(
                            progress = { (tasa / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    FilaDato("Compromisos del mes", resumen.total.toString())
                    FilaDato("Anulados (no cuentan)", resumen.anulados.toString())
                    if (resumen.enRevision > 0) FilaDato("En revisión", resumen.enRevision.toString())
                }
            }

            item {
                TituloLista(texto = "Personas con compromisos vencidos", subtitulo = "Más días de atraso primero")
            }
            if (vencidos.isEmpty()) {
                item { MensajeVacio("No hay compromisos vencidos cargados. ¡Bien!") }
            }
            items(vencidos, key = { it.idCompromiso }) { compromiso ->
                val persona = datos.usuarioPorId(compromiso.idUsuarioFinal)
                TarjetaLista(
                    iniciales = persona?.let { inicialesDe(it) },
                    colorAvatar = colorAvatarDe(persona?.idUsuario ?: 0),
                    titulo = persona?.let { nombreCorto(it) } ?: "",
                    datoDerecho = formatoPesos(totalAPagar(compromiso)),
                    linea2 = conceptoConCuota(compromiso),
                    linea3 = persona?.telefono,
                    lineaFinal = "Hace ${textoDias(diasAtraso(compromiso, datos.fechaHoy))}",
                    colorLineaFinal = MaterialTheme.coloresExtra.advertencia,
                    etiqueta = "Vencido",
                    colorEtiqueta = MaterialTheme.coloresExtra.vencido,
                    onClick = { onVerCompromiso(compromiso.idCompromiso) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CobranzaPreview() {
    PagoFlexTheme {
        CobranzaScreen(
            datos = BaseDatos(),
            onVerCompromiso = {}
        )
    }
}
