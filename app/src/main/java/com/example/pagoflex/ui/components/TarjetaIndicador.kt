package com.example.pagoflex.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.ui.styles.EstilosTarjeta
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme

@Composable
fun TarjetaIndicador(
    titulo: String,
    valor: String,
    detalle: String,
    @DrawableRes icono: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val contenido: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icono),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = valor,
                style = if (valor.length > 10) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
private fun TarjetaIndicadorPreview() {
    PagoFlexTheme {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaIndicador(
                titulo = "Por cobrar",
                valor = "$ 1.446.900",
                detalle = "8 compromisos",
                icono = R.drawable.ic_dinero,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            TarjetaIndicador(
                titulo = "Vencido",
                valor = "$ 335.500",
                detalle = "2 compromisos",
                icono = R.drawable.ic_calendario,
                color = Color(0xFFE5484D),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
