package com.example.pagoflex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun TarjetaLista(
    titulo: String,
    lineaFinal: String,
    etiqueta: String,
    colorEtiqueta: Color,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    iniciales: String? = null,
    colorAvatar: Color = MaterialTheme.colorScheme.primary,
    datoDerecho: String? = null,
    linea2: String? = null,
    linea3: String? = null,
    colorLineaFinal: Color = Color.Unspecified,
    destacarLineaFinal: Boolean = false
) {
    val contenido: @Composable ColumnScope.() -> Unit = {
        ListItem(
            leadingContent = if (iniciales != null) {
                { AvatarIniciales(iniciales = iniciales, color = colorAvatar) }
            } else null,
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (datoDerecho != null) {
                        Text(
                            text = datoDerecho,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (linea2 != null) {
                        Text(
                            text = linea2,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (linea3 != null) Text(text = linea3, style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lineaFinal,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (destacarLineaFinal) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (colorLineaFinal == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant
                            else colorLineaFinal,
                            modifier = Modifier.weight(1f)
                        )
                        EtiquetaInteres(
                            texto = etiqueta,
                            color = colorEtiqueta,
                            modifier = Modifier.padding(start = Dimens.espacioChico)
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = EstilosTarjeta.forma,
            colors = EstilosTarjeta.colores(),
            elevation = EstilosTarjeta.elevacion(),
            border = EstilosTarjeta.borde(),
            content = contenido
        )
    } else {
        Card(
            modifier = modifier,
            shape = EstilosTarjeta.forma,
            colors = EstilosTarjeta.colores(),
            elevation = EstilosTarjeta.elevacion(),
            border = EstilosTarjeta.borde(),
            content = contenido
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaListaPreview() {
    PagoFlexTheme(modoOscuro = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaLista(
                iniciales = "AM",
                titulo = "Altos del Maipo",
                datoDerecho = "$ 318.500",
                linea2 = "Cuota pie departamento (cuota 14 de 36)",
                lineaFinal = "Venció el 05-10-2026 (hace 1 día)",
                etiqueta = "Vencido",
                colorEtiqueta = MaterialTheme.coloresExtra.vencido,
                onClick = {}
            )
            TarjetaLista(
                iniciales = "CH",
                titulo = "Club Los Halcones",
                datoDerecho = "$ 25.000",
                linea2 = "Mensualidad octubre",
                lineaFinal = "Pagado desde la app",
                etiqueta = "A tiempo",
                colorEtiqueta = MaterialTheme.coloresExtra.pagado,
                onClick = null
            )
        }
    }
}
