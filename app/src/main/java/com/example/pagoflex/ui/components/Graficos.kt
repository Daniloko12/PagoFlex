package com.example.pagoflex.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

data class SegmentoGrafico(
    val etiqueta: String,
    val valor: Float,
    val color: Color
)

data class BarraGrafico(
    val etiqueta: String,
    val valor: Float,
    val textoValor: String
)

@Composable
fun GraficoDona(
    segmentos: List<SegmentoGrafico>,
    textoCentro: String,
    subtextoCentro: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 140.dp,
    grosor: Dp = 20.dp
) {
    val total = segmentos.sumOf { it.valor.toDouble() }.toFloat()
    val visibles = segmentos.filter { it.valor > 0f }
    val colorVacio = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val trazo = grosor.toPx()
            val lado = size.minDimension - trazo
            val esquina = Offset((size.width - lado) / 2f, (size.height - lado) / 2f)
            val area = Size(lado, lado)
            if (total <= 0f) {
                drawArc(colorVacio, 0f, 360f, false, topLeft = esquina, size = area, style = Stroke(width = trazo))
            } else {
                val separacion = if (visibles.size > 1) 2f else 0f
                var inicio = -90f
                visibles.forEach { segmento ->
                    val barrido = segmento.valor / total * 360f
                    drawArc(
                        segmento.color, inicio + separacion / 2f, barrido - separacion, false,
                        topLeft = esquina, size = area, style = Stroke(width = trazo)
                    )
                    inicio += barrido
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = textoCentro, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = subtextoCentro,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LeyendaGrafico(
    segmentos: List<SegmentoGrafico>,
    modifier: Modifier = Modifier,
    textoValor: (SegmentoGrafico) -> String = { it.valor.toInt().toString() }
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        segmentos.forEach { segmento ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(segmento.color)
                )
                Text(
                    text = segmento.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.espacioChico)
                )
                Text(
                    text = textoValor(segmento),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun GraficoBarras(
    barras: List<BarraGrafico>,
    modifier: Modifier = Modifier,
    alto: Dp = 120.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val maximo = barras.maxOfOrNull { it.valor }?.takeIf { it > 0f } ?: 1f
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio),
        verticalAlignment = Alignment.Bottom
    ) {
        barras.forEachIndexed { indice, barra ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = barra.textoValor,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(Modifier.height(Dimens.espacioMini))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((alto * (barra.valor / maximo)).coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(if (indice == barras.lastIndex) color else color.copy(alpha = 0.45f))
                )
                Spacer(Modifier.height(Dimens.espacioMini))
                Text(
                    text = barra.etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BarraHorizontal(
    etiqueta: String,
    valorTexto: String,
    fraccion: Float,
    color: Color,
    modifier: Modifier = Modifier,
    detalle: String? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(text = valorTexto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion.coerceIn(0.03f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
        if (detalle != null) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GraficosPreview() {
    val segmentos = listOf(
        SegmentoGrafico("Pendientes", 8f, Color(0xFFE0A100)),
        SegmentoGrafico("Vencidos", 3f, Color(0xFFE5484D)),
        SegmentoGrafico("Pagados", 7f, Color(0xFF2ECC85))
    )
    PagoFlexTheme(modoOscuro = false) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GraficoDona(segmentos = segmentos, textoCentro = "18", subtextoCentro = "compromisos")
                LeyendaGrafico(segmentos = segmentos, modifier = Modifier.padding(start = 16.dp))
            }
            GraficoBarras(
                barras = listOf(
                    BarraGrafico("Jul", 0f, "$0"),
                    BarraGrafico("Ago", 15f, "$15 mil"),
                    BarraGrafico("Sep", 1155f, "$1,2 M"),
                    BarraGrafico("Oct", 15f, "$15 mil")
                )
            )
            BarraHorizontal("Plaza Oriente", "$ 520.000", 1f, Color(0xFF2ECC85), detalle = "1 compromiso")
        }
    }
}
