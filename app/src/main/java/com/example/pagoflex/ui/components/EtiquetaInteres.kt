package com.example.pagoflex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pagoflex.ui.theme.Dimens
import com.example.pagoflex.ui.theme.PagoFlexTheme
import com.example.pagoflex.ui.theme.coloresExtra

private val formaPildora = RoundedCornerShape(50)

@Composable
fun EtiquetaInteres(
    texto: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    seleccionada: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val modificadorClick =
        if (onClick != null) Modifier.clip(formaPildora).clickable(role = Role.Button, onClick = onClick)
        else Modifier

    Surface(
        modifier = modifier.then(modificadorClick),
        shape = formaPildora,
        color = if (seleccionada) color else color.copy(alpha = 0.12f),
        contentColor = if (seleccionada) MaterialTheme.colorScheme.surface else color,
        border = if (onClick != null && !seleccionada) BorderStroke(Dimens.bordeEtiqueta, color.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(
                horizontal = Dimens.espacioMedio,
                vertical = if (onClick != null) 10.dp else Dimens.espacioMini + 2.dp
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> FilaEtiquetas(
    opciones: List<T>,
    seleccionada: T,
    onSeleccionar: (T) -> Unit,
    textoDe: (T) -> String,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        opciones.forEach { opcion ->
            EtiquetaInteres(
                texto = textoDe(opcion),
                color = MaterialTheme.colorScheme.primary,
                seleccionada = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EtiquetaInteresPreview() {
    PagoFlexTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaInteres(texto = "Pagado", color = MaterialTheme.coloresExtra.pagado)
                EtiquetaInteres(texto = "Vencido", color = MaterialTheme.coloresExtra.vencido)
            }
            FilaEtiquetas(
                opciones = listOf("Todos", "Abierto", "En gestión", "Resuelto"),
                seleccionada = "Abierto",
                onSeleccionar = {},
                textoDe = { it }
            )
        }
    }
}
