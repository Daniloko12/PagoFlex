package com.example.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.GrupoMovimiento
import com.example.pagoflex.model.Movimiento
import com.example.pagoflex.model.autorDe
import com.example.pagoflex.model.formatoFechaHora
import com.example.pagoflex.model.movimientosDe
import com.example.pagoflex.model.nombreCompleto
import com.example.pagoflex.model.usuarioPorId
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@DrawableRes
fun iconoDe(grupo: GrupoMovimiento): Int = when (grupo) {
    GrupoMovimiento.COMPROMISOS -> R.drawable.ic_lista
    GrupoMovimiento.PAGOS -> R.drawable.ic_dinero
    GrupoMovimiento.REPORTES -> R.drawable.ic_reporte
    GrupoMovimiento.CUENTA -> R.drawable.ic_persona
}

@Composable
fun FiltroGrupos(
    seleccionado: GrupoMovimiento?,
    onSeleccionar: (GrupoMovimiento?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        FilterChip(
            selected = seleccionado == null,
            onClick = { onSeleccionar(null) },
            label = { Text("Todos") }
        )
        GrupoMovimiento.entries.forEach { grupo ->
            FilterChip(
                selected = seleccionado == grupo,
                onClick = { onSeleccionar(if (seleccionado == grupo) null else grupo) },
                label = { Text(grupo.etiqueta) },
                leadingIcon = {
                    Icon(painterResource(iconoDe(grupo)), contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
fun FilaMovimiento(
    movimiento: Movimiento,
    datos: BaseDatos,
    modifier: Modifier = Modifier,
    mostrarTitular: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.elevacionTarjeta
    ) {
        Row(modifier = Modifier.padding(Dimens.espacioMedio), verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(iconoDe(movimiento.tipo.grupo)),
                contentDescription = movimiento.tipo.grupo.etiqueta,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Column(
                modifier = Modifier.padding(start = Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
            ) {
                Text(text = movimiento.tipo.etiqueta, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = formatoFechaHora(movimiento.fecha),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = movimiento.detalle, style = MaterialTheme.typography.bodyMedium)
                if (mostrarTitular) {
                    datos.usuarioPorId(movimiento.idUsuario)?.let {
                        Text(
                            text = "Cliente: ${nombreCompleto(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "Hecho por: ${datos.autorDe(movimiento)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LineaDeTiempo(
    movimientos: List<Movimiento>,
    datos: BaseDatos,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
        if (movimientos.isEmpty()) {
            Text("Todavía no hay movimientos.", style = MaterialTheme.typography.bodyMedium)
        }
        movimientos.forEachIndexed { indice, movimiento ->
            if (indice > 0) HorizontalDivider()
            Text(
                text = "${formatoFechaHora(movimiento.fecha)} · ${movimiento.tipo.etiqueta}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = movimiento.detalle, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Por: ${datos.autorDe(movimiento)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListaMovimientosPreview() {
    val datos = BaseDatos()
    PagoFlexTheme(modoOscuro = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroGrupos(seleccionado = GrupoMovimiento.PAGOS, onSeleccionar = {})
            datos.movimientosDe(1).take(3).forEach { FilaMovimiento(movimiento = it, datos = datos) }
        }
    }
}
