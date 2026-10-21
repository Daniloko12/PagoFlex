package com.example.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion(),
        border = EstilosTarjeta.borde()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            contenido()
        }
    }
}

@Composable
fun FilaDato(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    destacado: Boolean = false
) {
    val estiloValor = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    val pesoValor = if (destacado) FontWeight.Bold else FontWeight.Normal

    if (valor.length > 24) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = valor, style = estiloValor, fontWeight = pesoValor)
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valor,
                style = estiloValor,
                fontWeight = pesoValor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1.3f)
                    .padding(start = Dimens.espacioChico)
            )
        }
    }
}

@Composable
fun TarjetaMensaje(
    texto: String,
    modifier: Modifier = Modifier,
    exito: Boolean = false,
    @DrawableRes icono: Int = if (exito) R.drawable.ic_check else R.drawable.ic_info
) {
    val colores = MaterialTheme.coloresExtra
    val colorTexto: Color = if (exito) colores.exito else colores.informacion
    val colorFondo: Color = if (exito) colores.exitoFondo else colores.informacionFondo
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        color = colorFondo,
        contentColor = colorTexto
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = null,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSeccionPreview() {
    PagoFlexTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaSeccion(titulo = "Detalle del compromiso", subtitulo = "Folio CP-2026-0106") {
                FilaDato("Monto de la cuota", "$ 310.000")
                FilaDato("Recargo por atraso", "$ 8.500")
                FilaDato("Total a pagar", "$ 318.500", destacado = true)
            }
            TarjetaMensaje(texto = "Pago confirmado. Tu comprobante es CPR-000454.", exito = true)
        }
    }
}
