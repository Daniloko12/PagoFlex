package com.example.pagoflex.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.comunaPorId
import com.example.pagoflex.model.formatoFecha
import com.example.pagoflex.model.formatoPorcentaje
import com.example.pagoflex.model.indicadorDe
import com.example.pagoflex.model.mensajeCategoria
import com.example.pagoflex.model.recomendacionesDe
import com.example.pagoflex.model.reconocimientoPorId
import com.example.pagoflex.model.reconocimientosDe
import com.example.pagoflex.ui.components.EtiquetaInteres
import com.example.pagoflex.ui.components.TarjetaSeccion
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun AvanceScreen(
    datos: BaseDatos,
    usuario: Usuario,
    onRecomendar: () -> Unit,
    menu: MenuSesion = MenuSesion(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val indicador = indicadorDe(usuario.idUsuario)
    val obtenidos = datos.reconocimientosDe(usuario.idUsuario)
    val idsObtenidos = obtenidos.map { it.idReconocimiento }
    val porObtener = datos.reconocimientos.filter { it.idReconocimiento !in idsObtenidos }
    val recomendaciones = datos.recomendacionesDe(usuario.idUsuario)

    PantallaPrincipal(
        titulo = stringResource(R.string.titulo_avance),
        menu = menu,
        snackbarHostState = snackbarHostState,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onRecomendar,
                icon = { Icon(painterResource(R.drawable.ic_recomendar), contentDescription = null) },
                text = { Text(stringResource(R.string.btn_recomendar)) },
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
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            item {
                TarjetaSeccion(titulo = "Tu cumplimiento", subtitulo = "Compromisos de los últimos 12 meses") {
                    val porcentaje = indicador.porcentaje
                    if (porcentaje != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatoPorcentaje(porcentaje),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(end = Dimens.espacioChico)
                            )
                            EtiquetaInteres(texto = indicador.categoria.etiqueta, color = colorCategoria(indicador.categoria))
                        }
                        LinearProgressIndicator(
                            progress = { (porcentaje / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${indicador.aTiempo} de ${indicador.considerados} compromisos pagados a tiempo",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        EtiquetaInteres(texto = indicador.categoria.etiqueta, color = colorCategoria(indicador.categoria))
                    }
                    Text(text = mensajeCategoria(indicador.categoria), style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                TarjetaSeccion(
                    titulo = "Tus reconocimientos",
                    subtitulo = "Se basan en tus pagos y recomendaciones, nunca en montos. Los que obtienes no se pierden."
                ) {
                    if (obtenidos.isEmpty()) {
                        Text(text = "Aún no tienes reconocimientos. ¡El primero está muy cerca!", style = MaterialTheme.typography.bodyMedium)
                    }
                    obtenidos.forEach { obtenido ->
                        FilaReconocimiento(
                            nombre = datos.reconocimientoPorId(obtenido.idReconocimiento)?.nombre ?: "",
                            detalle = "${obtenido.motivo}. Lo obtuviste el ${formatoFecha(obtenido.fechaObtencion.toLocalDate())}.",
                            obtenido = true
                        )
                    }
                    if (porObtener.isNotEmpty()) {
                        HorizontalDivider()
                        Text(text = "Por obtener", style = MaterialTheme.typography.bodySmall)
                        porObtener.forEach { FilaReconocimiento(nombre = it.nombre, detalle = it.descripcion, obtenido = false) }
                    }
                }
            }

            item {
                TarjetaSeccion(
                    titulo = "Recomienda PagoFlex",
                    subtitulo = "¿Le pagas a un jardín, club o comunidad por transferencia? Recomiéndalo con el botón " +
                        "\"Recomendar\" y te contamos cómo avanza."
                ) {
                    if (recomendaciones.isEmpty()) {
                        Text(text = "Todavía no has recomendado ninguna organización.", style = MaterialTheme.typography.bodyMedium)
                    }
                    recomendaciones.forEachIndexed { indice, recomendacion ->
                        if (indice > 0) HorizontalDivider()
                        FilaRecomendacionUsuario(recomendacion, datos.comunaPorId(recomendacion.idComuna)?.nombre ?: "")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaReconocimiento(nombre: String, detalle: String, obtenido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_estrella),
            contentDescription = null,
            tint = if (obtenido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.padding(start = Dimens.espacioChico)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleMedium,
                color = if (obtenido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FilaRecomendacionUsuario(recomendacion: Recomendacion, comuna: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = recomendacion.nombreOrganizacion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$comuna, recomendada el " +
                    formatoFecha(recomendacion.fecha.toLocalDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        EtiquetaInteres(texto = recomendacion.estado.etiqueta, color = colorRecomendacion(recomendacion.estado))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AvancePreview() {
    PagoFlexTheme {
        AvanceScreen(datos = BaseDatos(), usuario = DatosAnexo9.usuarios.first(), onRecomendar = {})
    }
}
