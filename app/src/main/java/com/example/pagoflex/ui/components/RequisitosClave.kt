package com.example.pagoflex.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.R
import com.example.pagoflex.model.RequisitoClave
import com.example.pagoflex.model.cumpleRequisito
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

@Composable
fun RequisitosClave(
    clave: String,
    modifier: Modifier = Modifier
) {
    val cumplidos = RequisitoClave.entries.count { cumpleRequisito(clave, it) }
    val total = RequisitoClave.entries.size
    val colores = MaterialTheme.coloresExtra
    val colorAvance: Color = when {
        cumplidos == total -> colores.exito
        cumplidos >= 3 -> colores.advertencia
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
    ) {
        Text(
            text = "Seguridad de tu contraseña: $cumplidos de $total",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { cumplidos.toFloat() / total },
            modifier = Modifier.fillMaxWidth(),
            color = colorAvance,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        RequisitoClave.entries.forEach { requisito ->
            FilaRequisito(texto = requisito.texto, cumplido = cumpleRequisito(clave, requisito))
        }
    }
}

@Composable
private fun FilaRequisito(texto: String, cumplido: Boolean) {
    val color = if (cumplido) MaterialTheme.coloresExtra.exito else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(if (cumplido) R.drawable.ic_check_circulo else R.drawable.ic_info),
            contentDescription = if (cumplido) "Cumplido" else "Pendiente",
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(start = Dimens.espacioChico)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RequisitosClavePreview() {
    PagoFlexTheme {
        RequisitosClave(clave = "pago2026", modifier = Modifier.padding(16.dp))
    }
}
